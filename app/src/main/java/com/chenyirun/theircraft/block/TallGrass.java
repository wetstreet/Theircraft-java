package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class TallGrass extends Block {
    public TallGrass(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_TALL_GRASS, false);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return null;
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return null;
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return null;
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return CROSS_FACE_TEXTURE_COORDS; }

    // Flip top and bottom since bitmaps are loaded upside down.
    public static final float[] CROSS_FACE_TEXTURE_COORDS = {
            0/16.0f, 13/16.0f,
            1/16.0f, 13/16.0f,
            1/16.0f, 12/16.0f,
            0/16.0f, 12/16.0f,
    };
}
