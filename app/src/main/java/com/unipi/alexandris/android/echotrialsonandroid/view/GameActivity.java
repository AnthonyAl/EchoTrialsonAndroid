package com.unipi.alexandris.android.echotrialsonandroid.view;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.unipi.alexandris.android.echotrialsonandroid.controller.GameController;
import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;
import com.unipi.alexandris.android.echotrialsonandroid.utility.GameLoop;
import com.unipi.alexandris.android.echotrialsonandroid.utility.LevelID;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.SoundFXManager;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLGameSurfaceView;
import com.unipi.alexandris.android.echotrialsonandroid.utility.SessionManager;
import com.unipi.alexandris.android.echotrialsonandroid.cosmetics.CosmeticSoundManager;

/**
 * Main activity that hosts the game view.
 */
public class GameActivity extends Activity {
    private static final String TAG = "GameActivity";
    GLGameSurfaceView glGameSurfaceView;
    GameLoop gameLoop;
    boolean isGameRunning = true;
    LevelID levelID;
    boolean isLevelCustom;
    boolean isPrivateLevel;
    boolean isLevelVerified;

    private boolean verifyIntegrity() {
        String levelUid = getIntent().getStringExtra("level_uid");
        if(levelUid == null) {
            Log.e(TAG, "Missing required level data from intent");
            return false;
        }
        
        // Check if GameController has been initialized with the correct level
        String currentLevel = GameController.GAME.getCurrentLevel();
        if(!levelUid.equals(currentLevel)) {
            Log.e(TAG, "GameController not properly initialized with level: " + levelUid);
            return false;
        }

        // Determine if this is a built-in level or custom level
        levelID = LevelID.getByName(levelUid);
        if (levelID != null) {
            isLevelCustom = false; // Built-in level
        } else {
            isLevelCustom = true; // Custom level
            if(GameConstants.INSTANCE.getCurrentUserUid() == null) {
                Log.e(TAG, "Custom levels cannot be played in Offline mode");
                return false;
            }
            isPrivateLevel = getIntent().getBooleanExtra("is_private_level", true);
            isLevelVerified = getIntent().getBooleanExtra("is_level_verified", false);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        try {
            // Get intent extras, and VERIFY Game Instance integrity:
            if(!verifyIntegrity()) {
                finish();
                return;
            }

            // Clear collision regions - use singleton sound manager
            SessionManager.INSTANCE.initialize(this);
            GameConstants.INSTANCE.initializeSoundManagerIfNeeded(this);
            
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

            GameController.GAME.loadCurrent(this, camera);

            // Create OpenGL surface view with the same components as Canvas version
            glGameSurfaceView = new GLGameSurfaceView(this, camera);
            setContentView(glGameSurfaceView);
            
            // Start game loop in separate thread (not on UI thread)
            isGameRunning = true;
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
            }); // run tick() at fixed 60 FPS
            gameLoop.start();

        } catch (Exception e) {
            Log.e(TAG, "Error while initializing Game: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Removed gameThread cleanup as GameThread is removed
        cleanup();
    }

    private void stopGameLoop() {
        isGameRunning = false;

        if (gameLoop != null) {
            gameLoop.stopLoop();
        }
        gameLoop = null;
    }
    
    @Override
    protected void onPause() {
        super.onPause();

        // Stop the game loop when activity is paused
        stopGameLoop();
    }
    
    @Override
    protected void onResume() {
        super.onResume();

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

    private void cleanup() {
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