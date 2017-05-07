package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Cloud extends Block {
    public Cloud(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_CLOUD, false, false);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return getFaceCoords(15, 15);
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return getFaceCoords(15, 15);
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return getFaceCoords(15, 15);
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return null; }
}