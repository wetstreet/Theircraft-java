package com.chenyirun.theircraft.model;

public abstract class Block extends Point3Int {
    public static final int BLOCK_AIR = 0;
    public static final int BLOCK_GRASS = 1;
    public static final int BLOCK_SAND = 2;
    public static final int BLOCK_STONE = 3;
    public static final int BLOCK_BRICK = 4;
    public static final int BLOCK_WOOD = 5;
    public static final int BLOCK_CEMENT = 6;
    public static final int BLOCK_DIRT = 7;
    public static final int BLOCK_PLANK = 8;
    public static final int BLOCK_SNOW = 9;
    public static final int BLOCK_GLASS = 10;
    public static final int BLOCK_COBBLE = 11;
    public static final int BLOCK_LIGHT_STONE = 12;
    public static final int BLOCK_DARK_STONE = 13;
    public static final int BLOCK_CHEST = 14;
    public static final int BLOCK_LEAVES = 15;
    public static final int BLOCK_CLOUD = 16;
    public static final int BLOCK_TALL_GRASS = 17;
    public static final int BLOCK_YELLOW_FLOWER = 18;
    public static final int BLOCK_RED_FLOWER = 19;
    public static final int BLOCK_PURPLE_FLOWER = 20;
    public static final int BLOCK_SUN_FLOWER = 21;
    public static final int BLOCK_WHITE_FLOWER = 22;
    public static final int BLOCK_BLUE_FLOWER = 23;

    public static final int items[] = {
            BLOCK_GRASS,
            BLOCK_SAND,
            BLOCK_STONE,
            BLOCK_BRICK,
            BLOCK_WOOD,
            BLOCK_CEMENT,
            BLOCK_DIRT,
            BLOCK_PLANK,
            BLOCK_SNOW,
            BLOCK_GLASS,
            BLOCK_COBBLE,
            BLOCK_LIGHT_STONE,
            BLOCK_DARK_STONE,
            BLOCK_CHEST,
            BLOCK_LEAVES,
            BLOCK_TALL_GRASS,
            BLOCK_YELLOW_FLOWER,
            BLOCK_RED_FLOWER,
            BLOCK_PURPLE_FLOWER,
            BLOCK_SUN_FLOWER,
            BLOCK_WHITE_FLOWER,
            BLOCK_BLUE_FLOWER
    };

    private int type;
    private boolean collidable;
    private boolean transparent;

    public Block(int x, int y, int z, int type, boolean collidable, boolean transparent) {
        super(x, y, z);
        this.type = type;
        this.collidable = collidable;
        this.transparent = transparent;
    }

    public Block(int x, int y, int z, int type) {
        super(x, y, z);
        this.type = type;
        collidable = true;
        transparent = false;
    }

    public int getType(){
        return type;
    }

    public boolean isCollidable(){
        return collidable;
    }

    public boolean isTransparent(){
        return transparent;
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

    public abstract float[] getCrossFaceTextureCoords();

    // Flip top and bottom since bitmaps are loaded upside down.(top-left is 0,0)
    public static float[] getFaceCoords(int x, int y){
        float[] coords = {
                x / 16.0f , (y + 1) / 16.0f,
                (x + 1) / 16.0f , (y + 1) / 16.0f,
                (x + 1) / 16.0f , y / 16.0f,
                x / 16.0f , y / 16.0f
        };
        return coords;
    }
}