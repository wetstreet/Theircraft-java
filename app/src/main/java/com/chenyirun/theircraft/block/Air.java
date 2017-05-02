package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Air extends Block {
    public Air(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_AIR);
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
    public float[] getCrossFaceTextureCoords(){ return null; }
}