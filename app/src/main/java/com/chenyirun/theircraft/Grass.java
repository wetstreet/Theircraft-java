package com.chenyirun.theircraft;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.chenyirun.theircraft.model.Block;
import com.chenyirun.theircraft.model.Chunk;
import com.chenyirun.theircraft.model.Point3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenyirun on 2017/2/28.
 */

public class Grass {
    private int grassProgram;

    private VertexIndexTextureList vitList = new VertexIndexTextureList();
    private static final Map<Chunk, Buffers> chunkToBuffers = new HashMap<>();

    public final float[] modelGrass = new float[16];
    private final float[] modelView = new float[16];
    private final float[] modelViewProjection = new float[16];
    
    private int textureHandle;
    private int textureData;
    private int grassPositionParam;
    private int grassUVParam;
    private int grassModelViewProjectionParam;

    void load(Chunk chunk, List<Block> blocks, Set<Block> allBlocks) {
        Buffers buffers = createBuffers(blocks, allBlocks);
        synchronized(chunkToBuffers) {
            chunkToBuffers.put(chunk, buffers);
        }
    }

    void unload(Chunk chunk) {
        synchronized(chunkToBuffers) {
            chunkToBuffers.remove(chunk);
        }
    }

     private Buffers createBuffers(List<Block> blocks, Set<Block> allBlocks) {
        VertexIndexTextureList vitList = new VertexIndexTextureList();
        for (Block block : blocks) {
            // Only add faces that are not between two blocks and thus invisible.
            if (!allBlocks.contains(new Block(block.x, block.y + 1, block.z))) {
                addTopFace(vitList, block);
            }
            if (!allBlocks.contains(new Block(block.x, block.y, block.z + 1))) {
                addFrontFace(vitList, block);
            }
            if (!allBlocks.contains(new Block(block.x - 1, block.y, block.z))) {
                addLeftFace(vitList, block);
            }
            if (!allBlocks.contains(new Block(block.x + 1, block.y, block.z))) {
                addRightFace(vitList, block);
            }
            if (!allBlocks.contains(new Block(block.x, block.y, block.z - 1))) {
                addBackFace(vitList, block);
            }
            if (!allBlocks.contains(new Block(block.x, block.y - 1, block.z))) {
                addBottomFace(vitList, block);
            }
        }

        return new Buffers(
                GlHelper.createFloatBuffer(vitList.getVertexArray()),
                GlHelper.createShortBuffer(vitList.getIndexArray()),
                GlHelper.createFloatBuffer(vitList.getTextureCoordArray()));
    }

    private static final String VertexShaderCode =
            "uniform mat4 u_MVP;\n" +
                    "attribute vec4 a_Position;\n" +
                    "attribute vec2 a_textureCoord;\n" +
                    "varying vec2 v_textureCoord;\n" +
                    "\n" +
                    "void main() {\n" +
                    "   gl_Position = u_MVP * a_Position;\n" +
                    "   v_textureCoord = a_textureCoord;\n" +
                    "}";

    private static final String FragmentShaderCode =
            "precision mediump float;\n" +
                    "uniform sampler2D u_texture;\n" +
                    "varying vec2 v_textureCoord;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(u_texture, v_textureCoord);\n" +
                    "}";

    public Grass(){
    }

    public void grassInit(Resources resources){
        grassProgram = GlHelper.linkProgram(VertexShaderCode, FragmentShaderCode);
        GLES20.glUseProgram(grassProgram);

        textureData = GlHelper.loadTexture(resources, R.drawable.atlas);
        textureHandle = GLES20.glGetUniformLocation(grassProgram, "u_texture");

        grassUVParam = GLES20.glGetAttribLocation(grassProgram, "a_textureCoord");
        grassPositionParam = GLES20.glGetAttribLocation(grassProgram, "a_Position");
        grassModelViewProjectionParam = GLES20.glGetUniformLocation(grassProgram, "u_MVP");
    }

    private void add(VertexIndexTextureList vitList, Block block, Set<Block> allBlocks){
        if (!allBlocks.contains(new Block(block.x, block.y + 1, block.z))) {
            addTopFace(vitList, block);
        }
        if (!allBlocks.contains(new Block(block.x, block.y, block.z + 1))) {
            addFrontFace(vitList, block);
        }
        if (!allBlocks.contains(new Block(block.x - 1, block.y, block.z))) {
            addLeftFace(vitList, block);
        }
        if (!allBlocks.contains(new Block(block.x + 1, block.y, block.z))) {
            addRightFace(vitList, block);
        }
        if (!allBlocks.contains(new Block(block.x, block.y, block.z - 1))) {
            addBackFace(vitList, block);
        }
        if (!allBlocks.contains(new Block(block.x, block.y - 1, block.z))) {
            addBottomFace(vitList, block);
        }
    }

    public void addBlock(Block block, Set<Block> allBlocks){
        add(vitList, block, allBlocks);
        allBlocks.add(block);
    }

    public void addList(List<Block> list, Set<Block> allBlocks){
        for (Block block : list){
            add(vitList, block, allBlocks);
        }
        allBlocks.addAll(list);
    }

    public void setBufferFromList(){
        cubeBuffer = new Buffers(
                GlHelper.createFloatBuffer(vitList.getVertexArray()),
                GlHelper.createShortBuffer(vitList.getIndexArray()),
                GlHelper.createFloatBuffer(vitList.getTextureCoordArray()));
    }

    public void draw(float[] view, float[] perspective){
        GLES20.glUseProgram(grassProgram);
        GLES20.glUniform1i(textureHandle, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureData);

        Matrix.setIdentityM(modelGrass, 0);
        Matrix.multiplyMM(modelView, 0, view, 0, modelGrass, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        GLES20.glUniformMatrix4fv(grassModelViewProjectionParam, 1, false, modelViewProjection, 0);

        GLES20.glVertexAttribPointer(grassPositionParam, 3, GLES20.GL_FLOAT, false, 0, cubeBuffer.vertexBuffer);
        GLES20.glVertexAttribPointer(grassUVParam, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer.textureCoordBuffer);

        for (Buffers b : chunkToBuffers.values()) {
            GLES20.glVertexAttribPointer(grassPositionParam, 3, GLES20.GL_FLOAT, false, 0, b.vertexBuffer);
            GLES20.glVertexAttribPointer(grassUVParam, 2, GLES20.GL_FLOAT, false, 0, b.textureCoordBuffer);

            GLES20.glDrawElements(
                    GLES20.GL_TRIANGLES, b.drawListBuffer.limit(),
                    GLES20.GL_UNSIGNED_SHORT, b.drawListBuffer);
        }
    }

    // Convenience vector for extracting the position from a matrix via multiplication.
    private static final float YAW_LIMIT = 0.12f;
    private static final float PITCH_LIMIT = 0.12f;
    private static final float[] POS_MATRIX_MULTIPLY_VEC = {0, 0, 0, 1.0f};
    private final float[] tempPosition = new float[16];

    private boolean isLookingAtObject(float[] headView) {
        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(modelView, 0, headView, 0, modelGrass, 0);
        Matrix.multiplyMV(tempPosition, 0, modelView, 0, POS_MATRIX_MULTIPLY_VEC, 0);

        float pitch = (float) Math.atan2(tempPosition[1], -tempPosition[2]);
        float yaw = (float) Math.atan2(tempPosition[0], -tempPosition[2]);

        return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
    }

    private Buffers cubeBuffer;

    private static class Buffers {
        private final FloatBuffer vertexBuffer;
        private final ShortBuffer drawListBuffer;
        private final FloatBuffer textureCoordBuffer;

        Buffers(FloatBuffer vertexBuffer, ShortBuffer drawListBuffer, FloatBuffer textureCoordBuffer) {
            this.vertexBuffer = vertexBuffer;
            this.drawListBuffer = drawListBuffer;
            this.textureCoordBuffer = textureCoordBuffer;
        }
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

    private static final short[] FACE_DRAW_LIST_IDXS = {
            0, 1, 3,
            3, 1, 2,
    };

    // Flip top and bottom since bitmaps are loaded upside down.
    private static final float[] TOP_FACE_TEXTURE_COORDS = {
            0.0f, 1.0f,
            0.5f, 1.0f,
            0.5f, 0.5f,
            0.0f, 0.5f,
    };

    private void addTopFace(VertexIndexTextureList vitList, Block block) {
        vitList.addFace(block, TOP_FACE, FACE_DRAW_LIST_IDXS, TOP_FACE_TEXTURE_COORDS);
    }

    private static final Point3 FRONT_FACE[] = {
            new Point3(-0.5f, -0.5f, 0.5f),  // bottom left
            new Point3(0.5f, -0.5f, 0.5f),  // bottom right
            new Point3(0.5f, 0.5f, 0.5f),  // top right
            new Point3(-0.5f, 0.5f, 0.5f)  // top left
    };

    // Flip top and bottom since bitmaps are loaded upside down.
    private static final float[] SIDE_FACE_TEXTURE_COORDS = {
            0.5f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.5f,
            0.5f, 0.5f,
    };

    private void addFrontFace(VertexIndexTextureList vitList, Block block) {
        vitList.addFace(block, FRONT_FACE, FACE_DRAW_LIST_IDXS, SIDE_FACE_TEXTURE_COORDS);
    }

    private static final Point3 LEFT_FACE[] = {
            new Point3(-0.5f, -0.5f, -0.5f),  // rear bottom
            new Point3(-0.5f, -0.5f, 0.5f),  // front bottom
            new Point3(-0.5f, 0.5f, 0.5f),  // front top
            new Point3(-0.5f, 0.5f, -0.5f)  // rear top
    };

    private void addLeftFace(VertexIndexTextureList vitList, Block block) {
        vitList.addFace(block, LEFT_FACE, FACE_DRAW_LIST_IDXS, SIDE_FACE_TEXTURE_COORDS);
    }

    private static final Point3 RIGHT_FACE[] = {
            new Point3(0.5f, -0.5f, 0.5f),  // front bottom
            new Point3(0.5f, -0.5f, -0.5f),  // rear bottom
            new Point3(0.5f, 0.5f, -0.5f),  // rear top
            new Point3(0.5f, 0.5f, 0.5f)  // front top
    };

    private void addRightFace(VertexIndexTextureList vitList, Block block) {
        vitList.addFace(block, RIGHT_FACE, FACE_DRAW_LIST_IDXS, SIDE_FACE_TEXTURE_COORDS);
    }

    private static final Point3 BACK_FACE[] = {
            new Point3(0.5f, -0.5f, -0.5f),  // bottom right
            new Point3(-0.5f, -0.5f, -0.5f),  // bottom left
            new Point3(-0.5f, 0.5f, -0.5f),  // top left
            new Point3(0.5f, 0.5f, -0.5f)  // top right
    };

    private void addBackFace(VertexIndexTextureList vitList, Block block) {
        vitList.addFace(block, BACK_FACE, FACE_DRAW_LIST_IDXS, SIDE_FACE_TEXTURE_COORDS);
    }

    private static final Point3 BOTTOM_FACE[] = {
            new Point3(-0.5f, -0.5f, -0.5f),  // rear left
            new Point3(0.5f, -0.5f, -0.5f),  // rear right
            new Point3(0.5f, -0.5f, 0.5f),  // front right
            new Point3(-0.5f, -0.5f, 0.5f)  // front left
    };

    // Flip top and bottom since bitmaps are loaded upside down.
    private static final float[] BOTTOM_FACE_TEXTURE_COORDS = {
            0.0f, 0.5f,
            0.5f, 0.5f,
            0.5f, 0.0f,
            0.0f, 0.0f,
    };

    private void addBottomFace(VertexIndexTextureList vitList, Block block) {
        vitList.addFace(block, BOTTOM_FACE, FACE_DRAW_LIST_IDXS, BOTTOM_FACE_TEXTURE_COORDS);
    }
}
