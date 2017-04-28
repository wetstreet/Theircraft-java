package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Brick extends Block {
    public Brick(int x, int y, int z){
        super(x, y, z, BLOCK_BRICK);
    }

    public Brick(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_BRICK);
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
            3/16.0f, 1,
            4/16.0f, 1,
            4/16.0f, 15/16.0f,
            3/16.0f, 15/16.0f,
    };

    // Flip top and bottom since bitmaps are loaded upside down.
    public static final float[] SIDE_FACE_TEXTURE_COORDS = {
            3/16.0f, 1,
            4/16.0f, 1,
            4/16.0f, 15/16.0f,
            3/16.0f, 15/16.0f,
    };

    // Flip top and bottom since bitmaps are loaded upside down.
    public static final float[] BOTTOM_FACE_TEXTURE_COORDS = {
            3/16.0f, 1,
            4/16.0f, 1,
            4/16.0f, 15/16.0f,
            3/16.0f, 15/16.0f,
    };
}
