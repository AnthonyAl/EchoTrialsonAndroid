package com.unipi.alexandris.android.echotrialsonandroid.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.unipi.alexandris.android.echotrialsonandroid.controller.GameController;
import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.data.GameStateSnapshot;
import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.utility.GameLoop;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.SoundFXManager;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLGameSurfaceView;
import com.unipi.alexandris.android.echotrialsonandroid.utility.SessionManager;
import com.unipi.alexandris.android.echotrialsonandroid.cosmetics.CosmeticSoundManager;

/**
 * Main activity that hosts the game view.
 */
public class GameComponent extends FrameLayout {
    private static final String TAG = "GameComponent";
    GLGameSurfaceView glGameSurfaceView;
    GameLoop gameLoop;
    boolean isGameRunning = true;
    boolean isLevelCustom;
    boolean isPrivateLevel;
    boolean isLevelVerified;
    
    // Game restart functionality
    private GameStateSnapshot gameStateSnapshot = null;
    private boolean snapshotCreated = false;

    public GameComponent(@NonNull Context context) {
        super(context);
    }

    public GameComponent(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GameComponent(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void initiate(boolean isLevelCustom, boolean isPrivateLevel, boolean isLevelVerified) {
        this.isPrivateLevel = isPrivateLevel;
        this.isLevelVerified = isLevelVerified;
        this.isLevelCustom = isLevelCustom;


        try {

            // Clear collision regions
            GameConstants.INSTANCE.setContext(getContext());
            
            // Initialize cosmetic system with enum singleton SessionManager - use singleton sound manager
            SessionManager.INSTANCE.initialize(getContext());
            GameConstants.INSTANCE.initializeSoundManagerIfNeeded(getContext());
            
            GameConstants.INSTANCE.resetGlobalArea();

            // Get screen dimensions and density for proper scaling
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            float density = metrics.density;
            int screenWidth = (int)(metrics.widthPixels / density);
            int screenHeight = (int)(metrics.heightPixels / density);

            // Initialize camera with screen dimensions and proper density scaling
            Camera camera = new Camera(0, 100*48, screenWidth, screenHeight, density); // Keep density for proper physics scaling

            // Set screen dimensions BEFORE loading level (so camera positioning works correctly)
            GameController.GAME.setScreenDimensions(screenWidth, screenHeight);

            GameController.GAME.loadCurrent(getContext(), camera);

            // Create OpenGL surface view with the same components as Canvas version
            glGameSurfaceView = new GLGameSurfaceView(getContext(), camera);
            // Add game view as background (first child)
            addView(glGameSurfaceView, 0);
            
            // Start game loop in separate thread (not on UI thread)
            isGameRunning = true;
            gameLoop = new GameLoop(60, () -> {
                try {
                    // Create snapshot on first frame if not already created
                    if (!snapshotCreated) {
                        createGameStateSnapshot();
                    }
                    
                    // Only tick if game is not paused and we have a valid game state
                    if (!GameConstants.INSTANCE.isGamePaused() && GameController.GAME.getCamera() != null) {
                        GameController.GAME.tick();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in game tick: " + e.getMessage());
                }
                
                // Request render update after each tick for efficient 60fps rendering
                if (glGameSurfaceView != null) {
                    glGameSurfaceView.requestRenderUpdate();
                }
            }); // run tick() at fixed 60 FPS
            gameLoop.start();

        } catch (Exception e) {
            Log.e(TAG, "Error while initializing Game: " + e.getMessage());
            throw e;
        }
    }

    private void stopGameLoop() {
        isGameRunning = false;

        if (gameLoop != null) {
            gameLoop.stopLoop();
        }
        gameLoop = null;
    }

    protected void pause() {
        // Stop the game loop when activity is paused
        stopGameLoop();
    }

    protected void resume() {
        // Only restart game loop if it's not already running
        if (isGameRunning && (gameLoop == null || gameLoop.isStopped())) {
            gameLoop = new GameLoop(60, () -> {
                try {
                    // Only tick if game is not paused and we have a valid game state
                    if (!GameConstants.INSTANCE.isGamePaused() && GameController.GAME.getCamera() != null) {
                        GameController.GAME.tick();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in game tick: " + e.getMessage());
                }
                
                // Request render update after each tick for efficient 60fps rendering
                if (glGameSurfaceView != null) {
                    glGameSurfaceView.requestRenderUpdate();
                }
            });
            gameLoop.start();
        }
    }

    /**
     * Creates a snapshot of the current game state for restart functionality
     */
    private void createGameStateSnapshot() {
        try {
            ObjectHandler handler = GameConstants.INSTANCE.getObjectHandler();
            if (handler != null && handler.getTotalObjectCount() > 0) {
                gameStateSnapshot = new GameStateSnapshot(handler);
                snapshotCreated = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating game state snapshot: " + e.getMessage());
        }
    }
    
    /**
     * Restarts the game using the stored snapshot
     */
    public void restartGame() {
        if (gameStateSnapshot == null) {
            Log.w(TAG, "No game state snapshot available for restart");
            return;
        }
        
        try {
            // Pause the game during restart
            GameConstants.INSTANCE.setGamePaused(true);
            
            // Restore the game state from snapshot
            ObjectHandler handler = GameConstants.INSTANCE.getObjectHandler();
            if (handler != null) {
                gameStateSnapshot.restore(handler);
            } else {
                Log.e(TAG, "ObjectHandler is null during restart");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error during game restart: " + e.getMessage());
        }
    }
    
    /**
     * Gets the game state snapshot (for external access)
     */
    public GameStateSnapshot getGameStateSnapshot() {
        return gameStateSnapshot;
    }
    
    /**
     * Checks if a snapshot has been created
     */
    public boolean hasSnapshot() {
        return snapshotCreated && gameStateSnapshot != null;
    }
    
    /**
     * Gets the GL game surface view for external access to touch controls
     */
    public GLGameSurfaceView getGLGameSurfaceView() {
        return glGameSurfaceView;
    }
    
    public void cleanup() {
        // Stop the game loop
        stopGameLoop();

        // Clean up GL surface view
        if (glGameSurfaceView != null) {
            glGameSurfaceView.onPause();
            glGameSurfaceView = null;
        }

        // Clean up game controller
        GameController.GAME.cleanup();

        // Reset GameConstants state
        GameConstants.INSTANCE.resetHandler();
        GameConstants.INSTANCE.resetGlobalArea();
        
        // Note: Sound manager persists across activities - don't release it here

        // Force garbage collection to clean up GL resources
        System.gc();
    }
} 