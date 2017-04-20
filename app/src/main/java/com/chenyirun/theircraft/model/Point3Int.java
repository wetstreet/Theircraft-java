package com.chenyirun.theircraft.model;

/** Immutable 3d integer coordinates of a point. */
public class Point3Int {
    public int x;
    public int y;
    public int z;

    public Point3Int(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3Int(float x, float y, float z) {
        this.x = Math.round(x);
        this.y = Math.round(y);
        this.z = Math.round(z);
    }

    public Point3Int(Point3 point) {
        this.x = Math.round(point.x);
        this.y = Math.round(point.y);
        this.z = Math.round(point.z);
    }

    public void add(Point3 point){
        this.x += Math.round(point.x);
        this.y += Math.round(point.y);
        this.z += Math.round(point.z);
    }

    public Point3 toPoint3() {
        return new Point3(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point3Int point3Int = (Point3Int) o;
        return (x == point3Int.x) && (y == point3Int.y) && (z == point3Int.z);
    }

    @Override
    public int hashCode() {
        return 31 * 31 * x + 31 * y + z;
    }

    @Override
    public String toString() {
        return "Point3Int{x=" + x + ", y=" + y + ", z=" + z + '}';
    }
}
