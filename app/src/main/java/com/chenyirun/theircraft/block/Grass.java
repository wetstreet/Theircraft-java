package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Grass extends Block {
    public Grass(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_GRASS);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return TOP_FACE_TEXTURE_COORDS;
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return SIDE_FACE_TEXTURE_COORDS;
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return BOTTOM_FACE_TEXTURE_COORDS;
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return null; }

    // Flip top and bottom since bitmaps are loaded upside down.
    public static final float[] TOP_FACE_TEXTURE_COORDS = {
            0, 14/16.0f,
            1/16.0f, 14/16.0f,
            1/16.0f, 13/16.0f,
            0, 13/16.0f,
    };

    // Flip top and bottom since bitmaps are loaded upside down.
    public static final float[] SIDE_FACE_TEXTURE_COORDS = {
            0, 15/16.0f,
            1/16.0f, 15/16.0f,
            1/16.0f, 14/16.0f,
            0, 14/16.0f,
    };

    // Flip top and bottom since bitmaps are loaded upside down.
    public static final float[] BOTTOM_FACE_TEXTURE_COORDS = {
            0, 1,
            1/16.0f, 1,
            1/16.0f, 15/16.0f,
            0, 15/16.0f,
    };
}
