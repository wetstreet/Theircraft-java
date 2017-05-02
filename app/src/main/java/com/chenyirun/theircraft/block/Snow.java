package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Snow extends Block {
    public Snow(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_SNOW);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return getFaceCoords(8, 13);
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return getFaceCoords(8, 14);
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return getFaceCoords(8, 15);
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return null; }
}