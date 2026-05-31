package com.unipi.alexandris.android.echotrialsonandroid.utility.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.unipi.alexandris.android.echotrialsonandroid.controller.GameController;
import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * OpenGL ES renderer that follows the exact same patterns as the original JavaFX Game.render() method.
 * Maintains the same rendering order and structure:
 * 1. Clear screen
 * 2. Draw background
 * 3. Render game objects
 * 4. Draw foreground 
 * 5. Render UI
 * <br>
 * This ensures compatibility with the original JavaFX rendering approach.
 */
public class GLGameRenderer implements GLSurfaceView.Renderer {
    
    private final Context context;
    private final Camera camera;
    
    // OpenGL rendering components
    private GLSpriteBatch spriteBatch;
    private GLTextureManager textureManager;
    private GLBackgroundRenderer backgroundRenderer;
    private GLUIRenderer uiRenderer;
    
    // View matrices - following OpenGL ES standard patterns
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    
    // Screen dimensions
    private int screenWidth;
    private int screenHeight;
    
    // Performance tracking (same as original)
    private long lastFpsTime = 0;
    private int frameCount = 0;
    
    // Control enabling flag
    private boolean enableControls = true;
    


    /**
     * Creates a new OpenGL renderer that maintains JavaFX compatibility.
     */
    public GLGameRenderer(Context context, Camera camera) {
        this(context, camera, true); // Default: enable controls
    }
    
    /**
     * Creates a new OpenGL renderer with optional control enabling.
     * 
     * @param context The Android context
     * @param camera The camera instance
     * @param enableControls Whether to enable UI controls (default: true)
     */
    public GLGameRenderer(Context context, Camera camera, boolean enableControls) {
        this.context = context;
        this.camera = camera;
        this.enableControls = enableControls;
    }
    
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Configure OpenGL state - equivalent to JavaFX Graphics2D setup
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // Black background (same as original)
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST); // 2D rendering doesn't need depth
        
        // Initialize OpenGL rendering components
        try {
            textureManager = new GLTextureManager(context);
            spriteBatch = new GLSpriteBatch();
            spriteBatch.setTextureManager(textureManager); // Connect texture manager to sprite batch
            backgroundRenderer = new GLBackgroundRenderer(textureManager);
            uiRenderer = new GLUIRenderer(spriteBatch, textureManager, enableControls);
            
    
        } catch (Exception e) {
            System.err.println("Failed to initialize OpenGL renderer: " + e.getMessage());
        }
    }
    
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Update viewport and projection matrix
        GLES20.glViewport(0, 0, width, height);
        screenWidth = width;
        screenHeight = height;
        
        // Set up orthographic projection - standard for 2D games
        // This matches the Canvas coordinate system used in the original
        Matrix.orthoM(projectionMatrix, 0, 0, width, height, 0, -1, 1);

        GameController.GAME.setScreenDimensions(width, height);
        
        // Notify components of size change
        if (backgroundRenderer != null) {
            backgroundRenderer.onScreenSizeChanged(width, height);
        }
        if (uiRenderer != null) {
            uiRenderer.onScreenSizeChanged(width, height);
        }
        

    }
    
    @Override
    public void onDrawFrame(GL10 gl) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            return;
        }
        
        updateFPS();
        
        if (spriteBatch == null || camera == null) {
            return;
        }

        //Call GameController to start rendering
        GameController.GAME.renderGL(this);
    }


    // ===== RENDERING SERVICE METHODS - Called by GameController =====
    /**
     * Clears the screen - OpenGL service method.
     */
    public void clearScreen() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }
    
    /**
     * Sets up camera transformation - OpenGL service method.
     * @param camera The camera to use for transformation
     */
    public void setupCamera(Camera camera) {
        if (camera != null) {
            setupCameraMatrix(camera);
        }
    }
    
    /**
     * Begins object rendering batch - OpenGL service method.
     */
    public void beginObjectBatch() {
        if (spriteBatch != null) {
            spriteBatch.begin(viewProjectionMatrix);
        }
    }
    
    /**
     * Ends object rendering batch - OpenGL service method.
     */
    public void endObjectBatch() {
        if (spriteBatch != null) {
            spriteBatch.end();
        }
    }
    
    // ===== INTERNAL IMPLEMENTATION METHODS =====
    
    /**
     * Sets up camera transformation matrix.
     * Equivalent to Graphics2D transform in JavaFX version.
     */
    private void setupCameraMatrix(Camera camera) {
        // Create translation matrix for camera offset (simpler than lookAt for 2D)
        // This matches the Canvas camera transform: translate by -camera position
        Matrix.setIdentityM(viewMatrix, 0);
        
        // IMPORTANT: Use double precision for camera coordinates to avoid drift
        double cameraX = camera.getX();
        double cameraY = camera.getY();
        
        // Apply camera translation (negative because we move the world, not the camera)
        Matrix.translateM(viewMatrix, 0, -(float)cameraX, -(float)cameraY, 0f);
        
        // Combine view and projection matrices (order matters: projection * view)
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
    }
    
    /**
     * Renders background - OpenGL service method called by GameController.
     */
    public void renderBackground() {
        if (backgroundRenderer != null) {
            backgroundRenderer.render(spriteBatch, camera, viewProjectionMatrix);
        }
    }
    
    /**
     * Renders foreground - OpenGL service method called by GameController.
     */
    public void renderForeground() {
        // SimpleForeground rendering not implemented yet - most levels don't use foreground elements
        // This maintains the same rendering order as the original JavaFX version
    }
    
    /**
     * Renders UI elements - OpenGL service method called by GameController.
     */
    public void renderUI() {
        if (uiRenderer == null) return;
        
        // Render UI with screen-space projection (no camera transform)
        uiRenderer.render(spriteBatch, projectionMatrix, screenWidth, screenHeight, camera);
    }
    

    
    /**
     * Sets whether UI rendering is enabled.
     * 
     * @param enabled true to enable UI rendering, false to disable
     */
    public void setUIRenderingEnabled(boolean enabled) {
        if (uiRenderer != null) {
            uiRenderer.setRenderingEnabled(enabled);
        }
    }
    

    

    
    /**
     * Updates FPS calculation - exact same logic as original.
     */
    private void updateFPS() {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastFpsTime >= 1000) { // Update every second
            frameCount = 0;
            lastFpsTime = currentTime;
        }
    }
    
    /**
     * Gets the sprite batch for advanced rendering operations.
     */
    public GLSpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
    
    /**
     * Gets the texture manager for loading/managing textures.
     */
    public GLTextureManager getTextureManager() {
        return textureManager;
    }
    
    /**
     * Gets the UI renderer for UI operations.
     */
    public GLUIRenderer getUIRenderer() {
        return uiRenderer;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }
}