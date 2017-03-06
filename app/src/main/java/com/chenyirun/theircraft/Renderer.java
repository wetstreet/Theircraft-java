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

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import javax.microedition.khronos.egl.EGLConfig;

public class Renderer implements GvrView.StereoRenderer {

    private Floor mFloor;
    private Grass mGrass[];
    public Point mPosition;

    // direction of the body
    public float mYaw;

    public float mHeadingX;
    public float mHeadingY;
    private float mHeadingAngle;
    private float mHeadingMagnitude;

    private int mDPadState;

    public class Point{
        public float x;
        public float y;
        public float z;

        Point(float x, float y, float z){
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private final float[] camera = new float[16];
    private final float[] view = new float[16];

    private Resources resources;
    private float EYE_HEIGHT = 1.8f;

    Renderer(Resources resources) {
        mPosition = new Point(0.0f, EYE_HEIGHT, 0.01f);
        this.resources = resources;
    }

    @Override
    public void onRendererShutdown() {}

    @Override
    public void onSurfaceChanged(int width, int height) {}

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        mFloor = new Floor();
        mGrass = new Grass[3];
        mGrass[0] = new Grass(1.0f, 0.5f, -2f, resources);
        mGrass[1] = new Grass(0.0f, 0.5f, -2f, resources);
        mGrass[2] = new Grass(-1.0f, 0.5f, -2f, resources);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        GLES20.glClearColor(0.5f, 0.69f, 1.0f, 1.0f);
        Matrix.setLookAtM(camera, 0, mPosition.x, mPosition.y, mPosition.z,
                mPosition.x, mPosition.y, mPosition.z - 0.01f,
                0.0f, 1.0f, 0.0f);
        float[] eulerAngles = new float[3];
        headTransform.getEulerAngles(eulerAngles, 0);
        mYaw = eulerAngles[1];
        if (mHeadingX != 0 || mHeadingY != 0){
            move();
        }
    }

    private static final float PI = 3.14159265358979323846f;
    private static final float speed = 0.2f;

    void move(){
        float xAngle = mYaw - PI/2;
        // move forward and backward
        mPosition.z += speed * (mHeadingY * -Math.sin(xAngle) + mHeadingX * -Math.cos(xAngle));
        // move rightward and leftward
        mPosition.x += speed * (mHeadingY * Math.cos(xAngle) + mHeadingX * -Math.sin(xAngle));
    }

    void moveUp(){
        mPosition.y += 0.1f;
    }

    void jump(){
    }

    void pressX(){
    }

    private final float[] lightPosInEyeSpace = new float[4];
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] {0.0f, 2.0f, 0.0f, 1.0f};

    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);
        Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, LIGHT_POS_IN_WORLD_SPACE, 0);
        float[] perspective = eye.getPerspective(0.1f, 100.0f);
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        mFloor.draw(view, perspective, lightPosInEyeSpace);
        for (Grass g : mGrass) {
            g.draw(view, perspective);
        }
    }

    @Override
    public void onFinishFrame(Viewport viewport) {}

    public boolean onGenericMotionEvent(MotionEvent event, InputDevice device) {
        if (0 == mDPadState) {
            final int historySize = event.getHistorySize();
            for (int i = 0; i < historySize; i++) {
                processJoystickInput(event, i, device);
            }

            processJoystickInput(event, -1, device);
        }
        return true;
    }
    
    private void processJoystickInput(MotionEvent event, int historyPos, InputDevice device) {
        if (null == device) {
            device = event.getDevice();
        }
        mHeadingX = getCenteredAxis(event, device, MotionEvent.AXIS_X, historyPos);
        if (mHeadingX == 0) {
            mHeadingX = getCenteredAxis(event, device, MotionEvent.AXIS_HAT_X, historyPos);
        }

        mHeadingY = getCenteredAxis(event, device, MotionEvent.AXIS_Y, historyPos);
        if (mHeadingY == 0) {
            mHeadingY = getCenteredAxis(event, device, MotionEvent.AXIS_HAT_Y, historyPos);
        }

        mHeadingMagnitude = pythag(mHeadingX, mHeadingY);
        if (mHeadingMagnitude > 0.1f) {
            mHeadingAngle = (float) Math.atan2(mHeadingY, mHeadingX);
        }

        //move(historyPos < 0 ? event.getEventTime() : event.getHistoricalEventTime(historyPos));
    }

    private static float pythag(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }

    private static float getCenteredAxis(MotionEvent event, InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range = device.getMotionRange(axis, event.getSource());
        if (range != null) {
            final float flat = range.getFlat();
            final float value = historyPos < 0
                    ? event.getAxisValue(axis) : event.getHistoricalAxisValue(axis, historyPos);

            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
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
