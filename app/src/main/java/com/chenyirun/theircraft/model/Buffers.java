package com.chenyirun.theircraft.model;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Buffers {
    public final FloatBuffer vertexBuffer;
    public final ShortBuffer drawListBuffer;
    public final FloatBuffer textureCoordBuffer;

    public Buffers(FloatBuffer vertexBuffer, ShortBuffer drawListBuffer, FloatBuffer textureCoordBuffer) {
        this.vertexBuffer = vertexBuffer;
        this.drawListBuffer = drawListBuffer;
        this.textureCoordBuffer = textureCoordBuffer;
    }
}
