package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Plank extends Block {
    public Plank(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_PLANK);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return getFaceCoords(7, 15);
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return getFaceCoords(7, 15);
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return getFaceCoords(7, 15);
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return null; }
}