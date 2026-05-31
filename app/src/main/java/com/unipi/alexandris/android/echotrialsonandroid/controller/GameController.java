package com.unipi.alexandris.android.echotrialsonandroid.controller;

import android.content.Context;

import com.unipi.alexandris.android.echotrialsonandroid.model.Player;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLGameRenderer;
import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;
import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;

import org.w3c.dom.Document;

/**
 * Main game controller that manages the game state and resources.
 */
public enum GameController {
    GAME;
    private LevelLoader levelLoader;
    private Camera camera;
    private Document levelData;
    private String levelName;

    public void initialize(Document levelData, String levelName) {
        GameConstants.INSTANCE.resetHandler();
        GameConstants.INSTANCE.initializeObjectHandler();
        this.levelData = levelData;
        this.levelName = levelName;
    }

    public void loadCurrent(Context context, Camera camera) {
        loadCurrent(context, camera, true);
    }

    public void loadCurrent(Context context, Camera camera, boolean enableBlockUnification) {
        GameConstants.INSTANCE.setContext(context);
        this.camera = camera;
        levelLoader = new LevelLoader(camera);
        levelLoader.setEnableBlockUnification(enableBlockUnification);

        levelLoader.loadLevel(levelData, levelName);
    }

    public void setScreenDimensions(int width, int height) {
        GameConstants.INSTANCE.setScreenWidth(width);
        GameConstants.INSTANCE.setScreenHeight(height);
    }

    public void tick() {
        
        // Skip updates if paused
        if (GameConstants.INSTANCE.isGamePaused()) return;
        
        // Update game objects
        GameConstants.INSTANCE.getObjectHandler().tick();
        
        // Update camera to follow player
        if (GameConstants.INSTANCE.getPlayer() != null && camera != null) {
            camera.tick(GameConstants.INSTANCE.getPlayer());
        }
    }
    
    /**
     * Renders the game using OpenGL.
     * Maintains same architecture and coordination as Canvas version.
     * Takes full control of rendering coordination, using GLGameRenderer as a service.
     * 
     * @param renderer The OpenGL renderer service
     */
    public void renderGL(GLGameRenderer renderer) {
        if (renderer == null) return;
        
        // Coordinate rendering using renderer as a service (same order as Canvas version)
        // This maintains GameController's coordination role while using OpenGL
        
        // 1. Clear screen and setup camera (use GameController's camera, not renderer's)
        renderer.clearScreen();
        renderer.setupCamera(camera);
        
        // 2. Render background
        renderer.renderBackground();
        
        // 3. Render game objects
        renderer.beginObjectBatch();
        GameConstants.INSTANCE.getObjectHandler().renderGL(renderer.getSpriteBatch(), camera);
        renderer.endObjectBatch();
        
        // 4. Render foreground
        renderer.renderForeground();
        
        // 5. Render UI
        renderer.renderUI();
    }
    


    public Camera getCamera() {
        return camera;
    }

    public Player getPlayer() {
        return GameConstants.INSTANCE.getPlayer();
    }

    public void cleanup() {
        // Clear all game objects and scheduled tasks
        if (GameConstants.INSTANCE.getObjectHandler() != null) {
            GameConstants.INSTANCE.getObjectHandler().clearObjects();
            GameConstants.INSTANCE.getObjectHandler().clearAllTasks();
        }
        
        // Reset game state completely
        GameConstants.INSTANCE.resetGlobalArea();
        GameConstants.INSTANCE.setPlayer(null);
        GameConstants.INSTANCE.setGamePaused(false);

        camera = null;
        levelLoader = null;
    }

    public String getCurrentLevel() {
        return levelName;
    }
} 