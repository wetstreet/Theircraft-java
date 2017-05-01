package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class LightStone extends Block {
    public LightStone(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_LIGHT_STONE);
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
            11/16.0f, 1,
            12/16.0f, 1,
            12/16.0f, 15/16.0f,
            11/16.0f, 15/16.0f,
    };
}
