package com.unipi.alexandris.android.echotrialsonandroid.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.unipi.alexandris.android.echotrialsonandroid.controller.GameController;
import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.model.Player;
import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;
import com.unipi.alexandris.android.echotrialsonandroid.utility.GameLoop;
import com.unipi.alexandris.android.echotrialsonandroid.utility.LevelID;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.SoundFXManager;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLGameSurfaceView;
import com.unipi.alexandris.android.echotrialsonandroid.ai.ObstacleDetector;
import com.unipi.alexandris.android.echotrialsonandroid.data.DeathCause;
import com.unipi.alexandris.android.echotrialsonandroid.utility.TickRunnable;
import com.unipi.alexandris.android.echotrialsonandroid.utility.SessionManager;
import com.unipi.alexandris.android.echotrialsonandroid.cosmetics.CosmeticSoundManager;
import com.unipi.alexandris.android.echotrialsonandroid.cosmetics.EquippedCosmetics;

import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.concurrent.ThreadLocalRandom;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Custom ViewGroup component that provides a game background with OpenGL rendering.
 * This component encapsulates a complete game environment that can be used as a background
 * in any Activity, with optional touch interaction capabilities.
 * <br>
 * Features:
 * - OpenGL game rendering with static camera (always static)
 * - Touch interaction with game objects (block deletion only)
 * - Configurable camera behavior (static/following)
 * - Background game loop with physics simulation
 * - Overlay UI support
 * - Game UI hidden for background use
 * - Player touch controls disabled
 */
public class GameBackgroundComponent extends FrameLayout {
    private static final String TAG = "GameBackgroundComponent";
    private GLGameSurfaceView gameView;
    private GameLoop gameLoop;
    private Camera camera;
    private GameController gameController;
    private boolean isInitialized = false;
    private boolean isInitializing = false; // Prevent multiple simultaneous initializations
    private final boolean touchInteractionEnabled = true;
    private boolean isGameRunning = false;
    private View loadingOverlay;
    private ObstacleDetector obstacleDetector;
    
    // Built-in state tracking
    private boolean isPaused = false;
    boolean deathHandled = false;
    private TickRunnable currentRespawnTask = null;
    private int counter = 0;
    private double prevX, prevY;
    
    // Enhanced touch interaction callback
    public interface TouchInteractionCallback {
        void onGameAreaTouched(float x, float y);
        void onPauseStateChanged(boolean isPaused);
    }
    
    private TouchInteractionCallback touchCallback;
    
    public GameBackgroundComponent(Context context) {
        super(context);
        initialize();
    }
    
    public GameBackgroundComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }
    
    public GameBackgroundComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }
    
    private void initialize() {
        // Initialize game resources - use singleton sound manager
        SessionManager.INSTANCE.initialize(getContext());
        GameConstants.INSTANCE.initializeSoundManagerIfNeeded(getContext());
        
        GameConstants.INSTANCE.resetGlobalArea();
    }

    public void initiateGame(LevelID... levelIDs) {
        // Prevent multiple simultaneous initializations
        if (isInitializing) {
            return;
        }
        isInitializing = true;
        // Ensure proper cleanup before re-initialization
        if (isInitialized) {
            cleanup();
        }

        LevelID id = LevelID.LEVEL_DEBUG_I;
        if(levelIDs.length > 0) {
            int choice = ThreadLocalRandom.current().nextInt(0, levelIDs.length);
            id = levelIDs[choice];
        }

        try {
            InputStream inputStream = getContext().getResources().openRawResource(id.resourceId);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            inputStream.close();

            // Get screen dimensions and density
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            float density = metrics.density;
            int screenWidth = (int)(metrics.widthPixels / density);
            int screenHeight = (int)(metrics.heightPixels / density);

            // Initialize camera (always static for background)
            camera = new Camera(0, 100*48, screenWidth, screenHeight, density);

            // Initialize game controller
            gameController = GameController.GAME;

            // Ensure clean state before initialization
            gameController.cleanup();

            gameController.setScreenDimensions(screenWidth, screenHeight);

            gameController.initialize(document, id.name());
            gameController.loadCurrent(getContext(), camera);

            // Create OpenGL game view with controls disabled for background mode
            gameView = new GLGameSurfaceView(getContext(), camera, false);

            // Set up player interaction touch controls
            setupPlayerInteractionTouchControls();

            // Add game view as background (first child)
            addView(gameView, 0);

            deathHandled = false;
            // Start game loop
            startGameLoop();

            // Initialize obstacle detector if player exists
            if (GameConstants.INSTANCE.getPlayer() != null) {
                obstacleDetector = new ObstacleDetector(
                        GameConstants.INSTANCE.getPlayer(),
                        GameConstants.INSTANCE.getObjectHandler()
                );
                GameConstants.INSTANCE.getPlayer().setPlaySounds(false);
                GameConstants.INSTANCE.getPlayer().setRestartOnDeath(false);
                
                // Enable background mode for random cosmetic selection
                EquippedCosmetics.INSTANCE.setBackgroundMode(true);
            }

            isInitialized = true;
            isInitializing = false; // Reset initialization flag

        } catch (Exception e) {
            Log.e(TAG, "Error identifying LevelID: " + e.getMessage());
        }
    }
    
    /**
     * Sets up touch controls for player interaction - kill player on tap.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupPlayerInteractionTouchControls() {
        gameView.setOnTouchListener((v, event) -> {
            try {
                // Get touch coordinates in screen space
                float touchX = event.getX();
                float touchY = event.getY();

                // Convert screen coordinates to world coordinates
                if (camera != null) {
                    float cameraX = (float) camera.getX();
                    float cameraY = (float) camera.getY();
                    float worldX = touchX + cameraX;
                    float worldY = touchY + cameraY;

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // Check if player was tapped and kill them
                            if (touchInteractionEnabled && checkPlayerTap(worldX, worldY)) {
                                killPlayer();
                            } else {
                                if (touchCallback != null) {
                                    touchCallback.onGameAreaTouched(worldX, worldY);
                                }
                            }
                            break;

                        case MotionEvent.ACTION_MOVE:
                            // No action on move for player killing
                            break;

                        case MotionEvent.ACTION_UP:
                            // No action on up for player killing
                            break;
                    }
                }

                // Consume all touch events to prevent player movement
                return true;
            } catch (Exception ignored) {return false;}
        });
    }

    public Camera getCamera() {
        return camera;
    }

    public GameController getGameController() {
        return gameController;
    }

    public GLGameSurfaceView getGameView() {
        return gameView;
    }

    private void startGameLoop() {
        if (gameLoop != null && gameLoop.isAlive()) {
            return;
        }

        isGameRunning = true;
        gameLoop = new GameLoop(30, this::tickBackgroundGame); // run tick() at fixed 60 FPS
        gameLoop.start();
    }

    private void tickBackgroundGame() {
        // Skip updates if paused
        if (GameConstants.INSTANCE.isGamePaused()) return;
        
        // Update game objects (physics simulation)
        GameConstants.INSTANCE.getObjectHandler().tick();
        
        // Keep camera static at spawn point (no following player)
        if (GameConstants.INSTANCE.getPlayer() != null && camera != null) {
            setupCameraForLevel();

            // Update obstacle detector for smart movement
            if (obstacleDetector != null) {
                obstacleDetector.update();
            }

            checkPlayerDeath(GameConstants.INSTANCE.getPlayer());

            if(counter == 0) {
                prevX = GameConstants.INSTANCE.getPlayer().getX();
                prevY = GameConstants.INSTANCE.getPlayer().getY();
            }
            if(++counter >= 30) {
                if(prevX == GameConstants.INSTANCE.getPlayer().getX() &&
                        prevY == GameConstants.INSTANCE.getPlayer().getY())
                    GameConstants.INSTANCE.getPlayer().takeDamage(DeathCause.SUFFOCATION);
                counter = 0;
            }
        }



        // Request render update after each tick for efficient 60fps rendering
        if (gameView != null) {
            gameView.requestRenderUpdate();
        }
    }

    private void setupCameraForLevel() {
        if (camera == null) return;
        
        // Get spawn coordinates and screen dimensions
        int spawnX = GameConstants.INSTANCE.getSpawnX();
        int spawnY = GameConstants.INSTANCE.getSpawnY();
        int screenHeight = GameConstants.INSTANCE.getScreenHeight();
        
        // Position camera at spawn point (static positioning)
        camera.setX(spawnX);
        camera.setY(spawnY - screenHeight * 0.6);
    }

    public LevelID getCurrentLevel() {
        return GameConstants.INSTANCE.getCurrentLevel();
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    private void initiateRandomPlayerValues(Player player) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        player.setSpeedX(random.nextInt(2, 30));
        player.setSpeedY(random.nextInt(5, 25));
        player.setWidthMultiplier(random.nextDouble(0.3, 1.3));
        player.setHeightMultiplier(random.nextDouble(0.3, 2));
        player.setCancelRIGHT(false);
        player.setCancelUP(false);
        player.setPlaySounds(false);
        deathHandled = false;
    }

    private void checkPlayerDeath(Player player) {
        // TODO: counter for achievements -- implement the achievements system
        if(player.isDead() && !deathHandled) {
            deathHandled = true;
            player.setSpeedX(0);
            player.setSpeedY(0);
            
            // Use ObjectHandler's TickRunnable system instead of postDelayed for better thread safety
            // 2500ms ≈ 150 ticks at 60 FPS (2500/16.67)
            
            // Cancel any existing respawn task
            if (currentRespawnTask != null) {
                GameConstants.INSTANCE.getObjectHandler().removeTask(currentRespawnTask);
            }
            
            currentRespawnTask = new TickRunnable(
                () -> {
                    initiateRandomPlayerValues(player);
                    currentRespawnTask = null; // Clear reference after execution
                },
                80, // 150 ticks delay (≈2500ms at 60 FPS)
                0,   // No period (one-time task)
                1,   // Execute once
                "PlayerRespawnTask"
            );
            GameConstants.INSTANCE.getObjectHandler().runRepeating(currentRespawnTask);
        }
    }

    public void stopGameLoop() {
        isGameRunning = false;

        if (gameLoop != null) {
            gameLoop.stopLoop(); // This now waits for thread completion
        }
        gameLoop = null;
    }

    public void pauseGame() {
        isPaused = true;
        GameConstants.INSTANCE.setGamePaused(true);
        if (touchCallback != null) {
            touchCallback.onPauseStateChanged(isPaused);
        }

        if (gameLoop != null) {
            gameLoop.stopLoop();
        }
    }

    public void resumeGame() {
        isPaused = false;
        GameConstants.INSTANCE.setGamePaused(false);
        if (touchCallback != null) {
            touchCallback.onPauseStateChanged(false);
        }

        // Only start if not already running
        if (!isGameRunning || gameLoop == null || gameLoop.isStopped()) {
            isGameRunning = true;
            gameLoop = new GameLoop(30, this::tickBackgroundGame); // run tick() at fixed 30 FPS for background
            gameLoop.start();
        }
    }

    public void togglePause() {
        if (isPaused) {
            resumeGame();
        } else {
            pauseGame();
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    private boolean checkPlayerTap(float worldX, float worldY) {
        Player player = GameConstants.INSTANCE.getPlayer();
        if (player == null || player.isDead()) {
            return false;
        }
        
        // Check if touch is within the player bounds
        return worldX >= player.getX() && worldX <= player.getX() + player.getWidth() &&
               worldY >= player.getY() && worldY <= player.getY() + player.getHeight();
    }

    private void killPlayer() {
        Player player = GameConstants.INSTANCE.getPlayer();
        if (player != null && !player.isDead()) {
            // Kill the player with FALL death cause (no particles, just death)
            player.takeDamage(DeathCause.TAP, true);
            
            // Notify callback if available
            if (touchCallback != null) {
                touchCallback.onGameAreaTouched((float)player.getX(), (float)player.getY());
            }
        }
    }

    public void cleanup() {
        // Stop the game loop first and wait for completion
        stopGameLoop();

        // Clean up the game view and GL context
        if (gameView != null) {
            // Pause the GL surface view to stop rendering
            gameView.onPause();
            // Remove from parent to trigger GL context cleanup
            removeView(gameView);
            gameView = null;
        }

        // Clean up game controller (this clears objects and tasks)
        if (gameController != null) {
            gameController.cleanup();
            gameController = null;
        }

        // Reset GameConstants state properly
        GameConstants.INSTANCE.resetHandler();
        GameConstants.INSTANCE.resetGlobalArea();
        
        // Note: Sound manager persists across activities - don't release it here

        // Cancel any scheduled tasks
        if (currentRespawnTask != null) {
            if (GameConstants.INSTANCE.getObjectHandler() != null) {
                GameConstants.INSTANCE.getObjectHandler().removeTask(currentRespawnTask);
            }
            currentRespawnTask = null;
        }
        
        // Additional safety cleanup - clear any remaining tasks
        if (GameConstants.INSTANCE.getObjectHandler() != null) {
            GameConstants.INSTANCE.getObjectHandler().clearAllTasks();
        }
        
        // Clean up obstacle detector
        if (obstacleDetector != null) {
            obstacleDetector.cleanup();
            obstacleDetector = null;
        }
        
        // Disable background mode
        EquippedCosmetics.INSTANCE.setBackgroundMode(false);
        
        // Reset internal state
        camera = null;
        isInitialized = false;
        isInitializing = false; // Reset initialization flag
        isPaused = false;
        isGameRunning = false;
        deathHandled = false;
        
        // Force garbage collection to clean up GL resources
        System.gc();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cleanup();
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Re-initialize if needed
    }
}
