package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Cobble extends Block {
    public Cobble(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_COBBLE);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return getFaceCoords(10, 15);
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return getFaceCoords(10, 15);
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return getFaceCoords(10, 15);
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return null; }
}