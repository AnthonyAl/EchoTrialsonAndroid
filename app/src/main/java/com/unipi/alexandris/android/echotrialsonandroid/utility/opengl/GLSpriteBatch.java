package com.unipi.alexandris.android.echotrialsonandroid.utility.opengl;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * OpenGL ES sprite batch system for efficient 2D rendering.
 * Batches multiple sprites into single draw calls for maximum performance.
 * <br>
 * This replaces individual Canvas.drawBitmap() calls with batched OpenGL rendering,
 * providing significant performance improvements for many objects.
 */
public class GLSpriteBatch {
    
    // Vertex shader source - handles sprite positioning, texturing, and per-vertex color
    private static final String VERTEX_SHADER_SOURCE = 
        "attribute vec2 a_position;\n" +
        "attribute vec2 a_texCoord;\n" +
        "attribute vec4 a_color;\n" +
        "uniform mat4 u_mvpMatrix;\n" +
        "varying vec2 v_texCoord;\n" +
        "varying vec4 v_color;\n" +
        "void main() {\n" +
        "    gl_Position = u_mvpMatrix * vec4(a_position, 0.0, 1.0);\n" +
        "    v_texCoord = a_texCoord;\n" +
        "    v_color = a_color;\n" +
        "}\n";
    
    // Fragment shader source - handles texture sampling and per-vertex color blending
    private static final String FRAGMENT_SHADER_SOURCE = 
        "precision mediump float;\n" +
        "varying vec2 v_texCoord;\n" +
        "varying vec4 v_color;\n" +
        "uniform sampler2D u_texture;\n" +
        "void main() {\n" +
        "    gl_FragColor = texture2D(u_texture, v_texCoord) * v_color;\n" +
        "}\n";
    
    
    private static final int MAX_SPRITES_PER_BATCH = 1000;
    private static final int VERTICES_PER_SPRITE = 6;
    private static final int FLOATS_PER_VERTEX = 8;
    private static final int BATCH_SIZE = MAX_SPRITES_PER_BATCH * VERTICES_PER_SPRITE * FLOATS_PER_VERTEX;
    private int shaderProgram;
    private int positionHandle;
    private int texCoordHandle;
    private int colorHandle;
    private int mvpMatrixHandle;
    private int textureHandle;
    private final FloatBuffer vertexBuffer;
    private final float[] vertices = new float[BATCH_SIZE];
    private int vertexCount = 0;
    
    private boolean batching = false;
    private int currentTexture = -1;
    private final float[] currentColor = {1.0f, 1.0f, 1.0f, 1.0f};
    
    private GLTextureManager textureManager;
    
    
    public GLSpriteBatch() {
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        
        initializeShaders();
    }
    
    public void setTextureManager(GLTextureManager textureManager) {
        this.textureManager = textureManager;
    }
    
    public GLTextureManager getTextureManager() {
        return textureManager;
    }
    
    private void initializeShaders() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_SOURCE);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_SOURCE);
        
        // Create and configure shader program
        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);
        GLES20.glLinkProgram(shaderProgram);
        
        // Get shader attribute/uniform locations
        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "a_position");
        texCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "a_texCoord");
        colorHandle = GLES20.glGetAttribLocation(shaderProgram, "a_color");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "u_mvpMatrix");
        textureHandle = GLES20.glGetUniformLocation(shaderProgram, "u_texture");
    }
    
    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        
        // Check compilation status
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            String error = GLES20.glGetShaderInfoLog(shader);
            System.err.println("Shader compilation failed: " + error);
            GLES20.glDeleteShader(shader);
            return 0;
        }
        
        return shader;
    }
    
    public void begin(float[] mvpMatrix) {
        if (batching) {
            throw new IllegalStateException("Must call end() before calling begin() again");
        }
        
        batching = true;
        vertexCount = 0;
        
        // Set up OpenGL state for sprite rendering
        GLES20.glUseProgram(shaderProgram);
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glUniform1i(textureHandle, 0); // Use texture unit 0
        
        // Enable vertex attributes
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(texCoordHandle);
        GLES20.glEnableVertexAttribArray(colorHandle);
    }
    
    /**
     * Adds a sprite to the current batch.
     * If the batch is full or texture changes, it will automatically flush.
     * 
     * @param textureId OpenGL texture SimpleID
     * @param x X position
     * @param y Y position  
     * @param width Sprite width
     * @param height Sprite height
     */
    public void draw(int textureId, float x, float y, float width, float height) {
        draw(textureId, x, y, width, height, false, false);
    }
    
    public void draw(int textureId, float x, float y, float width, float height, 
                     float r, float g, float b, float a) {
        draw(textureId, x, y, width, height, false, false, r, g, b, a);
    }
    
    public void draw(int textureId, float x, float y, float width, float height, boolean flipX, boolean flipY) {
        draw(textureId, x, y, width, height, flipX, flipY, currentColor[0], currentColor[1], currentColor[2], currentColor[3]);
    }
    
    public void draw(int textureId, float x, float y, float width, float height, boolean flipX, boolean flipY,
                     float r, float g, float b, float a) {
        if (!batching) {
            throw new IllegalStateException("Must call begin() before drawing sprites");
        }
        
        // Check if we need to flush (texture change or batch full)
        if (currentTexture != textureId || vertexCount + VERTICES_PER_SPRITE > MAX_SPRITES_PER_BATCH * VERTICES_PER_SPRITE) {
            flush();
            currentTexture = textureId;
        }
        
        // Calculate texture coordinates based on flipping
        float u1 = flipX ? 1.0f : 0.0f;
        float u2 = flipX ? 0.0f : 1.0f;
        float v1 = flipY ? 1.0f : 0.0f;
        float v2 = flipY ? 0.0f : 1.0f;
        
        addVertex(x, y, u1, v1, r, g, b, a);
        addVertex(x, y + height, u1, v2, r, g, b, a);
        addVertex(x + width, y, u2, v1, r, g, b, a);
        
        addVertex(x + width, y, u2, v1, r, g, b, a);
        addVertex(x, y + height, u1, v2, r, g, b, a);
        addVertex(x + width, y + height, u2, v2, r, g, b, a);
    }
    
    private void addVertex(float x, float y, float u, float v, float r, float g, float b, float a) {
        int index = vertexCount * FLOATS_PER_VERTEX;
        vertices[index] = x;
        vertices[index + 1] = y;
        vertices[index + 2] = u;
        vertices[index + 3] = v;
        vertices[index + 4] = r;
        vertices[index + 5] = g;
        vertices[index + 6] = b;
        vertices[index + 7] = a;
        vertexCount++;
    }
    
    public void flush() {
        if (vertexCount == 0) return;
        
        // Bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, currentTexture);
        
        // Upload vertex data
        vertexBuffer.clear();
        vertexBuffer.put(vertices, 0, vertexCount * FLOATS_PER_VERTEX);
        vertexBuffer.position(0);
        
        // Set vertex attribute pointers
        int stride = FLOATS_PER_VERTEX * 4;
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, stride, vertexBuffer);
        vertexBuffer.position(2);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, stride, vertexBuffer);
        vertexBuffer.position(4);
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, stride, vertexBuffer);
        
        // Draw all sprites in batch
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        
        // Reset batch
        vertexCount = 0;
    }
    
    /**
     * Ends the current batch and flushes any remaining sprites.
     */
    public void end() {
        if (!batching) {
            throw new IllegalStateException("Must call begin() before calling end()");
        }
        
        flush();
        
        // Disable vertex attributes
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);
        
        batching = false;
        currentTexture = -1;
    }
    
    public void setColor(float r, float g, float b, float a) {
        currentColor[0] = r;
        currentColor[1] = g;
        currentColor[2] = b;
        currentColor[3] = a;
    }
    
    public void resetColor() {
        setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
} 