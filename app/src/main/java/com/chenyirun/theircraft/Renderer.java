package com.chenyirun.theircraft;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by chenyirun on 2017/3/6.
 */

public class Renderer implements GvrView.StereoRenderer {
    public Grass mGrass;
    public Steve steve;
    private Generator generator;
    public final Performance performance = new Performance();
    public final Physics physics = new Physics();

    private final float[] camera = new float[16];
    private final float[] view = new float[16];

    private Resources resources;

    Renderer(Resources resources) {
        this.resources = resources;
        generator = new Generator(new Random().nextInt());
    }

    /** Given (x,z) coordinates, finds and returns the highest y so that (x,y,z) is a solid block. */
    private float highestSolidY(float x, float z) {
        float maxY = Generator.minElevation();
        for (Block block : mGrass.getBlocks()) {
            if (block.x != x || block.z != z) {
                continue;
            }
            if (block.y > maxY) {
                maxY = block.y;
            }
        }
        return maxY;
    }

    @Override
    public void onRendererShutdown() {}

    @Override
    public void onSurfaceChanged(int width, int height) {}

    @Override
    public void onSurfaceCreated(EGLConfig config) {

        mGrass = new Grass(resources);

        // make sure that steve will spawn on blocks
        int square = 4;
        for (int x = -square; x <= square; x++){
            for (int z = -square; z <= square; z++){
                mGrass.addBlock(new Block(x,70,z));
                if (x<=1 && x>=-1 && z<=1 && z>=-1){
                    mGrass.addBlock(new Block(x,71,z));
                }
            }
        }

        // generate chunks
        int chunkY = 4*4 + 2;
        for (int x = -1; x <= 1; x++){
            for (int z = -1; z <= 1; z++){
                mGrass.addList(generator.generateChunk(new Chunk(x, chunkY, z)));
            }
        }
        mGrass.setBufferFromList();

        int x = Chunk.CHUNK_SIZE / 2;
        int z = Chunk.CHUNK_SIZE / 2;
        steve = new Steve(new Block(x, highestSolidY(x, z), z));
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        GLES20.glClearColor(0.5f, 0.69f, 1.0f, 1.0f);
        float x = steve.position().x;
        float y = steve.position().y;
        float z = steve.position().z;
        Matrix.setLookAtM(camera, 0, x, y, z, x, y, z - 0.01f, 0.0f, 1.0f, 0.0f);
        float[] eulerAngles = new float[3];
        headTransform.getEulerAngles(eulerAngles, 0);
        steve.mYaw = eulerAngles[1];
    }

    void pressX(){
    }

    private static final int PHYSICS_ITERATIONS_PER_FRAME = 5;

    @Override
    public void onDrawEye(Eye eye) {
        float dt = Math.min(performance.startFrame(), 0.2f);
        for (int i = 0; i < PHYSICS_ITERATIONS_PER_FRAME; ++i) {
            physics.move(steve, dt / PHYSICS_ITERATIONS_PER_FRAME, mGrass.blocks);
        }

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glFrontFace(GLES20.GL_CCW);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        float[] perspective = eye.getPerspective(0.1f, 100.0f);
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        mGrass.drawList(view, perspective);
        performance.endFrame();
    }

    @Override
    public void onFinishFrame(Viewport viewport) {}

    public boolean onGenericMotionEvent(MotionEvent event, InputDevice device) {
        steve.processJoystickInput(event, -1, device);
        return true;
    }
}
