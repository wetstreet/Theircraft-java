package com.chenyirun.theircraft;

import android.view.InputDevice;
import android.view.MotionEvent;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Chunk;
import com.chenyirun.theircraft.model.Point3;

import java.util.HashSet;
import java.util.Set;

class Steve {
    private static final float STEVE_EYE_LEVEL = 1.62f;  // meters from feet.
    private static final float STEVE_HITBOX_HEIGHT = 1.8f;  // meters from feet.
    private static final float STEVE_HITBOX_WIDTH = 0.6f;  // meters

    private final Eye eye;
    public boolean jumping = false;
    /** Speed in axis y direction (up), in m/s. */
    private float verticalSpeed = 0.0f;

    public float mYaw;
    public float mHeadingX;
    public float mHeadingY;

    private final Object currentChunkLock = new Object();
    private Chunk currentChunk;

    /** Create Steve based on block he is standing on. */
    Steve(Block block) {
        /**
         * Initially, the eye is located at (block.x, block.z) in xz plane, at height block.y + 2.12
         * (feet to eye 1.62 + 0.5 displacement from block the feet are on).
         */
        eye = new Eye(block.x, block.y + 0.5f + STEVE_EYE_LEVEL, block.z);
        currentChunk = new Chunk(block);
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
        if (!jumping){
            verticalSpeed = Physics.JUMP_SPEED;
            jumping = true;
        }
    }

    float verticalSpeed() {
        return verticalSpeed;
    }

    void setVerticalSpeed(float verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }

    private static final Point3 ZERO_VECTOR = new Point3(0.0f, 0.0f, 0.0f);

    Point3 position() {
        return eye.position();
    }

    void setPosition(Point3 eyePosition) {
        eye.setPosition(eyePosition);
    }
    void setPosition(Block block) {
        eye.setPosition(new Point3(block.x, block.y + 0.5f + STEVE_EYE_LEVEL, block.z));
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
    Set<Block> hitboxCornerBlocks(Point3 eyePosition) {
        Hitbox hit = hitbox(eyePosition);

        Set<Block> result = new HashSet<Block>();
        result.add(new Block(hit.minX, hit.minY, hit.minZ));
        result.add(new Block(hit.maxX, hit.minY, hit.minZ));
        result.add(new Block(hit.minX, hit.maxY, hit.minZ));
        result.add(new Block(hit.maxX, hit.maxY, hit.minZ));
        result.add(new Block(hit.minX, hit.minY, hit.maxZ));
        result.add(new Block(hit.maxX, hit.minY, hit.maxZ));
        result.add(new Block(hit.minX, hit.maxY, hit.maxZ));
        result.add(new Block(hit.maxX, hit.maxY, hit.maxZ));
        return result;
    }

    /** Given Steve's eye position, returns his hitbox. */
    Hitbox hitbox(Point3 eyePosition) {
        float minX = eyePosition.x - STEVE_HITBOX_WIDTH / 2.0f;
        float maxX = minX + STEVE_HITBOX_WIDTH;
        float minY = eyePosition.y - STEVE_EYE_LEVEL;
        float maxY = minY + STEVE_HITBOX_HEIGHT;
        float minZ = eyePosition.z - STEVE_HITBOX_WIDTH / 2.0f;
        float maxZ = minZ + STEVE_HITBOX_WIDTH;

        return new Hitbox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Given Steve's eye position, returns a set of blocks corresponding to 4 points around his knees.
     */
    Set<Block> kneeBlocks(Point3 eyePosition) {
        Hitbox hit = hitbox(eyePosition);
        float kneeY = 0.5f * (hit.minY + hit.maxY);

        Set<Block> result = new HashSet<Block>();
        result.add(new Block(hit.minX, kneeY, hit.minZ));
        result.add(new Block(hit.maxX, kneeY, hit.minZ));
        result.add(new Block(hit.minX, kneeY, hit.maxZ));
        result.add(new Block(hit.maxX, kneeY, hit.maxZ));
        return result;
    }

    /**
     * Given Steve's eye position, returns a set of blocks corresponding to 4 points around his head.
     */
    Set<Block> headBlocks(Point3 eyePosition) {
        Hitbox hit = hitbox(eyePosition);

        Set<Block> result = new HashSet<Block>();
        result.add(new Block(hit.minX, hit.maxY, hit.minZ));
        result.add(new Block(hit.maxX, hit.maxY, hit.minZ));
        result.add(new Block(hit.minX, hit.maxY, hit.maxZ));
        result.add(new Block(hit.maxX, hit.maxY, hit.maxZ));
        return result;
    }
}
