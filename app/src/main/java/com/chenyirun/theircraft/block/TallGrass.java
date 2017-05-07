package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class TallGrass extends Block {
    public TallGrass(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_TALL_GRASS, false, true);
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
    public float[] getCrossFaceTextureCoords(){ return getFaceCoords(0, 12); }
}