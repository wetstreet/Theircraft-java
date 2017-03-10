package com.chenyirun.theircraft;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.chenyirun.theircraft.model.Point3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenyirun on 2017/2/28.
 */

public class Grass {
    private final int grassProgram;

    private float x;
    private float y;
    private float z;

    private List<Point3> list = new ArrayList<>();

    public final float[] modelGrass = new float[16];
    private final float[] modelView = new float[16];
    private final float[] modelViewProjection = new float[16];

    private FloatBuffer cubeVertices;
    private FloatBuffer cubeUVs;

    private int textureHandle;
    private int textureData;
    private int grassPositionParam;
    private int grassUVParam;
    private int grassModelViewProjectionParam;

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

    public Grass(Resources resources){
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_COORDS.length * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        cubeVertices = bbVertices.asFloatBuffer();
        cubeVertices.put(WorldLayoutData.CUBE_COORDS);
        cubeVertices.position(0);

        ByteBuffer bbUVs = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_UVS.length * 4);
        bbUVs.order(ByteOrder.nativeOrder());
        cubeUVs = bbUVs.asFloatBuffer();
        cubeUVs.put(WorldLayoutData.CUBE_UVS);
        cubeUVs.position(0);

        int vertexShader = Renderer.loadShader(GLES20.GL_VERTEX_SHADER, VertexShaderCode);
        int fragmentShader = Renderer.loadShader(GLES20.GL_FRAGMENT_SHADER, FragmentShaderCode);
        grassProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(grassProgram, vertexShader);
        GLES20.glAttachShader(grassProgram, fragmentShader);
        GLES20.glLinkProgram(grassProgram);
        GLES20.glUseProgram(grassProgram);

        textureData = Renderer.loadTexture(resources, R.drawable.atlas);
        textureHandle = GLES20.glGetUniformLocation(grassProgram, "u_texture");

        grassUVParam = GLES20.glGetAttribLocation(grassProgram, "a_textureCoord");
        grassPositionParam = GLES20.glGetAttribLocation(grassProgram, "a_Position");
        grassModelViewProjectionParam = GLES20.glGetUniformLocation(grassProgram, "u_MVP");
    }

    public void add(float x, float y, float z){
        list.add(new Point3(x,y,z));
    }

    public void setList(List<Point3> list){
        this.list = list;
    }

    public void addList(List<Point3> list){
        for (Point3 p : list) {
            this.list.add(p);
        }
    }

    public List<Point3> getList(){
        return list;
    }

    public void drawList(float[] view, float[] perspective){
        GLES20.glUseProgram(grassProgram);
        GLES20.glUniform1i(textureHandle, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureData);

        for (Point3 position : list) {
            draw(position.x,position.y,position.z,view,perspective);
        }
    }

    public void draw(float x, float y, float z, float[] view, float[] perspective) {
        Matrix.setIdentityM(modelGrass, 0);
        Matrix.translateM(modelGrass, 0, x, y, z);
        Matrix.multiplyMM(modelView, 0, view, 0, modelGrass, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        GLES20.glUniformMatrix4fv(grassModelViewProjectionParam, 1, false, modelViewProjection, 0);

        GLES20.glVertexAttribPointer(grassPositionParam, 3, GLES20.GL_FLOAT, false, 0, cubeVertices);
        GLES20.glVertexAttribPointer(grassUVParam, 2, GLES20.GL_FLOAT, false, 0, cubeUVs);

        GLES20.glEnableVertexAttribArray(grassPositionParam);
        GLES20.glEnableVertexAttribArray(grassUVParam);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

        GLES20.glDisableVertexAttribArray(grassUVParam);
        GLES20.glDisableVertexAttribArray(grassPositionParam);
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
}
