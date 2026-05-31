package com.unipi.alexandris.android.echotrialsonandroid.utility.opengl;

import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;

/**
 * OpenGL background renderer that maintains the same approach as BackgroundManager.
 * Renders a single composite background for efficiency.
 */
public class GLBackgroundRenderer {
    
    private final GLTextureManager textureManager;
    private int backgroundTextureId = -1;
    
    public GLBackgroundRenderer(GLTextureManager textureManager) {
        this.textureManager = textureManager;
        this.backgroundTextureId = textureManager.getBackgroundTexture();
    }
    
    /**
     * Renders the background - equivalent to BackgroundManager.renderBackground().
     * Uses tiled background rendering for efficiency (same approach as BackgroundManager).
     */
    public void render(GLSpriteBatch spriteBatch, Camera camera, float[] viewProjectionMatrix) {
        if (backgroundTextureId == -1 || spriteBatch == null || camera == null) {
            return;
        }
        
        // Calculate visible shapeArea based on camera (same logic as BackgroundManager)
        float cameraX = (float) camera.getX();
        float cameraY = (float) camera.getY();
        
        // Render background tiles to cover screen + buffer for smooth scrolling
        int tileSize = 64; // Standard tile size (matches original)
        int screenWidth = (int) camera.getWidth();
        int screenHeight = (int) camera.getHeight();
        
        int tilesX = (screenWidth / tileSize) + 3; // Extra tiles for smooth scrolling
        int tilesY = (screenHeight / tileSize) + 3;
        
        int startX = (int) (cameraX / tileSize) - 1;
        int startY = (int) (cameraY / tileSize) - 1;
        
        spriteBatch.begin(viewProjectionMatrix);
        
        // Render background tiles (efficient batched rendering)
        for (int x = 0; x < tilesX; x++) {
            for (int y = 0; y < tilesY; y++) {
                float drawX = (startX + x) * tileSize;
                float drawY = (startY + y) * tileSize;
                spriteBatch.draw(backgroundTextureId, drawX, drawY, tileSize, tileSize);
            }
        }
        
        spriteBatch.end();
    }
    
    /**
     * Called when screen size changes.
     * Updates internal calculations for efficient background rendering.
     */
    public void onScreenSizeChanged(int width, int height) {
        // SimpleBackground rendering is dynamic based on camera position,
        // so no pre-generation needed (unlike BackgroundManager's composite approach)
        System.out.println("GLBackgroundRenderer: Screen size changed to " + width + "x" + height);
    }
} 