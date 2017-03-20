package com.chenyirun.theircraft.model;

/** Immutable 3d coordinates of a point. */
public class Point3 {
  public float x;
  public float y;
  public float z;

  public Point3(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Point3 plus(float x, float y, float z) {
    return new Point3(this.x + x, this.y + y, this.z + z);
  }

  public Point3 plus(Point3 p) {
    return new Point3(x + p.x, y + p.y, z + p.z);
  }

  public Point3 times(float mult) {
    return new Point3(mult * x, mult * y, mult * z);
  }

  public Point3 plusY(float dy) {
    return new Point3(x, y + dy, z);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Point3 point3 = (Point3) o;
    return (Float.compare(x, point3.x) == 0) &&
        (Float.compare(y, point3.y) == 0) &&
        (Float.compare(z, point3.z) == 0);
  }

  @Override
  public int hashCode() {
    return 31 * 31 * (x != +0.0f ? Float.floatToIntBits(x) : 0) +
        31 * (y != +0.0f ? Float.floatToIntBits(y) : 0) +
        (z != +0.0f ? Float.floatToIntBits(z) : 0);
  }

  @Override
  public String toString() {
    return "Point3{x=" + x + ", y=" + y + ", z=" + z + '}';
  }
}
