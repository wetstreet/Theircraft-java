package com.chenyirun.theircraft;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Buffers;
import com.chenyirun.theircraft.model.Chunk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlockMap {
    private final Object blocksLock = new Object();
    private final Set<Block> blocks = new HashSet<>();
    private final Map<Chunk, List<Block>> chunkBlocks = new HashMap<>();

    public boolean exposed(Block block) {
        return !contain(block.x - 1, block.y, block.z) ||
                !contain(block.x + 1, block.y, block.z) ||
                !contain(block.x, block.y - 1, block.z) ||
                !contain(block.x, block.y + 1, block.z) ||
                !contain(block.x, block.y, block.z - 1) ||
                !contain(block.x, block.y, block.z + 1);
    }

    public boolean contain(int x, int y, int z){
        for (Block block : blocks) {
            if (block.x == x && block.y == y && block.z == z){
                return true;
            }
        }
        return false;
    }

    public Set<Block> getBlocks(){
        return blocks;
    }

    public List<Block> getChunkBlocks(Chunk chunk){
        return chunkBlocks.get(chunk);
    }
}
