package com.chenyirun.theircraft;

import com.chenyirun.theircraft.model.Block;

/**
 * Created by chenyirun on 2017/3/10.
 */

public class Head {
    Eye eye;

    Head(float x, float y, float z){
        eye = new Eye(x, y, z);
    }
}
