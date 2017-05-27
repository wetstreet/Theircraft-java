package com.chenyirun.theircraft.map;

import android.content.res.Resources;
import android.os.SystemClock;

import com.chenyirun.theircraft.GLHelper;
import com.chenyirun.theircraft.Performance;
import com.chenyirun.theircraft.block.*;
import com.chenyirun.theircraft.database.DBService;
import com.chenyirun.theircraft.map.BlockMap;
import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Buffers;
import com.chenyirun.theircraft.model.Chunk;
import com.chenyirun.theircraft.model.Point3;
import com.chenyirun.theircraft.model.Point3Int;
import com.chenyirun.theircraft.model.SaveAndConfig;
import com.chenyirun.theircraft.perlin.Generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class MapManager {
    private Generator generator;
    private final Performance performance = Performance.getInstance();
    private final GLHelper glHelper = new GLHelper();

    private final Object blocksLock = new Object();
    private final BlockMap blockMap = new BlockMap();
    private final BlockingDeque<ChunkChange> chunkChanges = new LinkedBlockingDeque<>();
    private final Thread chunkLoader;
    private final List<Chunk> preloadedChunks;
    private final DBService dbService;
    private final SaveAndConfig saveAndConfig;

    private static final Map<Chunk, Buffers> chunkToBuffers = new HashMap<>();

    private static int SHOWN_CHUNK_RADIUS = 3;

    public MapManager(SaveAndConfig saveAndConfig){
        this.saveAndConfig = saveAndConfig;
        dbService = DBService.getInstance();
        SHOWN_CHUNK_RADIUS = saveAndConfig.chunk_radius;
        int seed = saveAndConfig.seed;
        //int seed = dbService.getSeed();
        generator = new Generator(seed);
        chunkLoader = createChunkLoader();
        chunkLoader.start();

        preloadedChunks = preloadedChunks();
        for (Chunk chunk : preloadedChunks) {
            chunkChanges.add(new ChunkLoad(chunk));
        }

        waitForChunkLoad();
    }

    public void waitForChunkLoad(){
        while (chunkChanges.size() > 0) {
            SystemClock.sleep(100L);
        }
    }

    public void onSurfaceCreated(Resources resources){
        glHelper.attachVariables(resources);
    }

    public void draw(float[] view, float[] perspective, Point3Int loc, Point3 sightVector, Point3 pos){
        glHelper.computeMVP(view ,perspective);

        glHelper.beforeDrawBlocks();
        synchronized(chunkToBuffers) {
            if (!chunkToBuffers.isEmpty()){
                for (Buffers b : chunkToBuffers.values()) {
                    glHelper.drawBlocks(b);
                }
            }
        }
        glHelper.afterDrawBlocks();

        if (loc != null){
            glHelper.drawWireFrame(loc);
        }
        //glHelper.drawCrossHair();
        //glHelper.drawSightVector(sightVector, pos);
    }

    public void loadNeighboringChunks(Chunk currChunk){
        Set<Chunk> chunksToLoad = neighboringChunks(currChunk);
        chunksToLoad.removeAll(preloadedChunks);
        for (Chunk chunk : chunksToLoad) {
            chunkChanges.add(new ChunkLoad(chunk));
        }
    }

    private static Set<Chunk> neighboringChunks(Chunk center) {
        return neighboringChunks(center, SHOWN_CHUNK_RADIUS);
    }

    // Returns chunks within some radius of center, but only those containing any blocks.
    public static Set<Chunk> neighboringChunks(Chunk center, int radius) {
        int minChunkY = Generator.minChunkY();
        int maxChunkY = Generator.maxChunkY();

        Set<Chunk> result = new HashSet<>();
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dy = -radius; dy <= radius; ++dy) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    if (!chunkShown(dx, dy, dz)) {
                        continue;
                    }
                    Chunk chunk = center.plus(dx, dy, dz);
                    if (chunk.y < minChunkY || chunk.y > maxChunkY) {
                        continue;
                    }
                    result.add(chunk);
                }
            }
        }
        return result;
    }

    private interface ChunkChange {}
    private static class ChunkLoad implements ChunkChange {
        private final Chunk chunk;

        ChunkLoad(Chunk chunk) {
            this.chunk = chunk;
        }
    }
    private static class ChunkUnload implements ChunkChange {
        private final Chunk chunk;

        ChunkUnload(Chunk chunk) {
            this.chunk = chunk;
        }
    }

    private static boolean chunkShown(int dx, int dy, int dz) {
        return dx * dx + dy * dy + dz * dz <= SHOWN_CHUNK_RADIUS * SHOWN_CHUNK_RADIUS;
    }

    private List<Chunk> preloadedChunks() {
        // Generate a stack of chunks around the starting position (8, 8), other chunks will be loaded
        // in the background.
        int minChunkY = Generator.minChunkY();
        int maxChunkY = Generator.maxChunkY();

        List<Chunk> preloadedChunks = new ArrayList<>();
        for (int y = minChunkY; y <= maxChunkY; ++y) {
            preloadedChunks.add(new Chunk(0, y, 0));
        }
        return preloadedChunks;
    }

    // Asynchronous chunk loader.
    private Thread createChunkLoader() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        ChunkChange cc = chunkChanges.takeFirst();
                        if (cc instanceof ChunkLoad) {
                            performance.startChunkLoad();
                            Chunk chunk = ((ChunkLoad) cc).chunk;
                            synchronized(blocksLock) {
                                loadChunk(chunk);
                                addChunkBuffer(chunk, blockMap.shownBlocks(chunk));
                            }
                            performance.endChunkLoad();
                        } else if (cc instanceof ChunkUnload) {
                            performance.startChunkUnload();
                            Chunk chunk = ((ChunkUnload) cc).chunk;
                            synchronized(blocksLock) {
                                unloadChunk(chunk);
                                removeChunkBuffer(chunk);
                            }
                            performance.endChunkUnload();
                        } else {
                            throw new RuntimeException("Unknown ChunkChange subtype: " + cc.getClass().getName());
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    SystemClock.sleep(1);
                }
            }
        };
        return new Thread(runnable);
    }

    // Adds blocks within a single chunk generated based on 3d Perlin noise.
    private void loadChunk(Chunk chunk) {
        if (blockMap.containChunk(chunk)) {
            return;
        }

        // generate chunk blocks
        generator.generateChunk(chunk);
        List<Block> blocksInChunk = generator.getBlocksInChunk() ;
        Set<Point3Int> blockLocations = generator.getChunkBlockLocs();
        // add the chunk to block map
        blockMap.addChunk(chunk, blocksInChunk, blockLocations);
        // if db is enabled, load block from db
        if(DBService.DBEnabled){
            List<Block> list = dbService.getBlockChangesInChunk(saveAndConfig.id, chunk);
            for (Block block : list) {
                switch (block.getType()){
                    case Block.BLOCK_AIR:
                        Block realBlock = blockMap.getBlock(block.getLocation());
                        if (realBlock != null){
                            blockMap.removeBlock(realBlock);
                        }
                        break;
                    default:
                        blockMap.addBlock(block);
                        break;
                }
            }
        }
    }

    private void unloadChunk(Chunk chunk) {
        blockMap.removeChunk(chunk);
    }

    // generate buffer according to the blocks of the chunk
    private void addChunkBuffer(Chunk chunk, List<Block> shownBlocks) {
        Buffers buffers = blockMap.createBuffers(shownBlocks);
        synchronized(chunkToBuffers) {
            // if there is a buffer already, replace the old buffer with the new_save one
            chunkToBuffers.put(chunk, buffers);
        }
    }

    private void removeChunkBuffer(Chunk chunk) {
        synchronized(chunkToBuffers) {
            chunkToBuffers.remove(chunk);
        }
    }

    public void queueChunkLoads(Chunk beforeChunk, Chunk afterChunk) {
        Set<Chunk> beforeShownChunks = neighboringChunks(beforeChunk);
        Set<Chunk> afterShownChunks = neighboringChunks(afterChunk);

        for (Chunk chunk : setDiff(afterShownChunks, beforeShownChunks)) {
            chunkChanges.add(new ChunkLoad(chunk));
        }
        for (Chunk chunk : setDiff(beforeShownChunks, afterShownChunks)) {
            chunkChanges.add(new ChunkUnload(chunk));
        }
    }

    private static <T> Set<T> setDiff(Set<T> s1, Set<T> s2) {
        Set<T> result = new HashSet<T>(s1);
        result.removeAll(s2);
        return result;
    }

    public void addBlock(Block block){
        if (blockMap.contain(block)){
            return;
        }
        Chunk chunk = new Chunk(block);
        blockMap.addBlock(block);
        dbService.insertBlock(saveAndConfig.id, block);
        // reload chunk
        chunkChanges.add(new ChunkLoad(chunk));
    }

    public void destroyBlock(Point3Int blockLocation){
        Chunk chunk = new Chunk(blockLocation);
        Block block = blockMap.getBlock(blockLocation);
        if (block == null){
            return;
        }
        blockMap.removeBlock(block);
        dbService.deleteBlock(saveAndConfig.id, block);
        chunkChanges.add(new ChunkLoad(chunk));
    }

    // Given (x,z) coordinates, finds and returns the highest y so that (x,y,z) is a solid block.
    public float highestSolidY(float x, float z) {
        return blockMap.highestSolidY(x, z);
    }

    public BlockMap getBlockMap(){
        return blockMap;
    }

    public static Block createBlock(Point3Int blockLocation, int type){
        switch (type){
            case Block.BLOCK_AIR:
                return new Air(blockLocation);
            case Block.BLOCK_GRASS:
                return new Grass(blockLocation);
            case Block.BLOCK_SAND:
                return new Sand(blockLocation);
            case Block.BLOCK_STONE:
                return new Stone(blockLocation);
            case Block.BLOCK_BRICK:
                return new Brick(blockLocation);
            case Block.BLOCK_WOOD:
                return new Wood(blockLocation);
            case Block.BLOCK_CEMENT:
                return new Cement(blockLocation);
            case Block.BLOCK_DIRT:
                return new Dirt(blockLocation);
            case Block.BLOCK_PLANK:
                return new Plank(blockLocation);
            case Block.BLOCK_SNOW:
                return new Snow(blockLocation);
            case Block.BLOCK_GLASS:
                return new Glass(blockLocation);
            case Block.BLOCK_COBBLE:
                return new Cobble(blockLocation);
            case Block.BLOCK_LIGHT_STONE:
                return new LightStone(blockLocation);
            case Block.BLOCK_DARK_STONE:
                return new DarkStone(blockLocation);
            case Block.BLOCK_CHEST:
                return new Chest(blockLocation);
            case Block.BLOCK_LEAVES:
                return new Leaves(blockLocation);
            case Block.BLOCK_CLOUD:
                return new Cloud(blockLocation);
            case Block.BLOCK_TALL_GRASS:
                return new TallGrass(blockLocation);
            case Block.BLOCK_YELLOW_FLOWER:
                return new YellowFlower(blockLocation);
            case Block.BLOCK_RED_FLOWER:
                return new RedFlower(blockLocation);
            case Block.BLOCK_PURPLE_FLOWER:
                return new PurpleFlower(blockLocation);
            case Block.BLOCK_SUN_FLOWER:
                return new SunFlower(blockLocation);
            case Block.BLOCK_WHITE_FLOWER:
                return new WhiteFlower(blockLocation);
            case Block.BLOCK_BLUE_FLOWER:
                return new BlueFlower(blockLocation);
        }
        return null;
    }
 }