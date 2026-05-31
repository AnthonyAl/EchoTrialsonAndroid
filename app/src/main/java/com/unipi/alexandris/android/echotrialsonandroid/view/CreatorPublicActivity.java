package com.unipi.alexandris.android.echotrialsonandroid.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.view.CreatorMyLevelsActivity.LevelData;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.ButtonSoundHelper;
import com.unipi.alexandris.android.echotrialsonandroid.utility.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class CreatorPublicActivity extends AppCompatActivity {
    
    private LinearLayout levelsContainer;
    private ProgressBar progressBar;
    private ImageButton sortByDateButton;
    private ImageButton sortByPopularityButton;
    
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private List<LevelData> publicLevels;
    private boolean sortByDate = true; // true = newest first, false = most liked first
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creator_public);
        
        // Initialize Firebase and SessionManager
        db = FirebaseFirestore.getInstance();
        SessionManager.INSTANCE.initialize(this);
        sessionManager = SessionManager.INSTANCE;
        
        // Initialize views
        levelsContainer = findViewById(R.id.levelsContainer);
        progressBar = findViewById(R.id.progressBar2);
        sortByDateButton = findViewById(R.id.sortByDateButton);
        sortByPopularityButton = findViewById(R.id.sortByPopularityButton);
        
        // Set up back button
        ImageButton backButton = findViewById(R.id.backButton);
        ButtonSoundHelper.addClickSound(backButton, v -> onBackPressed());
        
        // Set up sort buttons
        ButtonSoundHelper.addClickSound(sortByDateButton, v -> {
            sortByDate = true;
            loadPublicLevels();
        });
        
        ButtonSoundHelper.addClickSound(sortByPopularityButton, v -> {
            sortByDate = false;
            loadPublicLevels();
        });
        
        // Load public levels
        loadPublicLevels();
    }
    
    private void loadPublicLevels() {
        showLoading(true);
        
        Query query = db.collection("public_levels");
        
        // Apply sorting
        if (sortByDate) {
            query = query.orderBy("lastEdited", Query.Direction.DESCENDING); // Newest first
        } else {
            // For now, sort by name since we don't have likes field
            query = query.orderBy("name", Query.Direction.ASCENDING);
        }
        
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    publicLevels = new ArrayList<>();
                    
                    for (var document : queryDocumentSnapshots) {
                        LevelData levelData = document.toObject(LevelData.class);
                        levelData.uid = document.getId();
                        publicLevels.add(levelData);
                    }
                    
                    displayLevels();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    // Handle error
                });
    }
    
    @SuppressLint("SetTextI18n")
    private void displayLevels() {
        levelsContainer.removeAllViews();
        
        if (publicLevels.isEmpty()) {
            TextView noLevelsText = new TextView(this);
            noLevelsText.setText("No public levels available");
            noLevelsText.setTextColor(getResources().getColor(android.R.color.white));
            noLevelsText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            levelsContainer.addView(noLevelsText);
            return;
        }
        
        for (LevelData level : publicLevels) {
            View levelView = createLevelView(level);
            levelsContainer.addView(levelView);
        }
    }
    
    @SuppressLint("SetTextI18n")
    private View createLevelView(LevelData level) {
        View levelView = LayoutInflater.from(this).inflate(R.layout.listelement_level_details, levelsContainer, false);
        
        TextView levelNameText = levelView.findViewById(R.id.levelNameText);
        TextView lastEditedText = levelView.findViewById(R.id.textView6);
        ImageButton loadButton = levelView.findViewById(R.id.loadButton);
        ImageButton trashButton = levelView.findViewById(R.id.trashButton);
        
        // Set level name
        levelNameText.setText(level.name);
        
        // Set last edited date
        if (level.lastEdited != null) {
            lastEditedText.setText("Last Edited: " + level.lastEdited.toString());
        } else {
            lastEditedText.setText("Last Edited: Unknown");
        }
        
        // Hide both load and trash buttons for public levels
        loadButton.setVisibility(View.GONE);
        trashButton.setVisibility(View.GONE);
        
        // Set up click listener for the entire view to open LevelView
        ButtonSoundHelper.addClickSound(levelView, v -> handleViewLevel(level));
        
        return levelView;
    }
    
    /**
     * Handles viewing a level (opens LevelView)
     */
    private void handleViewLevel(LevelData level) {
        Intent intent = new Intent(this, CreatorLevelViewActivity.class);
        intent.putExtra("level_uid", level.uid);
        intent.putExtra("is_private", false); // This is a public level
        startActivity(intent);
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        levelsContainer.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, CreatorMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
