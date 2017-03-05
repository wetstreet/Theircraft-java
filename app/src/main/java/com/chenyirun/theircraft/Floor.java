package com.chenyirun.theircraft;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by chenyirun on 2017/3/3.
 */

public class Floor {
    private int floorProgram;

    public float[] modelFloor = new float[16];
    private final float[] modelView = new float[16];
    private final float[] modelViewProjection = new float[16];

    private float floorDepth = 20f;

    private FloatBuffer floorVertices;
    private FloatBuffer floorColors;
    private FloatBuffer floorNormals;

    private int floorPositionParam;
    private int floorNormalParam;
    private int floorColorParam;
    private int floorModelParam;
    private int floorModelViewParam;
    private int floorModelViewProjectionParam;
    private int floorLightPosParam;

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
            "varying vec3 v_Grid;\n" +
            "\n" +
            "void main() {\n" +
            "    float depth = gl_FragCoord.z / gl_FragCoord.w; // Calculate world-space distance.\n" +
            "\n" +
            "    if ((mod(abs(v_Grid.x), 10.0) < 0.1) || (mod(abs(v_Grid.z), 10.0) < 0.1)) {\n" +
            "        gl_FragColor = max(0.0, (90.0-depth) / 90.0) * vec4(1.0, 1.0, 1.0, 1.0)\n" +
            "                + min(1.0, depth / 90.0) * v_Color;\n" +
            "    } else {\n" +
            "        gl_FragColor = v_Color;\n" +
            "    }\n" +
            "}";

    public Floor(){
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COORDS.length * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        floorVertices = bbVertices.asFloatBuffer();
        floorVertices.put(WorldLayoutData.FLOOR_COORDS);
        floorVertices.position(0);

        ByteBuffer bbColors = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COLORS.length * 4);
        bbColors.order(ByteOrder.nativeOrder());
        floorColors = bbColors.asFloatBuffer();
        floorColors.put(WorldLayoutData.FLOOR_COLORS);
        floorColors.position(0);

        ByteBuffer bbNormals = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_NORMALS.length * 4);
        bbNormals.order(ByteOrder.nativeOrder());
        floorNormals = bbNormals.asFloatBuffer();
        floorNormals.put(WorldLayoutData.FLOOR_NORMALS);
        floorNormals.position(0);

        int vertexShader = Renderer.loadShader(GLES20.GL_VERTEX_SHADER, VertexShaderCode);
        int fragmentShader = Renderer.loadShader(GLES20.GL_FRAGMENT_SHADER, FragmentShaderCode);
        floorProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(floorProgram, vertexShader);
        GLES20.glAttachShader(floorProgram, fragmentShader);
        GLES20.glLinkProgram(floorProgram);
        GLES20.glUseProgram(floorProgram);

        floorPositionParam = GLES20.glGetAttribLocation(floorProgram, "a_Position");
        floorNormalParam = GLES20.glGetAttribLocation(floorProgram, "a_Normal");
        floorColorParam = GLES20.glGetAttribLocation(floorProgram, "a_Color");
        floorModelParam = GLES20.glGetUniformLocation(floorProgram, "u_Model");
        floorModelViewParam = GLES20.glGetUniformLocation(floorProgram, "u_MVMatrix");
        floorModelViewProjectionParam = GLES20.glGetUniformLocation(floorProgram, "u_MVP");
        floorLightPosParam = GLES20.glGetUniformLocation(floorProgram, "u_LightPos");

        Matrix.setIdentityM(modelFloor, 0);
        Matrix.translateM(modelFloor, 0, 0, -floorDepth, 0);
    }

    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
        Matrix.multiplyMM(modelView, 0, view, 0, modelFloor, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

        GLES20.glUseProgram(floorProgram);

        GLES20.glUniform3fv(floorLightPosParam, 1, lightPosInEyeSpace, 0);

        GLES20.glUniformMatrix4fv(floorModelParam, 1, false, modelFloor, 0);
        GLES20.glUniformMatrix4fv(floorModelViewParam, 1, false, modelView, 0);
        GLES20.glUniformMatrix4fv(floorModelViewProjectionParam, 1, false, modelViewProjection, 0);

        GLES20.glVertexAttribPointer(floorPositionParam, 3, GLES20.GL_FLOAT, false, 0, floorVertices);
        GLES20.glVertexAttribPointer(floorNormalParam, 3, GLES20.GL_FLOAT, false, 0, floorNormals);
        GLES20.glVertexAttribPointer(floorColorParam, 4, GLES20.GL_FLOAT, false, 0, floorColors);

        GLES20.glEnableVertexAttribArray(floorPositionParam);
        GLES20.glEnableVertexAttribArray(floorNormalParam);
        GLES20.glEnableVertexAttribArray(floorColorParam);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 24);

        GLES20.glDisableVertexAttribArray(floorPositionParam);
        GLES20.glDisableVertexAttribArray(floorNormalParam);
        GLES20.glDisableVertexAttribArray(floorColorParam);
    }
}
