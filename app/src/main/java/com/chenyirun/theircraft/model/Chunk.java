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

    public Chunk plus(int x, int y, int z) {
        return new Chunk(this.x + x, this.y + y, this.z + z);
    }

    @Override
    public String toString() {
        return "Chunk(" + x + ", " + y + ", " + z + ')';
    }
}
