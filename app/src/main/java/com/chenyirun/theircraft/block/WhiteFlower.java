package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class WhiteFlower extends Block {
    public WhiteFlower(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_WHITE_FLOWER, false, true);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return null;
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return null;
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return null;
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return getFaceCoords(5, 12); }
}