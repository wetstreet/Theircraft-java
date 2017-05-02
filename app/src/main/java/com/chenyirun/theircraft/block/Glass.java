package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Glass extends Block {
    public Glass(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_GLASS);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return getFaceCoords(9, 15);
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return getFaceCoords(9, 15);
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return getFaceCoords(9, 15);
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return null; }
}