package com.chenyirun.theircraft;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.Matrix;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.os.SystemClock;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Chunk;
import com.chenyirun.theircraft.model.Point3Int;
import com.google.vr.sdk.base.*;

public class World {
    private static final String TAG = "World";
    private final Context context;
    private final Steve steve;
    private final Performance performance = Performance.getInstance();
    private final Physics physics = Physics.getInstance();

    private final Thread wireFrameThread;

    private final float[] camera = new float[16];
    private final float[] view = new float[16];
    private final float[] eulerAngles = new float[3];
    private final DBService dbService;
    private final Resources resources;
    private final MapManager mapManager;

    private int itemIndex = 2;

    private Point3Int wireFramePos = null;

    private static final int PHYSICS_ITERATIONS_PER_FRAME = 5;

    World(Context context, Resources resources){
        this.context = context;
        this.resources = resources;
        dbService = new DBService(this.context);
        mapManager = new MapManager(dbService);

        Point3Int steveBlock = dbService.getSteve();
        if (steveBlock == null){
            float blockY = mapManager.highestSolidY(0, 0);
            steveBlock = new Point3Int(0, blockY, 0);
            dbService.insertSteve(steveBlock);
        }
        steve = new Steve(steveBlock);

        Chunk currChunk = steve.currentChunk();
        mapManager.loadNeighboringChunks(currChunk);

        mapManager.waitForChunkLoad();

        wireFrameThread = createWireFrameThread();
        wireFrameThread.start();
    }

    // Asynchronous chunk loader.
    private Thread createWireFrameThread() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    wireFramePos = physics.hitTest(false, mapManager.getBlockMap(), steve);
                    SystemClock.sleep(10);
                }
            }
        };
        return new Thread(runnable);
    }

    public void onSurfaceCreated(){
        mapManager.onSurfaceCreated(resources);
    }

    public void onDrawEye(Eye eye){
        float dt = Math.min(performance.startFrame(), 0.2f);
        for (int i = 0; i < PHYSICS_ITERATIONS_PER_FRAME; ++i) {
            physics.move(steve, dt / PHYSICS_ITERATIONS_PER_FRAME, mapManager.getBlockMap());
        }
        if (steve.isOnTheGround() && !dbService.steveLocation().equals(steve.location())){
            dbService.updateSteve(steve.location());
        }

        Chunk beforeChunk = steve.currentChunk();
        Chunk afterChunk = new Chunk(steve.location());
        if (!afterChunk.equals(beforeChunk)) {
            mapManager.queueChunkLoads(beforeChunk, afterChunk);
            steve.setCurrentChunk(afterChunk);
            Log.i(TAG, "onDrawEye: update steve chunk, now at " + afterChunk);
        }

        GLHelper.beforeDraw();

        calculateView(eye);
        float[] perspective = eye.getPerspective(0.1f, 100.0f);

        performance.startRendering();
        mapManager.draw(view, perspective, wireFramePos, steve.sightVector(), steve.position());
        performance.endRendering();

        performance.endFrame();

        float fps = performance.fps();
        if (fps <= 70.0f){
            Log.i(TAG, "onDrawEye: fps=" + fps);
        }
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

    public boolean onGenericMotionEvent(MotionEvent event, InputDevice device) {
        steve.processJoystickInput(event, -1, device);
        return true;
    }

    public void onCardboardTrigger() {
        steve.jump();
    }

    public void pressA(){
        steve.jump();
    }

    public void pressX(){
        if (wireFramePos != null){
            mapManager.destroyBlock(wireFramePos);
        }
    }

    public void pressB(){
        Point3Int blockLocation = physics.hitTest(true, mapManager.getBlockMap(), steve);
        if (blockLocation == null){
            return;
        }
        if (!blockLocation.equals(steve.headLocation()) && !blockLocation.equals(steve.kneeLocation())){
            Block block = MapManager.createBlock(blockLocation, Block.items[itemIndex]);
            if (block != null){
                mapManager.addBlock(block);
            }
        }
    }

    public void pressLB(){
        if (--itemIndex < 0){
            itemIndex = Block.items.length - 1;
        }
    }

    public void pressRB(){
        //Toast.makeText(context, "RB is pressed", Toast.LENGTH_SHORT).show();
        if (++itemIndex >= Block.items.length){
            itemIndex = 0;
        }
    }

    public void walk(int walking){
        steve.walk(walking);
    }

    public void onDestroy(){
        dbService.onDestroy();
    }
}
