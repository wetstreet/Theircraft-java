package com.chenyirun.theircraft.model;

public class Hitbox {
    public float minX;
    public float minY;
    public float minZ;
    public float maxX;
    public float maxY;
    public float maxZ;

    public static Point3Int hitbox[] = new Point3Int[12];
    static{
        for (int i = 0; i < 12; i++){
            hitbox[i] = new Point3Int();
        }
    }

    public Hitbox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }
}
