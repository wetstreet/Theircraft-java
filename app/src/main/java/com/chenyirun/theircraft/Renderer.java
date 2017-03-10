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

    private final Set<Block> blocks = new HashSet<Block>();

    private Resources resources;

    Renderer(Resources resources) {
        this.resources = resources;
        generator = new Generator(new Random().nextInt());
    }

    /** Given (x,z) coordinates, finds and returns the highest y so that (x,y,z) is a solid block. */
    private float highestSolidY(float x, float z) {
        float maxY = Generator.minElevation();
        for (Block block : mGrass.getList()) {
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

    private final static int mFloorHeight = 71;

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glFrontFace(GLES20.GL_CCW);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        mGrass = new Grass(resources);

        // preset blocks
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

        blocks.addAll(mGrass.getList());

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

    private final float[] lightPosInEyeSpace = new float[4];
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] {0.0f, 2.0f, 0.0f, 1.0f};
    private static final int PHYSICS_ITERATIONS_PER_FRAME = 5;

    @Override
    public void onDrawEye(Eye eye) {
        float dt = Math.min(performance.startFrame(), 0.2f);
        physics.move(steve, dt / PHYSICS_ITERATIONS_PER_FRAME, blocks);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);
        Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, LIGHT_POS_IN_WORLD_SPACE, 0);
        float[] perspective = eye.getPerspective(0.1f, 100.0f);
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        mGrass.drawList(view, perspective);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {}

    public boolean onGenericMotionEvent(MotionEvent event, InputDevice device) {
        steve.processJoystickInput(event, -1, device);
        return true;
    }

    public static int loadShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        if (compileStatus[0] == 0) {
            Log.e("TreasureHuntActivity", "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    public static int loadTexture(Resources resources, int resourceId) {
        int textureHandles[] = new int[1];
        GLES20.glGenTextures(1, textureHandles, 0);
        if (textureHandles[0] == 0) {
            throw new RuntimeException("Failed to create a texture");
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandles[0]);

        Bitmap bitmap = loadBitmap(resources, resourceId);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        return textureHandles[0];
    }

    private static Bitmap loadBitmap(Resources resources, int resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;   // No pre-scaling
        Bitmap result = BitmapFactory.decodeResource(resources, resourceId, options);
        if (result == null) {
            throw new RuntimeException("Failed to decode bitmap from resource");
        }
        return result;
    }
}
