package com.unipi.alexandris.android.echotrialsonandroid.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.levelcreator.LevelCreatorActivity;
import com.unipi.alexandris.android.echotrialsonandroid.utility.SessionManager;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.ButtonSoundHelper;
import com.unipi.alexandris.android.echotrialsonandroid.utility.UniversalDialogAnimator;
import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * CreatorMyLevelsActivity manages personal and public levels for the authenticated user.
 * Provides functionality to view, create, edit, and delete levels with Firestore integration.
 */
public class CreatorMyLevelsActivity extends AppCompatActivity {
    
    // UI Components
    private ImageButton backButton;
    private ImageButton switchViewButton;
    private ImageButton newLevelButton;
    private ProgressBar progressBar;
    private LinearLayout levelsContainer;
    
    // Session management
    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    
    // State management
    private boolean showingPersonalLevels = true;
    private List<LevelData> personalLevels = new ArrayList<>();
    private List<LevelData> publicLevels = new ArrayList<>();
    
    // Level data structure
    public static class LevelData {
        public String uid;
        public String name;
        public Date lastEdited;
        public boolean isPublic;
        public String levelData; // Serialized level data
        public boolean isOpenSource; // Whether the level is open source
        public boolean isVerified; // Whether the level has been verified
        public long targetCompletionTime; // Target completion time in seconds for star calculation
        public long bestCompletionTime; // Best completion time in seconds (starts at MAX_VALUE)
        
        // Default constructor required for Firestore
        public LevelData() {
            // Required empty constructor for Firestore
        }
        
        public LevelData(String uid, String name, Date lastEdited, boolean isPublic, String levelData) {
            this.uid = uid;
            this.name = name;
            this.lastEdited = lastEdited;
            this.isPublic = isPublic;
            this.levelData = levelData;
            this.isOpenSource = false; // Default to closed source
            this.isVerified = false; // Default to unverified
            this.targetCompletionTime = -1; // Default -1 seconds target
            this.bestCompletionTime = -1; // Start with no completion time
        }
    }
    
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creator_personal);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        SessionManager.INSTANCE.initialize(this);
        sessionManager = SessionManager.INSTANCE;
        
        // Verify authentication
        if (!sessionManager.isAuthenticated()) {
            Toast.makeText(this, "Please sign in to access your levels", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Initialize views
        backButton = findViewById(R.id.backButton);
        switchViewButton = findViewById(R.id.switchViewButton);
        newLevelButton = findViewById(R.id.newLevelButton);
        progressBar = findViewById(R.id.progressBar2);
        
        // Get the LinearLayout inside the ScrollView
        levelsContainer = findViewById(R.id.levelsContainer);
        
        // Set up button animations and click listeners
        setupButtonAnimations();
        
        // Set up back button
        backButton.setOnTouchListener(LevelCreatorActivity.createButtonTapFeedback());
        ButtonSoundHelper.addClickSound(backButton, v -> onBackPressed());
        
        // Load initial data (personal levels)
        loadPersonalLevels();
    }
    
    /**
     * Sets up button animations and click listeners
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonAnimations() {
        // Apply touch animations to all buttons
        View.OnTouchListener touchListener = LevelCreatorActivity.createButtonTapFeedback();
        
        switchViewButton.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(switchViewButton, v -> handleSwitchViewButton());
        
        newLevelButton.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(newLevelButton, v -> handleNewLevelButton());
    }
    
    /**
     * Handles switch view button - toggles between personal and public levels
     */
    private void handleSwitchViewButton() {
        showingPersonalLevels = !showingPersonalLevels;
        
        if (showingPersonalLevels) {
            loadPersonalLevels();
        } else {
            loadPublicLevels();
        }
    }
    
    /**
     * Handles new level button - creates a new level
     */
    private void handleNewLevelButton() {
        // Create new level with random UID
        String newLevelUid = generateLevelUid();
        
        // Launch LevelCreatorActivity with new level UID
        Intent intent = new Intent(this, LevelCreatorActivity.class);
        intent.putExtra("level_uid", newLevelUid);
        intent.putExtra("is_new_level", true);
        startActivity(intent);
    }
    
    /**
     * Loads personal levels from Firestore
     */
    private void loadPersonalLevels() {
        showLoading(true);
        
        String userId = GameConstants.INSTANCE.getCurrentUserUid();
        System.out.println("🔍 Loading personal levels for user: " + userId);
        
        db.collection("users").document(userId)
                .collection("levels")
                .whereEqualTo("isPublic", false)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        personalLevels.clear();
                        System.out.println("Found " + task.getResult().size() + " personal levels");
                        
                        for (DocumentSnapshot doc : task.getResult()) {
                            try {
                                LevelData level = doc.toObject(LevelData.class);
                                if (level != null) {
                                    level.uid = doc.getId();
                                    personalLevels.add(level);
                                    System.out.println("Loaded level: " + level.name + " (UID: " + level.uid + ")");
                                } else {
                                    System.out.println("Failed to deserialize level: " + doc.getId());
                                }
                            } catch (Exception e) {
                                System.out.println("Error deserializing level " + doc.getId() + ": " + e.getMessage());
                            }
                        }
                        displayLevels(personalLevels, true);
                    } else {
                        System.out.println("Failed to load personal levels: " +
                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        Toast.makeText(this, "Failed to load personal levels", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    /**
     * Loads public levels from Firestore (user's published levels)
     */
    private void loadPublicLevels() {
        showLoading(true);
        
        String userId = GameConstants.INSTANCE.getCurrentUserUid();
        System.out.println("Loading user's published levels: " + userId);
        
        db.collection("public_levels")
                .whereEqualTo("ownerUid", userId)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        System.out.println("Found " + task.getResult().size() + " published levels");
                        publicLevels.clear();
                        
                        for (DocumentSnapshot doc : task.getResult()) {
                            try {
                                LevelData level = doc.toObject(LevelData.class);
                                if (level != null) {
                                    level.uid = doc.getId();
                                    System.out.println("Loaded published level: " + level.name + " (UID: " + level.uid + ")");
                                    publicLevels.add(level);
                                }
                            } catch (Exception e) {
                                System.out.println("Failed to deserialize published level: " + doc.getId());
                                System.out.println("Error deserializing published level " + doc.getId() + ": " + e.getMessage());
                            }
                        }
                        
                        displayLevels(publicLevels, false);
                    } else {
                        System.out.println("Failed to load published levels: " +
                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        Toast.makeText(this, "Failed to load published levels", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    /**
     * Displays levels in the scroll view
     */
    private void displayLevels(List<LevelData> levels, boolean isPersonal) {
        levelsContainer.removeAllViews();
        
        // Sort levels by latest edit date (newest first)
        levels.sort((level1, level2) -> {
            Date date1 = level1.lastEdited != null ? level1.lastEdited : new Date(0);
            Date date2 = level2.lastEdited != null ? level2.lastEdited : new Date(0);
            return date2.compareTo(date1); // Descending order (newest first)
        });
        
        // Add level views with alternating background colors
        for (int i = 0; i < levels.size(); i++) {
            LevelData level = levels.get(i);
            View levelView = createLevelView(level, isPersonal, i);
            levelsContainer.addView(levelView);
        }
    }
    
    /**
     * Creates a view for a single level
     */
    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    private View createLevelView(LevelData level, boolean isPersonal, int index) {
        View view = LayoutInflater.from(this).inflate(R.layout.listelement_level_details, levelsContainer, false);
        
        // Apply alternating background colors
        if (index % 2 == 1) { // Every second element (odd indices)
            view.setBackgroundColor(0x32481F19); // #32481F19 with alpha
        }
        
        TextView levelNameText = view.findViewById(R.id.levelNameText);
        TextView lastEditedText = view.findViewById(R.id.textView6);
        ImageButton loadButton = view.findViewById(R.id.loadButton);
        ImageButton trashButton = view.findViewById(R.id.trashButton);
        LinearLayout loadButtonContainer = view.findViewById(R.id.loadButtonContainer);
        
        // Set level name with null safety
        levelNameText.setText(level.name != null ? level.name : "Unnamed Level");
        
        // Set last edited date with null safety
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (level.lastEdited != null) {
            lastEditedText.setText("Last Edited: " + sdf.format(level.lastEdited));
        } else {
            lastEditedText.setText("Last Edited: Unknown");
        }
        
        // Set up button animations
        View.OnTouchListener touchListener = LevelCreatorActivity.createButtonTapFeedback();
        loadButton.setOnTouchListener(touchListener);
        trashButton.setOnTouchListener(touchListener);
        
        // Set up click listeners for the new interaction pattern
        if (isPersonal) {
            // Clicking anywhere on the list item (except load button) opens LevelView
            ButtonSoundHelper.addClickSound(view, v -> handleViewLevel(level));
            
            // Clicking the load button opens LevelCreatorActivity directly
            ButtonSoundHelper.addClickSound(loadButton, v -> handleEditLevel(level));
            
            // Prevent the load button click from triggering the parent view click
            loadButton.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }
                return touchListener.onTouch(v, event);
            });
            
            // Set up delete button (visible for private levels)
            trashButton.setVisibility(View.VISIBLE);
            ButtonSoundHelper.addClickSound(trashButton, v -> handleDeleteLevel(level));
        } else {
            // For public levels, clicking anywhere opens LevelView
            ButtonSoundHelper.addClickSound(view, v -> handleViewLevel(level));
            
            // Hide both load and delete buttons for public levels
            loadButton.setVisibility(View.GONE);
            trashButton.setVisibility(View.GONE);
        }
        
        return view;
    }
    
    /**
     * Handles viewing a level (opens LevelView)
     */
    private void handleViewLevel(LevelData level) {
        Intent intent = new Intent(this, CreatorLevelViewActivity.class);
        intent.putExtra("level_uid", level.uid);
        intent.putExtra("is_private", showingPersonalLevels);
        startActivity(intent);
    }
    
    /**
     * Handles editing a level (opens LevelCreatorActivity directly)
     */
    private void handleEditLevel(LevelData level) {
        Intent intent = new Intent(this, LevelCreatorActivity.class);
        intent.putExtra("level_uid", level.uid);
        intent.putExtra("is_new_level", false);
        startActivity(intent);
    }
    
    /**
     * Handles deleting a level
     */
    private void handleDeleteLevel(LevelData level) {
        // Show animated confirmation dialog
        UniversalDialogAnimator.AnimatedDialog animatedDialog = UniversalDialogAnimator.showConfirmationDialog(
            this,
            "Are you sure you want to delete this level?",
            null, // No additional message
            () -> {
                // Yes button clicked - delete the level
                deleteLevelFromFirestore(level);
            },
            () -> {
                // No button clicked - just dismiss (handled automatically)
            }
        );
        
        // Show the animated dialog
        animatedDialog.show();
    }
    
    /**
     * Deletes a level from Firestore
     */
    private void deleteLevelFromFirestore(LevelData level) {
        showLoading(true);
        
        String userId = GameConstants.INSTANCE.getCurrentUserUid();
        db.collection("users").document(userId)
                .collection("levels")
                .document(level.uid)
                .delete()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Level deleted successfully", Toast.LENGTH_SHORT).show();
                        // Refresh the current view
                        if (showingPersonalLevels) {
                            loadPersonalLevels();
                        } else {
                            loadPublicLevels();
                        }
                    } else {
                        Toast.makeText(this, "Failed to delete level", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    /**
     * Shows or hides loading indicator
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    /**
     * Generates a unique level UID
     */
    private String generateLevelUid() {
        return "level_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the current view when returning from LevelCreatorActivity
        if (showingPersonalLevels) {
            loadPersonalLevels();
        } else {
            loadPublicLevels();
        }
    }
    
    @Override
    public void onBackPressed() {
        // Navigate to CreatorMainActivity
        super.onBackPressed();
        Intent intent = new Intent(this, CreatorMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
