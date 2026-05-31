package com.unipi.alexandris.android.echotrialsonandroid.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.levelcreator.LevelCreatorActivity;
import com.unipi.alexandris.android.echotrialsonandroid.utility.SessionManager;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.ButtonSoundHelper;

/**
 * CreatorMainActivity serves as a hub for level creation and management features.
 * Provides access to personal levels, favorites, and level search functionality.
 * Requires online authentication to access.
 */
public class CreatorMainActivity extends AppCompatActivity {
    
    // UI Components
    private ImageButton backButton;
    private ImageButton favouritesButton;
    private ImageButton creatorButton;
    private ImageButton searchButton;
    
    // Session management
    private SessionManager sessionManager;
    
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creator_main);
        
        // Initialize session manager
        SessionManager.INSTANCE.initialize(this);
        sessionManager = SessionManager.INSTANCE;
        
        // Initialize views
        backButton = findViewById(R.id.backButton2);
        favouritesButton = findViewById(R.id.favouritesButton);
        creatorButton = findViewById(R.id.CreatorButton);
        searchButton = findViewById(R.id.SearchButton);
        
        // Set up button animations and click listeners
        setupButtonAnimations();
        
        // Set up back button
        backButton.setOnTouchListener(LevelCreatorActivity.createButtonTapFeedback());
        ButtonSoundHelper.addClickSound(backButton, v -> finish());
        
        // Verify user is authenticated
        if (!sessionManager.isAuthenticated()) {
            Toast.makeText(this, "Please sign in to access Creator features", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }
    
    /**
     * Sets up button animations and click listeners
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonAnimations() {
        // Apply touch animations to all buttons
        View.OnTouchListener touchListener = LevelCreatorActivity.createButtonTapFeedback();
        
        // Set up button click listeners with sounds
        favouritesButton.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(favouritesButton, v -> handleFavouritesButton());
        
        creatorButton.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(creatorButton, v -> handleCreatorButton());
        
        searchButton.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(searchButton, v -> handleSearchButton());
    }
    
    /**
     * Handles favourites button click
     */
    private void handleFavouritesButton() {
        // TODO: Implement favourites functionality
        Toast.makeText(this, "Favourites feature coming soon!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Handles creator button click - opens personal levels management
     */
    private void handleCreatorButton() {
        Intent intent = new Intent(this, CreatorMyLevelsActivity.class);
        startActivity(intent);
    }
    
    /**
     * Handles search button click
     */
    private void handleSearchButton() {
        Intent intent = new Intent(this, CreatorPublicActivity.class);
        startActivity(intent);
    }
    
    @Override
    public void onBackPressed() {
        // Return to MainActivity
        super.onBackPressed();
    }
}
