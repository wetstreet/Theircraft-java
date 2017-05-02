package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class LightStone extends Block {
    public LightStone(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_LIGHT_STONE);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return getFaceCoords(11, 15);
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return getFaceCoords(11, 15);
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return getFaceCoords(11, 15);
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return null; }
}