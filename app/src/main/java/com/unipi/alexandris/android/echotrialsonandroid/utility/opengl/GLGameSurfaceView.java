package com.unipi.alexandris.android.echotrialsonandroid.utility.opengl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;

import java.util.Arrays;

/**
 * OpenGL ES implementation of GameSurfaceView.
 * Maintains the same API as the original JavaFX rendering patterns.
 * Provides immediate testability while offering OpenGL ES performance benefits.
 */
public class GLGameSurfaceView extends GLSurfaceView {
    
    private final GLGameRenderer renderer;
    private final Camera camera;
    
    // Touch tracking - exact same as original GameSurfaceView
    private final int[] activeTouches = new int[10];
    private static final int TOUCH_NONE = -1;
    


    /**
     * Main constructor for OpenGL game surface.
     * Maintains exact same signature as original GameSurfaceView.
     */
    public GLGameSurfaceView(Context context, Camera camera) {
        this(context, camera, true); // Default: enable controls
    }
    
    /**
     * Constructor for OpenGL game surface with optional control enabling.
     * 
     * @param context The Android context
     * @param camera The camera instance
     * @param enableControls Whether to enable UI controls (default: true)
     */
    public GLGameSurfaceView(Context context, Camera camera, boolean enableControls) {
        super(context);
        this.camera = camera;
        
        // Initialize touch tracking (same as original)
        Arrays.fill(activeTouches, TOUCH_NONE);
        
        // Configure OpenGL ES 2.0
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
        
        // Create renderer with control enabling option
        renderer = new GLGameRenderer(context, camera, enableControls);
        setRenderer(renderer);
        
        // Set render mode - use when dirty for efficient 60fps rendering
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
        setFocusable(true);
    }

    /**
     * Constructor for XML inflation (same as original).
     */
    public GLGameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.camera = null;
        this.renderer = null;
        
        // Initialize touch tracking
        Arrays.fill(activeTouches, TOUCH_NONE);
        
        setFocusable(true);
    }
    
    /**
     * Touch event handling - EXACT same logic as original GameSurfaceView.
     * Maintains same multi-touch support and input handling.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Get touch position
        float x = event.getX();
        float y = event.getY();
        
        // Track pointer for multi-touch (same as original)
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        int actionMasked = event.getActionMasked();
        
        // Process touch event (exact same logic as original)
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (pointerId < activeTouches.length) {
                    activeTouches[pointerId] = 1;
                }
                
                // Use OpenGL touch control renderer
                if (renderer != null && renderer.getUIRenderer() != null) {
                    GLTouchControlRenderer glTouchRenderer = renderer.getUIRenderer().getTouchControlRenderer();
                    if (glTouchRenderer != null) {
                        boolean handled = glTouchRenderer.processTouchInput(x, y, true, pointerId);
                        if (handled) {
                            return true;
                        }
                    }
                }
                break;
                
            case MotionEvent.ACTION_MOVE:
                // Process all active pointers (same as original)
                for (int i = 0; i < event.getPointerCount(); i++) {
                    int id = event.getPointerId(i);
                    if (id < activeTouches.length && activeTouches[id] != TOUCH_NONE) {
                        float touchX = event.getX(i);
                        float touchY = event.getY(i);
                        
                        // Use OpenGL touch control renderer for move events
                        if (renderer != null && renderer.getUIRenderer() != null) {
                            GLTouchControlRenderer glTouchRenderer = renderer.getUIRenderer().getTouchControlRenderer();
                            if (glTouchRenderer != null) {
                                glTouchRenderer.processTouchMove(touchX, touchY, id);
                            }
                        }
                    }
                }
                break;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (pointerId < activeTouches.length) {
                    activeTouches[pointerId] = TOUCH_NONE;
                }
                
                // Use OpenGL touch control renderer
                if (renderer != null && renderer.getUIRenderer() != null) {
                    GLTouchControlRenderer glTouchRenderer = renderer.getUIRenderer().getTouchControlRenderer();
                    if (glTouchRenderer != null) {
                        boolean handled = glTouchRenderer.processTouchInput(x, y, false, pointerId);
                        if (handled) {
                            return true;
                        }
                    }
                }
                break;
                
            case MotionEvent.ACTION_CANCEL:
                // Clear all touch tracking (same as original)
                Arrays.fill(activeTouches, TOUCH_NONE);

                // Use OpenGL touch control renderer
                if (renderer != null && renderer.getUIRenderer() != null) {
                    GLTouchControlRenderer glTouchRenderer = renderer.getUIRenderer().getTouchControlRenderer();
                    if (glTouchRenderer != null) {
                        glTouchRenderer.releaseAllControls();
                    }
                }
                break;
        }
        
        return true;
    }

    public Camera getCamera() { return camera; }
    
    /**
     * Gets the OpenGL renderer for advanced operations.
     */
    public GLGameRenderer getGLRenderer() { return renderer; }
    
    /**
     * Requests a render update - called from game loop to trigger rendering.
     */
    public void requestRenderUpdate() {
        requestRender();
    }
} 