package com.unipi.alexandris.android.echotrialsonandroid.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;
import java.util.HashMap;
import java.util.Map;
import com.unipi.alexandris.android.echotrialsonandroid.data.PlayerStatistics;
import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;

/**
 * SessionManager handles Firebase authentication state and player data management.
 * Provides session persistence and encrypted local storage for player statistics.
 * Implemented as enum singleton for thread safety and serialization protection.
 */
public enum SessionManager {
    INSTANCE;
    
    private static final String TAG = "SessionManager";
    private static final String PREFS_NAME = "EchoTrialsSession";
    private static final String KEY_PLAYER_STATS = "encrypted_player_stats";
    private static final String KEY_SESSION_STATE = "session_state";
    private static final String KEY_LAST_SYNC = "last_sync_timestamp";
    
    public enum SessionState {
        OFFLINE,
        ONLINE
    }
    
    /**
     * Interface for listening to session state changes
     */
    public interface SessionStateListener {
        void onSessionStateChanged(SessionState newState);
        void onUserAuthenticated(FirebaseUser user);
        void onUserSignedOut();
    }

    private Context context;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private EncryptionManager encryptionManager;
    private SharedPreferences sharedPreferences;
    
    private PlayerStatistics playerStatistics;
    private SessionState currentState;
    private SessionStateListener sessionStateListener;
    private boolean isInitialized = false;

    public boolean isInitialized() {
        return isInitialized;
    }

    public void initialize(Context context) {
        if (isInitialized) {
            return; // Already initialized
        }
        this.context = context.getApplicationContext();
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.encryptionManager = new EncryptionManager(context);
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        initializeSession();
        setupAuthStateListener();
        isInitialized = true;
    }

    private void initializeSession() {
        // Load encrypted player statistics
        loadPlayerStatistics();
        
        // Determine initial session state
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        this.currentState = (currentUser != null) ? SessionState.ONLINE : SessionState.OFFLINE;
        
        // Set user UID in GameConstants if user is already signed in
        if (currentState == SessionState.ONLINE) {
            GameConstants.INSTANCE.setCurrentUserUid(currentUser.getUid());
        }
    }

    private void setupAuthStateListener() {
        firebaseAuth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            SessionState newState = (user != null) ? SessionState.ONLINE : SessionState.OFFLINE;

            if (newState != currentState) {
                currentState = newState;

                // Update GameConstants with user UID
                if (currentState == SessionState.ONLINE) {
                    GameConstants.INSTANCE.setCurrentUserUid(user.getUid());
                } else {
                    GameConstants.INSTANCE.clearCurrentUserUid();
                }

                if (sessionStateListener != null) {
                    sessionStateListener.onSessionStateChanged(currentState);

                    if (currentState == SessionState.ONLINE && user != null) {
                        sessionStateListener.onUserAuthenticated(user);
                    } else if (currentState == SessionState.OFFLINE) {
                        sessionStateListener.onUserSignedOut();
                    }
                }
            }
        });
    }

    public void setSessionStateListener(SessionStateListener listener) {
        this.sessionStateListener = listener;
    }

    public SessionState getCurrentState() {
        return currentState;
    }

    public boolean isAuthenticated() {
        return currentState == SessionState.ONLINE;
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public PlayerStatistics getPlayerStatistics() {
        return playerStatistics;
    }

    public void updatePlayerStatistics(PlayerStatistics stats) {
        this.playerStatistics = stats;
        savePlayerStatistics();
    }

    private void loadPlayerStatistics() {
        try {
            String encryptedData = sharedPreferences.getString(KEY_PLAYER_STATS, null);
            
            if (encryptedData != null) {
                String decryptedJson = encryptionManager.decrypt(encryptedData);
                this.playerStatistics = PlayerStatistics.fromJson(decryptedJson);
            } else {
                // Create new player statistics if none exist
                this.playerStatistics = new PlayerStatistics();
                savePlayerStatistics();
            }
        } catch (Exception e) {
            // Create new player statistics if loading fails
            this.playerStatistics = new PlayerStatistics();
            savePlayerStatistics();
        }
    }

    public void loadPlayerStatisticsFromFile() {
        loadPlayerStatistics();
    }

    private void savePlayerStatistics() {
        try {
            String json = playerStatistics.toJson();
            String encryptedData = encryptionManager.encrypt(json);
            
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_PLAYER_STATS, encryptedData);
            editor.apply();
        } catch (Exception e) {
            System.out.println("SESSION: Failed to save player statistics: " + e.getMessage());
        }
    }

    public void uploadPlayerStatistics(OnCompleteListener<Void> listener) {
        if (!isAuthenticated()) {
            System.out.println("SESSION: Cannot upload - user not authenticated");
            return;
        }
        
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            System.out.println("SESSION: No current user for upload");
            return;
        }
        
        // Update user ID in statistics
        playerStatistics.setUserId(user.getUid());
        
        // Create a batch write for both user statistics and username registration
        WriteBatch batch = firestore.batch();
        
        // Add user statistics
        DocumentReference userStatsRef = firestore.collection("users")
                .document(user.getUid())
                .collection("data")
                .document("statistics");
        batch.set(userStatsRef, playerStatistics);
        
        // Add username registration if username exists
        if (playerStatistics.getUsername() != null && !playerStatistics.getUsername().isEmpty()) {
            DocumentReference usernameRef = firestore.collection("usernames")
                    .document(playerStatistics.getUsername());
            Map<String, Object> usernameData = new HashMap<>();
            usernameData.put("userId", user.getUid());
            usernameData.put("createdAt", System.currentTimeMillis());
            batch.set(usernameRef, usernameData);
        }
        
        // Execute the batch
        batch.commit().addOnCompleteListener(listener);
    }

    public void downloadPlayerStatistics(OnCompleteListener<DocumentSnapshot> listener) {
        if (!isAuthenticated()) {
            System.out.println("SESSION: Cannot download - user not authenticated");
            return;
        }
        
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            System.out.println("SESSION: No current user for download");
            return;
        }
        
        firestore.collection("users")
                .document(user.getUid())
                .collection("data")
                .document("statistics")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot document = task.getResult();
                        PlayerStatistics cloudStats = document.toObject(PlayerStatistics.class);
                        
                        if (cloudStats != null) {
                            // Merge with local data (cloud takes precedence)
                            mergePlayerStatistics(cloudStats);
                        }
                    }
                    
                    if (listener != null) {
                        listener.onComplete(task);
                    }
                });
    }

    private void mergePlayerStatistics(PlayerStatistics cloudStats) {
        // Cloud data takes precedence for most fields
        this.playerStatistics = cloudStats;
        
        // Ensure local user ID is preserved
        if (getCurrentUser() != null) {
            this.playerStatistics.setUserId(getCurrentUser().getUid());
        }
        
        // Save merged data locally
        savePlayerStatistics();
    }

    public void signOut() {
        // Clear user UID from GameConstants before signing out
        GameConstants.INSTANCE.clearCurrentUserUid();
        firebaseAuth.signOut();
    }

    public void clearLocalData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        
        this.playerStatistics = new PlayerStatistics();
        savePlayerStatistics();
    }

    public long getLastSyncTimestamp() {
        return sharedPreferences.getLong(KEY_LAST_SYNC, 0);
    }

    public void updateLastSyncTimestamp() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LAST_SYNC, System.currentTimeMillis());
        editor.apply();
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }
}
