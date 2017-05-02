package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Cement extends Block {
    public Cement(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_CEMENT);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return getFaceCoords(5, 15);
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return getFaceCoords(5, 15);
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return getFaceCoords(5, 15);
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return null; }
}