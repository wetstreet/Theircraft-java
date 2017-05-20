package com.chenyirun.theircraft.map;

import com.chenyirun.theircraft.GLHelper;
import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Buffers;
import com.chenyirun.theircraft.model.Chunk;
import com.chenyirun.theircraft.model.Point3Int;
import com.chenyirun.theircraft.model.VertexIndexTextureList;
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
    private final Set<Point3Int> noncolliding = new HashSet<>();
    private final Set<Point3Int> transparent = new HashSet<>();
    private final Map<Chunk, List<Block>> chunkBlocks = new HashMap<>();

    private final Object chunkBlocksLock = new Object();

    public Block getBlock(Point3Int pos){
        Chunk chunk = new Chunk(pos);
        List<Block> blocksInChunk = new ArrayList<>();
        blocksInChunk.addAll(getChunkBlocks(chunk));
        for (Block block : blocksInChunk) {
            if (block == null){
                continue;
            }
            Point3Int blockLocation = block.getLocation();
            if (pos.equals(blockLocation)){
                return block;
            }
        }
        return null;
    }

    public boolean contain(Point3Int point){
        return blockLocations.contains(point);
    }

    public boolean noncolliding(Point3Int loc){
        return noncolliding.contains(loc);
    }

    public boolean isTransparent(Point3Int loc){
        return transparent.contains(loc);
    }

    public boolean containChunk(Chunk chunk){
        return chunkBlocks.keySet().contains(chunk);
    }

    public List<Block> getChunkBlocks(Chunk chunk){
        return chunkBlocks.get(chunk);
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
        //blockLocations.removeAll(blocksInChunk);
        chunkBlocks.remove(chunk);
    }

    public void addBlock(Block block){
        Chunk chunk = new Chunk(block);
        List<Block> blocksInChunk = getChunkBlocks(chunk);
        // if the chunk is not loaded, do nothing
        if (blocksInChunk == null){
            return;
        }
        synchronized (chunkBlocksLock){
            blocksInChunk.add(block);
        }
        blocks.add(block);
        blockLocations.add(block.getLocation());
        if (!block.isCollidable()){
            noncolliding.add(block.getLocation());
        }
        if (block.isTransparent()){
            transparent.add(block.getLocation());
        }
    }

    public void removeBlock(Block block){
        Chunk chunk = new Chunk(block);
        List<Block> blocksInChunk = getChunkBlocks(chunk);
        // if the chunk is not loaded, do nothing
        if (blocksInChunk == null){
            return;
        }
        synchronized (chunkBlocksLock){
            blocksInChunk.remove(block);
        }
        blocks.remove(block);
        blockLocations.remove(block.getLocation());
        if (!block.isCollidable()){
            noncolliding.remove(block.getLocation());
        }
    }

    public List<Block> shownBlocks(Chunk chunk) {
        List<Block> blocksInChunk = new ArrayList<>();
        blocksInChunk.addAll(getChunkBlocks(chunk));
        List<Block> result = new ArrayList<>();
        for (Block block : blocksInChunk) {
            if (exposed(block)) {
                result.add(block);
            }
        }
        return result;
    }

    public boolean exposed(Block block) {
        return !contain(block.getLeftLoc()) || isTransparent(block.getLeftLoc()) ||
                !contain(block.getRightLoc()) || isTransparent(block.getLeftLoc()) ||
                !contain(block.getBottomLoc()) || isTransparent(block.getBottomLoc()) ||
                !contain(block.getTopLoc()) || isTransparent(block.getTopLoc()) ||
                !contain(block.getBackLoc()) || isTransparent(block.getBackLoc()) ||
                !contain(block.getFrontLoc()) || isTransparent(block.getFrontLoc());
    }

    public Buffers createBuffers(List<Block> shownBlocks) {
        VertexIndexTextureList vitList = new VertexIndexTextureList();
        for (Block block : shownBlocks) {
            if (block.isCollidable()){
                // Only add faces that are not between two blocks and thus invisible.
                if (!contain(block.getTopLoc()) || isTransparent(block.getTopLoc())) {
                    vitList.addTopFace(block);
                }
                if (!contain(block.getFrontLoc()) || isTransparent(block.getFrontLoc())) {
                    vitList.addFrontFace(block);
                }
                if (!contain(block.getLeftLoc()) || isTransparent(block.getLeftLoc())) {
                    vitList.addLeftFace(block);
                }
                if (!contain(block.getRightLoc()) || isTransparent(block.getRightLoc())) {
                    vitList.addRightFace(block);
                }
                if (!contain(block.getBackLoc()) || isTransparent(block.getBackLoc())) {
                    vitList.addBackFace(block);
                }
                if (!contain(block.getBottomLoc()) || isTransparent(block.getBottomLoc())) {
                    vitList.addBottomFace(block);
                }
            } else {
                vitList.addPrimaryCrossFace(block);
                vitList.addSecondaryCrossFace(block);
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