package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Stone extends Block {
    public Stone(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_STONE);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return getFaceCoords(2, 15);
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return getFaceCoords(2, 15);
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return getFaceCoords(2, 15);
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return null; }
}