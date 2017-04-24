package com.chenyirun.theircraft;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Point3;

import java.util.ArrayList;
import java.util.List;

class VertexIndexTextureList {
    // Here vertices are represented as 3 consecutive Floats, thus the length of the inner list is
    // 3 times the number of vertices.
    private final List<Float> coords = new ArrayList<Float>();
    private final List<Short> indices = new ArrayList<Short>();
    private final List<Float> textureCoords = new ArrayList<Float>();

    void addFace(Block block, Point3[] vertices, short[] drawListIdxs, float[] texCoords) {
        Point3 p = block.toPoint3();
        short[] faceIndices = {
                add(coords, p.plus(vertices[0])),
                add(coords, p.plus(vertices[1])),
                add(coords, p.plus(vertices[2])),
                add(coords, p.plus(vertices[3]))
        };
        for (int indexIndex : drawListIdxs) {
            indices.add(faceIndices[indexIndex]);
        }
        for (float textureCoord : texCoords) {
            textureCoords.add(textureCoord);
        }
    }

    private static final int MAX_UNSIGNED_SHORT = 65535;

    /** Adds the coordinate triple to the list and then returns the vertex's index. */
    private static short add(List<Float> coords, Point3 vertex) {
        int vertexCount = coords.size() / 3;
        // Overflowing signed short into unsigned short is fine, will not do comparisons or arithmetic.
        if (vertexCount > MAX_UNSIGNED_SHORT) {
            throw new IllegalStateException("Too many elements");
        }
        coords.add(vertex.x);
        coords.add(vertex.y);
        coords.add(vertex.z);
        return (short) vertexCount;
    }

    float[] getVertexArray() {
        return toFloatArray(coords);
    }

    short[] getIndexArray() {
        return toShortArray(indices);
    }

    float[] getTextureCoordArray() {
        return toFloatArray(textureCoords);
    }

    private static float[] toFloatArray(List<Float> list) {
        float[] result = new float[list.size()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = list.get(i);
        }
        return result;
    }

    private static short[] toShortArray(List<Short> list) {
        short[] result = new short[list.size()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = list.get(i);
        }
        return result;
    }
    
    // OpenGL coordinates:
    //        ^ y
    //        |     x
    //        +--->
    //   z   /
    //      v
    private static final Point3 TOP_FACE[] = {
            new Point3(-0.5f, 0.5f, 0.5f),  // front left
            new Point3(0.5f, 0.5f, 0.5f),  // front right
            new Point3(0.5f, 0.5f, -0.5f),  // rear right
            new Point3(-0.5f, 0.5f, -0.5f)  // rear left
    };

    private static final Point3 FRONT_FACE[] = {
            new Point3(-0.5f, -0.5f, 0.5f),  // bottom left
            new Point3(0.5f, -0.5f, 0.5f),  // bottom right
            new Point3(0.5f, 0.5f, 0.5f),  // top right
            new Point3(-0.5f, 0.5f, 0.5f)  // top left
    };

    private static final Point3 LEFT_FACE[] = {
            new Point3(-0.5f, -0.5f, -0.5f),  // rear bottom
            new Point3(-0.5f, -0.5f, 0.5f),  // front bottom
            new Point3(-0.5f, 0.5f, 0.5f),  // front top
            new Point3(-0.5f, 0.5f, -0.5f)  // rear top
    };

    private static final Point3 RIGHT_FACE[] = {
            new Point3(0.5f, -0.5f, 0.5f),  // front bottom
            new Point3(0.5f, -0.5f, -0.5f),  // rear bottom
            new Point3(0.5f, 0.5f, -0.5f),  // rear top
            new Point3(0.5f, 0.5f, 0.5f)  // front top
    };

    private static final Point3 BACK_FACE[] = {
            new Point3(0.5f, -0.5f, -0.5f),  // bottom right
            new Point3(-0.5f, -0.5f, -0.5f),  // bottom left
            new Point3(-0.5f, 0.5f, -0.5f),  // top left
            new Point3(0.5f, 0.5f, -0.5f)  // top right
    };

    private static final Point3 BOTTOM_FACE[] = {
            new Point3(-0.5f, -0.5f, -0.5f),  // rear left
            new Point3(0.5f, -0.5f, -0.5f),  // rear right
            new Point3(0.5f, -0.5f, 0.5f),  // front right
            new Point3(-0.5f, -0.5f, 0.5f)  // front left
    };

    // Face texture vertex
    // 3 2
    // 0 1
    // First triangle
    // 3
    // 0 1
    // Second triangle
    // 3 2
    //   1
    private static final short[] FACE_DRAW_LIST_IDXS = {
            0, 1, 3,
            3, 1, 2,
    };

    public void addTopFace(Block block) {
        addFace(block, TOP_FACE, FACE_DRAW_LIST_IDXS, block.getTopFaceTextureCoords());
    }

    public void addFrontFace(Block block) {
        addFace(block, FRONT_FACE, FACE_DRAW_LIST_IDXS, block.getSideFaceTextureCoords());
    }

    public void addLeftFace(Block block) {
        addFace(block, LEFT_FACE, FACE_DRAW_LIST_IDXS, block.getSideFaceTextureCoords());
    }

    public void addRightFace(Block block) {
        addFace(block, RIGHT_FACE, FACE_DRAW_LIST_IDXS, block.getSideFaceTextureCoords());
    }

    public void addBackFace(Block block) {
        addFace(block, BACK_FACE, FACE_DRAW_LIST_IDXS, block.getSideFaceTextureCoords());
    }

    public void addBottomFace(Block block) {
        addFace(block, BOTTOM_FACE, FACE_DRAW_LIST_IDXS, block.getBottomFaceTextureCoords());
    }
}
