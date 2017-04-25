package com.chenyirun.theircraft.model;

public abstract class Block extends Point3Int {
    public static final int BLOCK_AIR = 0;
    public static final int BLOCK_GRASS = 1;
    public static final int BLOCK_SOIL = 2;
    private int type;

    public Block(int x, int y, int z, int type) {
        super(x, y, z);
        this.type = type;
    }

    public int getType(){
        return type;
    }

    public Point3Int getLocation(){
        return new Point3Int(x, y, z);
    }

    @Override
    public String toString() {
        return "Block{x=" + x + ", y=" + y + ", z=" + z + '}';
    }

    public abstract float[] getTopFaceTextureCoords();

    public abstract float[] getSideFaceTextureCoords();

    public abstract float[] getBottomFaceTextureCoords();
}