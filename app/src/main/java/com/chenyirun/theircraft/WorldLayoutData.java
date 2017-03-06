package com.chenyirun.theircraft;

/**
 * Created by chenyirun on 2017/3/6.
 */

public final class WorldLayoutData {
    // Flip top and bottom since bitmaps are loaded upside down.
    public static final float[] CUBE_UVS = new float[] {
            // Front face
            0.5f,	0.5f,
            0.5f,	1.0f,
            1.0f, 0.5f,
            0.5f,	1.0f,
            1.0f, 1.0f,
            1.0f, 0.5f,

            // Right face
            0.5f,	0.5f,
            0.5f,	1.0f,
            1.0f, 0.5f,
            0.5f,	1.0f,
            1.0f, 1.0f,
            1.0f, 0.5f,

            // Back face
            0.5f,	0.5f,
            0.5f,	1.0f,
            1.0f, 0.5f,
            0.5f,	1.0f,
            1.0f, 1.0f,
            1.0f, 0.5f,

            // Left face
            0.5f,	0.5f,
            0.5f,	1.0f,
            1.0f, 0.5f,
            0.5f,	1.0f,
            1.0f, 1.0f,
            1.0f, 0.5f,

            // Top face
            0.0f,	1.0f,
            0.0f,	0.5f,
            0.5f, 1.0f,
            0.0f,	0.5f,
            0.5f, 0.5f,
            0.5f, 1.0f,

            // Bottom face
            0.0f,	0.5f,
            0.0f,	0.0f,
            0.5f, 0.5f,
            0.0f,	0.0f,
            0.5f, 0.0f,
            0.5f, 0.5f,
    };

    public static final float[] CUBE_COORDS = new float[] {
            // Front face
            -0.5f, 0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,

            // Right face
            0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,

            // Back face
            0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,

            // Left face
            -0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,

            // Top face
            -0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,

            // Bottom face
            0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f,
    };

    // The grid lines on the floor are rendered procedurally and large polygons cause floating point
    // precision problems on some architectures. So we split the floor into 4 quadrants.
    public static final float[] FLOOR_COORDS = new float[] {
            // +X, +Z quadrant
            200, 0, 0,
            0, 0, 0,
            0, 0, 200,
            200, 0, 0,
            0, 0, 200,
            200, 0, 200,

            // -X, +Z quadrant
            0, 0, 0,
            -200, 0, 0,
            -200, 0, 200,
            0, 0, 0,
            -200, 0, 200,
            0, 0, 200,

            // +X, -Z quadrant
            200, 0, -200,
            0, 0, -200,
            0, 0, 0,
            200, 0, -200,
            0, 0, 0,
            200, 0, 0,

            // -X, -Z quadrant
            0, 0, -200,
            -200, 0, -200,
            -200, 0, 0,
            0, 0, -200,
            -200, 0, 0,
            0, 0, 0,
    };

    public static final float[] FLOOR_NORMALS = new float[] {
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
    };

    public static final float[] FLOOR_COLORS = new float[] {
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
            0.0f, 0.3398f, 0.9023f, 1.0f,
    };
}
