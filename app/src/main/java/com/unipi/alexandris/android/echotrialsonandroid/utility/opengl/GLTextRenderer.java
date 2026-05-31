package com.unipi.alexandris.android.echotrialsonandroid.utility.opengl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * OpenGL text renderer that creates text textures for efficient text display.
 * Provides the same text rendering capabilities as Canvas.drawText() but using OpenGL.
 */
public class GLTextRenderer {
    
    private final GLSpriteBatch spriteBatch;
    private final Paint textPaint;
    
    // Text texture cache for common strings
    private static final int MAX_CACHED_TEXTURES = 50;
    private final java.util.Map<String, Integer> textTextureCache = new java.util.HashMap<>();
    private final java.util.Map<String, TextMetrics> textMetricsCache = new java.util.HashMap<>();
    
    /**
     * Stores text metrics for positioning.
     */
    private static class TextMetrics {
        final int width;
        final int height;
        final int baseline;
        
        TextMetrics(int width, int height, int baseline) {
            this.width = width;
            this.height = height;
            this.baseline = baseline;
        }
    }
    
    public GLTextRenderer(GLSpriteBatch spriteBatch) {
        this.spriteBatch = spriteBatch;
        
        // Initialize text paint (same settings as Canvas debug text)
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(20);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.DEFAULT);
    }
    
    /**
     * Draws text at the specified position.
     * @param text Text to draw
     * @param x X position (screen coordinates)
     * @param y Y position (screen coordinates)
     * @param color Text color (ARGB)
     */
    public void drawText(String text, float x, float y, int color) {
        if (text == null || text.isEmpty()) return;
        
        // Get or create texture for this text
        int textureId = getTextTexture(text, color);
        if (textureId == -1) return;
        
        // Get text metrics for positioning
        TextMetrics metrics = textMetricsCache.get(text);
        if (metrics == null) return;
        
        // Draw text texture (adjust Y for baseline)
        float drawY = y - metrics.baseline;
        spriteBatch.draw(textureId, x, drawY, metrics.width, metrics.height);
    }
    
    /**
     * Draws text with default white color.
     */
    public void drawText(String text, float x, float y) {
        drawText(text, x, y, Color.WHITE);
    }
    
    /**
     * Gets or creates a texture for the specified text.
     */
    private int getTextTexture(String text, int color) {
        String cacheKey = text + "_" + Integer.toHexString(color);
        
        // Check cache first
        if (textTextureCache.containsKey(cacheKey)) {
            return textTextureCache.get(cacheKey);
        }
        
        // Create new text texture
        textPaint.setColor(color);
        
        // Measure text
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        int textWidth = (int) Math.ceil(textPaint.measureText(text));
        int textHeight = (int) Math.ceil(fontMetrics.bottom - fontMetrics.top);
        int baseline = (int) Math.ceil(-fontMetrics.top);
        
        if (textWidth <= 0 || textHeight <= 0) return -1;
        
        // Create bitmap for text
        Bitmap textBitmap = Bitmap.createBitmap(textWidth, textHeight, Bitmap.Config.ARGB_8888);
        Canvas textCanvas = new Canvas(textBitmap);
        
        // Clear bitmap with transparent background
        textCanvas.drawColor(Color.TRANSPARENT);
        
        // Draw text on bitmap
        textCanvas.drawText(text, 0, baseline, textPaint);
        
        // Create OpenGL texture
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        int textureId = textureIds[0];
        
        // Bind and configure texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        
        // Upload bitmap to GPU
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textBitmap, 0);
        
        // Clean up bitmap
        textBitmap.recycle();
        
        // Cache texture and metrics
        if (textTextureCache.size() < MAX_CACHED_TEXTURES) {
            textTextureCache.put(cacheKey, textureId);
            textMetricsCache.put(text, new TextMetrics(textWidth, textHeight, baseline));
        }
        
        return textureId;
    }
    
    /**
     * Sets text size (same as Paint.setTextSize()).
     */
    public void setTextSize(float size) {
        textPaint.setTextSize(size);
        // Clear cache since text size changed
        clearCache();
    }
    
    /**
     * Sets text color (same as Paint.setColor()).
     */
    public void setTextColor(int color) {
        textPaint.setColor(color);
    }
    
    /**
     * Clears the text texture cache.
     */
    public void clearCache() {
        // Delete OpenGL textures
        for (int textureId : textTextureCache.values()) {
            GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
        }
        textTextureCache.clear();
        textMetricsCache.clear();
    }
    
    /**
     * Measures text width (same as Paint.measureText()).
     */
    public float measureText(String text) {
        return textPaint.measureText(text);
    }
} 