package com.chenyirun.theircraft;

import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Chunk;
import com.chenyirun.theircraft.model.Point3;
import com.chenyirun.theircraft.model.Point3Int;

import java.util.HashSet;
import java.util.Set;

class Steve {
    private static final String TAG = "Steve";
    // due to the precision limit, we need to give it some value for it to work properly
    private static final float PRECISION_COMPENSATION = 0.01f;
    // give steve a initial elevation so it won't fall through the block right after spawn
    private static final float INIT_ELEVATION = 0.15f;

    public static final float STEVE_EYE_LEVEL = 1.62f;  // meters from feet.
    public static final float STEVE_HITBOX_HEIGHT = 1.8f;  // meters from feet.
    public static final float STEVE_HITBOX_WIDTH = 0.6f;  // meters

    public static final int NOT_WALKING = 0;
    public static final int WALKING_FORWARD = 1;
    public static final int WALKING_BACKWARD = 2;

    private final SteveEye eye;
    private int walking = NOT_WALKING;
    /** Speed in axis y direction (up), in m/s. */
    private float verticalSpeed = 0.0f;

    public float mPitch;
    public float mYaw;
    public float mRoll;
    public float mHeadingX;
    public float mHeadingY;

    private final Object currentChunkLock = new Object();
    private Chunk currentChunk;

    /** Create Steve based on block he is standing on. */
    Steve(Point3Int pos) {
        /**
         * Initially, the eye is located at (block.x, block.z) in xz plane, at height block.y + 2.12
         * (feet to eye 1.62 + 0.5 displacement from block the feet are on).
         */
        eye = new SteveEye(pos.x, pos.y + 0.5f + STEVE_EYE_LEVEL + INIT_ELEVATION, pos.z);
        currentChunk = new Chunk(pos);
    }

    public boolean isOnTheGround(){
        return verticalSpeed == 0;
    }

    public Point3 getSightVector(){
        double m = Math.cos(mPitch);
        float xAngle = mYaw - Physics.PI/2;
        double x = Math.cos(xAngle) * m;
        double y = Math.sin(mPitch);
        double z = Math.sin(xAngle) * m;
        return new Point3((float)x,(float)y,(float)z);
    }

    // return the location of the block steve is standing on
    public Point3Int location(){
        return new Point3Int(position().x, position().y - 0.5f - STEVE_EYE_LEVEL + PRECISION_COMPENSATION, position().z);
    }

    public void processJoystickInput(MotionEvent event, int historyPos, InputDevice device) {
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
    }

    public static float mFlat = 0.02f;
    private static float getCenteredAxis(MotionEvent event, InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range = device.getMotionRange(axis, event.getSource());
        if (range != null) {
            final float value = historyPos < 0
                    ? event.getAxisValue(axis) : event.getHistoricalAxisValue(axis, historyPos);

            if (Math.abs(value) > mFlat) {
                return value;
            }
        }
        return 0;
    }

    void jump(){
        if (verticalSpeed == 0){
            verticalSpeed = Physics.JUMP_SPEED;
        }
    }

    float verticalSpeed() {
        return verticalSpeed;
    }

    void setVerticalSpeed(float verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }

    Point3 position() {
        return eye.position();
    }

    void setPosition(Point3Int pos){
        setPosition(new Point3(pos.x, pos.y + 0.5f + STEVE_EYE_LEVEL + INIT_ELEVATION, pos.z));
    }

    void setPosition(Point3 eyePosition) {
        eye.setPosition(eyePosition);
        verticalSpeed = 0;
    }

    void walk(int status){
        this.walking = status;
    }
    boolean isWalking(){
        return walking == WALKING_BACKWARD || walking == WALKING_FORWARD;
    }

    private static final Point3 ZERO_VECTOR = new Point3(0.0f, 0.0f, 0.0f);
    Point3 motionVector() {
        if (walking == NOT_WALKING) {
            return ZERO_VECTOR;
        }

        float xAngle = mYaw - Physics.PI/2;
        float z = 0;
        float x = 0;
        if (walking == WALKING_BACKWARD){
            z = (float)-Math.sin(xAngle);
            x = (float)Math.cos(xAngle);
        } else if (walking == WALKING_FORWARD){
            z = (float)Math.sin(xAngle);
            x = (float)-Math.cos(xAngle);
        }
        return new Point3(x, 0.0f, z);
    }

    Chunk currentChunk() {
        synchronized(currentChunkLock) {
            return currentChunk;
        }
    }

    public void setCurrentChunk(Chunk chunk) {
        synchronized(currentChunkLock) {
            this.currentChunk = chunk;
        }
    }

    /**
     * Given Steve's eye position, returns a set of blocks corresponding to all 8 corners of his
     * hitbox.
     */
    Set<Point3Int> hitboxCornerBlocks(Point3 eyePosition) {
        Hitbox hit = hitbox(eyePosition);
        float minY = hit.minY + PRECISION_COMPENSATION;
        
        Set<Point3Int> result = new HashSet<>();
        result.add(new Point3Int(hit.minX, hit.maxY, hit.minZ));
        result.add(new Point3Int(hit.maxX, hit.maxY, hit.minZ));
        result.add(new Point3Int(hit.minX, hit.maxY, hit.maxZ));
        result.add(new Point3Int(hit.maxX, hit.maxY, hit.maxZ));
        result.add(new Point3Int(hit.minX, hit.minY, hit.minZ));
        result.add(new Point3Int(hit.maxX, hit.minY, hit.minZ));
        result.add(new Point3Int(hit.minX, hit.minY, hit.maxZ));
        result.add(new Point3Int(hit.maxX, hit.minY, hit.maxZ));
        result.add(new Point3Int(hit.minX, minY, hit.minZ));
        result.add(new Point3Int(hit.maxX, minY, hit.minZ));
        result.add(new Point3Int(hit.minX, minY, hit.maxZ));
        result.add(new Point3Int(hit.maxX, minY, hit.maxZ));
        return result;
    }

    /** Given Steve's eye position, returns his hitbox. */
    Hitbox hitbox(Point3 eyePosition) {
        float minX = eyePosition.x - STEVE_HITBOX_WIDTH / 2.0f;
        float maxX = minX + STEVE_HITBOX_WIDTH;
        float minY = eyePosition.y - STEVE_EYE_LEVEL + PRECISION_COMPENSATION;
        float maxY = minY + STEVE_HITBOX_HEIGHT;
        float minZ = eyePosition.z - STEVE_HITBOX_WIDTH / 2.0f;
        float maxZ = minZ + STEVE_HITBOX_WIDTH;

        return new Hitbox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Given Steve's eye position, returns a set of blocks corresponding to 4 points around his knees.
     */
    Set<Point3Int> kneeBlocks(Point3 eyePosition) {
        Hitbox hit = hitbox(eyePosition);
        float kneeY = 0.5f * (hit.minY + hit.maxY);

        Set<Point3Int> result = new HashSet<>();
        result.add(new Point3Int(hit.minX, kneeY, hit.minZ));
        result.add(new Point3Int(hit.maxX, kneeY, hit.minZ));
        result.add(new Point3Int(hit.minX, kneeY, hit.maxZ));
        result.add(new Point3Int(hit.maxX, kneeY, hit.maxZ));
        return result;
    }

    /**
     * Given Steve's eye position, returns a set of blocks corresponding to 4 points around his head.
     */
    Set<Point3Int> headBlocks(Point3 eyePosition) {
        Hitbox hit = hitbox(eyePosition);

        Set<Point3Int> result = new HashSet<>();
        result.add(new Point3Int(hit.minX, hit.maxY, hit.minZ));
        result.add(new Point3Int(hit.maxX, hit.maxY, hit.minZ));
        result.add(new Point3Int(hit.minX, hit.maxY, hit.maxZ));
        result.add(new Point3Int(hit.maxX, hit.maxY, hit.maxZ));
        return result;
    }
}
