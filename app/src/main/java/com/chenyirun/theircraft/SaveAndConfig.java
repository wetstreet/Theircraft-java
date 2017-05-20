package com.chenyirun.theircraft;

import com.chenyirun.theircraft.model.Point3Int;

public class SaveAndConfig {
    public static final String ID = "id";
    public static final String SEED = "seed";
    public static final String STEVE_X = "steve_x";
    public static final String STEVE_Y = "steve_y";
    public static final String STEVE_Z = "steve_z";

    public static int chunk_radius = 3;
    public static boolean sight_vector = false;

    public int id;
    public int seed;
    public Point3Int steveBlock;

    public SaveAndConfig(int id, int seed, Point3Int steveBlock){
        this.id = id;
        this.seed = seed;
        this.steveBlock = steveBlock;
    }

    public String toString(){
        return new String("id="+id+",seed="+seed+",steve="+steveBlock+"chunkRadius="+chunk_radius);
    }
}
