package com.chenyirun.theircraft.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 13;
    public static final String DATABASE_NAME = "database.db";
    public static final String TABLE_SAVE = "save";
    public static final String SAVE_PREFIX = "world_";

    private static final String SQL_CREATE_SAVE =
            "CREATE TABLE " + TABLE_SAVE + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "seed INTEGER," +
                    "date TEXT," +
                    "x INTEGER," +
                    "y INTEGER," +
                    "z INTEGER);";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS ";

    private static DBHelper instance = null;

    private static Context context = null;

    public static void setContext(Context c){
        context = c;
    }

    public synchronized static DBHelper getInstance(){
        if (instance == null) {
            instance = new DBHelper(context);
        }
        return instance;
    }

    private DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_SAVE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(SQL_DROP_TABLE + TABLE_SAVE);
        onCreate(db);
    }
}
