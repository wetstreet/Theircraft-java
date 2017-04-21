package com.chenyirun.theircraft;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Buffers;
import com.chenyirun.theircraft.model.Chunk;
import com.chenyirun.theircraft.perlin.Generator;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
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
    private final Grass mGrass = new Grass();
    private final Performance performance = Performance.getInstance();

    private final Object blocksLock = new Object();
    private final Set<Block> blocks = new HashSet<>();
    private final Map<Chunk, List<Block>> chunkBlocks = new HashMap<>();
    private final BlockingDeque<ChunkChange> chunkChanges = new LinkedBlockingDeque<>();
    private final Thread chunkLoader;
    private final List<Chunk> preloadedChunks;
    private final DBService dbService;
    
    private int blockProgram;

    private static final Map<Chunk, Buffers> chunkToBuffers = new HashMap<>();

    public final float[] modelBlock = new float[16];
    private final float[] modelView = new float[16];
    private final float[] modelViewProjection = new float[16];

    private int textureHandle;
    private int textureData;
    private int blockPositionParam;
    private int blockUVParam;
    private int blockModelViewProjectionParam;

    private static final String VertexShaderCode =
            "uniform mat4 u_MVP;\n" +
                    "attribute vec4 a_Position;\n" +
                    "attribute vec2 a_textureCoord;\n" +
                    "varying vec2 v_textureCoord;\n" +
                    "\n" +
                    "void main() {\n" +
                    "   gl_Position = u_MVP * a_Position;\n" +
                    "   v_textureCoord = a_textureCoord;\n" +
                    "}";

    private static final String FragmentShaderCode =
            "precision mediump float;\n" +
                    "uniform sampler2D u_texture;\n" +
                    "varying vec2 v_textureCoord;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(u_texture, v_textureCoord);\n" +
                    "}";

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

        while (chunkChanges.size() > 0) {
            SystemClock.sleep(100L);
        }
    }

    public void onSurfaceCreated(Resources resources){
        blockProgram = GLHelper.linkProgram(VertexShaderCode, FragmentShaderCode);
        GLES20.glUseProgram(blockProgram);

        textureData = GLHelper.loadTexture(resources, R.drawable.atlas);
        textureHandle = GLES20.glGetUniformLocation(blockProgram, "u_texture");

        blockUVParam = GLES20.glGetAttribLocation(blockProgram, "a_textureCoord");
        blockPositionParam = GLES20.glGetAttribLocation(blockProgram, "a_Position");
        blockModelViewProjectionParam = GLES20.glGetUniformLocation(blockProgram, "u_MVP");
    }

    public void draw(float[] view, float[] perspective){
        GLES20.glUseProgram(blockProgram);
        GLES20.glUniform1i(textureHandle, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureData);

        Matrix.setIdentityM(modelBlock, 0);
        Matrix.multiplyMM(modelView, 0, view, 0, modelBlock, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        GLES20.glUniformMatrix4fv(blockModelViewProjectionParam, 1, false, modelViewProjection, 0);

        GLES20.glEnableVertexAttribArray(blockPositionParam);
        GLES20.glEnableVertexAttribArray(blockUVParam);

        synchronized(chunkToBuffers) {
            if (!chunkToBuffers.isEmpty()){
                for (Buffers b : chunkToBuffers.values()) {
                    GLES20.glVertexAttribPointer(blockPositionParam, 3, GLES20.GL_FLOAT, false, 0, b.vertexBuffer);
                    GLES20.glVertexAttribPointer(blockUVParam, 2, GLES20.GL_FLOAT, false, 0, b.textureCoordBuffer);

                    GLES20.glDrawElements(
                            GLES20.GL_TRIANGLES, b.drawListBuffer.limit(),
                            GLES20.GL_UNSIGNED_SHORT, b.drawListBuffer);
                }
            }
        }
    }

    public void loadNeighboringChunks(Chunk currChunk){
        Set<Chunk> chunksToLoad = neighboringChunks(currChunk);
        chunksToLoad.removeAll(preloadedChunks);
        for (Chunk chunk : chunksToLoad) {
            chunkChanges.add(new ChunkLoad(chunk));
        }
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
        List<Block> blocksInChunk = chunkBlocks.get(new Chunk(block));
        if (!blocksInChunk.contains(block)){
            blocksInChunk.add(block);
        }
        blocks.add(block);
        chunkChanges.add(new ChunkLoad(new Chunk(block)));
        dbService.insertBlock(block);
    }

    private void destroyBlock(Chunk chunk, Block block){
        chunkBlocks.get(chunk).remove(block);
        blocks.remove(block);
        chunkChanges.add(new ChunkLoad(new Chunk(block)));
        dbService.deleteBlock(block);
    }

    void load(Chunk chunk, Buffers buffers) {
        synchronized(chunkToBuffers) {
            chunkToBuffers.put(chunk, buffers);
        }
    }

    void unload(Chunk chunk) {
        synchronized(chunkToBuffers) {
            chunkToBuffers.remove(chunk);
        }
    }
}
