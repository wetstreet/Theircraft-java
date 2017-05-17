package com.chenyirun.theircraft;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.chenyirun.theircraft.block.Air;
import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Chunk;
import com.chenyirun.theircraft.model.Point3Int;

import java.util.ArrayList;
import java.util.List;

public class DBService {
    private static final String TAG = "DBService";
    private Point3Int steveLocation;
    public static final boolean DBEnabled = true;

    private static DBService instance = null;

    public static void setContext(Context context){
        DBHelper.setContext(context);
    }

    public synchronized static DBService getInstance(){
        if (instance == null) {
            instance = new DBService();
        }
        return instance;
    }

    public Cursor pageCursorQuery(){
        SQLiteDatabase db = DBHelper.getInstance().getReadableDatabase();
        String[] projection = { "id as _id", "name", "seed", "date" };
        Cursor cursor = db.query(DBHelper.TABLE_SAVE, projection, null, null, null, null, null, null);
        return cursor;
    }

    public void createBlockTable(int id){
        String sql =
                "CREATE TABLE world_" + id + " (" +
                        "chunkX INTEGER," +
                        "chunkY INTEGER," +
                        "chunkZ INTEGER," +
                        "blockX INTEGER," +
                        "blockY INTEGER," +
                        "blockZ INTEGER," +
                        "blockType INTEGER," +
                        "PRIMARY KEY(blockX, blockY, blockZ));";
        SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();
        db.execSQL(sql);
    }

    public void dropBlockTable(int id){
        String sql = "DROP TABLE IF EXISTS world_" + id;
        SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();
        db.execSQL(sql);
    }

    public void addSave(String name, int seed, String date){
        SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("seed", seed);
        values.put("date", date);
        int id = (int)db.insert(DBHelper.TABLE_SAVE, null, values);
        Log.i(TAG, "addSave: rowid="+id);
        createBlockTable(id);
    }

    public void removeSave(int id) {
        SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();
        String selection = "id = ?";
        String[] selectionArgs = { Integer.toString(id) };
        db.delete(DBHelper.TABLE_SAVE, selection, selectionArgs);
        dropBlockTable(id);
    }

    public SaveAndConfig getSave(int id){
        SQLiteDatabase db = DBHelper.getInstance().getReadableDatabase();
        String[] projection = { "seed", "x", "y", "z" };
        String selection = "id = ?";
        String[] selectionArgs = { Integer.toString(id) };
        Cursor cursor = db.query(DBHelper.TABLE_SAVE, projection, selection, selectionArgs, null, null, null);
        cursor.moveToNext();
        int seed = cursor.getInt(cursor.getColumnIndexOrThrow("seed"));
        int x = cursor.getInt(cursor.getColumnIndexOrThrow("x"));
        int y = cursor.getInt(cursor.getColumnIndexOrThrow("y"));
        int z = cursor.getInt(cursor.getColumnIndexOrThrow("z"));
        SaveAndConfig save = new SaveAndConfig(id, seed, new Point3Int(x, y ,z));
        cursor.close();
        return save;
    }

    void updateSteve(int id, Point3Int pos){
        SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();
        String selection = "id = ?";
        String[] selectionArgs = { Integer.toString(id) };
        ContentValues values = new ContentValues();
        values.put("x", pos.x);
        values.put("y", pos.y);
        values.put("z", pos.z);
        db.update(DBHelper.TABLE_SAVE, values, selection, selectionArgs);
        steveLocation = pos;
        Log.i(TAG, "update steve position at "+pos);
    }

    private boolean blockExists(int id, Block block){
        boolean result;
        SQLiteDatabase db = DBHelper.getInstance().getReadableDatabase();
        String[] projection = { "chunkX", "chunkY", "chunkZ", "blockX", "blockY", "blockZ", "blockType" };
        String selection = "blockX = ? and blockY = ? and blockZ = ?";
        String[] selectionArgs = { Integer.toString(block.x), Integer.toString(block.y), Integer.toString(block.z) };
        Cursor cursor = db.query(DBHelper.SAVE_PREFIX + id, projection, selection, selectionArgs, null, null, null);
        if (cursor.moveToNext()){
            result = true;
        } else {
            result = false;
        }
        cursor.close();
        return result;
    }

    void insertBlock(int id, Block block){
        if (blockExists(id, block)){
            Log.i(TAG, "insertBlock: block exists!");
            return;
        }
        SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        Chunk chunk = new Chunk(block);
        values.put("chunkX", chunk.x);
        values.put("chunkY", chunk.y);
        values.put("chunkZ", chunk.z);
        values.put("blockX", block.x);
        values.put("blockY", block.y);
        values.put("blockZ", block.z);
        values.put("blockType", block.getType());
        long newRowId = db.insert(DBHelper.SAVE_PREFIX + id, null, values);
        Log.i(TAG, "insert a new block at ("+block.x+","+block.y+","+block.z+")"+" type="+block.getType()+" row id:"+ newRowId);
    }

    void deleteBlock(int id, Block block){
        if (blockExists(block)){
            SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();
            String selection = "blockX = ? and blockY = ? and blockZ = ?";
            String[] selectionArgs = { Integer.toString(block.x), Integer.toString(block.y), Integer.toString(block.z) };
            db.delete(DBHelper.SAVE_PREFIX + id, selection, selectionArgs);
        } else {
            Air air = new Air(block.getLocation());
            insertBlock(air);
        }
    }

    List<Block> getBlockChangesInChunk(int id, Chunk chunk){
        SQLiteDatabase db = DBHelper.getInstance().getReadableDatabase();
        List<Block> result = new ArrayList<>();
        String[] projection = { "chunkX", "chunkY", "chunkZ", "blockX", "blockY", "blockZ", "blockType" };
        String selection = "chunkX = ? and chunkY = ? and chunkZ = ?";
        String[] selectionArgs = { Integer.toString(chunk.x), Integer.toString(chunk.y), Integer.toString(chunk.z) };
        Cursor cursor = db.query(DBHelper.SAVE_PREFIX + id, projection, selection, selectionArgs, null, null, null);
        while (cursor.moveToNext()) {
            int x = cursor.getInt(cursor.getColumnIndexOrThrow("blockX"));
            int y = cursor.getInt(cursor.getColumnIndexOrThrow("blockY"));
            int z = cursor.getInt(cursor.getColumnIndexOrThrow("blockZ"));
            int type = cursor.getInt(cursor.getColumnIndexOrThrow("blockType"));
            Block block = MapManager.createBlock(new Point3Int(x, y, z), type);
            if (block != null){
                result.add(block);
            }
        }
        cursor.close();
        return result;
    }
/*
    public int getSeed(){
        int seed;
        if (DBEnabled){
            Log.i(TAG, "getSeed: database enabled");
            SQLiteDatabase db = DBHelper.getInstance().getReadableDatabase();
            String[] projection = {"seed"};
            Cursor cursor = db.query(DBHelper.TABLE_SEED, projection, null, null, null, null, null);
            if (cursor.moveToNext()){
                seed = cursor.getInt(cursor.getColumnIndexOrThrow("seed"));
                Log.i(TAG, "getSeed: found seed:"+seed);
            } else {
                Log.i(TAG, "getSeed: no seed in database!");
                seed = -1451589742;
                //seed = new Random().nextInt();
                insertSeed(seed);
            }
            cursor.close();
        } else {
            Log.i(TAG, "getSeed: database disabled");
            seed = -1451589742;
            //seed = new Random().nextInt();
        }
        return seed;
    }

    void insertSeed(int seed){
        SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("seed", seed);
        long newRowId = db.insert(DBHelper.TABLE_SEED, null, values);
        Log.i(TAG, "insert new seed! seed:"+seed+" row id:"+ newRowId);
    }

    public Point3Int getSteve(){
        Point3Int pos = null;

        if (!DBEnabled){
            Log.i(TAG, "getSteve: database disabled");
            return null;
        }

        Log.i(TAG, "getSteve: database enabled");
        SQLiteDatabase db = DBHelper.getInstance().getReadableDatabase();
        String[] projection = {"x" ,"y", "z"};
        Cursor cursor = db.query(DBHelper.TABLE_STEVE, projection, null, null, null, null, null);
        if (cursor.moveToNext()){
            int x = cursor.getInt(cursor.getColumnIndexOrThrow("x"));
            int y = cursor.getInt(cursor.getColumnIndexOrThrow("y"));
            int z = cursor.getInt(cursor.getColumnIndexOrThrow("z"));
            pos = new Point3Int(x,y,z);
            steveLocation = pos;
            Log.i(TAG, "getSteve: found steve at " + pos);
        } else {
            Log.i(TAG, "getSteve: no steve in database");
        }
        cursor.close();
        return pos;
    }

    boolean steveExists(){
        boolean result;
        SQLiteDatabase db = DBHelper.getInstance().getReadableDatabase();
        String[] projection = {"x" ,"y", "z"};
        Cursor cursor = db.query(DBHelper.TABLE_STEVE, projection, null, null, null, null, null);
        if (cursor.moveToNext()){
            result = true;
        } else {
            result = false;
        }
        cursor.close();
        return result;
    }

    void updateSteve(Point3Int pos){
        if (steveExists()){
            SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("x", pos.x);
            values.put("y", pos.y);
            values.put("z", pos.z);
            db.update(DBHelper.TABLE_STEVE, values, null, null);
            steveLocation = pos;
            Log.i(TAG, "update steve position at "+pos);
        } else {
            Log.i(TAG, "updateSteve: steve does not exist!");
        }
    }

    void insertSteve(Point3Int blockLocation){
        if (steveExists()){
            Log.i(TAG, "insertSteve: steve exists");
            return;
        }
        SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("x", blockLocation.x);
        values.put("y", blockLocation.y);
        values.put("z", blockLocation.z);
        long newRowId = db.insert(DBHelper.TABLE_STEVE, null, values);
        steveLocation = blockLocation;
        Log.i(TAG, "insert new steve! position"+blockLocation+" row id:"+ newRowId);
    }
*/
    Point3Int steveLocation(){
        return steveLocation;
    }

    private boolean blockExists(Block block){
        boolean result;
        SQLiteDatabase db = DBHelper.getInstance().getReadableDatabase();
        String[] projection = { "chunkX", "chunkY", "chunkZ", "blockX", "blockY", "blockZ", "blockType" };
        String selection = "blockX = ? and blockY = ? and blockZ = ?";
        String[] selectionArgs = { Integer.toString(block.x), Integer.toString(block.y), Integer.toString(block.z) };
        Cursor cursor = db.query(DBHelper.TABLE_BLOCK, projection, selection, selectionArgs, null, null, null);
        if (cursor.moveToNext()){
            result = true;
        } else {
            result = false;
        }
        cursor.close();
        return result;
    }

    void insertBlock(Block block){
        if (blockExists(block)){
            Log.i(TAG, "insertBlock: block exists!");
            return;
        }
        SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        Chunk chunk = new Chunk(block);
        values.put("chunkX", chunk.x);
        values.put("chunkY", chunk.y);
        values.put("chunkZ", chunk.z);
        values.put("blockX", block.x);
        values.put("blockY", block.y);
        values.put("blockZ", block.z);
        values.put("blockType", block.getType());
        long newRowId = db.insert(DBHelper.TABLE_BLOCK, null, values);
        Log.i(TAG, "insert a new block at ("+block.x+","+block.y+","+block.z+")"+" type="+block.getType()+" row id:"+ newRowId);
    }

    void deleteBlock(Block block){
        if (blockExists(block)){
            SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();
            String selection = "blockX = ? and blockY = ? and blockZ = ?";
            String[] selectionArgs = { Integer.toString(block.x), Integer.toString(block.y), Integer.toString(block.z) };
            db.delete(DBHelper.TABLE_BLOCK, selection, selectionArgs);
        } else {
            Air air = new Air(block.getLocation());
            insertBlock(air);
        }
    }

    List<Block> getBlockChangesInChunk(Chunk chunk){
        SQLiteDatabase db = DBHelper.getInstance().getReadableDatabase();
        List<Block> result = new ArrayList<>();
        String[] projection = { "chunkX", "chunkY", "chunkZ", "blockX", "blockY", "blockZ", "blockType" };
        String selection = "chunkX = ? and chunkY = ? and chunkZ = ?";
        String[] selectionArgs = { Integer.toString(chunk.x), Integer.toString(chunk.y), Integer.toString(chunk.z) };
        Cursor cursor = db.query(DBHelper.TABLE_BLOCK, projection, selection, selectionArgs, null, null, null);
        while (cursor.moveToNext()) {
            int x = cursor.getInt(cursor.getColumnIndexOrThrow("blockX"));
            int y = cursor.getInt(cursor.getColumnIndexOrThrow("blockY"));
            int z = cursor.getInt(cursor.getColumnIndexOrThrow("blockZ"));
            int type = cursor.getInt(cursor.getColumnIndexOrThrow("blockType"));
            Block block = MapManager.createBlock(new Point3Int(x, y, z), type);
            if (block != null){
                result.add(block);
            }
        }
        cursor.close();
        return result;
    }

    void onDestroy(){
        DBHelper.getInstance().close();
    }
}
