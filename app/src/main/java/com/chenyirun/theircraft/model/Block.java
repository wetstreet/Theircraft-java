package com.chenyirun.theircraft.model;

public class Block extends Point3Int {
    public static final int BLOCK_AIR = 0;
    public static final int BLOCK_GRASS = 1;
    public int type;

    public Block(int x, int y, int z) {
        super(x, y, z);
        type = BLOCK_GRASS;
    }

    public Block(int x, int y, int z, int type) {
        super(x, y, z);
        this.type = type;
    }

    public Block(float x, float y, float z) {
        super(x, y, z);
        type = BLOCK_GRASS;
    }

    public Block(Point3 p) {
        this(p.x, p.y, p.z);
    }

    @Override
    public String toString() {
        return "Block{x=" + x + ", y=" + y + ", z=" + z + '}';
    }
}
