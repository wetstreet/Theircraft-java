package com.chenyirun.theircraft.model;

public class Chunk extends Point3Int {
    /** Blocks per side of a chunk. */
    public static final int CHUNK_SIZE = 16;

    public Chunk(int x, int y, int z) {
        super(x, y, z);
    }

    public Chunk(Block block) {
        this(block.x / CHUNK_SIZE, block.y / CHUNK_SIZE, block.z / CHUNK_SIZE);
    }

    public Chunk(Point3Int pos) {
        this(pos.x / CHUNK_SIZE, pos.y / CHUNK_SIZE, pos.z / CHUNK_SIZE);
    }

    // return the distance to another chunk
    public double distance(Chunk chunk){
        return Math.sqrt(Math.pow((chunk.x - x), 2) + Math.pow((chunk.y - y), 2));
    }

    public Chunk(Point3 position) {
        this(new Point3Int(position));
    }

    public Chunk plus(Chunk chunk) {
        return new Chunk(x + chunk.x, y + chunk.y, z + chunk.z);
    }

    @Override
    public String toString() {
        return "Chunk(" + x + ", " + y + ", " + z + ')';
    }
}
