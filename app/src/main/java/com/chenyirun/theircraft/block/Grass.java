package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Grass extends Block {
    public Grass(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_GRASS);
    }
    @Override
    public float[] getTopFaceTextureCoords(){
        return getFaceCoords(0, 13);
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return getFaceCoords(0, 14);
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return getFaceCoords(0, 15);
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return null; }
}