package com.chenyirun.theircraft;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Chunk;
import com.chenyirun.theircraft.model.Point3Int;
import com.chenyirun.theircraft.perlin.Generator;
import com.google.vr.sdk.base.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class World {
    private static final String TAG = "World";
    private final Grass mGrass = new Grass();
    private final Steve steve;
    private Generator generator;
    private final Performance performance = Performance.getInstance();
    private final Physics physics = new Physics();

    private final float[] camera = new float[16];
    private final float[] view = new float[16];
    private final float[] eulerAngles = new float[3];
    private final Object blocksLock = new Object();
    private final Set<Block> blocks = new HashSet<>();
    private final Map<Chunk, List<Block>> chunkBlocks = new HashMap<>();
    private final BlockingDeque<ChunkChange> chunkChanges = new LinkedBlockingDeque<>();
    private final Thread chunkLoader;
    private final DBService dbService;
    private final Resources resources;

    private final MapManager mapManager;

    World(Context context, Resources resources){
        this.resources = resources;
        dbService = new DBService(context);
        mapManager = new MapManager(dbService);

        int seed = dbService.getSeed();
        generator = new Generator(seed);

        // Start the thread for loading chunks in the background.
        chunkLoader = createChunkLoader();
        chunkLoader.start();

        List<Chunk> preloadedChunks = preloadedChunks();
        for (Chunk chunk : preloadedChunks) {
            chunkChanges.add(new ChunkLoad(chunk));
        }

        // Wait for the background thread to finish loading all of them.  The whole stack of chunks
        // around the starting position is needed to determine Steve's initial position's y coordinate.
        while (chunkChanges.size() > 0) {
            SystemClock.sleep(100L);
        }

        Block steveBlock = dbService.getSteve(blocks);
        steve = new Steve(steveBlock);

        Chunk currChunk = steve.currentChunk();
        Set<Chunk> chunksToLoad = neighboringChunks(currChunk);
        chunksToLoad.removeAll(preloadedChunks);
        for (Chunk chunk : chunksToLoad) {
            chunkChanges.add(new ChunkLoad(chunk));
        }
    }

    public void onSurfaceCreated(){
        mGrass.grassInit(resources);
    }

    private static final int PHYSICS_ITERATIONS_PER_FRAME = 5;
    public void onDrawEye(Eye eye){
        float dt = Math.min(performance.startFrame(), 0.2f);
        for (int i = 0; i < PHYSICS_ITERATIONS_PER_FRAME; ++i) {
            physics.move(steve, dt / PHYSICS_ITERATIONS_PER_FRAME, blocks);
        }
        if (steve.isOnTheGround() && !dbService.compareStevePosition(steve.position())){
            dbService.updateSteve(steve.getBlock());
        }

        Chunk beforeChunk = steve.currentChunk();
        Chunk afterChunk = new Chunk(steve.position());
        if (!afterChunk.equals(beforeChunk)) {
            queueChunkLoads(beforeChunk, afterChunk);
            steve.setCurrentChunk(afterChunk);
        }

        GLHelper.beforeDraw();

        calculateView(eye);
        float[] perspective = eye.getPerspective(0.1f, 100.0f);

        performance.startRendering();
        mGrass.draw(view, perspective);
        performance.endRendering();

        performance.endFrame();

    }

    private void calculateView(Eye eye){
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);
    }

    public void calculateCamera(){
        float x = steve.position().x;
        float y = steve.position().y;
        float z = steve.position().z;
        Matrix.setLookAtM(camera, 0, x, y, z, x, y, z - 0.01f, 0.0f, 1.0f, 0.0f);
    }

    public void setSteveAngles(HeadTransform headTransform){
        headTransform.getEulerAngles(eulerAngles, 0);
        steve.mPitch = eulerAngles[0];
        steve.mYaw = eulerAngles[1];
        steve.mRoll = eulerAngles[2];
    }

    private interface ChunkChange {}
    private static class ChunkLoad implements World.ChunkChange {
        private final Chunk chunk;

        ChunkLoad(Chunk chunk) {
            this.chunk = chunk;
        }
    }
    private static class ChunkUnload implements World.ChunkChange {
        private final Chunk chunk;

        ChunkUnload(Chunk chunk) {
            this.chunk = chunk;
        }
    }

    private static final int SHOWN_CHUNK_RADIUS = 3;
    private Set<Chunk> neighboringChunks(Chunk center) {
        return neighboringChunks(center, SHOWN_CHUNK_RADIUS);
    }

    /* Returns chunks within some radius of center, but only those containing any blocks. */
    private Set<Chunk> neighboringChunks(Chunk center, int radius) {
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

    /** Asynchronous chunk loader. */
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
                                mGrass.load(chunk, shownBlocks(chunkBlocks.get(chunk)), blocks);
                            }
                            performance.endChunkLoad();
                        } else if (cc instanceof ChunkUnload) {
                            performance.startChunkUnload();
                            Chunk chunk = ((ChunkUnload) cc).chunk;
                            synchronized(blocksLock) {
                                unloadChunk(chunk);
                                mGrass.unload(chunk);
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

    /** Adds blocks within a single chunk generated based on 3d Perlin noise. */
    private void loadChunk(Chunk chunk) {
        if (chunkBlocks.keySet().contains(chunk)) {
            return;
        }

        List<Block> blocksInChunk = generator.generateChunk(chunk);
        if(DBService.DBEnabled){
            List<Block> list = dbService.getBlockChangesInChunk(chunk);
            for (Block block : list) {
                switch (block.type){
                    case Block.BLOCK_AIR:
                        blocksInChunk.remove(block);
                        break;
                    case Block.BLOCK_GRASS:
                        blocksInChunk.add(block);
                        break;
                }
            }
        } else {
            blocksInChunk = generator.generateChunk(chunk);
        }
        addChunkBlocks(chunk, blocksInChunk);
    }

    private void addChunkBlocks(Chunk chunk, List<Block> blocksInChunk) {
        blocks.addAll(blocksInChunk);
        chunkBlocks.put(chunk, blocksInChunk);
    }

    private void unloadChunk(Chunk chunk) {
        List<Block> blocksInChunk = chunkBlocks.get(chunk);
        if (blocksInChunk == null) {
            return;
        }
        chunkBlocks.remove(chunk);
        blocks.removeAll(blocksInChunk);
    }

    private List<Block> shownBlocks(List<Block> blocks) {
        List<Block> result = new ArrayList<>();
        if (blocks == null) {
            return result;
        }

        for (Block block : blocks) {
            if (exposed(block)) {
                result.add(block);
            }
        }
        return result;
    }

    private boolean exposed(Block block) {
        return !blocks.contains(new Block(block.x - 1, block.y, block.z)) ||
                !blocks.contains(new Block(block.x + 1, block.y, block.z)) ||
                !blocks.contains(new Block(block.x, block.y - 1, block.z)) ||
                !blocks.contains(new Block(block.x, block.y + 1, block.z)) ||
                !blocks.contains(new Block(block.x, block.y, block.z - 1)) ||
                !blocks.contains(new Block(block.x, block.y, block.z + 1));
    }

    private void queueChunkLoads(Chunk beforeChunk, Chunk afterChunk) {
        Set<Chunk> beforeShownChunks = neighboringChunks(beforeChunk);
        Set<Chunk> afterShownChunks = neighboringChunks(afterChunk);

        // chunksToLoad = afterShownChunks \ beforeShownChunks
        // chunksToUnload = beforeShownChunks \ afterShownChunks
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

    public boolean onGenericMotionEvent(MotionEvent event, InputDevice device) {
        steve.processJoystickInput(event, -1, device);
        return true;
    }

    private void addBlock(Block block){
        List<Block> blocksInChunk = chunkBlocks.get(new Chunk(block));
        if (!blocksInChunk.contains(block)){
            blocksInChunk.add(block);
        }
        blocks.add(block);
        chunkChanges.add(new ChunkLoad(new Chunk(block)));
        dbService.insertBlock(block);
    }

    private void destroyBlock(Block block){
        chunkBlocks.get(steve.currentChunk()).remove(block);
        blocks.remove(block);
        chunkChanges.add(new ChunkLoad(new Chunk(block)));
        dbService.deleteBlock(block);
    }

    private void resetSteve(){
        Block block = new Block(0, 74, 0);
        steve.setPosition(block);
        steve.setCurrentChunk(new Chunk(block));
        dbService.updateSteve(block);
    }

    public void pressX(){
        //Point3Int pos = physics.hitTest(false, chunkBlocks, steve);
        resetSteve();
        /*
        Block floatingBlock = new Block(steve.position().plus(0, 2, 0));
        if (!blocks.contains(floatingBlock)){
            addBlock(floatingBlock);
            Log.i(TAG, "pressX: add floating block");
        } else {
            Log.i(TAG, "pressX: block already exists!");
        }*/
    }

    public void pressB(){
        Block floatingBlock = new Block(steve.position().plus(0, 2, 0));
        destroyBlock(floatingBlock);
    }

    public void jump(){
        steve.jump();
    }

    public void walk(int walking){
        steve.walk(walking);
    }

    public void onDestroy(){
        dbService.onDestroy();
    }
}
