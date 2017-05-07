package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Leaves extends Block {
    public Leaves(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_LEAVES, true, true);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return getFaceCoords(14, 15);
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return getFaceCoords(14, 15);
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return getFaceCoords(14, 15);
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return null; }
}