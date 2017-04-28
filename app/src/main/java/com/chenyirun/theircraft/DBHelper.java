package com.chenyirun.theircraft;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 9;
    public static final String DATABASE_NAME = "database.db";
    public static final String TABLE_BLOCK = "block";
    public static final String TABLE_SEED = "seed";
    public static final String TABLE_STEVE = "steve";

    private static final String SQL_CREATE_BLOCK =
            "CREATE TABLE " + TABLE_BLOCK + " (" +
                    "chunkX INTEGER," +
                    "chunkY INTEGER," +
                    "chunkZ INTEGER," +
                    "blockX INTEGER," +
                    "blockY INTEGER," +
                    "blockZ INTEGER," +
                    "blockType INTEGER," +
                    "PRIMARY KEY(blockX, blockY, blockZ));";

    private static final String SQL_CREATE_SEED =
            "CREATE TABLE " + TABLE_SEED + " (" +
                    "seed INTEGER PRIMARY KEY);";

    private static final String SQL_CREATE_STEVE =
            "CREATE TABLE " + TABLE_STEVE + " (" +
                    "x INTEGER," +
                    "y INTEGER," +
                    "z INTEGER," +
                    "PRIMARY KEY(x, y, z));";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS ";

    private static DBHelper instance = null;

    public synchronized static DBHelper getInstance(Context context){
        if (instance == null) {
            instance = new DBHelper(context);
        }
        return instance;
    }

    private DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_BLOCK);
        db.execSQL(SQL_CREATE_SEED);
        db.execSQL(SQL_CREATE_STEVE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(SQL_DROP_TABLE + TABLE_BLOCK);
        db.execSQL(SQL_DROP_TABLE + TABLE_SEED);
        db.execSQL(SQL_DROP_TABLE + TABLE_STEVE);
        onCreate(db);
    }
}
