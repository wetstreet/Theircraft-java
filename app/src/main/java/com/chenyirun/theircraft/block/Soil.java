package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;

public class Soil extends Block{
    public Soil(int x, int y, int z){
        super(x, y, z, BLOCK_SOIL);
    }
/*
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
    }*/

    // Flip top and bottom since bitmaps are loaded upside down.
    public static float[] TOP_FACE_TEXTURE_COORDS = {
            0, 1,
            1/16, 1,
            1/16, 15/16,
            0, 15/16,
    };

    // Flip top and bottom since bitmaps are loaded upside down.
    public static final float[] SIDE_FACE_TEXTURE_COORDS = {
            0, 1,
            1/16, 1,
            1/16, 15/16,
            0, 15/16,
    };

    // Flip top and bottom since bitmaps are loaded upside down.
    public static final float[] BOTTOM_FACE_TEXTURE_COORDS = {
            0, 1,
            1/16, 1,
            1/16, 15/16,
            0, 15/16,
    };
}
