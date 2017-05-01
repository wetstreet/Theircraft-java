package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Plank extends Block {
    public Plank(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_PLANK);
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
            7/16.0f, 1,
            8/16.0f, 1,
            8/16.0f, 15/16.0f,
            7/16.0f, 15/16.0f,
    };
}
