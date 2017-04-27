package com.chenyirun.theircraft.model;

public class Chunk extends Point3Int {
    /** Blocks per side of a chunk. */
    public static final int CHUNK_SIZE = 16;

    public Chunk(int x, int y, int z) {
        super(x, y, z);
    }

    public Chunk(Point3Int block) {
        super((int)Math.floor(block.x / (float)CHUNK_SIZE),
                (int)Math.floor(block.y / (float)CHUNK_SIZE),
                (int)Math.floor(block.z / (float)CHUNK_SIZE));
    }

    public Chunk plus(Chunk chunk) {
        return new Chunk(x + chunk.x, y + chunk.y, z + chunk.z);
    }

    @Override
    public String toString() {
        return "Chunk(" + x + ", " + y + ", " + z + ')';
    }
}
