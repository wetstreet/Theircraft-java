package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Stone extends Block {
    public Stone(int x, int y, int z){
        super(x, y, z, BLOCK_STONE);
    }

    public Stone(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_STONE);
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

    // Flip top and bottom since bitmaps are loaded upside down.
    public static final float[] TOP_FACE_TEXTURE_COORDS = {
            5/16.0f, 1,
            6/16.0f, 1,
            6/16.0f, 15/16.0f,
            5/16.0f, 15/16.0f,
    };

    // Flip top and bottom since bitmaps are loaded upside down.
    public static final float[] SIDE_FACE_TEXTURE_COORDS = {
            5/16.0f, 1,
            6/16.0f, 1,
            6/16.0f, 15/16.0f,
            5/16.0f, 15/16.0f,
    };

    // Flip top and bottom since bitmaps are loaded upside down.
    public static final float[] BOTTOM_FACE_TEXTURE_COORDS = {
            5/16.0f, 1,
            6/16.0f, 1,
            6/16.0f, 15/16.0f,
            5/16.0f, 15/16.0f,
    };
}
