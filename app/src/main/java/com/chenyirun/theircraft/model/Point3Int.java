package com.chenyirun.theircraft.model;

/** Immutable 3d integer coordinates of a point. */
public class Point3Int {
    public int x;
    public int y;
    public int z;

    public Point3Int set(Point3 point){
        this.x = Math.round(point.x);
        this.y = Math.round(point.y);
        this.z = Math.round(point.z);
        return this;
    }

    public Point3Int set(float x, float y, float z){
        this.x = Math.round(x);
        this.y = Math.round(y);
        this.z = Math.round(z);
        return this;
    }

    public Point3Int set(Point3Int point){
        x = point.x;
        y = point.y;
        z = point.z;
        return this;
    }

    public Point3Int() {}

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

    public Point3Int(Point3Int point) {
        this.x = point.x;
        this.y = point.y;
        this.z = point.z;
    }

    public float distance(Point3Int pos){
        return (float)Math.sqrt(Math.pow(pos.x - x, 2) + Math.pow(pos.y - y, 2) + Math.pow(pos.z - z, 2));
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

    static private Point3Int loc = new Point3Int();
    
    public Point3Int getTopLoc(){
        return loc.set(x, y + 1, z);
    }

    public Point3Int getBottomLoc(){
        return loc.set(x, y - 1, z);
    }

    public Point3Int getFrontLoc(){
        return loc.set(x, y, z + 1);
    }

    public Point3Int getBackLoc(){
        return loc.set(x, y, z - 1);
    }

    public Point3Int getRightLoc(){
        return loc.set(x + 1, y, z);
    }

    public Point3Int getLeftLoc(){
        return loc.set(x - 1, y, z);
    }
}
