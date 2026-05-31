package com.unipi.alexandris.android.echotrialsonandroid.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.controller.GameController;
import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.data.PlayerStatistics;
import com.unipi.alexandris.android.echotrialsonandroid.utility.SessionManager;
import com.unipi.alexandris.android.echotrialsonandroid.levelcreator.LevelCreatorActivity;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.ButtonSoundHelper;
import com.unipi.alexandris.android.echotrialsonandroid.utility.DialogAnimationHelper;
import com.unipi.alexandris.android.echotrialsonandroid.utility.LevelID;
import com.unipi.alexandris.android.echotrialsonandroid.view.ChallengeTrialsActivity;

/**
 * Main activity that hosts the game view.
 */
public class GameHostActivity extends AppCompatActivity {
    private static final String TAG = "GameHostActivity";
    private GameComponent game;
    LevelID levelID;
    boolean isLevelCustom;
    boolean isPrivateLevel;
    boolean isLevelVerified;
    boolean isChallengeMode;
    
    // UI Components
    private ImageButton pauseButton;
    private FrameLayout backgroundEffect;
    private View trialOnPauseDialog;
    private View trialFailDialog;
    private View trialCompleteDialog;
    private ImageButton playButton1;
    private ImageButton backButton1;
    private ImageButton retryButton1;
    private ImageButton backButton2;
    private ImageButton retryButton2;
    private ImageButton backButton3;
    private ImageButton retryButton3;
    private TextView timeText3;
    private TextView globalTimerText;
    private View starsLayout;
    private TextView privateLevelVerifiedMessageText;
    private ImageView star1;
    private ImageView star2;
    private ImageView star3;
    
    // Game state tracking
    private boolean isDialogShowing = false;
    private int gameSeconds = 0; // Simple seconds counter
    private String currentLevelUid;
    private SessionManager sessionManager;
    private boolean isRestartInProgress = false; // Flag to prevent immediate completion after restart

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
        
        // Check if this is challenge mode
        isChallengeMode = getIntent().getBooleanExtra("is_challenge_mode", false);
        
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // Set the content view to the layout
        setContentView(R.layout.activity_game);

        try {
            // Get intent extras, and VERIFY Game Instance integrity:
            if (!verifyIntegrity()) {
                finish();
                return;
            }
            
            // Initialize game component
            game = findViewById(R.id.game);
            if (game == null) {
                Log.e(TAG, "Game component not found in layout");
                finish();
                return;
            }
            
            // Initialize game state
            currentLevelUid = getIntent().getStringExtra("level_uid");
            gameSeconds = 0; // Reset seconds counter
            
            // Initialize session manager
            SessionManager.INSTANCE.initialize(this);
            sessionManager = SessionManager.INSTANCE;

            // Store this activity reference for player death handling
            GameConstants.INSTANCE.setGameHostActivity(this);

            // Initialize the game component
            game.initiate(isLevelCustom, isPrivateLevel, isLevelVerified);

            // Initialize UI components
            initializeViews();
            
            // Set up button animations and click listeners
            setupButtonAnimations();

            // Start the timer update mechanism
            startTimerUpdate();

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
    
    @Override
    protected void onPause() {
        super.onPause();

        game.pause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        game.resume();
    }
    
    /**
     * Initializes all UI components
     */
    private void initializeViews() {
        pauseButton = findViewById(R.id.pauseButton);
        backgroundEffect = findViewById(R.id.backgroundEffect);
        trialOnPauseDialog = findViewById(R.id.trial_on_pause);
        trialFailDialog = findViewById(R.id.trial_fail);
        trialCompleteDialog = findViewById(R.id.trial_complete);
        playButton1 = findViewById(R.id.playButton1);
        backButton1 = findViewById(R.id.backButton1);
        retryButton1 = findViewById(R.id.retryButton1);
        backButton2 = findViewById(R.id.backButton2);
        retryButton2 = findViewById(R.id.retryButton2);
        backButton3 = findViewById(R.id.backButton3);
        retryButton3 = findViewById(R.id.retryButton3);
        timeText3 = findViewById(R.id.timeText3);
        globalTimerText = findViewById(R.id.globalTimerText);
        starsLayout = findViewById(R.id.starsLayout);
        privateLevelVerifiedMessageText = findViewById(R.id.privateLevelVerifiedMessageText);
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
    }
    
    /**
     * Sets up button animations and click listeners following the established pattern
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonAnimations() {
        // Get the shared button tap feedback listener
        View.OnTouchListener tapFeedback = LevelCreatorActivity.createButtonTapFeedback();

        // Apply tap feedback to pause button
        if (pauseButton != null) {
            pauseButton.setOnTouchListener(tapFeedback);
            ButtonSoundHelper.addClickSound(pauseButton, v -> handlePauseButton());
        }
        
        // Apply tap feedback to pause dialog buttons
        if (playButton1 != null) {
            playButton1.setOnTouchListener(tapFeedback);
            ButtonSoundHelper.addClickSound(playButton1, v -> handlePlayButton());
        }
        
        if (backButton1 != null) {
            backButton1.setOnTouchListener(tapFeedback);
            ButtonSoundHelper.addClickSound(backButton1, v -> handleBackButton());
        }
        
        if (retryButton1 != null) {
            retryButton1.setOnTouchListener(tapFeedback);
            ButtonSoundHelper.addClickSound(retryButton1, v -> handleRetryButton());
        }
        
        // Apply tap feedback to fail dialog buttons
        if (backButton2 != null) {
            backButton2.setOnTouchListener(tapFeedback);
            ButtonSoundHelper.addClickSound(backButton2, v -> handleBackButton2());
        }
        
        if (retryButton2 != null) {
            retryButton2.setOnTouchListener(tapFeedback);
            ButtonSoundHelper.addClickSound(retryButton2, v -> handleRetryButton2());
        }
        
        // Apply tap feedback to complete dialog buttons
        if (backButton3 != null) {
            backButton3.setOnTouchListener(tapFeedback);
            ButtonSoundHelper.addClickSound(backButton3, v -> handleBackButton3());
        }
        
        if (retryButton3 != null) {
            retryButton3.setOnTouchListener(tapFeedback);
            ButtonSoundHelper.addClickSound(retryButton3, v -> handleRetryButton3());
        }
    }
    
    /**
     * Handles pause button click
     */
    private void handlePauseButton() {
        showPauseDialog();
    }
    
    /**
     * Shows the pause dialog and pauses the game
     */
    private void showPauseDialog() {
        if (isDialogShowing) return;
        
        isDialogShowing = true;
        pauseButton.setVisibility(View.GONE);
        pauseButton.setEnabled(false);
        pauseButton.setClickable(false);
        pauseButton.setAlpha(0.0f); // Make it completely transparent
        pauseButton.setTranslationX(10000f); // Move it far off-screen
        
        backgroundEffect.setBackground(new ColorDrawable(0xCC000000)); // Darken background
        backgroundEffect.setClickable(false);
        backgroundEffect.setFocusable(false);
        backgroundEffect.invalidate();
        
        trialOnPauseDialog.setVisibility(View.VISIBLE);
        
        // Enable dialog buttons
        if (playButton1 != null) {
            playButton1.setEnabled(true);
            playButton1.setClickable(true);
        }
        if (backButton1 != null) {
            backButton1.setEnabled(true);
            backButton1.setClickable(true);
        }
        if (retryButton1 != null) {
            retryButton1.setEnabled(true);
            retryButton1.setClickable(true);
        }
        
        // Add smooth slide-in animation
        DialogAnimationHelper.addSlideInAnimation(trialOnPauseDialog);
        
        // Pause the game logic (keep game loop running)
        GameConstants.INSTANCE.setGamePaused(true);
    }
    
    /**
     * Hides the pause dialog and resumes the game
     */
    private void hidePauseDialog() {
        if (!isDialogShowing) return;
        
        isDialogShowing = false;
        pauseButton.setVisibility(View.VISIBLE);
        pauseButton.setEnabled(true);
        pauseButton.setClickable(true);
        pauseButton.setAlpha(1.0f); // Make it fully visible
        pauseButton.setTranslationX(0f); // Move it back to original position
        
        backgroundEffect.setBackground(new ColorDrawable(0x00000000)); // Clear background
        backgroundEffect.setClickable(false);
        backgroundEffect.setFocusable(false);
        backgroundEffect.invalidate();
        
        // Disable dialog buttons to prevent interference with game controls
        if (playButton1 != null) {
            playButton1.setEnabled(false);
            playButton1.setClickable(false);
        }
        if (backButton1 != null) {
            backButton1.setEnabled(false);
            backButton1.setClickable(false);
        }
        if (retryButton1 != null) {
            retryButton1.setEnabled(false);
            retryButton1.setClickable(false);
        }
        
        // Add smooth slide-out animation before hiding
        DialogAnimationHelper.addSlideOutAnimationAndHide(trialOnPauseDialog);
        
        // Resume the game logic
        GameConstants.INSTANCE.setGamePaused(false);
    }
    
    /**
     * Handles play button click in pause dialog
     */
    private void handlePlayButton() {
        hidePauseDialog();
    }
    
    /**
     * Handles back button click in pause dialog
     */
    private void handleBackButton() {
        if (isChallengeMode) {
            // Navigate back to Challenge Trials Activity for challenge levels
            Intent intent = new Intent(this, ChallengeTrialsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else {
            // Navigate to fresh LevelView activity instance for custom levels
            Intent intent = new Intent(this, CreatorLevelViewActivity.class);
            intent.putExtra("level_uid", currentLevelUid);
            intent.putExtra("is_private", isLevelCustom && isPrivateLevel);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finish();
    }
    
    /**
     * Handles retry button click in pause dialog
     */
    private void handleRetryButton() {
        // Hide the pause dialog first
        hidePauseDialog();
        
        // Reset game timer
        resetGameTimer();
        
        // Restart the game using the snapshot
        if (game != null && game.hasSnapshot()) {
            game.restartGame();
        } else {
            Log.w(TAG, "Cannot restart game - no snapshot available");
            // Fallback: finish the activity to restart the level
            finish();
        }
    }
    
    /**
     * Shows the fail dialog when the player dies
     */
    public void showFailDialog() {
        if (isDialogShowing) {
            return;
        }
        
        isDialogShowing = true;
        pauseButton.setVisibility(View.GONE);
        pauseButton.setEnabled(false);
        pauseButton.setClickable(false);
        pauseButton.setAlpha(0.0f);
        pauseButton.setTranslationX(10000f);
        
        backgroundEffect.setBackground(new ColorDrawable(0xCC000000)); // Darken background
        backgroundEffect.setClickable(false);
        backgroundEffect.setFocusable(false);
        backgroundEffect.invalidate();
        
        trialFailDialog.setVisibility(View.VISIBLE);
        
        // Enable dialog buttons
        if (backButton2 != null) {
            backButton2.setEnabled(true);
            backButton2.setClickable(true);
        }
        if (retryButton2 != null) {
            retryButton2.setEnabled(true);
            retryButton2.setClickable(true);
        }
        
        // Add smooth slide-in animation
        DialogAnimationHelper.addSlideInAnimation(trialFailDialog);
        
        // Game is already paused by the player death
    }
    
    /**
     * Hides the fail dialog
     */
    private void hideFailDialog() {
        if (!isDialogShowing) return;
        
        isDialogShowing = false;
        pauseButton.setVisibility(View.VISIBLE);
        pauseButton.setEnabled(true);
        pauseButton.setClickable(true);
        pauseButton.setAlpha(1.0f);
        pauseButton.setTranslationX(0f);
        
        backgroundEffect.setBackground(new ColorDrawable(0x00000000)); // Clear background
        backgroundEffect.setClickable(false);
        backgroundEffect.setFocusable(false);
        backgroundEffect.invalidate();
        
        // Disable dialog buttons to prevent interference with game controls
        if (backButton2 != null) {
            backButton2.setEnabled(false);
            backButton2.setClickable(false);
        }
        if (retryButton2 != null) {
            retryButton2.setEnabled(false);
            retryButton2.setClickable(false);
        }
        
        // Add smooth slide-out animation before hiding
        DialogAnimationHelper.addSlideOutAnimationAndHide(trialFailDialog);
    }
    
    /**
     * Handles back button click in fail dialog
     */
    private void handleBackButton2() {
        if (isChallengeMode) {
            // Navigate back to Challenge Trials Activity for challenge levels
            Intent intent = new Intent(this, ChallengeTrialsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else {
            // Navigate to fresh LevelView activity instance for custom levels
            Intent intent = new Intent(this, CreatorLevelViewActivity.class);
            intent.putExtra("level_uid", currentLevelUid);
            intent.putExtra("is_private", isLevelCustom && isPrivateLevel);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finish();
    }
    
    /**
     * Handles retry button click in fail dialog
     */
    private void handleRetryButton2() {
        // Hide the fail dialog first
        hideFailDialog();
        
        // Reset game timer
        resetGameTimer();
        
        // Restart the game using the snapshot
        if (game != null && game.hasSnapshot()) {
            game.restartGame();
        } else {
            Log.w(TAG, "Cannot restart game - no snapshot available");
            // Fallback: finish the activity to restart the level
            finish();
        }
    }
    
    /**
     * Shows the trial complete dialog when the player reaches the portal
     */
    public void showCompleteDialog() {
        if (isDialogShowing || isRestartInProgress) {
            Log.d(TAG, "Completion dialog blocked - isDialogShowing: " + isDialogShowing + ", isRestartInProgress: " + isRestartInProgress);
            return;
        }
        
        
        isDialogShowing = true;
        pauseButton.setVisibility(View.GONE);
        pauseButton.setEnabled(false);
        pauseButton.setClickable(false);
        pauseButton.setAlpha(0.0f);
        pauseButton.setTranslationX(10000f);
        
        backgroundEffect.setBackground(new ColorDrawable(0xCC000000)); // Darken background
        backgroundEffect.setClickable(false);
        backgroundEffect.setFocusable(false);
        backgroundEffect.invalidate();
        
        // Use the current global timer value
        if (timeText3 != null && globalTimerText != null) {
            timeText3.setText(globalTimerText.getText());
        }
        
        // Ensure gameSeconds is synchronized with the displayed timer
        // Parse the displayed time to get the actual seconds value
        if (globalTimerText != null) {
            String timeText = globalTimerText.getText().toString();
            try {
                // Parse time format like "0:30" or "1:45" to get total seconds
                if (timeText.contains(":")) {
                    String[] parts = timeText.split(":");
                    int minutes = Integer.parseInt(parts[0]);
                    int seconds = Integer.parseInt(parts[1]);
                    gameSeconds = minutes * 60 + seconds;
                } else {
                    // Fallback to current gameSeconds if parsing fails
                    Log.w(TAG, "Could not parse timer text: " + timeText);
                }
            } catch (Exception e) {
                Log.w(TAG, "Error parsing timer text: " + timeText, e);
                // Keep current gameSeconds value
            }
        }
        
        // Handle level completion based on type
        if (!isLevelCustom && levelID != null) {
            // Built-in level completion (like challenge levels)
            if (starsLayout != null) {
                starsLayout.setVisibility(View.VISIBLE);
            }
            if (privateLevelVerifiedMessageText != null) {
                privateLevelVerifiedMessageText.setVisibility(View.GONE);
            }
            
            // Update main level completion and show star animations
            updateMainLevelCompletionTime();
        } else if (isLevelCustom && isPrivateLevel) {
            // Private custom level completion
            if (starsLayout != null) {
                starsLayout.setVisibility(View.GONE);
            }
            if (privateLevelVerifiedMessageText != null) {
                privateLevelVerifiedMessageText.setVisibility(View.VISIBLE);
            }
            
            // Update Firestore with verification and target time
            updatePrivateLevelVerification();
        } else {
            // Public custom level completion
            if (starsLayout != null) {
                starsLayout.setVisibility(View.VISIBLE);
            }
            if (privateLevelVerifiedMessageText != null) {
                privateLevelVerifiedMessageText.setVisibility(View.GONE);
            }
            
            // Update best completion time for public levels and show star animations
            updatePublicLevelCompletionTime();
        }
        
        trialCompleteDialog.setVisibility(View.VISIBLE);
        
        // Enable dialog buttons
        if (backButton3 != null) {
            backButton3.setEnabled(true);
            backButton3.setClickable(true);
        }
        if (retryButton3 != null) {
            retryButton3.setEnabled(true);
            retryButton3.setClickable(true);
        }
        
        // Add smooth slide-in animation
        DialogAnimationHelper.addSlideInAnimation(trialCompleteDialog);
        
        // Pause the game
        GameConstants.INSTANCE.setGamePaused(true);
    }
    
    /**
     * Hides the trial complete dialog
     */
    private void hideCompleteDialog() {
        if (!isDialogShowing) return;
        
        isDialogShowing = false;
        pauseButton.setVisibility(View.VISIBLE);
        pauseButton.setEnabled(true);
        pauseButton.setClickable(true);
        pauseButton.setAlpha(1.0f);
        pauseButton.setTranslationX(0f);
        
        backgroundEffect.setBackground(new ColorDrawable(0x00000000)); // Clear background
        backgroundEffect.setClickable(false);
        backgroundEffect.setFocusable(false);
        backgroundEffect.invalidate();
        
        // Disable dialog buttons to prevent interference with game controls
        if (backButton3 != null) {
            backButton3.setEnabled(false);
            backButton3.setClickable(false);
        }
        if (retryButton3 != null) {
            retryButton3.setEnabled(false);
            retryButton3.setClickable(false);
        }
        
        // Add smooth slide-out animation before hiding
        DialogAnimationHelper.addSlideOutAnimationAndHide(trialCompleteDialog);
    }
    
    /**
     * Handles back button click in complete dialog
     */
    private void handleBackButton3() {
        if (isChallengeMode) {
            // Navigate back to Challenge Trials Activity for challenge levels
            Intent intent = new Intent(this, ChallengeTrialsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else {
            // Navigate to fresh LevelView activity instance for custom levels
            Intent intent = new Intent(this, CreatorLevelViewActivity.class);
            intent.putExtra("level_uid", currentLevelUid);
            intent.putExtra("is_private", isLevelCustom && isPrivateLevel);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finish();
    }
    
    /**
     * Handles retry button click in complete dialog
     */
    private void handleRetryButton3() {
        // Hide the complete dialog first
        hideCompleteDialog();
        
        // Reset all game state
        resetGameTimer();
        isDialogShowing = false; // Ensure dialog state is reset
        
        // Reset pause button state
        if (pauseButton != null) {
            pauseButton.setVisibility(View.VISIBLE);
            pauseButton.setEnabled(true);
            pauseButton.setClickable(true);
            pauseButton.setAlpha(1.0f);
            pauseButton.setTranslationX(0f);
        }
        
        // Clear background effect
        if (backgroundEffect != null) {
            backgroundEffect.setBackground(new ColorDrawable(0x00000000));
            backgroundEffect.setClickable(false);
            backgroundEffect.setFocusable(false);
        }
        
        // Set restart flag to prevent immediate completion
        isRestartInProgress = true;
        
        // Restart the game using the snapshot
        if (game != null && game.hasSnapshot()) {
            game.restartGame();
            
            // IMPORTANT: Unpause the game after restart and clear restart flag
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                GameConstants.INSTANCE.setGamePaused(false);
                Log.d(TAG, "Game unpaused after restart");
                
                // Clear restart flag after a short delay to allow player to move away from portal
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    isRestartInProgress = false;
                    Log.d(TAG, "Restart flag cleared - completion dialog can now be triggered");
                }, 1000); // 1 second delay to allow player movement
            }, 100); // Small delay to ensure restart is complete
        } else {
            Log.w(TAG, "Cannot restart game - no snapshot available");
            // Fallback: finish the activity to restart the level
            finish();
        }
    }
    
    /**
     * Resets the game timer and updates the global timer display
     */
    private void resetGameTimer() {
        gameSeconds = 0;
        updateGlobalTimer();
    }
    
    /**
     * Starts the timer update mechanism
     */
    private void startTimerUpdate() {
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                // Only increment timer if game is not paused
                if (!GameConstants.INSTANCE.isGamePaused()) {
                    gameSeconds++;
                    updateGlobalTimer();
                }
                handler.postDelayed(this, 1000); // Update every second
            }
        };
        handler.post(timerRunnable);
    }

    /**
     * Updates the global timer display with current elapsed time
     */
    private void updateGlobalTimer() {
        if (globalTimerText != null) {
            int seconds = gameSeconds % 60;
            int minutes = gameSeconds / 60;
            String timeString = String.format("TIME: %02d:%02d", minutes, seconds);
            globalTimerText.setText(timeString);
        }
    }
    
    /**
     * Updates Firestore with level verification and target time for private levels
     */
    private void updatePrivateLevelVerification() {
        if (currentLevelUid == null || GameConstants.INSTANCE.getCurrentUserUid() == null) {
            return;
        }
        
        try {
            // Get Firestore instance
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            
            // Prepare the update data
            java.util.Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("isVerified", true);
            updates.put("targetCompletionTime", gameSeconds); // Use the current seconds counter
            updates.put("lastVerified", new java.util.Date());
            
            // Update the level document
            String userId = GameConstants.INSTANCE.getCurrentUserUid();
            db.collection("users").document(userId)
                    .collection("levels").document(currentLevelUid)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        // Success - level is now verified
                
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        android.util.Log.e(TAG, "Failed to verify private level: " + e.getMessage());
                    });
                    
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error updating private level verification: " + e.getMessage());
        }
    }
    
    /**
     * Updates best completion time for public levels and player stats
     */
    private void updatePublicLevelCompletionTime() {
        if (currentLevelUid == null) {
            return;
        }
        
        try {
            // Get Firestore instance
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            
            // Update the public level document with new best time if it's better
            db.collection("public_levels").document(currentLevelUid)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            com.google.firebase.firestore.DocumentSnapshot doc = task.getResult();
                            Long currentBestTime = doc.getLong("bestCompletionTime");
                            Long targetTime = doc.getLong("targetCompletionTime");
                            String ownerUid = doc.getString("ownerUid");
                            
                            // Check if user owns this level (don't update stats for own levels)
                            String currentUserId = GameConstants.INSTANCE.getCurrentUserUid();
                            boolean userOwnsLevel = currentUserId != null && currentUserId.equals(ownerUid);
                            
                            // Update if current time is better or if no best time exists
                            if (currentBestTime == null || currentBestTime == -1 || gameSeconds < currentBestTime) {
                                java.util.Map<String, Object> updates = new java.util.HashMap<>();
                                updates.put("bestCompletionTime", gameSeconds);
                                updates.put("lastEdited", new java.util.Date());
                                
                                db.collection("public_levels").document(currentLevelUid)
                                        .update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                    
                                            
                                            // Update star display BEFORE updating stats (so we can detect first completion)
                                            updateStarDisplay(currentLevelUid, gameSeconds, targetTime != null ? targetTime : -1L, true);
                                            
                                            updatePlayerStatsForPublicLevel(currentLevelUid, gameSeconds, targetTime != null ? targetTime : -1L);
                                        })
                                        .addOnFailureListener(e -> {
                                            android.util.Log.e(TAG, "Failed to update best completion time: " + e.getMessage());
                                        });
                            } else {
                                // Even if no new best time, update player stats for completion
                                // Update star display BEFORE updating stats (so we can detect first completion)
                                updateStarDisplay(currentLevelUid, gameSeconds, targetTime != null ? targetTime : -1L, true);
                                
                                updatePlayerStatsForPublicLevel(currentLevelUid, gameSeconds, targetTime != null ? targetTime : -1L);
                            }
                        }
                    });
                    
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error updating public level completion time: " + e.getMessage());
        }
    }
    
    /**
     * Updates player stats for completing a public level
     */
    private void updatePlayerStatsForPublicLevel(String levelUid, long completionTime, long targetTime) {
        try {
            // Get player statistics
            com.unipi.alexandris.android.echotrialsonandroid.data.PlayerStatistics stats = sessionManager.getPlayerStatistics();
            if (stats == null) {
                String userId = GameConstants.INSTANCE.getCurrentUserUid();
                stats = new com.unipi.alexandris.android.echotrialsonandroid.data.PlayerStatistics(userId);
            }
            
            // Check if level was already completed
            boolean wasAlreadyCompleted = stats.hasCompletedCreatorLevel(levelUid);
            
            if (!wasAlreadyCompleted) {
                // First completion - add stars for this completion
                int starsForThisCompletion = calculateStarsForTime(completionTime, targetTime);
                stats.addStars(starsForThisCompletion);
                stats.setCreatorLevelCompletionTime(levelUid, completionTime);
                
    
            } else {
                // Subsequent completion - check if we earned more stars
                Long previousTime = stats.getCreatorLevelCompletionTime(levelUid);
                int previousStars = calculateStarsForTime(previousTime, targetTime);
                int currentStars = calculateStarsForTime(completionTime, targetTime);
                
                if (currentStars > previousStars) {
                    // Earned more stars - add the difference
                    int additionalStars = currentStars - previousStars;
                    stats.addStars(additionalStars);
                    stats.setCreatorLevelCompletionTime(levelUid, completionTime);
                    
    
                } else {
                    // No improvement - just update time if better
                    if (completionTime < previousTime) {
                        stats.setCreatorLevelCompletionTime(levelUid, completionTime);
    
                    } else {
    
                    }
                }
            }
            
            // Save stats (this automatically saves to file)
            sessionManager.updatePlayerStatistics(stats);
    
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating player stats for public level: " + e.getMessage());
        }
    }
    
    /**
     * Calculates stars based on completion time vs target time
     */
    private int calculateStarsForTime(long completionTime, long targetTime) {
        if (targetTime <= 0) return 1; // No target time set, default to 1 star
        
        if (completionTime <= targetTime) {
            return 3; // 3 stars if completed at or faster than target time
        } else if (completionTime <= targetTime * 1.5) {
            return 2; // 2 stars if completed within 1.5x target time
        } else {
            return 1; // 1 star if completed beyond 1.5x target time
        }
    }
    
    /**
     * Updates star display and animates newly earned stars
     */
    private void updateStarDisplay(String levelUid, long completionTime, long targetTime, boolean isPublicLevel) {
        try {
    
            
            // Get player statistics
            com.unipi.alexandris.android.echotrialsonandroid.data.PlayerStatistics stats = sessionManager.getPlayerStatistics();
            if (stats == null) {
                String userId = GameConstants.INSTANCE.getCurrentUserUid();
                stats = new com.unipi.alexandris.android.echotrialsonandroid.data.PlayerStatistics(userId);
            }
            
            // Check if level was already completed
            boolean wasAlreadyCompleted = stats.hasCompletedCreatorLevel(levelUid);
            
            if (!wasAlreadyCompleted) {
                // First completion: start with empty stars, then animate to current stars
                int currentStars = calculateStarsForTime(completionTime, targetTime);
                updateStarImages(0); // Start with empty stars
                animateNewStars(1, currentStars);
            } else {
                // Subsequent completion: show previous best as golden, animate only new stars
                Long previousTime = stats.getCreatorLevelCompletionTime(levelUid);
                int previousStars = calculateStarsForTime(previousTime, targetTime);
                int currentStars = calculateStarsForTime(completionTime, targetTime);
                
                // Show previous best stars as golden
                updateStarImages(previousStars);
                
                if (currentStars > previousStars) {
                    // New stars earned - animate only the additional ones
                    animateNewStars(previousStars + 1, currentStars);
                } else {
                    // No new stars earned - just show previous best
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating star display: " + e.getMessage());
        }
    }
    
    /**
     * Updates star images based on earned stars
     */
    private void updateStarImages(int earnedStars) {

        
        if (star1 != null) {
            star1.setImageResource(earnedStars >= 1 ? R.drawable.ui_star : R.drawable.ui_star_empty);
        }
        if (star2 != null) {
            star2.setImageResource(earnedStars >= 2 ? R.drawable.ui_star : R.drawable.ui_star_empty);
        }
        if (star3 != null) {
            star3.setImageResource(earnedStars >= 3 ? R.drawable.ui_star : R.drawable.ui_star_empty);
        }
    }
    
    /**
     * Animates newly earned stars sequentially
     */
    private void animateNewStars(int startStar, int endStar) {
        animateStarSequentially(startStar, endStar);
    }
    
    /**
     * Animates stars one by one, waiting for each to complete
     */
    private void animateStarSequentially(int currentStar, int endStar) {
        if (currentStar > endStar) {
            return; // All stars animated
        }
        
        ImageView starView = getStarView(currentStar);
        if (starView != null) {
            animateStarWithCallback(starView, () -> {
                // Animate next star after this one completes
                animateStarSequentially(currentStar + 1, endStar);
            });
        } else {
            // If this star view is null, try the next one
            animateStarSequentially(currentStar + 1, endStar);
        }
    }
    
    /**
     * Gets the ImageView for a specific star number
     */
    private ImageView getStarView(int starNumber) {
        switch (starNumber) {
            case 1: return star1;
            case 2: return star2;
            case 3: return star3;
            default: return null;
        }
    }
    
    /**
     * Animates a single star with rotationY
     */
    private void animateStar(ImageView starView) {
        animateStarWithCallback(starView, null);
    }
    
    /**
     * Animates a single star with rotationY and callback
     */
    private void animateStarWithCallback(ImageView starView, Runnable onComplete) {
        if (starView == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        
        // Start with empty star
        starView.setImageResource(R.drawable.ui_star_empty);
        
        // First rotation: 360 degrees (rotateY) - moderate speed
        android.animation.ObjectAnimator rotate1 = android.animation.ObjectAnimator.ofFloat(starView, "rotationY", 0f, 360f);
        rotate1.setDuration(400);
        
        // Second rotation: 1800 degrees (rotateY) - turn gold during this rotation - moderate speed
        android.animation.ObjectAnimator rotate2 = android.animation.ObjectAnimator.ofFloat(starView, "rotationY", 0f, 1800f);
        rotate2.setDuration(800);
        
        // Chain animations
        rotate1.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                rotate2.start();
            }
        });
        
        rotate2.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {
                // Turn star gold at the start of the second rotation
                starView.setImageResource(R.drawable.ui_star);
                
                // Play star collection sound when star turns gold
                GameConstants.INSTANCE.getSoundManager().playSound(com.unipi.alexandris.android.echotrialsonandroid.utility.audio.SoundID.STAR_COLLECTED);
            }
            
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (onComplete != null) onComplete.run();
            }
        });
        
        rotate1.start();
    }
    
    /**
     * Updates completion time for built-in levels (main levels with LevelID)
     */
    private void updateMainLevelCompletionTime() {
        if (levelID == null || sessionManager == null) {
            Log.w(TAG, "Cannot update main level completion - missing level ID or session manager");
            return;
        }
        
        PlayerStatistics stats = sessionManager.getPlayerStatistics();
        if (stats == null) {
            Log.w(TAG, "Cannot update main level completion - no player statistics");
            return;
        }
        
        long completionTime = gameSeconds;
        long targetTime = levelID.targetCompletionTime;
        String levelUid = levelID.name();
        
        Log.d(TAG, "Main level " + levelUid + " completed in " + completionTime + 
              "s (target: " + targetTime + "s). Challenge mode: " + isChallengeMode);
        
        // Check if level was already completed
        boolean wasAlreadyCompleted = stats.hasCompletedMainLevel(levelUid);
        
        // Always update the completion time (even if not 3 stars)
        stats.setMainLevelCompletionTime(levelUid, completionTime);
        
        // Determine how many stars to award based on completion time
        int starsToAward = 0;
        if (targetTime > 0 && completionTime <= targetTime) {
            starsToAward = 3; // Award 3 stars for completing within target time
        }
        
        if (!wasAlreadyCompleted && starsToAward > 0) {
            // First completion with 3 stars
            stats.addStars(starsToAward);
            animateEarnedStars(starsToAward);
        } else if (wasAlreadyCompleted) {
            // Already completed - check if we improved to 3 stars
            Long previousTime = stats.getMainLevelCompletionTime(levelUid);
            boolean hadThreeStars = (previousTime != null && targetTime > 0 && previousTime <= targetTime);
            
            if (!hadThreeStars && starsToAward == 3) {
                // First time getting 3 stars on this level
                stats.addStars(3);
                animateEarnedStars(3);
            } else if (hadThreeStars) {
                // Already had 3 stars - just show them
                showExistingStars();
            } else {
                // Still no 3 stars
                showNoStars();
            }
        } else {
            // First completion but no stars (over time limit)
            showNoStars();
        }
        
        // Save stats
        sessionManager.updatePlayerStatistics(stats);
        
        // Note: Player can manually navigate back using the back button when ready
    }
    
    /**
     * Animates the stars that were just earned
     */
    private void animateEarnedStars(int starsToAward) {
        Log.d(TAG, "Animating " + starsToAward + " earned stars");
        
        // Start with empty stars
        updateStarImages(0);
        
        // Animate stars with moderate delays for balanced sequential effect
        if (starsToAward >= 1) {
            animateStar(star1);
            if (starsToAward >= 2) {
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    animateStar(star2);
                    if (starsToAward >= 3) {
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            animateStar(star3);
                        }, 400); // 400ms delay between stars
                    }
                }, 400); // 400ms delay between stars
            }
        }
    }
    
    /**
     * Shows existing stars without animation (for already completed levels)
     */
    private void showExistingStars() {
        Log.d(TAG, "Showing existing stars for completed level");
        // For already completed levels with 3 stars, show 3 gold stars immediately
        updateStarImages(3);
    }
    
    /**
     * Shows no stars (for levels completed over time limit)
     */
    private void showNoStars() {
        Log.d(TAG, "Showing no stars - over time limit");
        // Show empty stars
        updateStarImages(0);
    }
    

    @Override
    public void onBackPressed() {
        // Show pause dialog when back button is pressed
        if (!isDialogShowing) {
            showPauseDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void cleanup() {
        game.cleanup();
        // Note: Sound manager persists across activities - don't release it here
    }
} 