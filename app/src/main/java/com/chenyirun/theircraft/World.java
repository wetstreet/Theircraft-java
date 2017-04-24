package com.chenyirun.theircraft;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.Matrix;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Chunk;
import com.chenyirun.theircraft.model.Point3Int;
import com.google.vr.sdk.base.*;

public class World {
    private static final String TAG = "World";
    private final Steve steve;
    private final Performance performance = Performance.getInstance();
    private final Physics physics = new Physics();

    private final float[] camera = new float[16];
    private final float[] view = new float[16];
    private final float[] eulerAngles = new float[3];
    private final DBService dbService;
    private final Resources resources;
    private final MapManager mapManager;

    World(Context context, Resources resources){
        this.resources = resources;
        dbService = new DBService(context);
        mapManager = new MapManager(dbService);

        Point3Int steveBlock = dbService.getSteve();
        if (steveBlock == null){
            float blockY = mapManager.highestSolidY(0, 0);
            steveBlock = new Point3Int(0, blockY, 0);
        }
        steve = new Steve(steveBlock);

        Chunk currChunk = steve.currentChunk();
        mapManager.loadNeighboringChunks(currChunk);
    }

    public void onSurfaceCreated(){
        mapManager.onSurfaceCreated(resources);
    }

    private static final int PHYSICS_ITERATIONS_PER_FRAME = 5;
    public void onDrawEye(Eye eye){
        float dt = Math.min(performance.startFrame(), 0.2f);
        for (int i = 0; i < PHYSICS_ITERATIONS_PER_FRAME; ++i) {
            physics.move(steve, dt / PHYSICS_ITERATIONS_PER_FRAME, mapManager.getBlockMap());
        }
        if (steve.isOnTheGround() && dbService.steveNeedsUpdate(steve.position())){
            dbService.updateSteve(steve.getBlock());
        }

        Chunk beforeChunk = steve.currentChunk();
        Chunk afterChunk = new Chunk(steve.position());
        if (!afterChunk.equals(beforeChunk)) {
            mapManager.queueChunkLoads(beforeChunk, afterChunk);
            steve.setCurrentChunk(afterChunk);
            Log.i(TAG, "onDrawEye: update steve chunk, now at " + afterChunk);
        }

        GLHelper.beforeDraw();

        calculateView(eye);
        float[] perspective = eye.getPerspective(0.1f, 100.0f);

        performance.startRendering();
        mapManager.draw(view, perspective);
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

    public boolean onGenericMotionEvent(MotionEvent event, InputDevice device) {
        steve.processJoystickInput(event, -1, device);
        return true;
    }

    private void resetSteve(){
        Point3Int pos = new Point3Int(0, 74, 0);
        steve.setPosition(pos);
        dbService.updateSteve(pos);
    }

    public void pressX(){
        //physics.hitTest(false, chunkBlocks, steve);
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
        Point3Int floatingBlock = new Point3Int(steve.position().plus(0, 2, 0));
        mapManager.destroyBlock(floatingBlock);
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
