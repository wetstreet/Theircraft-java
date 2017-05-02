package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Brick extends Block {
    public Brick(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_BRICK);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return getFaceCoords(3, 15);
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return getFaceCoords(3, 15);
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return getFaceCoords(3, 15);
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return null; }
}