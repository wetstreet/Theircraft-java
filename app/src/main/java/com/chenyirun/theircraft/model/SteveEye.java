package com.chenyirun.theircraft.model;

import com.chenyirun.theircraft.model.Point3;

public class SteveEye {
    /** SteveEye position. */
    private Point3 position;

    public SteveEye(float x, float y, float z) {
        this.position = new Point3(x, y, z);
    }

    public Point3 position() {
        return position;
    }

    public void setPosition(Point3 xyz) {
        position = xyz;
    }
}