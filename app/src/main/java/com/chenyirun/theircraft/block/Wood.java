package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Wood extends Block {
    public Wood(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_WOOD);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return getFaceCoords(4, 13);
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return getFaceCoords(4, 14);
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return getFaceCoords(4, 15);
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return null; }
}