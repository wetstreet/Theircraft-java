package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Snow extends Block {
    public Snow(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_SNOW);
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
            8/16.0f, 14/16.0f,
            9/16.0f, 14/16.0f,
            9/16.0f, 13/16.0f,
            8/16.0f, 13/16.0f,
    };

    // Flip top and bottom since bitmaps are loaded upside down.
    public static final float[] SIDE_FACE_TEXTURE_COORDS = {
            8/16.0f, 15/16.0f,
            9/16.0f, 15/16.0f,
            9/16.0f, 14/16.0f,
            8/16.0f, 14/16.0f,
    };

    // Flip top and bottom since bitmaps are loaded upside down.
    public static final float[] BOTTOM_FACE_TEXTURE_COORDS = {
            8/16.0f, 1,
            9/16.0f, 1,
            9/16.0f, 15/16.0f,
            8/16.0f, 15/16.0f,
    };
}
