package com.unipi.alexandris.android.echotrialsonandroid.utility.opengl;

import android.graphics.Color;

import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;

/**
 * OpenGL UI renderer for displaying game UI elements.
 * Renders screen-space UI elements like FPS counter, debug info, touch controls, etc.
 * Provides the same UI functionality as Canvas GameSurfaceView.renderUI().
 */
public class GLUIRenderer {
    
    private final GLTextRenderer textRenderer;
    private final GLTouchControlRenderer touchControlRenderer;
    
    // FPS tracking (same as Canvas version)
    private long lastFpsLogTime = 0;
    private int frameCount = 0;
    private float fps = 0;
    private long fpsUpdateTime = System.currentTimeMillis();
    
    // UI rendering control
    private boolean renderingEnabled = true;
    
    public GLUIRenderer(GLSpriteBatch spriteBatch, GLTextureManager textureManager) {
        this(spriteBatch, textureManager, true); // Default: enable controls
    }
    
    /**
     * Creates a new GLUIRenderer with optional control enabling.
     * 
     * @param spriteBatch The sprite batch for rendering
     * @param textureManager The texture manager
     * @param enableControls Whether to enable touch controls (default: true)
     */
    public GLUIRenderer(GLSpriteBatch spriteBatch, GLTextureManager textureManager, boolean enableControls) {
        this.textRenderer = new GLTextRenderer(spriteBatch);
        this.touchControlRenderer = new GLTouchControlRenderer(spriteBatch, textureManager, textRenderer);
        
        // Set initial state based on constructor parameter
        this.renderingEnabled = enableControls;
        this.touchControlRenderer.setControlsEnabled(enableControls);
    }
    
    /**
     * Gets the touch control renderer for input handling.
     */
    public GLTouchControlRenderer getTouchControlRenderer() {
        return touchControlRenderer;
    }
    
    /**
     * Updates FPS calculation (same logic as Canvas GameSurfaceView.updateFPS()).
     */
    private void updateFPS() {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        
        // Update FPS every 500ms for smooth display (same as Canvas version)
        if (currentTime - fpsUpdateTime >= 500) {
            long timeElapsed = currentTime - fpsUpdateTime;
            fps = (frameCount * 1000f) / timeElapsed;
            frameCount = 0;
            fpsUpdateTime = currentTime;
        }
    }
    
    /**
     * Renders UI elements with screen-space projection.
     * Provides the same UI functionality as Canvas GameSurfaceView.renderUI().
     * @param spriteBatch Sprite batch for rendering
     * @param projectionMatrix Screen-space projection matrix (no camera transform)
     * @param screenWidth Screen width for UI positioning
     * @param screenHeight Screen height for UI positioning
     * @param camera Camera for position info
     */
    public void render(GLSpriteBatch spriteBatch, float[] projectionMatrix, int screenWidth, int screenHeight, Camera camera) {
        
        // Check if UI rendering is enabled
        if (!renderingEnabled) {
            return;
        }
        
        // Update FPS calculation (same as Canvas version)
        updateFPS();
        
        // Update touch controls (handles jump auto-release timer)
        touchControlRenderer.update(1); // 1 onTick per frame
        
        // Begin UI rendering with screen-space projection
        spriteBatch.begin(projectionMatrix);
        
        // Render touch controls (same as Canvas TouchControlController.render())
        touchControlRenderer.render(screenWidth, screenHeight);
        
        // Render debug information (same as Canvas GameSurfaceView.renderUI())
        if (GameConstants.INSTANCE.isDEBUG_MODE()) {
            renderDebugInfo(GameConstants.INSTANCE.getObjectHandler(), camera);
        }
        
        spriteBatch.end();
        
        // Log FPS periodically for debugging (same as original debug output)
        long currentTime = System.currentTimeMillis();
        if (fps > 0 && currentTime - lastFpsLogTime > 2000) { // Every 2 seconds
            System.out.println("OpenGL FPS: " + (int)fps + 
                             " | Objects: " + (GameConstants.INSTANCE.getObjectHandler() != null ? GameConstants.INSTANCE.getObjectHandler().getObjects().size() : 0) +
                             " | Camera: (" + (int)camera.getX() + "," + (int)camera.getY() + ")");
            lastFpsLogTime = currentTime;
        }
    }
    

    
    /**
     * Sets whether UI rendering is enabled.
     * 
     * @param enabled true to enable UI rendering, false to disable
     */
    public void setRenderingEnabled(boolean enabled) {
        this.renderingEnabled = enabled;
    }
    
    /**
     * Renders debug information (same as Canvas GameSurfaceView.renderUI() debug section).
     */
    private void renderDebugInfo(ObjectHandler handler, Camera camera) {
        // Set debug text properties (same as Canvas version)
        textRenderer.setTextSize(20);
        textRenderer.setTextColor(Color.WHITE);
        
        // Group all debug text (same as Canvas version)
        String[] debugLines = {
            "FPS: " + (int)fps,
            "Camera: " + (int)camera.getX() + "," + (int)camera.getY(),
            "Objects: " + (handler != null ? handler.getObjects().size() : 0),
            "BG: Ready", // SimpleBackground is always ready in OpenGL
            "FG: Ready"  // SimpleForeground is always ready in OpenGL
        };
        
        // Add player info if available (same as Canvas version)
        if (GameConstants.INSTANCE.getPlayer() != null) {
            String playerInfo = "SimplePlayer: " + (int)GameConstants.INSTANCE.getPlayer().getX() + "," +
                               (int)GameConstants.INSTANCE.getPlayer().getY();
            String[] newLines = new String[debugLines.length + 1];
            System.arraycopy(debugLines, 0, newLines, 0, debugLines.length);
            newLines[debugLines.length] = playerInfo;
            debugLines = newLines;
        }
        
        // Draw all debug text (same positioning as Canvas version)
        for (int i = 0; i < debugLines.length; i++) {
            textRenderer.drawText(debugLines[i], 10, 30 + (i * 25));
        }
    }
    
    /**
     * Called when screen size changes.
     * Updates UI layout calculations for new screen dimensions.
     */
    public void onScreenSizeChanged(int width, int height) {
        // UI elements will be positioned relative to screen dimensions
        System.out.println("GLUIRenderer: Screen size changed to " + width + "x" + height);
    }
    

    
    /**
     * Gets current FPS for external display.
     */
    public float getCurrentFPS() {
        return fps;
    }
} 