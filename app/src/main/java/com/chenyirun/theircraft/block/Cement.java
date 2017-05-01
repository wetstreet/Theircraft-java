package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Cement extends Block {
    public Cement(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_CEMENT);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return FACE_TEXTURE_COORDS;
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return FACE_TEXTURE_COORDS;
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return FACE_TEXTURE_COORDS;
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return null; }

    // Flip top and bottom since bitmaps are loaded upside down.
    public static final float[] FACE_TEXTURE_COORDS = {
            5/16.0f, 1,
            6/16.0f, 1,
            6/16.0f, 15/16.0f,
            5/16.0f, 15/16.0f,
    };
}
