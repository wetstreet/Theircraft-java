package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Dirt extends Block {
    public Dirt(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_DIRT);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return getFaceCoords(6, 15);
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return getFaceCoords(6, 15);
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return getFaceCoords(6, 15);
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return null; }
}