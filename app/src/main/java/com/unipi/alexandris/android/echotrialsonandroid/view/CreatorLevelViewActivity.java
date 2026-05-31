package com.unipi.alexandris.android.echotrialsonandroid.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.controller.GameController;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.ButtonSoundHelper;
import com.unipi.alexandris.android.echotrialsonandroid.utility.DialogAnimationHelper;
import com.unipi.alexandris.android.echotrialsonandroid.utility.UniversalDialogAnimator;
import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.levelcreator.LevelCreatorActivity;
import com.unipi.alexandris.android.echotrialsonandroid.utility.SessionManager;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * CreatorLevelViewActivity displays level information and provides actions based on ownership.
 * Handles both private (user's own) and public (others') levels with different functionality.
 */
public class CreatorLevelViewActivity extends AppCompatActivity {
    private static final String TAG = "CreatorLevelViewActivity";
    
    // UI Components
    private ImageButton backButton;
    private View loadingOverlay;
    private TextView levelNameText;
    private ImageButton duplicateButton;
    private ImageButton trashButton;
    private ImageButton publishLevel;
    private ImageButton unpublishLevel;
    private ImageButton infoButton;
    private ImageButton playButton;
    private ImageButton creatorButton;
    private ImageButton rateDifficultyButton;
    private ImageButton addToFavouritesButton;
    private ImageButton likeButton;
    private TextView viewsText;
    private TextView likesText;
    private ProgressBar progressBar;
    
    // Firebase and session management
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private SessionManager sessionManager;
    
    // Level data
    private String levelUid;
    private String levelData;
    private boolean isPrivateLevel;
    private boolean isLevelVerified;
    private boolean isLevelOpenSource;
    private String levelOwnerUid;
    private String currentLevelName;
    private int currentViews;
    private int currentLikes;
    private boolean isLikedByUser;
    private boolean isInUserFavourites;
    private long levelTargetCompletionTime; // Target completion time in seconds
    private long levelBestCompletionTime; // Best completion time in seconds
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creator_level_view);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        SessionManager.INSTANCE.initialize(this);
        sessionManager = SessionManager.INSTANCE;
        
        // Verify authentication
        if (!sessionManager.isAuthenticated()) {
            Toast.makeText(this, "Please sign in to view levels", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Get level data from intent
        Intent intent = getIntent();
        levelUid = intent.getStringExtra("level_uid");
        isPrivateLevel = intent.getBooleanExtra("is_private", true);
        
        if (levelUid == null) {
            Toast.makeText(this, "Invalid level data", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Initialize views
        initializeViews();
        
        // Set up button animations and click listeners
        setupButtonAnimations();
        
        // Load level data
        loadLevelData();
    }
    
    /**
     * Initializes all UI components
     */
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        levelNameText = findViewById(R.id.levelNameText);
        duplicateButton = findViewById(R.id.duplicateButton);
        trashButton = findViewById(R.id.trashButton);
        publishLevel = findViewById(R.id.publishLevel);
        unpublishLevel = findViewById(R.id.unpublishLevel);
        infoButton = findViewById(R.id.infoButton);
        playButton = findViewById(R.id.playButton);
        creatorButton = findViewById(R.id.creatorButton);
        rateDifficultyButton = findViewById(R.id.rateDifficultyButton);
        addToFavouritesButton = findViewById(R.id.addToFavouritesButton);
        likeButton = findViewById(R.id.likeButton);
        viewsText = findViewById(R.id.viewsText);
        likesText = findViewById(R.id.likesText);
        progressBar = findViewById(R.id.progressBar2);
        
        // Set up level name click listener for editing
        ButtonSoundHelper.addClickSound(levelNameText, v -> showLevelNameEditDialog());
    }
    
    /**
     * Sets up button animations and click listeners
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonAnimations() {
        // Apply touch animations to all buttons
        View.OnTouchListener touchListener = LevelCreatorActivity.createButtonTapFeedback();
        
        // Set up back button
        backButton.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(backButton, v -> finish());
        
        // Set up action buttons
        duplicateButton.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(duplicateButton, v -> handleDuplicateButton());
        
        trashButton.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(trashButton, v -> handleDeleteButton());
        
        publishLevel.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(publishLevel, v -> handlePublishButton());
        
        unpublishLevel.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(unpublishLevel, v -> handleUnpublishButton());
        
        infoButton.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(infoButton, v -> handleInfoButton());
        
        playButton.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(playButton, v -> handlePlayButton());
        
        creatorButton.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(creatorButton, v -> handleCreatorButton());
        
        rateDifficultyButton.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(rateDifficultyButton, v -> handleRateDifficultyButton());
        
        addToFavouritesButton.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(addToFavouritesButton, v -> handleFavouritesButton());
        
        likeButton.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(likeButton, v -> handleLikeButton());
    }
    
    /**
     * Loads level data from Firestore and sets up UI accordingly
     */
    private void loadLevelData() {
        showLoading(true);
        
        String collectionPath = isPrivateLevel ? 
            "users/" + GameConstants.INSTANCE.getCurrentUserUid() + "/levels" :
            "public_levels";
        
        db.collection(collectionPath).document(levelUid)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        updateLevelData(doc);
                        setupUIForLevelType();
                    } else {
                        Toast.makeText(this, "Failed to load level data", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }
    
    /**
     * Updates level data from Firestore document
     */
    @SuppressLint("SetTextI18n")
    private void updateLevelData(DocumentSnapshot doc) {
        currentLevelName = doc.getString("name");
        isLevelVerified = doc.getBoolean("isVerified") != null && Boolean.TRUE.equals(doc.getBoolean("isVerified"));
        isLevelOpenSource = doc.getBoolean("isOpenSource") != null && Boolean.TRUE.equals(doc.getBoolean("isOpenSource"));
        levelOwnerUid = doc.getString("ownerUid");
        currentViews = doc.getLong("views") != null ? Objects.requireNonNull(doc.getLong("views")).intValue() : 0;
        currentLikes = doc.getLong("likes") != null ? Objects.requireNonNull(doc.getLong("likes")).intValue() : 0;
        levelData = doc.getString("levelData");
        
        // Load completion time data
        Long targetTime = doc.getLong("targetCompletionTime");
        Long bestTime = doc.getLong("bestCompletionTime");
        levelTargetCompletionTime = targetTime != null ? targetTime : -1L; // Default 60 seconds
        levelBestCompletionTime = bestTime != null ? bestTime : -1; // Default no completion

        // Check if level is actually published (for private levels)
        if (isPrivateLevel) {
            Boolean isPublic = doc.getBoolean("isPublic");
            if (isPublic != null && isPublic) {
                // Level is published - treat as public level
                isPrivateLevel = false;
            }
        }

        // Update UI with level data
        levelNameText.setText(currentLevelName != null ? currentLevelName : "Unnamed Level");
        viewsText.setText("Views: " + currentViews);
        likesText.setText("Likes: " + currentLikes);
        
        // Check if user has liked this level (for public levels)
        if (!isPrivateLevel) {
            checkUserLikeStatus();
            checkUserFavouritesStatus();
        }
    }
    
    /**
     * Sets up UI visibility based on level type (private/public)
     */
    private void setupUIForLevelType() {
        if (isPrivateLevel) {
            // Private level - user owns it
            setupPrivateLevelUI();
        } else {
            // Public level - someone else's level
            setupPublicLevelUI();
        }
    }
    
    /**
     * Sets up UI for private level (user's own level)
     */
    private void setupPrivateLevelUI() {
        // Show private-only buttons
        trashButton.setVisibility(View.VISIBLE);
        creatorButton.setVisibility(View.VISIBLE);
        
        // Hide public-only elements
        viewsText.setVisibility(View.GONE);
        likesText.setVisibility(View.GONE);
        likeButton.setVisibility(View.GONE);
        addToFavouritesButton.setVisibility(View.GONE);
        
        // Set publish/unpublish button visibility based on current status
        updatePublishButtonVisibility();
    }
    
    /**
     * Sets up UI for public level (someone else's level)
     */
    private void setupPublicLevelUI() {
        // Hide private-only buttons
        trashButton.setVisibility(View.GONE);
        publishLevel.setVisibility(View.GONE);
        creatorButton.setVisibility(View.GONE);
        
        // Show public-only elements
        viewsText.setVisibility(View.VISIBLE);
        likesText.setVisibility(View.VISIBLE);
        likeButton.setVisibility(View.VISIBLE);
        addToFavouritesButton.setVisibility(View.VISIBLE);
        
        // Show duplicate button if level is open source OR if user owns the level
        String currentUserId = GameConstants.INSTANCE.getCurrentUserUid();
        boolean userOwnsLevel = currentUserId != null && currentUserId.equals(levelOwnerUid);
        duplicateButton.setVisibility((isLevelOpenSource || userOwnsLevel) ? View.VISIBLE : View.GONE);
        
        // Set publish/unpublish button visibility
        updatePublishButtonVisibility();
    }
    
    /**
     * Updates publish/unpublish button visibility based on current level status
     */
    private void updatePublishButtonVisibility() {
        // Check if level is currently published
        if (isPrivateLevel) {
            // Private level - show publish button if verified
            publishLevel.setVisibility(isLevelVerified ? View.VISIBLE : View.GONE);
            unpublishLevel.setVisibility(View.GONE);
        } else {
            // Public level - check if user owns it
            String currentUserId = GameConstants.INSTANCE.getCurrentUserUid();
            if (currentUserId != null && currentUserId.equals(levelOwnerUid)) {
                // User owns this public level - show unpublish button
                publishLevel.setVisibility(View.GONE);
                unpublishLevel.setVisibility(View.VISIBLE);
                // Hide edit button for public levels
                creatorButton.setVisibility(View.GONE);
            } else {
                // User doesn't own this public level - hide both buttons
                publishLevel.setVisibility(View.GONE);
                unpublishLevel.setVisibility(View.GONE);
                creatorButton.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * Checks if current user has liked this level
     */
    private void checkUserLikeStatus() {
        // TODO: Check if user's UID is in the level's likedBy list
        // For now, assume not liked
        isLikedByUser = false;
        updateLikeButtonAppearance();
    }
    
    /**
     * Checks if current user has this level in favourites
     */
    private void checkUserFavouritesStatus() {
        // TODO: Check if level UID is in user's favourites collection
        // For now, assume not in favourites
        isInUserFavourites = false;
        updateFavouritesButtonAppearance();
    }
    
    /**
     * Updates like button appearance based on current like status
     */
    private void updateLikeButtonAppearance() {
        // TODO: Update button image based on isLikedByUser
        // likeButton.setImageResource(isLikedByUser ? R.drawable.ui_unlike : R.drawable.ui_like);
    }
    
    /**
     * Updates favourites button appearance based on current favourites status
     */
    private void updateFavouritesButtonAppearance() {
        // TODO: Update button image based on isInUserFavourites
        // addToFavouritesButton.setImageResource(isInUserFavourites ? R.drawable.ui_remove_favourites : R.drawable.ui_add_favourites);
    }
    
    /**
     * Shows loading indicator
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    // ================== BUTTON HANDLERS ==================
    
    /**
     * Handles duplicate button click
     */
    private void handleDuplicateButton() {
        if (isPrivateLevel) {
            // Private level: duplicate with (COPY) suffix
            duplicateLevel(true);
        } else {
            // Public level: check if user owns it or if it's open source
            String currentUserId = GameConstants.INSTANCE.getCurrentUserUid();
            boolean userOwnsLevel = currentUserId != null && currentUserId.equals(levelOwnerUid);
            
            if (userOwnsLevel || isLevelOpenSource) {
                // User owns the level or it's open source: duplicate to private collection
                duplicateLevel(false);
            } else {
                // Public non-open source level that user doesn't own: cannot duplicate
                Toast.makeText(this, "Cannot duplicate non-open source levels", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Duplicates a level with (COPY) suffix
     * @param isPrivateLevel Whether the original level is private
     */
    private void duplicateLevel(boolean isPrivateLevel) {
        showLoading(true);
        
        String userId = GameConstants.INSTANCE.getCurrentUserUid();
        String newLevelName = currentLevelName + " (COPY)";
        
        // Create new level data
        Map<String, Object> newLevelData = new HashMap<>();
        newLevelData.put("name", newLevelName);
        newLevelData.put("lastEdited", new Date());
        newLevelData.put("isPublic", false); // Always save to private collection
        newLevelData.put("isOpenSource", false); // Duplicates are not open source
        newLevelData.put("isVerified", false); // Duplicates need to be verified again
        newLevelData.put("levelData", levelData); // Copy the level data
        newLevelData.put("targetCompletionTime", levelTargetCompletionTime); // Preserve target time
        newLevelData.put("bestCompletionTime", -1L); // Reset best time for duplicate
        
        // Generate new UID for the duplicate
        String newLevelUid = db.collection("users").document(userId).collection("levels").document().getId();
        
        // Save to Firestore
        db.collection("users").document(userId)
                .collection("levels")
                .document(newLevelUid)
                .set(newLevelData)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Level duplicated successfully!", Toast.LENGTH_SHORT).show();
                        // Return to CreatorMyLevelsActivity to show the new level
                        Intent intent = new Intent(this, CreatorMyLevelsActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to duplicate level: " + 
                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                            Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    /**
     * Handles delete button click
     */
    private void handleDeleteButton() {
        if (!isPrivateLevel) {
            Toast.makeText(this, "Cannot delete public levels", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show animated confirmation dialog
        // Yes button clicked - delete the level
        UniversalDialogAnimator.AnimatedDialog animatedDialog = UniversalDialogAnimator.showConfirmationDialog(
            this,
            "Are you sure you want to delete this level?",
            null, // No additional message
                this::deleteLevelFromFirestore,
            () -> {
                // No button clicked - just dismiss (handled automatically)
            }
        );
        
        // Show the animated dialog
        animatedDialog.show();
    }
    
    /**
     * Handles publish button click
     */
    private void handlePublishButton() {
        if (!isPrivateLevel) {
            Toast.makeText(this, "Cannot publish public levels", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!isLevelVerified) {
            Toast.makeText(this, "Level must be verified before publishing", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Show confirmation dialog
        showPublishConfirmationDialog();
    }
    
    /**
     * Handles unpublish button click
     */
    private void handleUnpublishButton() {
        // Check if user owns this level (for both private and public levels)
        String currentUserId = GameConstants.INSTANCE.getCurrentUserUid();
        boolean userOwnsLevel = currentUserId != null && currentUserId.equals(levelOwnerUid);
        
        if (!userOwnsLevel) {
            Toast.makeText(this, "Cannot unpublish levels you don't own", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show confirmation dialog
        showUnpublishConfirmationDialog();
    }
    
    /**
     * Handles info button click
     */
    private void handleInfoButton() {
        // TODO: Implement info popup with comments
        Toast.makeText(this, "Info feature coming soon!", Toast.LENGTH_SHORT).show();
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
     * Handles play button click
     */
    private void handlePlayButton() {
        // Show loading screen immediately
        showLoadingScreen();
        
        // Add small delay to ensure loading screen is visible before heavy operations
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            try {
                // Parse the level data XML
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                InputSource inputSource = new InputSource(new StringReader(levelData));
                Document document = builder.parse(inputSource);

                // Initialize the game controller with level data (but don't load yet)
                GameController.GAME.initialize(document, levelUid);

                // Launch GameActivity which will handle camera creation and level loading
                Intent intent = new Intent(this, GameHostActivity.class);
                intent.putExtra("level_uid", levelUid);
                intent.putExtra("is_private_level", isPrivateLevel);
                intent.putExtra("is_level_verified", isLevelVerified);
                startActivity(intent);
                finish(); // Finish LevelView when launching game
            }
            catch (Exception e) {
                Log.e(TAG, "Error loading XML from string", e);
                // Hide loading screen on error
                if (loadingOverlay != null) {
                    loadingOverlay.setVisibility(View.GONE);
                }
            }
        }, 700);
    }
    
    /**
     * Handles creator button click
     */
    private void handleCreatorButton() {
        if (!isPrivateLevel) {
            Toast.makeText(this, "Cannot edit public levels", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Launch LevelCreatorActivity
        Intent intent = new Intent(this, LevelCreatorActivity.class);
        intent.putExtra("level_uid", levelUid);
        intent.putExtra("is_new_level", false);
        startActivity(intent);
        finish(); // Finish LevelView when launching editor
    }
    
    /**
     * Handles rate difficulty button click
     */
    private void handleRateDifficultyButton() {
        // TODO: Implement difficulty rating
        Toast.makeText(this, "Difficulty rating coming soon!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Handles favourites button click
     */
    private void handleFavouritesButton() {
        if (isPrivateLevel) {
            Toast.makeText(this, "Cannot add own levels to favourites", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // TODO: Implement add/remove from favourites
        Toast.makeText(this, "Favourites feature coming soon!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Handles like button click
     */
    private void handleLikeButton() {
        if (isPrivateLevel) {
            Toast.makeText(this, "Cannot like own levels", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // TODO: Implement like/unlike functionality
        Toast.makeText(this, "Like feature coming soon!", Toast.LENGTH_SHORT).show();
    }
    
    // ================== HELPER METHODS ==================
    
    /**
     * Shows dialog to edit level name
     */
    private void showLevelNameEditDialog() {
        if (!isPrivateLevel) {
            Toast.makeText(this, "Cannot edit public level names", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Level Name");

        final EditText input = new EditText(this);
        input.setText(currentLevelName);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty() && !newName.equals(currentLevelName)) {
                updateLevelName(newName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Add smooth slide-in animation
        DialogAnimationHelper.addSlideInAnimation(dialog);
    }

    /**
     * Updates level name in Firestore
     */
    private void updateLevelName(String newName) {
        showLoading(true);
        
        String userId = GameConstants.INSTANCE.getCurrentUserUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("lastEdited", new Date());
        
        db.collection("users").document(userId)
                .collection("levels")
                .document(levelUid)
                .update(updates)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        currentLevelName = newName;
                        levelNameText.setText(newName);
                        Toast.makeText(this, "Level name updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to update level name", Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    /**
     * Deletes level from Firestore
     */
    private void deleteLevelFromFirestore() {
        showLoading(true);
        
        String userId = GameConstants.INSTANCE.getCurrentUserUid();
        db.collection("users").document(userId)
                .collection("levels")
                .document(levelUid)
                .delete()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Level deleted successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete level", Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    /**
     * Publishes a level from private collection to public collection
     */
    private void publishLevelToPublic() {
        showLoading(true);
        
        String userId = GameConstants.INSTANCE.getCurrentUserUid();
        
        // Prepare level data for public collection
        Map<String, Object> publicLevelData = new HashMap<>();
        publicLevelData.put("name", currentLevelName);
        publicLevelData.put("lastEdited", new Date());
        publicLevelData.put("isPublic", true);
        publicLevelData.put("isOpenSource", isLevelOpenSource);
        publicLevelData.put("isVerified", isLevelVerified);
        publicLevelData.put("targetCompletionTime", levelTargetCompletionTime);
        publicLevelData.put("bestCompletionTime", levelBestCompletionTime);
        publicLevelData.put("levelData", levelData);
        publicLevelData.put("ownerUid", userId);
        publicLevelData.put("views", 0L);
        publicLevelData.put("likes", 0L);
        
        // Add to public collection
        db.collection("public_levels")
                .document(levelUid)
                .set(publicLevelData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Remove from private collection (move operation)
                        db.collection("users").document(userId)
                                .collection("levels").document(levelUid)
                                .delete()
                                .addOnCompleteListener(deleteTask -> {
                                    showLoading(false);
                                    if (deleteTask.isSuccessful()) {
                                        Toast.makeText(this, "Level published successfully!", Toast.LENGTH_SHORT).show();
                                        // Navigate to CreatorMyLevelsActivity and finish
                                        Intent intent = new Intent(this, CreatorMyLevelsActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Error removing from private collection", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Failed to publish level: " + 
                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                            Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    /**
     * Unpublishes a level from public collection back to private collection
     */
    private void unpublishLevelToPrivate() {
        showLoading(true);
        
        String userId = GameConstants.INSTANCE.getCurrentUserUid();
        
        // Remove from public collection
        db.collection("public_levels")
                .document(levelUid)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Add back to private collection with reset values
                        Map<String, Object> privateLevelData = new HashMap<>();
                        privateLevelData.put("name", currentLevelName);
                        privateLevelData.put("lastEdited", new Date());
                        privateLevelData.put("isPublic", false);
                        privateLevelData.put("isOpenSource", isLevelOpenSource);
                        privateLevelData.put("isVerified", false); // Reset to unverified
                        privateLevelData.put("targetCompletionTime", -1L); // Reset target time
                        privateLevelData.put("bestCompletionTime", -1L); // Reset best time
                        privateLevelData.put("levelData", levelData);
                        
                        db.collection("users").document(userId)
                                .collection("levels").document(levelUid)
                                .set(privateLevelData)
                                .addOnCompleteListener(privateTask -> {
                                    showLoading(false);
                                    if (privateTask.isSuccessful()) {
                                        Toast.makeText(this, "Level unpublished successfully!", Toast.LENGTH_SHORT).show();
                                        // Navigate to CreatorMyLevelsActivity and finish
                                        Intent intent = new Intent(this, CreatorMyLevelsActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Error adding to private collection", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Failed to unpublish level: " + 
                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                            Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    /**
     * Shows publish confirmation dialog
     */
    private void showPublishConfirmationDialog() {
        // First fetch the latest data from Firestore
        String userId = GameConstants.INSTANCE.getCurrentUserUid();
        db.collection("users").document(userId)
                .collection("levels").document(levelUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        Long bestTime = doc.getLong("targetCompletionTime");
                        Boolean isVerified = doc.getBoolean("isVerified");
                        
                        // Show dialog with fresh data
                        showPublishConfirmationDialogWithData(bestTime != null ? bestTime : -1L, 
                                                             isVerified != null ? isVerified : false);
                    } else {
                        // Fallback to current data if fetch fails
                        showPublishConfirmationDialogWithData(levelBestCompletionTime, isLevelVerified);
                    }
                });
    }
    
    /**
     * Shows publish confirmation dialog with provided data
     */
    private void showPublishConfirmationDialogWithData(long bestCompletionTime, boolean isVerified) {
        // Format the best completion time
        String timeDisplay = "--";
        if (bestCompletionTime > 0) {
            timeDisplay = formatTime(bestCompletionTime);
        }
        
        // Show animated confirmation dialog
        // Yes button clicked - publish the level
        UniversalDialogAnimator.AnimatedDialog animatedDialog = UniversalDialogAnimator.showConfirmationDialog(
            this,
            "Do you wish to publish your Trial?",
            "Your current high score (best completion time) is: " + timeDisplay + 
            "\nWhen you publish this creation, that score will be the target completion time for this trial!",
                this::publishLevelToPublic,
            () -> {
                // No button clicked - just dismiss (handled automatically)
            }
        );
        
        // Show the animated dialog
        animatedDialog.show();
    }
    
    /**
     * Shows unpublish confirmation dialog
     */
    private void showUnpublishConfirmationDialog() {
        // Show animated confirmation dialog
        // Yes button clicked - unpublish the level
        UniversalDialogAnimator.AnimatedDialog animatedDialog = UniversalDialogAnimator.showConfirmationDialog(
            this,
            "Do you truly wish to dismantle your Trial?",
            "Dismantling your Trial will return it to your personal private collection.\n" +
            "However, this will reset all its Likes & Views and it will clear all the comments! Do you wish to proceed?",
                this::unpublishLevelToPrivate,
            () -> {
                // No button clicked - just dismiss (handled automatically)
            }
        );
        
        // Show the animated dialog
        animatedDialog.show();
    }
    
    /**
     * Formats time in MM:SS format
     */
    @SuppressLint("DefaultLocale")
    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
}
