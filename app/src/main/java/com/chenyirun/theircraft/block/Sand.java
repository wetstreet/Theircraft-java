package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Sand extends Block {
    public Sand(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_SAND);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return getFaceCoords(1, 15);
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return getFaceCoords(1, 15);
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return getFaceCoords(1, 15);
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return null; }
}