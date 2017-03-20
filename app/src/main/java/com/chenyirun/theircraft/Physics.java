package com.chenyirun.theircraft;

import android.util.Log;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3;

import java.util.HashSet;
import java.util.Set;

public class Physics {
    private static final String TAG = "Physics";

    private static final float PI = 3.14159265358979323846f;
    private static final float STEVE_WALKING_SPEED = 4.317f;  // m/s
    private static final float GRAVITY = 32.0f;  // m/s^2
    private static final float TERMINAL_VELOCITY = 78.4f; // m/s
    private static final float MAX_JUMP_HEIGHT = 1.252f;  // m
    public static final float JUMP_SPEED = (float)Math.sqrt(2.0f * GRAVITY * MAX_JUMP_HEIGHT);

    void move(Steve steve, float dt, Set<Block> blocks){
        if (dt <= 0.0f) {
            return;
        }

        if (dt > 0.05f) {
            Log.i(TAG, "Skipped physics, dt: " + dt);
            return;
        }

        float verticalSpeed = Math.max(steve.verticalSpeed() - dt * GRAVITY, -TERMINAL_VELOCITY);
        Point3 newPosition;
        if (steve.isWalking()){
            Point3 dxyz = steve.motionVector().times(dt * STEVE_WALKING_SPEED).plusY(dt * verticalSpeed);
            newPosition = steve.position().plus(dxyz);
        } else {
            // -1 <= x <= 1, -1 <= y <= 1
            float x = steve.mHeadingX;
            float y = steve.mHeadingY;
            float xAngle = steve.mYaw - PI/2;
            double dz = dt * STEVE_WALKING_SPEED * (y * -Math.sin(xAngle) + x * -Math.cos(xAngle));
            double dx = dt * STEVE_WALKING_SPEED * (y * Math.cos(xAngle) + x * -Math.sin(xAngle));
            double dy = dt * verticalSpeed;
            newPosition = steve.position().plus((float)dx,(float)dy,(float)dz);
        }
        PositionStopVertical adjusted = collisionAdjust(steve, newPosition, blocks);
        steve.setPosition(adjusted.position);

        verticalSpeed = adjusted.stopVertical ? 0.0f : verticalSpeed;
        steve.setVerticalSpeed(verticalSpeed);
        if (shouldJump(steve, newPosition, blocks)) {
            steve.jump();
        }
    }

    private static class PositionStopVertical {
        private final Point3 position;
        private final boolean stopVertical;

        PositionStopVertical(Point3 position, boolean stopVertical) {
            this.position = position;
            this.stopVertical = stopVertical;
        }
    }

    private PositionStopVertical collisionAdjust(Steve steve, Point3 eyePosition, Set<Block> blocks) {
        Set<Block> collidingBlocks = new HashSet<>();
        for (Block block : steve.hitboxCornerBlocks(eyePosition)) {
            if (blocks.contains(block)) {
                collidingBlocks.add(block);
            }
        }
        if (collidingBlocks.isEmpty()) {
            return new PositionStopVertical(eyePosition, false);
        }

        boolean stopVertical = false;
        for (Block collidingBlock : collidingBlocks) {
            PositionStopVertical adjusted = pushOut(steve, collidingBlock, eyePosition);
            eyePosition = adjusted.position;
            stopVertical |= adjusted.stopVertical;
        }
        return new PositionStopVertical(eyePosition, stopVertical);
    }

    private static final float OVERLAP_THRESHOLD = 0.25f;

    private PositionStopVertical pushOut(Steve steve, Block block, Point3 eyePosition) {
        Hitbox hit = steve.hitbox(eyePosition);

        float overlapX = Math.min(block.x + 0.5f, hit.maxX) - Math.max(block.x - 0.5f, hit.minX);
        if (overlapX < 0.0f) {
            overlapX = 0.0f;
        }

        float overlapY = Math.min(block.y + 0.5f, hit.maxY) - Math.max(block.y - 0.5f, hit.minY);
        if (overlapY < 0.0f) {
            overlapY = 0.0f;
        }

        float overlapZ = Math.min(block.z + 0.5f, hit.maxZ) - Math.max(block.z - 0.5f, hit.minZ);
        if (overlapZ < 0.0f) {
            overlapZ = 0.0f;
        }

        // Push out the smallest overlap, if the others are above a threshold.
        boolean stopVertical = false;
        if (overlapX <= overlapY && overlapX <= overlapZ) {
            if (overlapX > 0.0f && overlapY >= OVERLAP_THRESHOLD && overlapZ >= OVERLAP_THRESHOLD) {
                eyePosition = pushOutX(block, eyePosition, hit.minX, hit.maxX);
            }
        } else if (overlapY <= overlapX && overlapY <= overlapZ) {
            if (overlapY > 0.0f && overlapX >= OVERLAP_THRESHOLD && overlapZ >= OVERLAP_THRESHOLD) {
                eyePosition = pushOutY(block, eyePosition, hit.minY, hit.maxY);
                // If collided with ground or ceiling, immediately stop falling or rising.
                stopVertical = true;
            }
        } else {  // overlapZ <= overlapX && overlapZ <= overlapY
            if (overlapZ > 0.0f && overlapX >= OVERLAP_THRESHOLD && overlapY >= OVERLAP_THRESHOLD) {
                eyePosition = pushOutZ(block, eyePosition, hit.minZ, hit.maxZ);
            }
        }

        return new PositionStopVertical(eyePosition, stopVertical);
    }

    private Point3 pushOutX(Block block, Point3 p, float min, float max) {
        float mid = 0.5f * (min + max);
        if (mid < block.x) {
            float overlap = max - (block.x - 0.5f);
            return new Point3(p.x - overlap, p.y, p.z);
        } else {
            float overlap = (block.x + 0.5f) - min;
            return new Point3(p.x + overlap, p.y, p.z);
        }
    }

    private Point3 pushOutY(Block block, Point3 p, float min, float max) {
        float mid = 0.5f * (min + max);
        if (mid < block.y) {
            float overlap = max - (block.y - 0.5f);
            return new Point3(p.x, p.y - overlap, p.z);
        } else {
            float overlap = (block.y + 0.5f) - min;
            return new Point3(p.x, p.y + overlap, p.z);
        }
    }

    private Point3 pushOutZ(Block block, Point3 p, float min, float max) {
        float mid = 0.5f * (min + max);
        if (mid < block.z) {
            float overlap = max - (block.z - 0.5f);
            return new Point3(p.x, p.y, p.z - overlap);
        } else {
            float overlap = (block.z + 0.5f) - min;
            return new Point3(p.x, p.y, p.z + overlap);
        }
    }

    public boolean shouldJump(Steve steve, Point3 eyePosition, Set<Block> blocks) {
        return intersects(steve.kneeBlocks(eyePosition), blocks) &&
                !intersects(steve.headBlocks(eyePosition), blocks);
    }

    public static <T> boolean intersects(Set<T> smallSet, Set<T> largeSet) {
        for (T el : smallSet) {
            if (largeSet.contains(el)) {
                return true;
            }
        }
        return false;
    }
}
