package com.chenyirun.theircraft;

import android.util.Log;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Chunk;
import com.chenyirun.theircraft.model.Point3;
import com.chenyirun.theircraft.model.Point3Int;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Physics {
    private static final String TAG = "Physics";

    private static final boolean autoJump = false;

    public static final float PI = 3.14159265358979323846f;
    private static final float STEVE_WALKING_SPEED = 4.317f;  // m/s
    private static final float GRAVITY = 32.0f;  // m/s^2
    private static final float TERMINAL_VELOCITY = 78.4f; // m/s
    private static final float MAX_JUMP_HEIGHT = 1.252f;  // m
    public static final float JUMP_SPEED = (float)Math.sqrt(2.0f * GRAVITY * MAX_JUMP_HEIGHT);

    private static final int SAMPLE_RATE = 32;
    private static final int REACH_DISTANCE = 4;

    private Physics(){}

    private static Physics instance = new Physics();

    public synchronized static Physics getInstance(){
        return instance;
    }

    public Point3Int hitTest(boolean previous, BlockMap blockMap, Steve steve){
        Point3Int result = null;
        float nearest = 0;
        Set<Chunk> chunks = MapManager.neighboringChunks(steve.currentChunk(), 1);
        for (Chunk chunk : chunks) {
            Point3Int hitLoc = hitTestFunc(previous, blockMap, chunk, steve);
            if (hitLoc != null){
                float distance = hitLoc.distance(steve.headLocation());
                if (distance < nearest || nearest == 0){
                    nearest = distance;
                    result = hitLoc;
                }
            }
        }
        return result;
    }

    public Point3Int hitTestFunc(boolean previous, BlockMap blockMap, Chunk chunk, Steve steve){
        Point3 pos = new Point3(steve.position());
        Point3Int prevBlockLoc = new Point3Int(pos);
        List<Block> chunkBlocks = blockMap.getChunkBlocks(chunk);
        if (chunkBlocks == null){
            return null;
        }
        // get the position of the first pos in the sight direction
        for (int i = 0; i < REACH_DISTANCE * SAMPLE_RATE; i++){
            Point3Int newBlockPos = new Point3Int(pos);
            if (!prevBlockLoc.equals(newBlockPos)){
                if (blockMap.contain(newBlockPos)){
                    Block b = blockMap.getBlock(newBlockPos);
                    if (b != null){
                        if (previous){
                            return prevBlockLoc;
                        } else {
                            return b.getLocation();
                        }
                    }
                }
                prevBlockLoc = newBlockPos;
            }
            pos = pos.plus(steve.sightVector().divide(SAMPLE_RATE));
        }
        return null;
    }

    public void move(Steve steve, float dt, BlockMap blockMap){
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
        PositionStopVertical adjusted = collisionAdjust(steve, newPosition, blockMap);
        steve.setPosition(adjusted.position);

        verticalSpeed = adjusted.stopVertical ? 0.0f : verticalSpeed;
        steve.setVerticalSpeed(verticalSpeed);
        if (autoJump && shouldJump(steve, newPosition, blockMap)) {
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

    private PositionStopVertical collisionAdjust(Steve steve, Point3 eyePosition, BlockMap blockMap) {
        Set<Point3Int> collidingBlocks = new HashSet<>();
        Set<Point3Int> cornerBlocks = steve.hitboxCornerBlocks(eyePosition);
        for (Point3Int pos : cornerBlocks) {
            if (blockMap.contain(pos)) {
                collidingBlocks.add(pos);
            }
        }
        if (collidingBlocks.isEmpty()) {
            return new PositionStopVertical(eyePosition, false);
        }

        for (Point3Int pos : collidingBlocks) {
            if (blockMap.noncolliding(pos)){
                collidingBlocks.remove(pos);
            }
        }

        boolean stopVertical = false;
        for (Point3Int collidingBlock : collidingBlocks) {
            PositionStopVertical adjusted = pushOut(steve, collidingBlock, eyePosition);
            eyePosition = adjusted.position;
            stopVertical |= adjusted.stopVertical;
        }
        return new PositionStopVertical(eyePosition, stopVertical);
    }

    private static final float OVERLAP_THRESHOLD = 0.25f;

    private PositionStopVertical pushOut(Steve steve, Point3Int pos, Point3 eyePosition) {
        Hitbox hit = steve.hitbox(eyePosition);

        float overlapX = Math.min(pos.x + 0.5f, hit.maxX) - Math.max(pos.x - 0.5f, hit.minX);
        if (overlapX < 0.0f) {
            overlapX = 0.0f;
        }

        float overlapY = Math.min(pos.y + 0.5f, hit.maxY) - Math.max(pos.y - 0.5f, hit.minY);
        if (overlapY < 0.0f) {
            overlapY = 0.0f;
        }

        float overlapZ = Math.min(pos.z + 0.5f, hit.maxZ) - Math.max(pos.z - 0.5f, hit.minZ);
        if (overlapZ < 0.0f) {
            overlapZ = 0.0f;
        }

        // Push out the smallest overlap, if the others are above a threshold.
        boolean stopVertical = false;
        if (overlapX <= overlapY && overlapX <= overlapZ) {
            if (overlapX > 0.0f && overlapY >= OVERLAP_THRESHOLD && overlapZ >= OVERLAP_THRESHOLD) {
                eyePosition = pushOutX(pos, eyePosition, hit.minX, hit.maxX);
            }
        } else if (overlapY <= overlapX && overlapY <= overlapZ) {
            if (overlapY > 0.0f && overlapX >= OVERLAP_THRESHOLD && overlapZ >= OVERLAP_THRESHOLD) {
                eyePosition = pushOutY(pos, eyePosition, hit.minY, hit.maxY);
                // If collided with ground or ceiling, immediately stop falling or rising.
                stopVertical = true;
            }
        } else {  // overlapZ <= overlapX && overlapZ <= overlapY
            if (overlapZ > 0.0f && overlapX >= OVERLAP_THRESHOLD && overlapY >= OVERLAP_THRESHOLD) {
                eyePosition = pushOutZ(pos, eyePosition, hit.minZ, hit.maxZ);
            }
        }

        return new PositionStopVertical(eyePosition, stopVertical);
    }

    private Point3 pushOutX(Point3Int pos, Point3 p, float min, float max) {
        float mid = 0.5f * (min + max);
        if (mid < pos.x) {
            float overlap = max - (pos.x - 0.5f);
            return new Point3(p.x - overlap, p.y, p.z);
        } else {
            float overlap = (pos.x + 0.5f) - min;
            return new Point3(p.x + overlap, p.y, p.z);
        }
    }

    private Point3 pushOutY(Point3Int pos, Point3 p, float min, float max) {
        float mid = 0.5f * (min + max);
        if (mid < pos.y) {
            float overlap = max - (pos.y - 0.5f);
            return new Point3(p.x, p.y - overlap, p.z);
        } else {
            float overlap = (pos.y + 0.5f) - min;
            return new Point3(p.x, p.y + overlap, p.z);
        }
    }

    private Point3 pushOutZ(Point3Int pos, Point3 p, float min, float max) {
        float mid = 0.5f * (min + max);
        if (mid < pos.z) {
            float overlap = max - (pos.z - 0.5f);
            return new Point3(p.x, p.y, p.z - overlap);
        } else {
            float overlap = (pos.z + 0.5f) - min;
            return new Point3(p.x, p.y, p.z + overlap);
        }
    }

    private boolean shouldJump(Steve steve, Point3 eyePosition, BlockMap blockMap) {
        return blockMap.intersects(steve.kneeBlocks(eyePosition)) && !blockMap.intersects(steve.headBlocks(eyePosition));
    }
}
