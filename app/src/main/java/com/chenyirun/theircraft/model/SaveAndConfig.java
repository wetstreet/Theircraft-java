package com.chenyirun.theircraft.model;

public class SaveAndConfig {
    public static final String ID = "id";
    public static final String SEED = "seed";
    public static final String STEVE_X = "steve_x";
    public static final String STEVE_Y = "steve_y";
    public static final String STEVE_Z = "steve_z";

    public static final String KEY_CHUNK_RADIUS = "chunk_radius";
    public static final String KEY_SIGHT_VECTOR = "sight_vector";
    public static final String KEY_AUTO_JUMP = "auto_jump";

    public static int chunk_radius;
    public static boolean sight_vector;
    public static boolean auto_jump;

    public int id;
    public String name;
    public int seed;
    public String date;
    public Point3Int steveBlock;

    public SaveAndConfig(int id, int seed, Point3Int steveBlock){
        this.id = id;
        this.seed = seed;
        this.steveBlock = steveBlock;
    }

    public SaveAndConfig(int id, String name, int seed, String date, Point3Int steveBlock){
        this.id = id;
        this.name = name;
        this.seed = seed;
        this.date = date;
        this.steveBlock = steveBlock;
    }

    public String toString(){
        return new String("id="+id+",seed="+seed+",steve="+steveBlock+"chunkRadius="+chunk_radius);
    }
}
