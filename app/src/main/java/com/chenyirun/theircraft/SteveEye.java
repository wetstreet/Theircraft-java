package com.chenyirun.theircraft;

import com.chenyirun.theircraft.model.Point3;

class SteveEye {
  /** SteveEye position. */
  private Point3 position;

  SteveEye(float x, float y, float z) {
    this.position = new Point3(x, y, z);
  }

  Point3 position() {
    return position;
  }

  void setPosition(Point3 xyz) {
    position = xyz;
  }
}
