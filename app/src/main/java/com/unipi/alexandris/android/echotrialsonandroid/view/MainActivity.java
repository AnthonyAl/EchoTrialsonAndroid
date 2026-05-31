package com.unipi.alexandris.android.echotrialsonandroid.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.levelcreator.LevelCreatorActivity;
import com.unipi.alexandris.android.echotrialsonandroid.utility.LevelID;
import com.unipi.alexandris.android.echotrialsonandroid.utility.SessionManager;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.ButtonSoundHelper;

/**
 * Main menu activity with game background component.
 * Provides a minimal interface for navigation and game background.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private GameBackgroundComponent gameBackground;
    private ImageButton playButton;
    private ImageButton settingsButton;
    private ImageButton profileButton;
    private ImageButton achievementsButton;
    private ImageButton accountButton;
    private ImageButton downloadButton;
    private ImageButton uploadButton;
    private ImageButton infoButton;
    private ImageButton creatorButton;

    // Session management
    private SessionManager sessionManager;
    private AccountDialog accountDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize session manager
        SessionManager.INSTANCE.initialize(this);
        sessionManager = SessionManager.INSTANCE;

        // Initialize views
        gameBackground = findViewById(R.id.gameBackground);
        playButton = findViewById(R.id.playButton);
        settingsButton = findViewById(R.id.settingsButton);
        profileButton = findViewById(R.id.profileButton);
        achievementsButton = findViewById(R.id.achievementsButton);
        accountButton = findViewById(R.id.accountButton);
        infoButton = findViewById(R.id.infoButton);
        downloadButton = findViewById(R.id.downloadButton);
        uploadButton = findViewById(R.id.uploadButton);
        creatorButton = findViewById(R.id.creatorButton);

        // Initialize the game background
        gameBackground.initiateGame(LevelID.LEVEL_MAIN_MENU_I);

        // Set up button animations and click listeners
        setupButtonAnimations();

        // Set up session state listener
        setupSessionStateListener();

        // Update UI based on current session state
        updateUIForSessionState();
    }

    /**
     * Sets up session state listener to handle authentication changes
     */
    private void setupSessionStateListener() {
        sessionManager.setSessionStateListener(new SessionManager.SessionStateListener() {
            @Override
            public void onSessionStateChanged(SessionManager.SessionState newState) {
                runOnUiThread(() -> updateUIForSessionState());
            }

            @Override
            public void onUserAuthenticated(FirebaseUser user) {
                runOnUiThread(() -> {
                    updateUIForSessionState();
                    Toast.makeText(MainActivity.this, "Welcome, " +
                        (user.getDisplayName() != null ? user.getDisplayName() : "User") + "!",
                        Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onUserSignedOut() {
                runOnUiThread(() -> {
                    updateUIForSessionState();
                    Toast.makeText(MainActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Updates UI elements based on current session state (online/offline)
     */
    private void updateUIForSessionState() {
        boolean isOnline = sessionManager.isAuthenticated();

        // Update button opacity and enabled state based on online status
        float onlineAlpha = 1.0f;
        float offlineAlpha = 0.5f;

        // Buttons that require online access
        if (downloadButton != null) {
            downloadButton.setAlpha(isOnline ? onlineAlpha : offlineAlpha);
            downloadButton.setEnabled(isOnline);
        }

        if (uploadButton != null) {
            uploadButton.setAlpha(isOnline ? onlineAlpha : offlineAlpha);
            uploadButton.setEnabled(isOnline);
        }

        if (creatorButton != null) {
            creatorButton.setAlpha(isOnline ? onlineAlpha : offlineAlpha);
            creatorButton.setEnabled(isOnline);
        }

        // Buttons that work offline
        if (playButton != null) {
            playButton.setAlpha(onlineAlpha);
            playButton.setEnabled(true);
        }

        if (settingsButton != null) {
            settingsButton.setAlpha(onlineAlpha);
            settingsButton.setEnabled(true);
        }

        if (profileButton != null) {
            profileButton.setAlpha(onlineAlpha);
            profileButton.setEnabled(true);
        }

        if (achievementsButton != null) {
            achievementsButton.setAlpha(onlineAlpha);
            achievementsButton.setEnabled(true);
        }

        if (accountButton != null) {
            accountButton.setAlpha(onlineAlpha);
            accountButton.setEnabled(true);
        }

        if (infoButton != null) {
            infoButton.setAlpha(onlineAlpha);
            infoButton.setEnabled(true);
        }
    }

    /**
     * Sets up button animations and click listeners following the established dialog pattern.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonAnimations() {
        // Get the shared button tap feedback listener
        View.OnTouchListener tapFeedback = LevelCreatorActivity.createButtonTapFeedback();

        // Apply tap feedback and click sounds to all buttons
        if (playButton != null) {
            playButton.setOnTouchListener(tapFeedback);
            ButtonSoundHelper.addClickSound(playButton, v -> handlePlayButton());
        }

        if (settingsButton != null) {
            settingsButton.setOnTouchListener(tapFeedback);
            ButtonSoundHelper.addClickSound(settingsButton, v -> handleSettingsButton());
        }

        if (profileButton != null) {
            profileButton.setOnTouchListener(tapFeedback);
            ButtonSoundHelper.addClickSound(profileButton, v -> handleProfileButton());
        }

        if (achievementsButton != null) {
            achievementsButton.setOnTouchListener(tapFeedback);
            ButtonSoundHelper.addClickSound(achievementsButton, v -> handleAchievementsButton());
        }

        if (accountButton != null) {
            accountButton.setOnTouchListener(tapFeedback);
            ButtonSoundHelper.addClickSound(accountButton, v -> handleAccountButton());
        }

        if (infoButton != null) {
            infoButton.setOnTouchListener(tapFeedback);
            ButtonSoundHelper.addClickSound(infoButton, v -> handleInfoButton());
        }

        if (downloadButton != null) {
            downloadButton.setOnTouchListener(tapFeedback);
            ButtonSoundHelper.addClickSound(downloadButton, v -> handleDownloadButton());
        }

        if (uploadButton != null) {
            uploadButton.setOnTouchListener(tapFeedback);
            ButtonSoundHelper.addClickSound(uploadButton, v -> handleUploadButton());
        }

        if (creatorButton != null) {
            creatorButton.setOnTouchListener(tapFeedback);
            ButtonSoundHelper.addClickSound(creatorButton, v -> handleCreatorButton());
        }
    }

    // Button click handlers
    private void handlePlayButton() {
        // Stop/cleanup background game to avoid GL/context clashes
        if (gameBackground != null) {
            gameBackground.cleanup();
        }
        
        // Launch Challenge Trials Activity
        Intent intent = new Intent(this, ChallengeTrialsActivity.class);
        startActivity(intent);
    }
    
    private void handleSettingsButton() {
        Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show();
    }
    
    private void handleProfileButton() {
        Toast.makeText(this, "Profile coming soon!", Toast.LENGTH_SHORT).show();
    }
    
    private void handleAchievementsButton() {
        // TODO: Implement achievements functionality
    }
    
    private void handleAccountButton() {
        // Create and show the account dialog
        if (accountDialog == null) {
            accountDialog = new AccountDialog(this, sessionManager);
            accountDialog.setAccountDialogListener(new AccountDialog.AccountDialogListener() {
                @Override
                public void onAuthenticationSuccess(FirebaseUser user) {
                    // Authentication successful - UI will be updated by session listener
                    System.out.println("MAIN: User authenticated successfully");
                }
                
                @Override
                public void onAuthenticationFailure(String error) {
                    // Authentication failed
                    System.out.println("MAIN: Authentication failed: " + error);
                }
                
                @Override
                public void onSignOut() {
                    // User signed out - UI will be updated by session listener
                    System.out.println("MAIN: User signed out");
                }
            });
        }
        
        accountDialog.show();
    }
    
    private void handleInfoButton() {
        Toast.makeText(this, "Info coming soon!", Toast.LENGTH_SHORT).show();
    }
    
    private void handleDownloadButton() {
        if (!sessionManager.isAuthenticated()) {
            Toast.makeText(this, "Please sign in to download your data", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading indicator
        Toast.makeText(this, "Downloading your data...", Toast.LENGTH_SHORT).show();
        
        // Download player statistics from Firebase
        sessionManager.downloadPlayerStatistics(task -> {
            runOnUiThread(() -> {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Data downloaded successfully!", Toast.LENGTH_SHORT).show();
                    System.out.println("MAIN: Player data downloaded successfully");
                    // Refresh account dialog if it's open
                    refreshAccountDialog();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to download data: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                        Toast.LENGTH_LONG).show();
                    System.out.println("MAIN: Failed to download player data: " +
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                }
            });
        });
    }
    
    private void handleUploadButton() {
        if (!sessionManager.isAuthenticated()) {
            Toast.makeText(this, "Please sign in to upload your data", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading indicator
        Toast.makeText(this, "Uploading your data...", Toast.LENGTH_SHORT).show();
        
        // Upload player statistics to Firebase
        sessionManager.uploadPlayerStatistics(task -> {
            runOnUiThread(() -> {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Data uploaded successfully!", Toast.LENGTH_SHORT).show();
                    System.out.println("MAIN: Player data uploaded successfully");
                    // Refresh account dialog if it's open
                    refreshAccountDialog();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to upload data: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                        Toast.LENGTH_LONG).show();
                    System.out.println("MAIN: Failed to upload player data: " +
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                }
            });
        });
    }
    
    private void handleCreatorButton() {
        // Check if user is authenticated (Creator features require online access)
        if (!sessionManager.isAuthenticated()) {
            Toast.makeText(this, "Please sign in to access Creator features", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Stop/cleanup background game to avoid GL/context clashes
        if (gameBackground != null) {
            gameBackground.cleanup();
        }
        // Add delay to ensure GL context is properly released
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Start CreatorMainActivity
            Intent intent = new Intent(this, CreatorMainActivity.class);
            startActivity(intent);
        }, 0); // 100ms delay for GL context cleanup
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Ensure background component is paused/cleaned when leaving MainActivity
        if (gameBackground != null) {
            gameBackground.cleanup();
        }
        // Force garbage collection to clean up any remaining GL resources
        System.gc();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Update UI state when returning to MainActivity
        updateUIForSessionState();
        
        // Add longer delay when returning from other activities
        // to ensure proper GL context cleanup
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                if (gameBackground != null && !gameBackground.isInitialized()) {
                    // Force a clean state before initialization
                    GameConstants.INSTANCE.resetHandler();
                    GameConstants.INSTANCE.resetGlobalArea();
                    
                    gameBackground.initiateGame(LevelID.LEVEL_MAIN_MENU_I);
                }
            } catch (Exception e) {
                // Log the error and try to recover
                android.util.Log.e("MainActivity", "Error initializing background game", e);
                // Try a full cleanup and retry once
                if (gameBackground != null) {
                    gameBackground.cleanup();
                    try {
                        Thread.sleep(100); // Brief pause for cleanup
                        gameBackground.initiateGame(LevelID.LEVEL_MAIN_MENU_I);
                    } catch (Exception retryException) {
                        android.util.Log.e("MainActivity", "Failed to recover background game", retryException);
                    }
                }
            }
        }, 100); // Increased delay for proper GL context cleanup from other activities
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the game background
        if (gameBackground != null) {
            gameBackground.cleanup();
        }
        // Note: Sound manager persists across activities - don't release it here
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Handle Google Sign-In result
        if (requestCode == AccountDialog.getGoogleSignInRequestCode()) {
            if (accountDialog != null) {
                accountDialog.handleGoogleSignInResult(data);
            }
        }
    }

    private void refreshAccountDialog() {
        if (accountDialog != null && accountDialog.isShowing()) {
            accountDialog.updateUI();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
