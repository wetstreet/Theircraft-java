package com.chenyirun.theircraft;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.chenyirun.theircraft.block.Grass;
import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Chunk;
import com.chenyirun.theircraft.model.Point3Int;
import com.chenyirun.theircraft.perlin.Generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DBService {
    private static final String TAG = "DBService";
    private Context context;
    private Point3Int steveLocation;
    public static final boolean DBEnabled = true;

    public DBService(Context context){
        this.context = context;
    }

    public int getSeed(){
        int seed;
        if (DBEnabled){
            Log.i(TAG, "getSeed: database enabled");
            SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
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
        SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("seed", seed);
        long newRowId = db.insert(DBHelper.TABLE_SEED, null, values);
        Log.i(TAG, "insert new seed! seed:"+seed+" row id:"+ newRowId);
    }

    /** Given (x,z) coordinates, finds and returns the highest y so that (x,y,z) is a solid block. */
    private float highestSolidY(float x, float z, Set<Block> blocks) {
        float maxY = Generator.minElevation();
        for (Block block : blocks) {
            if (block.x != x || block.z != z) {
                continue;
            }
            if (block.y > maxY) {
                maxY = block.y;
            }
        }
        return maxY;
    }

    public Point3Int getSteve(){
        Point3Int pos = null;

        if (!DBEnabled){
            Log.i(TAG, "getSteve: database disabled");
            return null;
        }

        Log.i(TAG, "getSteve: database enabled");
        SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
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

    /*
    public Block getSteve(){
        if (DBEnabled){
            Log.i(TAG, "getSteve: database enabled");
            SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
            String[] projection = {"x" ,"y", "z"};
            Cursor cursor = db.query(DBHelper.TABLE_STEVE, projection, null, null, null, null, null);
            if (cursor.moveToNext()){
                int x = cursor.getInt(cursor.getColumnIndexOrThrow("x"));
                int y = cursor.getInt(cursor.getColumnIndexOrThrow("y"));
                int z = cursor.getInt(cursor.getColumnIndexOrThrow("z"));
                block = new Block(x,y,z);
                stevePosition = block;
                Log.i(TAG, "getSteve: found steve at "+block);
            } else {
                Log.i(TAG, "getSteve: no steve in database");
                block = new Block(0, highestSolidY(0, 0, blocks), 0);
                stevePosition = block;
                insertSteve(block);
            }
            cursor.close();
        } else {
            Log.i(TAG, "getSteve: database disabled");
            block = new Block(0, highestSolidY(0, 0, blocks), 0);
        }
        return block;
    }
    */

    boolean isSteveExisted(){
        boolean result;
        SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
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

    Point3Int steveLocation(){
        return steveLocation;
    }

    void updateSteve(Point3Int pos){
        if (isSteveExisted()){
            SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
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

    void insertSteve(Block block){
        if (isSteveExisted()){
            Log.i(TAG, "insertSteve: steve exists");
            return;
        }
        SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("x", block.x);
        values.put("y", block.y);
        values.put("z", block.z);
        long newRowId = db.insert(DBHelper.TABLE_STEVE, null, values);
        steveLocation = block;
        Log.i(TAG, "insert new steve! position"+block+" row id:"+ newRowId);
    }

    int chunked(int coordinate){
        return (int)Math.floor(coordinate / Chunk.CHUNK_SIZE);
    }

    private boolean isBlockExisted(Block block){
        boolean result;
        SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
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
        if (isBlockExisted(block)){
            Log.i(TAG, "insertBlock: block exists!");
            return;
        }
        SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("chunkX", chunked(block.x));
        values.put("chunkY", chunked(block.y));
        values.put("chunkZ", chunked(block.z));
        values.put("blockX", block.x);
        values.put("blockY", block.y);
        values.put("blockZ", block.z);
        values.put("blockType", block.getType());
        long newRowId = db.insert(DBHelper.TABLE_BLOCK, null, values);
        Log.i(TAG, "insert a new block at ("+block.x+","+block.y+","+block.z+")"+" type="+block.getType()+" row id:"+ newRowId);
    }

    void deleteBlock(Block block){
        SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
        String selection = "blockX = ? and blockY = ? and blockZ = ?";
        String[] selectionArgs = { Integer.toString(block.x), Integer.toString(block.y), Integer.toString(block.z) };
        db.delete(DBHelper.TABLE_BLOCK, selection, selectionArgs);
    }

    List<Block> getBlockChangesInChunk(Chunk chunk){
        SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
        List<Block> blocks = new ArrayList<>();
        String[] projection = { "chunkX", "chunkY", "chunkZ", "blockX", "blockY", "blockZ", "blockType" };
        String selection = "chunkX = ? and chunkY = ? and chunkZ = ?";
        String[] selectionArgs = { Integer.toString(chunk.x), Integer.toString(chunk.y), Integer.toString(chunk.z) };
        Cursor cursor = db.query(DBHelper.TABLE_BLOCK, projection, selection, selectionArgs, null, null, null);
        while (cursor.moveToNext()) {
            int x = cursor.getInt(cursor.getColumnIndexOrThrow("blockX"));
            int y = cursor.getInt(cursor.getColumnIndexOrThrow("blockY"));
            int z = cursor.getInt(cursor.getColumnIndexOrThrow("blockZ"));
            int type = cursor.getInt(cursor.getColumnIndexOrThrow("blockType"));
            addBlock(blocks, new Point3Int(x, y, z), type);
            Log.i(TAG, "getBlockChangesInChunk: block change found at Chunk"+chunk+" Block("+x+","+y+","+z+") type="+type);
        }
        cursor.close();
        return blocks;
    }

    private void addBlock(List<Block> blocks, Point3Int pos, int type){
        switch (type){
            case Block.BLOCK_AIR:
                break;
            case Block.BLOCK_GRASS:
                blocks.add(new Grass(pos));
                break;
        }
    }

    void onDestroy(){
        DBHelper.getInstance(context).close();
    }
}
