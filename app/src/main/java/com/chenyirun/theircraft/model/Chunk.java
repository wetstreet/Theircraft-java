package com.chenyirun.theircraft.model;

import com.chenyirun.theircraft.Grass;

public class Chunk extends Point3Int {
  /** Blocks per side of a chunk. */
  public static final int CHUNK_SIZE = 4;

  public Chunk(int x, int y, int z) {
    super(x, y, z);
  }

  public Chunk plus(Chunk chunk) {
    return new Chunk(x + chunk.x, y + chunk.y, z + chunk.z);
  }

  @Override
  public String toString() {
    return "Chunk(" + x + ", " + y + ", " + z + ')';
  }
}
