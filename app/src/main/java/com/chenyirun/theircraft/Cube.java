package com.chenyirun.theircraft;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by chenyirun on 2017/2/28.
 */

public class Cube {
    private int cubeProgram;

    public final float[] modelCube = new float[16];
    private final float[] modelView = new float[16];
    private final float[] modelViewProjection = new float[16];

    private FloatBuffer cubeVertices;
    private FloatBuffer cubeColors;
    private FloatBuffer cubeNormals;

    private int cubePositionParam;
    private int cubeNormalParam;
    private int cubeColorParam;
    private int cubeModelParam;
    private int cubeModelViewParam;
    private int cubeModelViewProjectionParam;
    private int cubeLightPosParam;

    private static final String VertexShaderCode =
            "uniform mat4 u_Model;\n" +
            "uniform mat4 u_MVP;\n" +
            "uniform mat4 u_MVMatrix;\n" +
            "uniform vec3 u_LightPos;\n" +
            "\n" +
            "attribute vec4 a_Position;\n" +
            "attribute vec4 a_Color;\n" +
            "attribute vec3 a_Normal;\n" +
            "\n" +
            "varying vec4 v_Color;\n" +
            "varying vec3 v_Grid;\n" +
            "\n" +
            "void main() {\n" +
            "   v_Grid = vec3(u_Model * a_Position);\n" +
            "\n" +
            "   vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);\n" +
            "   vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));\n" +
            "\n" +
            "   float distance = length(u_LightPos - modelViewVertex);\n" +
            "   vec3 lightVector = normalize(u_LightPos - modelViewVertex);\n" +
            "   float diffuse = max(dot(modelViewNormal, lightVector), 0.5);\n" +
            "\n" +
            "   diffuse = diffuse * (1.0 / (1.0 + (0.00001 * distance * distance)));\n" +
            "   v_Color = vec4(a_Color.rgb * diffuse, a_Color.a);\n" +
            "   gl_Position = u_MVP * a_Position;\n" +
            "}\n";

    private static final String FragmentShaderCode =
            "precision mediump float;\n" +
            "varying vec4 v_Color;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_FragColor = v_Color;\n" +
            "}";

    public Cube(float x, float y, float z){
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_COORDS.length * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        cubeVertices = bbVertices.asFloatBuffer();
        cubeVertices.put(WorldLayoutData.CUBE_COORDS);
        cubeVertices.position(0);

        ByteBuffer bbColors = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_COLORS.length * 4);
        bbColors.order(ByteOrder.nativeOrder());
        cubeColors = bbColors.asFloatBuffer();
        cubeColors.put(WorldLayoutData.CUBE_COLORS);
        cubeColors.position(0);

        ByteBuffer bbNormals = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_NORMALS.length * 4);
        bbNormals.order(ByteOrder.nativeOrder());
        cubeNormals = bbNormals.asFloatBuffer();
        cubeNormals.put(WorldLayoutData.CUBE_NORMALS);
        cubeNormals.position(0);

        int vertexShader = Renderer.loadShader(GLES20.GL_VERTEX_SHADER, VertexShaderCode);
        int fragmentShader = Renderer.loadShader(GLES20.GL_FRAGMENT_SHADER, FragmentShaderCode);
        cubeProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(cubeProgram, vertexShader);
        GLES20.glAttachShader(cubeProgram, fragmentShader);
        GLES20.glLinkProgram(cubeProgram);
        GLES20.glUseProgram(cubeProgram);

        cubePositionParam = GLES20.glGetAttribLocation(cubeProgram, "a_Position");
        cubeNormalParam = GLES20.glGetAttribLocation(cubeProgram, "a_Normal");
        cubeColorParam = GLES20.glGetAttribLocation(cubeProgram, "a_Color");
        cubeModelParam = GLES20.glGetUniformLocation(cubeProgram, "u_Model");
        cubeModelViewParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVMatrix");
        cubeModelViewProjectionParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVP");
        cubeLightPosParam = GLES20.glGetUniformLocation(cubeProgram, "u_LightPos");

        Matrix.setIdentityM(modelCube, 0);
        Matrix.translateM(modelCube, 0, x, y, z);
    }

    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
        Matrix.multiplyMM(modelView, 0, view, 0, modelCube, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

        GLES20.glUseProgram(cubeProgram);

        GLES20.glUniform3fv(cubeLightPosParam, 1, lightPosInEyeSpace, 0);

        GLES20.glUniformMatrix4fv(cubeModelParam, 1, false, modelCube, 0);
        GLES20.glUniformMatrix4fv(cubeModelViewParam, 1, false, modelView, 0);
        GLES20.glUniformMatrix4fv(cubeModelViewProjectionParam, 1, false, modelViewProjection, 0);

        GLES20.glVertexAttribPointer(cubePositionParam, 3, GLES20.GL_FLOAT, false, 0, cubeVertices);
        GLES20.glVertexAttribPointer(cubeNormalParam, 3, GLES20.GL_FLOAT, false, 0, cubeNormals);
        GLES20.glVertexAttribPointer(cubeColorParam, 4, GLES20.GL_FLOAT, false, 0, cubeColors);

        GLES20.glEnableVertexAttribArray(cubePositionParam);
        GLES20.glEnableVertexAttribArray(cubeNormalParam);
        GLES20.glEnableVertexAttribArray(cubeColorParam);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

        GLES20.glDisableVertexAttribArray(cubePositionParam);
        GLES20.glDisableVertexAttribArray(cubeNormalParam);
        GLES20.glDisableVertexAttribArray(cubeColorParam);
    }
}
