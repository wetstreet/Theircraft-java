package com.chenyirun.theircraft;

import android.util.Log;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Buffers;
import com.chenyirun.theircraft.model.Chunk;
import com.chenyirun.theircraft.model.Point3;
import com.chenyirun.theircraft.model.Point3Int;
import com.chenyirun.theircraft.perlin.Generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlockMap {
    private static final String TAG = "BlockMap";
    private final Set<Block> blocks = new HashSet<>();
    private final Set<Point3Int> blockLocations = new HashSet<>();
    private final Map<Chunk, List<Block>> chunkBlocks = new HashMap<>();

    private final Object listLock = new Object();

    public Block getBlock(Point3Int pos){
        Chunk chunk = new Chunk(pos);
        List<Block> blocksInChunk = getChunkBlocks(chunk);
        if (blocksInChunk == null){
            Log.i(TAG, "getBlock: chunk doesn't have a block");
            return null;
        }
        synchronized (listLock){
            for (Block block : blocksInChunk) {
                Point3Int blockLocation = block.getLocation();
                if (pos.equals(blockLocation)){
                    return block;
                }
            }
        }
        return null;
    }

    public boolean contain(int x, int y, int z){
        return blockLocations.contains(new Point3Int(x, y, z));
    }

    public boolean contain(Point3Int point){
        return contain(point.x, point.y, point.z);
    }

    public boolean containChunk(Chunk chunk){
        return chunkBlocks.keySet().contains(chunk);
    }

    public void addChunk(Chunk chunk, List<Block> blocksInChunk, Set<Point3Int> blockLocations){
        blocks.addAll(blocksInChunk);
        this.blockLocations.addAll(blockLocations);
        chunkBlocks.put(chunk, blocksInChunk);
    }

    public void removeChunk(Chunk chunk){
        List<Block> blocksInChunk = getChunkBlocks(chunk);
        if (blocksInChunk == null){
            return;
        }
        blocks.removeAll(blocksInChunk);
        blockLocations.removeAll(blocksInChunk);
        chunkBlocks.remove(chunk);
    }

    public void addBlock(Block block){
        Chunk chunk = new Chunk(block);
        List<Block> blocksInChunk = chunkBlocks.get(chunk);
        // if the chunk is not loaded, do nothing
        if (blocksInChunk == null){
            return;
        }
        synchronized (listLock){
            blocksInChunk.add(block);
        }
        blocks.add(block);
        blockLocations.add(block.getLocation());
    }

    public void removeBlock(Block block){
        Chunk chunk = new Chunk(block);
        List<Block> blocksInChunk = chunkBlocks.get(chunk);
        // if the chunk is not loaded, do nothing
        if (blocksInChunk == null){
            return;
        }
        synchronized (listLock){
            blocksInChunk.remove(block);
        }
        blocks.remove(block);
        blockLocations.remove(block.getLocation());
    }
/*
    public void removeBlock(Point3Int blockLocation){
        Block block = getBlock(blockLocation);
        if (block == null){
            return;
        }
        Chunk chunk = new Chunk(block);
        synchronized (listLock){
            chunkBlocks.get(chunk).remove(block);
        }
        blocks.remove(block);
        blockLocations.remove(block.getLocation());
    }*/

    public List<Block> shownBlocks(Chunk chunk) {
        List<Block> chunkBlocks = getChunkBlocks(chunk);
        List<Block> result = new ArrayList<>();
        if (chunkBlocks == null) {
            return result;
        }

        for (Block block : chunkBlocks) {
            if (exposed(block)) {
                result.add(block);
            }
        }
        return result;
    }

    public boolean exposed(Block block) {
        return !contain(block.x - 1, block.y, block.z) ||
                !contain(block.x + 1, block.y, block.z) ||
                !contain(block.x, block.y - 1, block.z) ||
                !contain(block.x, block.y + 1, block.z) ||
                !contain(block.x, block.y, block.z - 1) ||
                !contain(block.x, block.y, block.z + 1);
    }

    public List<Block> getChunkBlocks(Chunk chunk){
        return chunkBlocks.get(chunk);
    }

    public Buffers createBuffers(List<Block> shownBlocks) {
        VertexIndexTextureList vitList = new VertexIndexTextureList();
        for (Block block : shownBlocks) {
            // Only add faces that are not between two blocks and thus invisible.
            if (!contain(block.x, block.y + 1, block.z)) {
                vitList.addTopFace(block);
            }
            if (!contain(block.x, block.y, block.z + 1)) {
                vitList.addFrontFace(block);
            }
            if (!contain(block.x - 1, block.y, block.z)) {
                vitList.addLeftFace(block);
            }
            if (!contain(block.x + 1, block.y, block.z)) {
                vitList.addRightFace(block);
            }
            if (!contain(block.x, block.y, block.z - 1)) {
                vitList.addBackFace(block);
            }
            if (!contain(block.x, block.y - 1, block.z)) {
                vitList.addBottomFace(block);
            }
        }

        return new Buffers(
                GLHelper.createFloatBuffer(vitList.getVertexArray()),
                GLHelper.createShortBuffer(vitList.getIndexArray()),
                GLHelper.createFloatBuffer(vitList.getTextureCoordArray()));
    }

    // Given (x,z) coordinates, finds and returns the highest y so that (x,y,z) is a solid block.
    public float highestSolidY(float x, float z) {
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

    public boolean intersects(Set<Point3Int> hitBox) {
        for (Point3Int pos : hitBox) {
            if (blockLocations.contains(pos)) {
                return true;
            }
        }
        return false;
    }
}