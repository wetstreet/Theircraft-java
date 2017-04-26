package com.chenyirun.theircraft;

import android.content.res.Resources;
import android.os.SystemClock;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Buffers;
import com.chenyirun.theircraft.model.Chunk;
import com.chenyirun.theircraft.model.Point3Int;
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

    private static final Map<Chunk, Buffers> chunkToBuffers = new HashMap<>();

    MapManager(DBService dbService){
        this.dbService = dbService;
        int seed = dbService.getSeed();
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

    public void draw(float[] view, float[] perspective){
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

        glHelper.drawWireFrame(new Point3Int(141, 57, -83));
    }

    public void loadNeighboringChunks(Chunk currChunk){
        Set<Chunk> chunksToLoad = neighboringChunks(currChunk);
        chunksToLoad.removeAll(preloadedChunks);
        for (Chunk chunk : chunksToLoad) {
            chunkChanges.add(new ChunkLoad(chunk));
        }
    }

    private static final int SHOWN_CHUNK_RADIUS = 3;
    private Set<Chunk> neighboringChunks(Chunk center) {
        return neighboringChunks(center, SHOWN_CHUNK_RADIUS);
    }

    // Returns chunks within some radius of center, but only those containing any blocks.
    public Set<Chunk> neighboringChunks(Chunk center, int radius) {
        int minChunkY = Generator.minChunkY();
        int maxChunkY = Generator.maxChunkY();

        Set<Chunk> result = new HashSet<>();
        for (int dx = -SHOWN_CHUNK_RADIUS; dx <= SHOWN_CHUNK_RADIUS; ++dx) {
            for (int dy = -SHOWN_CHUNK_RADIUS; dy <= SHOWN_CHUNK_RADIUS; ++dy) {
                for (int dz = -SHOWN_CHUNK_RADIUS; dz <= SHOWN_CHUNK_RADIUS; ++dz) {
                    if (!chunkShown(dx, dy, dz)) {
                        continue;
                    }
                    Chunk chunk = center.plus(new Chunk(dx, dy, dz));
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
                                load(chunk, blockMap.shownBlocks(chunk));
                            }
                            performance.endChunkLoad();
                        } else if (cc instanceof ChunkUnload) {
                            performance.startChunkUnload();
                            Chunk chunk = ((ChunkUnload) cc).chunk;
                            synchronized(blocksLock) {
                                unloadChunk(chunk);
                                unload(chunk);
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

        List<Block> blocksInChunk = generator.generateChunk(chunk);
        Set<Point3Int> blockLocations = generator.generateChunkLocation(chunk);
        if(DBService.DBEnabled){
            List<Block> list = dbService.getBlockChangesInChunk(chunk);
            for (Block block : list) {
                Point3Int loc = new Point3Int(block.x, block.y, block.z);
                switch (block.getType()){
                    case Block.BLOCK_AIR:
                        blocksInChunk.remove(block);
                        blockLocations.remove(loc);
                        break;
                    case Block.BLOCK_GRASS:
                        blocksInChunk.add(block);
                        blockLocations.add(loc);
                        break;
                }
            }
        }
        blockMap.addChunk(chunk, blocksInChunk, blockLocations);
    }

    private void unloadChunk(Chunk chunk) {
        List<Block> blocksInChunk = blockMap.getChunkBlocks(chunk);
        if (blocksInChunk == null) {
            return;
        }
        blockMap.removeChunk(chunk, blocksInChunk);
    }

    private void load(Chunk chunk, List<Block> shownBlocks) {
        Buffers buffers = blockMap.createBuffers(shownBlocks);
        synchronized(chunkToBuffers) {
            // if there is a buffer already, replace the old buffer with the new one
            chunkToBuffers.put(chunk, buffers);
        }
    }

    private void unload(Chunk chunk) {
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

    private void addBlock(Block block){
        Chunk chunk = new Chunk(block);
        List<Block> blocksInChunk = blockMap.getChunkBlocks(chunk);
        Set<Point3Int> blockLocations = blockMap.getBlockLocations();
        if (!blocksInChunk.contains(block)){
            blocksInChunk.add(block);
        }
        blockLocations.add(new Point3Int(block.x, block.y ,block.z));
        blockMap.addChunk(chunk, blocksInChunk, blockLocations);
        chunkChanges.add(new ChunkLoad(new Chunk(block)));
        dbService.insertBlock(block);
    }

    public void destroyBlock(Point3Int pos){
        Chunk chunk = new Chunk(pos);
        Block block = blockMap.getBlock(pos);
        blockMap.removeBlock(chunk, block);
        chunkChanges.add(new ChunkLoad(chunk));
        dbService.deleteBlock(block);
    }

    // Given (x,z) coordinates, finds and returns the highest y so that (x,y,z) is a solid block.
    public float highestSolidY(float x, float z) {
        return blockMap.highestSolidY(x, z);
    }

    public BlockMap getBlockMap(){
        return blockMap;
    }
 }