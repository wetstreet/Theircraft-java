package com.chenyirun.theircraft;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.widget.TextView;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Chunk;
import com.chenyirun.theircraft.model.Point3;
import com.chenyirun.theircraft.perlin.Generator;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by chenyirun on 2017/3/6.
 */

public class Renderer implements GvrView.StereoRenderer {
    public final Grass mGrass = new Grass();
    private final Steve steve;
    private Generator generator;
    public final Performance performance = new Performance();
    public final Physics physics = new Physics();

    private final float[] camera = new float[16];
    private final float[] view = new float[16];
    // pitch, yaw, roll(in radian)
    private final float[] eulerAngles = new float[3];
    private final Object blocksLock = new Object();
    public final Set<Block> blocks = new HashSet<>();
    private final Map<Chunk, List<Block>> chunkBlocks = new HashMap<>();
    private final BlockingDeque<ChunkChange> chunkChanges = new LinkedBlockingDeque<>();
    private final Thread chunkLoader;
    private final TextView textView;

    private Resources resources;

    Renderer(Resources resources, TextView textView) {
        this.resources = resources;
        this.textView = textView;
        generator = new Generator(new Random().nextInt());

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

        int x = Chunk.CHUNK_SIZE / 2;
        int z = Chunk.CHUNK_SIZE / 2;
        steve = new Steve(new Block(x, highestSolidY(x, z), z));

        Chunk currChunk = steve.currentChunk();
        Set<Chunk> chunksToLoad = neighboringChunks(currChunk);
        chunksToLoad.removeAll(preloadedChunks);
        for (Chunk chunk : chunksToLoad) {
            chunkChanges.add(new ChunkLoad(chunk));
        }
    }

    @Override
    public void onRendererShutdown() {}

    @Override
    public void onSurfaceChanged(int width, int height) {}

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        mGrass.grassInit(resources);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        GLES20.glClearColor(0.5f, 0.69f, 1.0f, 1.0f);
        float x = steve.position().x;
        float y = steve.position().y;
        float z = steve.position().z;
        Matrix.setLookAtM(camera, 0, x, y, z, x, y, z - 0.01f, 0.0f, 1.0f, 0.0f);
        headTransform.getEulerAngles(eulerAngles, 0);
        steve.mYaw = eulerAngles[1];
    }

    private float getAngle(float radian){
        return radian * 180/3.1415926f;
    }

    private String floatIntString(float f){
        return Integer.toString(Math.round(f));
    }

    public void updateInformation(){
        float pitch = getAngle(eulerAngles[0]);
        float yaw = getAngle(eulerAngles[1]);
        float roll = getAngle(eulerAngles[2]);
        String message = "pitch=" + floatIntString(pitch) + "°\n" +
                "yaw=" + floatIntString(yaw) + "°\n" +
                "roll=" + floatIntString(roll) + "°\n" +
                "Blocks=" + Integer.toString(blocks.size()) + "\n" +
                "fps=" + floatIntString(performance.fps());
        textView.setText(message);
    }

    private static final int PHYSICS_ITERATIONS_PER_FRAME = 5;
    @Override
    public void onDrawEye(Eye eye) {
        float dt = Math.min(performance.startFrame(), 0.2f);
        for (int i = 0; i < PHYSICS_ITERATIONS_PER_FRAME; ++i) {
            physics.move(steve, dt / PHYSICS_ITERATIONS_PER_FRAME, blocks);
        }

        Chunk beforeChunk = steve.currentChunk();
        Chunk afterChunk = new Chunk(steve.position());
        if (!afterChunk.equals(beforeChunk)) {
            queueChunkLoads(beforeChunk, afterChunk);
            steve.setCurrentChunk(afterChunk);
        }

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glFrontFace(GLES20.GL_CCW);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        float[] perspective = eye.getPerspective(0.1f, 100.0f);
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        mGrass.draw(view, perspective);
        performance.endFrame();
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

    /** Returns chunks within some radius of center, but only those containing any blocks. */
    private Set<Chunk> neighboringChunks(Chunk center) {
        int minChunkY = Generator.minChunkY();
        int maxChunkY = Generator.maxChunkY();

        Set<Chunk> result = new HashSet<Chunk>();
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

    /** Given (x,z) coordinates, finds and returns the highest y so that (x,y,z) is a solid block. */
    private float highestSolidY(float x, float z) {
        float maxY = Generator.minElevation();
        for (Block block : blocks) {
            if (block.x != x || block.z != z) {
                continue;
            }
            if (block.y > maxY) {
                maxY = block.y;
            }
        }
        return maxY;
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

    @Override
    public void onFinishFrame(Viewport viewport) {}

    public boolean onGenericMotionEvent(MotionEvent event, InputDevice device) {
        steve.processJoystickInput(event, -1, device);
        return true;
    }

    public void pressX(){}
    public void jump(){
        steve.jump();
    }
}
