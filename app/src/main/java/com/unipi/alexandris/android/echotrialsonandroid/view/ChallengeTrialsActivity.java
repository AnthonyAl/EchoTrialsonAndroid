package com.unipi.alexandris.android.echotrialsonandroid.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.controller.GameController;
import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.data.PlayerStatistics;
import com.unipi.alexandris.android.echotrialsonandroid.levelcreator.LevelCreatorActivity;
import com.unipi.alexandris.android.echotrialsonandroid.utility.LevelID;
import com.unipi.alexandris.android.echotrialsonandroid.utility.SessionManager;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ChallengeTrialsActivity extends AppCompatActivity {
    
    private static final String TAG = "ChallengeTrialsActivity";
    
    // UI Components
    private ImageButton backButton;
    private View loadingOverlay;
    
    // Challenge UI Elements
    private LinearLayout pitsLayout, spikesLayout, pushLayout, gravityLayout, agilityLayout;
    private ImageView pitsImage, spikesImage, pushImage, gravityImage, agilityImage;
    private ImageView[] pitsStars, spikesStars, pushStars, gravityStars, agilityStars;
    
    // Player progress
    private PlayerStatistics playerStats;
    
    // Challenge definitions
    private static final String[] CHALLENGE_GROUPS = {"A", "B", "C", "D", "E"};
    private static final String[] CHALLENGE_NAMES = {"Pits", "Spikes", "Push", "Gravity", "Agility"};
    private static final LevelID[][] CHALLENGE_LEVELS = {
        {LevelID.LEVEL_A_I, LevelID.LEVEL_A_II, LevelID.LEVEL_A_III, LevelID.LEVEL_A_IV, LevelID.LEVEL_A_V},    // Pits
        {LevelID.LEVEL_B_I, LevelID.LEVEL_B_II, LevelID.LEVEL_B_III, LevelID.LEVEL_B_IV, LevelID.LEVEL_B_V},    // Spikes
        {LevelID.LEVEL_C_I, LevelID.LEVEL_C_II, LevelID.LEVEL_C_III, LevelID.LEVEL_C_IV, LevelID.LEVEL_C_V},    // Push
        {LevelID.LEVEL_D_I, LevelID.LEVEL_D_II, LevelID.LEVEL_D_III, LevelID.LEVEL_D_IV, LevelID.LEVEL_D_V},    // Gravity
        {LevelID.LEVEL_E_I, LevelID.LEVEL_E_II, LevelID.LEVEL_E_III, LevelID.LEVEL_E_IV, LevelID.LEVEL_E_V}     // Agility
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_trials);
        
        // Initialize sound system
        GameConstants.INSTANCE.initializeSoundManagerIfNeeded(this);
        
        try {
            // Get player statistics
            SessionManager.INSTANCE.initialize(this);
            playerStats = SessionManager.INSTANCE.getPlayerStatistics();
            
            if (playerStats == null) {
                Log.e(TAG, "PlayerStatistics is null after initialization!");
                playerStats = new PlayerStatistics(); // Create fallback
            }
            
            // Initialize views
            initializeViews();
            
            // Set up button animations and click listeners
            setupButtonAnimations();
            
            // Update UI based on player progress
            updateChallengeUI();
            
        } catch (Exception e) {
            Log.e(TAG, "Error during ChallengeTrialsActivity initialization", e);
            // Try to continue with basic functionality
            finish();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Hide loading screen when returning from a level
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        }
        // Refresh UI when returning from a level
        updateChallengeUI();
    }
    
    /**
     * Initialize UI components
     */
    private void initializeViews() {
        Log.d(TAG, "Initializing views...");
        
        backButton = findViewById(R.id.backButton);
        if (backButton == null) Log.w(TAG, "backButton is null!");
        
        loadingOverlay = findViewById(R.id.loadingOverlay);
        if (loadingOverlay == null) Log.w(TAG, "loadingOverlay is null!");
        
        // Challenge layouts
        pitsLayout = findViewById(R.id.pits);
        spikesLayout = findViewById(R.id.spikes);
        pushLayout = findViewById(R.id.push);
        gravityLayout = findViewById(R.id.gravity);
        agilityLayout = findViewById(R.id.agility);
        
        // Challenge images
        pitsImage = findViewById(R.id.pitsImage);
        spikesImage = findViewById(R.id.spikesImage);
        pushImage = findViewById(R.id.pushImage);
        gravityImage = findViewById(R.id.gravityImage);
        agilityImage = findViewById(R.id.agilityImage);
        
        if (pitsImage == null) Log.w(TAG, "pitsImage is null!");
        if (spikesImage == null) Log.w(TAG, "spikesImage is null!");
        
        // Initialize star arrays
        pitsStars = new ImageView[]{
            findViewById(R.id.pitsStar), findViewById(R.id.pitsStar1), findViewById(R.id.pitsStar2),
            findViewById(R.id.pitsStar3), findViewById(R.id.pitsStar4)
        };
        spikesStars = new ImageView[]{
            findViewById(R.id.spikesStar), findViewById(R.id.spikesStar1), findViewById(R.id.spikesStar2),
            findViewById(R.id.spikesStar3), findViewById(R.id.spikesStar4)
        };
        pushStars = new ImageView[]{
            findViewById(R.id.pushStar), findViewById(R.id.pushStar1), findViewById(R.id.pushStar2),
            findViewById(R.id.pushStar3), findViewById(R.id.pushStar4)
        };
        gravityStars = new ImageView[]{
            findViewById(R.id.gravityStar), findViewById(R.id.gravityStar1), findViewById(R.id.gravityStar2),
            findViewById(R.id.gravityStar3), findViewById(R.id.gravityStar4)
        };
        agilityStars = new ImageView[]{
            findViewById(R.id.agilityStar), findViewById(R.id.agilityStar1), findViewById(R.id.agilityStar2),
            findViewById(R.id.agilityStar3), findViewById(R.id.agilityStar4)
        };
        
        // Check for null stars
        for (int i = 0; i < pitsStars.length; i++) {
            if (pitsStars[i] == null) Log.w(TAG, "pitsStars[" + i + "] is null!");
        }
        
        Log.d(TAG, "Views initialized successfully");
    }
    
    /**
     * Set up button animations and click listeners
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonAnimations() {
        // Get the shared button tap feedback listener
        View.OnTouchListener tapFeedback = LevelCreatorActivity.createButtonTapFeedback();
        
        // Back button
        if (backButton != null) {
            backButton.setOnTouchListener(tapFeedback);
            addClickSoundToButton(backButton, v -> handleBackButton());
        }
        
        // Challenge image click listeners (with sound)
        addClickSoundToButton(pitsImage, v -> handleChallengeClick(0));
        addClickSoundToButton(spikesImage, v -> handleChallengeClick(1));
        addClickSoundToButton(pushImage, v -> handleChallengeClick(2));
        addClickSoundToButton(gravityImage, v -> handleChallengeClick(3));
        addClickSoundToButton(agilityImage, v -> handleChallengeClick(4));
        
        // Star click listeners (no sound for stars)
        setupStarClickListeners(pitsStars, 0);
        setupStarClickListeners(spikesStars, 1);
        setupStarClickListeners(pushStars, 2);
        setupStarClickListeners(gravityStars, 3);
        setupStarClickListeners(agilityStars, 4);
    }
    
    /**
     * Helper to add click sound to a button
     */
    private void addClickSoundToButton(View button, View.OnClickListener listener) {
        if (button != null) {
            button.setOnClickListener(v -> {
                // Play sound
                if (GameConstants.INSTANCE.getSoundManager() != null) {
                    GameConstants.INSTANCE.getSoundManager().playSound(
                        com.unipi.alexandris.android.echotrialsonandroid.utility.audio.SoundID.UI_BUTTON_CLICK);
                }
                // Execute original listener
                listener.onClick(v);
            });
        }
    }
    
    /**
     * Set up star click listeners for a challenge
     */
    private void setupStarClickListeners(ImageView[] stars, int challengeIndex) {
        for (int i = 0; i < stars.length; i++) {
            final int starIndex = i;
            if (stars[i] != null) {
                stars[i].setOnClickListener(v -> handleStarClick(challengeIndex, starIndex));
            }
        }
    }
    
    /**
     * Update UI based on player progress
     */
    private void updateChallengeUI() {
        ImageView[] challengeImages = {pitsImage, spikesImage, pushImage, gravityImage, agilityImage};
        ImageView[][] allStars = {pitsStars, spikesStars, pushStars, gravityStars, agilityStars};

        for (int challengeIndex = 0; challengeIndex < CHALLENGE_GROUPS.length; challengeIndex++) {
            String group = CHALLENGE_GROUPS[challengeIndex];
            int completedLevels = getCompletedLevelsForChallenge(group);
            boolean isUnlocked = isChallengeUnlocked(challengeIndex);

            // Update challenge image (locked/unlocked)
            ImageView challengeImage = challengeImages[challengeIndex];
            if (challengeImage != null) {
                challengeImage.setImageResource(isUnlocked ? R.drawable.portal : R.drawable.portal_locked);
                challengeImage.setClickable(isUnlocked);
                challengeImage.setEnabled(isUnlocked);
            }

            // Update stars display
            ImageView[] stars = allStars[challengeIndex];
            updateStarsDisplay(stars, completedLevels, isUnlocked);
        }
    }
    
    /**
     * Check if a challenge is unlocked
     */
    private boolean isChallengeUnlocked(int challengeIndex) {
        if (challengeIndex == 0) return true; // Pits is always unlocked

        // Check if previous challenge is fully completed (5/5 levels with 3 stars each)
        String previousGroup = CHALLENGE_GROUPS[challengeIndex - 1];
        return getCompletedLevelsForChallenge(previousGroup) >= 5;
    }
    
    /**
     * Update star display for a challenge
     */
    private void updateStarsDisplay(ImageView[] stars, int completedLevels, boolean isUnlocked) {
        if (stars == null) return;

        for (int i = 0; i < stars.length; i++) {
            if (stars[i] == null) continue;

            if (!isUnlocked) {
                // Challenge locked - all stars empty and not clickable
                stars[i].setImageResource(R.drawable.ui_star_empty);
                stars[i].setClickable(false);
            } else {
                // Challenge unlocked - show progress
                int levelIndex = i; // Each star represents one level (0-4)

                if (levelIndex < completedLevels) {
                    // Level completed with 3 stars - gold and clickable
                    stars[i].setImageResource(R.drawable.ui_star);
                    stars[i].setClickable(true);
                } else {
                    // Level not completed with 3 stars - empty and not clickable
                    stars[i].setImageResource(R.drawable.ui_star_empty);
                    stars[i].setClickable(false);
                }
            }
        }
    }
    
    /**
     * Gets the number of completed levels (with 3 stars) for a challenge group
     */
    private int getCompletedLevelsForChallenge(String challengeGroup) {
        String[] levels = {"I", "II", "III", "IV", "V"};
        int completedCount = 0;
        
        for (String level : levels) {
            String levelId = "LEVEL_" + challengeGroup + "_" + level;
            if (isLevelCompletedWith3Stars(levelId)) {
                completedCount++;
            }
        }
        
        return completedCount;
    }
    
    /**
     * Checks if a level is completed with 3 stars (within target time)
     */
    private boolean isLevelCompletedWith3Stars(String levelId) {
        if (!playerStats.hasCompletedMainLevel(levelId)) {
            return false;
        }
        
        // Get completion time and target time
        Long completionTime = playerStats.getMainLevelCompletionTime(levelId);
        LevelID levelEnum = LevelID.getByName(levelId);
        
        if (completionTime == null || levelEnum == null) {
            return false;
        }
        
        long targetTime = levelEnum.targetCompletionTime;
        return targetTime > 0 && completionTime <= targetTime;
    }
    
    /**
     * Handle challenge image click
     */
    private void handleChallengeClick(int challengeIndex) {
        if (!isChallengeUnlocked(challengeIndex)) {
            Log.d(TAG, "Challenge " + CHALLENGE_NAMES[challengeIndex] + " is locked");
            Toast.makeText(this, "Challenge Not Unlocked", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Play portal sound with independent MediaPlayer to avoid interruption
        playPortalSoundIndependent();
        
        // Find next level to play (first level without 3 stars)
        String group = CHALLENGE_GROUPS[challengeIndex];
        String nextLevelId = getNextIncompleteLevelForChallenge(group);
        
        // Find the corresponding LevelID enum
        LevelID levelId = LevelID.getByName(nextLevelId);
        if (levelId != null) {
            // Delay level start to give portal sound time to play
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                startLevel(levelId);
            }, 500); // 500ms delay to let sound play
        } else {
            Log.e(TAG, "Could not find LevelID for: " + nextLevelId);
        }
    }
    
    /**
     * Gets the next incomplete level for a challenge group
     */
    private String getNextIncompleteLevelForChallenge(String challengeGroup) {
        String[] levels = {"I", "II", "III", "IV", "V"};
        
        // Find first level without 3 stars
        for (String level : levels) {
            String levelId = "LEVEL_" + challengeGroup + "_" + level;
            if (!isLevelCompletedWith3Stars(levelId)) {
                return levelId;
            }
        }
        
        // All levels completed with 3 stars, return the first level for replay
        return "LEVEL_" + challengeGroup + "_I";
    }
    
    /**
     * Handle star click (replay specific level)
     */
    private void handleStarClick(int challengeIndex, int starIndex) {
        if (!isChallengeUnlocked(challengeIndex)) return;

        // Each star represents one level (starIndex = levelIndex)
        int levelIndex = starIndex;  // 0, 1, 2, 3, or 4

        // Only allow clicking on gold stars (completed levels with 3 stars)
        String group = CHALLENGE_GROUPS[challengeIndex];
        String[] levels = {"I", "II", "III", "IV", "V"};
        String levelId = "LEVEL_" + group + "_" + levels[levelIndex];

        if (!isLevelCompletedWith3Stars(levelId)) return; // Level not completed with 3 stars

        // Play portal sound with independent MediaPlayer to avoid interruption
        playPortalSoundIndependent();

        LevelID levelEnum = LevelID.getByName(levelId);
        if (levelEnum != null) {
            // Delay level start to give portal sound time to play
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                startLevel(levelEnum);
            }, 500); // 500ms delay to let sound play
        }
    }
    
    /**
     * Plays portal sound with independent MediaPlayer and audio focus management
     */
    private void playPortalSoundIndependent() {
        new Thread(() -> {
            try {
                android.media.MediaPlayer mediaPlayer = android.media.MediaPlayer.create(getApplicationContext(), com.unipi.alexandris.android.echotrialsonandroid.R.raw.portal);
                if (mediaPlayer != null) {
                    // Request audio focus to prevent interruption
                    android.media.AudioManager audioManager = (android.media.AudioManager) getSystemService(android.content.Context.AUDIO_SERVICE);
                    int result = audioManager.requestAudioFocus(
                        null,
                        android.media.AudioManager.STREAM_MUSIC,
                        android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                    );
                    
                    if (result == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        mediaPlayer.setVolume(0.8f, 0.8f);
                        mediaPlayer.setOnCompletionListener(mp -> {
                            try {
                                audioManager.abandonAudioFocus(null);
                                mp.release();
                            } catch (Exception e) {
                                Log.e(TAG, "Error releasing portal sound MediaPlayer", e);
                            }
                        });
                        mediaPlayer.start();
                    } else {
                        Log.w(TAG, "Could not gain audio focus for portal sound");
                        mediaPlayer.release();
                    }
                } else {
                    Log.w(TAG, "Failed to create independent MediaPlayer for portal sound");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error playing portal sound independently", e);
            }
        }, "IndependentPortalSound").start();
    }
    
    /**
     * Shows the loading screen with fade-in animation
     */
    private void showLoadingScreen() {
        if (loadingOverlay == null) return;
        
        // Make it visible and clickable to block all interactions
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingOverlay.setClickable(true);
        loadingOverlay.setFocusable(true);
        
        // Start with 0% transparency
        loadingOverlay.setAlpha(0f);
        
        // Animate to 100% transparency (fully black)
        loadingOverlay.animate()
            .alpha(1f)
            .setDuration(700) // 700ms fade-in
            .start();
        
        Log.d(TAG, "Loading screen shown with fade-in animation");
    }
    
    /**
     * Start a challenge level
     */
    private void startLevel(LevelID levelId) {
        Log.d(TAG, "Starting challenge level: " + levelId.name());
        
        // Show loading screen immediately
        showLoadingScreen();
        
        // Add small delay to ensure loading screen is visible before heavy operations
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            try {
                // Load the level data from resources
                InputStream inputStream = getResources().openRawResource(levelId.resourceId);
                
                // Parse the level data XML
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(inputStream);
                
                // Initialize the game controller with level data
                GameController.GAME.initialize(document, levelId.name());
                
                // Launch GameHostActivity (which is better for full game experience)
                Intent intent = new Intent(this, GameHostActivity.class);
                intent.putExtra("level_uid", levelId.name());
                intent.putExtra("is_level_custom", false);
                intent.putExtra("is_private_level", false);
                intent.putExtra("is_level_verified", true);
                intent.putExtra("is_challenge_mode", true); // Mark as challenge mode
                startActivity(intent);
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading challenge level: " + levelId.name(), e);
                // Hide loading screen on error
                if (loadingOverlay != null) {
                    loadingOverlay.setVisibility(View.GONE);
                }
            }
        }, 900); // 1100ms delay to allow fade-in animation to complete (700ms + buffer)
    }
    
    /**
     * Handle back button click
     */
    private void handleBackButton() {
        finish(); // Return to MainActivity
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handleBackButton();
    }
}
