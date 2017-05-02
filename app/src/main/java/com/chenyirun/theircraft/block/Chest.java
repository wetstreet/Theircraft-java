package com.chenyirun.theircraft.block;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3Int;

public class Chest extends Block {
    public Chest(Point3Int pos){
        super(pos.x, pos.y, pos.z, BLOCK_CHEST);
    }

    @Override
    public float[] getTopFaceTextureCoords(){
        return getFaceCoords(13, 15);
    }

    @Override
    public float[] getSideFaceTextureCoords(){
        return getFaceCoords(13, 15);
    }

    @Override
    public float[] getBottomFaceTextureCoords(){
        return getFaceCoords(13, 15);
    }

    @Override
    public float[] getCrossFaceTextureCoords(){ return null; }
}