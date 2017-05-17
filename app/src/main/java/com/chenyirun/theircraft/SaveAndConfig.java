package com.chenyirun.theircraft;

import com.chenyirun.theircraft.model.Point3Int;

public class SaveAndConfig {
    public static final String ID = "id";
    public static final String SEED = "seed";
    public static final String STEVE_X = "steve_x";
    public static final String STEVE_Y = "steve_y";
    public static final String STEVE_Z = "steve_z";

    public int id;
    public int seed;
    public Point3Int steveBlock;
    public int chunk_radius;

    public SaveAndConfig(int id, int seed, Point3Int steveBlock){
        this.id = id;
        this.seed = seed;
        this.steveBlock = steveBlock;
    }

    public SaveAndConfig(int id, int seed, Point3Int steveBlock, int chunk_radius){
        this.id = id;
        this.seed = seed;
        this.steveBlock = steveBlock;
        this.chunk_radius = chunk_radius;
    }

    public String toString(){
        return new String("id="+id+",seed="+seed+",steve="+steveBlock+"chunkRadius="+chunk_radius);
    }
}
