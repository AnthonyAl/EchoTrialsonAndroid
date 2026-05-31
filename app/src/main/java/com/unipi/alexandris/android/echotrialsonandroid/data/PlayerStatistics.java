package com.unipi.alexandris.android.echotrialsonandroid.data;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class PlayerStatistics {
    
    // Basic Info
    @SerializedName("username")
    private String username;
    
    @SerializedName("age")
    private int age; // hidden, encrypted
    
    @SerializedName("gender")
    private String gender; // hidden, encrypted
    
    @SerializedName("userId")
    private String userId; // Firebase UID
    
    // Achievement System
    @SerializedName("stars")
    private int stars; // achievement points
    
    @SerializedName("coins")
    private int coins; // future currency
    
    @SerializedName("ultraDifficultyLevelsCompleted")
    private int ultraDifficultyLevelsCompleted;
    
    @SerializedName("creatorCompletedLevels")
    private int creatorCompletedLevels;
    
    @SerializedName("totalProfileLikes")
    private int totalProfileLikes;
    
    // Lists (Firestore arrays)
    @SerializedName("completedMainLevels")
    private Map<String, Long> completedMainLevels; // LevelID.name() -> completion time in seconds (null/negative = not completed)
    
    @SerializedName("completedCreatorLevels")
    private Map<String, Long> completedCreatorLevels; // Level UID -> completion time in seconds (null/negative = not completed)
    
    @SerializedName("favouriteCreatorLevels")
    private List<String> favouriteCreatorLevels;
    
    @SerializedName("unlockedCosmetics")
    private List<String> unlockedCosmetics;
    
    @SerializedName("equippedCosmetics")
    private Map<String, String> equippedCosmetics; // CosmeticType.name() -> AvailableCosmetics.getId()
    
    @SerializedName("unlockedAchievements")
    private List<String> unlockedAchievements;
    
    @SerializedName("uploadedProfileNotices")
    private List<String> uploadedProfileNotices;
    
    // Timestamps
    @SerializedName("lastUpdated")
    private long lastUpdated;
    
    @SerializedName("createdAt")
    private long createdAt;

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public PlayerStatistics() {
        this.username = "Player";
        this.age = 0;
        this.gender = "Not specified";
        this.userId = "";
        this.stars = 0;
        this.coins = 0;
        this.ultraDifficultyLevelsCompleted = 0;
        this.creatorCompletedLevels = 0;
        this.totalProfileLikes = 0;
        
        // Initialize empty lists and maps
        this.completedMainLevels = new HashMap<>();
        this.completedCreatorLevels = new HashMap<>();
        this.favouriteCreatorLevels = new ArrayList<>();
        this.unlockedCosmetics = new ArrayList<>();
        this.equippedCosmetics = new HashMap<>();
        this.unlockedAchievements = new ArrayList<>();
        this.uploadedProfileNotices = new ArrayList<>();
        
        // Set timestamps
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }

    public PlayerStatistics(String userId) {
        this();
        this.userId = userId;
    }

    public String toJson() {
        return gson.toJson(this);
    }

    public static PlayerStatistics fromJson(String json) {
        try {
            return gson.fromJson(json, PlayerStatistics.class);
        } catch (Exception e) {
            System.out.println("PLAYER STATS: Error parsing JSON: " + e.getMessage());
            return new PlayerStatistics();
        }
    }

    public void updateTimestamp() {
        this.lastUpdated = System.currentTimeMillis();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { 
        this.username = username; 
        updateTimestamp();
    }
    public int getAge() { return age; }
    public void setAge(int age) { 
        this.age = age; 
        updateTimestamp();
    }
    public String getGender() { return gender; }
    public void setGender(String gender) { 
        this.gender = gender; 
        updateTimestamp();
    }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { 
        this.userId = userId; 
        updateTimestamp();
    }
    public int getStars() { return stars; }
    public void setStars(int stars) { 
        this.stars = stars; 
        updateTimestamp();
    }
    public void addStars(int starsToAdd) {
        this.stars += starsToAdd;
        updateTimestamp();
    }
    public int getCoins() { return coins; }
    public void setCoins(int coins) { 
        this.coins = coins; 
        updateTimestamp();
    }
    public void addCoins(int coinsToAdd) {
        this.coins += coinsToAdd;
        updateTimestamp();
    }
    public int getUltraDifficultyLevelsCompleted() { return ultraDifficultyLevelsCompleted; }
    public void setUltraDifficultyLevelsCompleted(int count) { 
        this.ultraDifficultyLevelsCompleted = count; 
        updateTimestamp();
    }
    public void incrementUltraDifficultyLevels() {
        this.ultraDifficultyLevelsCompleted++;
        updateTimestamp();
    }
    public int getCreatorCompletedLevels() { return creatorCompletedLevels; }
    public void setCreatorCompletedLevels(int count) { 
        this.creatorCompletedLevels = count; 
        updateTimestamp();
    }
    public void incrementCreatorCompletedLevels() {
        this.creatorCompletedLevels++;
        updateTimestamp();
    }
    public int getTotalProfileLikes() { return totalProfileLikes; }
    public void setTotalProfileLikes(int likes) { 
        this.totalProfileLikes = likes; 
        updateTimestamp();
    }
    public void addProfileLikes(int likesToAdd) {
        this.totalProfileLikes += likesToAdd;
        updateTimestamp();
    }
    public Map<String, Long> getCompletedMainLevels() { return completedMainLevels; }
    public void setCompletedMainLevels(Map<String, Long> levels) { 
        this.completedMainLevels = levels; 
        updateTimestamp();
    }
    public Long getMainLevelCompletionTime(String levelId) {
        return completedMainLevels.get(levelId);
    }
    public void setMainLevelCompletionTime(String levelId, long completionTime) {
        completedMainLevels.put(levelId, completionTime);
        updateTimestamp();
    }
    public boolean hasCompletedMainLevel(String levelId) {
        Long time = completedMainLevels.get(levelId);
        return time != null && time > 0;
    }
    public Map<String, Long> getCompletedCreatorLevels() { return completedCreatorLevels; }
    public void setCompletedCreatorLevels(Map<String, Long> levels) { 
        this.completedCreatorLevels = levels; 
        updateTimestamp();
    }
    public Long getCreatorLevelCompletionTime(String levelUid) {
        return completedCreatorLevels.get(levelUid);
    }
    public void setCreatorLevelCompletionTime(String levelUid, long completionTime) {
        completedCreatorLevels.put(levelUid, completionTime);
        updateTimestamp();
    }
    public boolean hasCompletedCreatorLevel(String levelUid) {
        Long time = completedCreatorLevels.get(levelUid);
        return time != null && time > 0;
    }

    /**
     * Calculates stars earned for a main level completion
     * @param levelId The LevelID name (e.g., "LEVEL_DEBUG_I")
     * @param completionTime Completion time in seconds
     * @return Number of stars earned (1, 2, or 3)
     */
    public int calculateMainLevelStars(String levelId, long completionTime) {
        // Find the LevelID enum to get target time
        for (com.unipi.alexandris.android.echotrialsonandroid.utility.LevelID levelID : 
             com.unipi.alexandris.android.echotrialsonandroid.utility.LevelID.values()) {
            if (levelID.name().equals(levelId)) {
                return calculateStars(completionTime, levelID.targetCompletionTime);
            }
        }
        return 1;
    }
    
    /**
     * Calculates stars earned for a creator level completion
     * @param levelUid The level UID
     * @param completionTime Completion time in seconds
     * @param targetTime Target completion time in seconds (from level data)
     * @return Number of stars earned (1, 2, or 3)
     */
    public int calculateCreatorLevelStars(String levelUid, long completionTime, long targetTime) {
        return calculateStars(completionTime, targetTime);
    }
    
    /**
     * Calculates stars based on completion time vs target time
     * @param completionTime Actual completion time in seconds
     * @param targetTime Target completion time in seconds
     * @return Number of stars earned (1, 2, or 3)
     */
    private int calculateStars(long completionTime, long targetTime) {
        if (targetTime <= 0) return 1; // No target time set, default to 1 star
        
        if (completionTime <= targetTime) {
            return 3; // 3 stars if completed at or faster than target time
        } else if (completionTime <= targetTime * 1.5) { // 75% of target time = 1.5x target
            return 2; // 2 stars if completed within 75% of target time
        } else {
            return 1; // 1 star if completed beyond 75% of target time
        }
    }
    
    public List<String> getFavouriteCreatorLevels() { return favouriteCreatorLevels; }
    public void setFavouriteCreatorLevels(List<String> levels) { 
        this.favouriteCreatorLevels = levels; 
        updateTimestamp();
    }
    
    public void addFavouriteCreatorLevel(String levelId) {
        if (!favouriteCreatorLevels.contains(levelId)) {
            favouriteCreatorLevels.add(levelId);
            updateTimestamp();
        }
    }
    
    public void removeFavouriteCreatorLevel(String levelId) {
        favouriteCreatorLevels.remove(levelId);
        updateTimestamp();
    }
    
    public boolean hasFavouriteCreatorLevel(String levelId) {
        return favouriteCreatorLevels.contains(levelId);
    }
    
    public List<String> getUnlockedCosmetics() { return unlockedCosmetics; }
    public void setUnlockedCosmetics(List<String> cosmetics) { 
        this.unlockedCosmetics = cosmetics; 
        updateTimestamp();
    }
    
    public Map<String, String> getEquippedCosmetics() { return equippedCosmetics; }
    public void setEquippedCosmetics(Map<String, String> equippedCosmetics) {
        this.equippedCosmetics = equippedCosmetics;
        updateTimestamp();
    }
    
    public void unlockCosmetic(String cosmeticId) {
        if (!unlockedCosmetics.contains(cosmeticId)) {
            unlockedCosmetics.add(cosmeticId);
            updateTimestamp();
        }
    }
    
    public boolean hasUnlockedCosmetic(String cosmeticId) {
        return unlockedCosmetics.contains(cosmeticId);
    }
    
    public List<String> getUnlockedAchievements() { return unlockedAchievements; }
    public void setUnlockedAchievements(List<String> achievements) { 
        this.unlockedAchievements = achievements; 
        updateTimestamp();
    }
    
    public void unlockAchievement(String achievementId) {
        if (!unlockedAchievements.contains(achievementId)) {
            unlockedAchievements.add(achievementId);
            updateTimestamp();
        }
    }
    
    public boolean hasUnlockedAchievement(String achievementId) {
        return unlockedAchievements.contains(achievementId);
    }
    
    public List<String> getUploadedProfileNotices() { return uploadedProfileNotices; }
    public void setUploadedProfileNotices(List<String> notices) { 
        this.uploadedProfileNotices = notices; 
        updateTimestamp();
    }
    
    public void addProfileNotice(String noticeId) {
        if (!uploadedProfileNotices.contains(noticeId)) {
            uploadedProfileNotices.add(noticeId);
            updateTimestamp();
        }
    }
    
    public long getLastUpdated() { return lastUpdated; }
    public long getCreatedAt() { return createdAt; }
    
    @NonNull
    @Override
    public String toString() {
        return "PlayerStatistics{" +
                "username='" + username + '\'' +
                ", stars=" + stars +
                ", coins=" + coins +
                ", ultraDifficultyLevelsCompleted=" + ultraDifficultyLevelsCompleted +
                ", creatorCompletedLevels=" + creatorCompletedLevels +
                ", totalProfileLikes=" + totalProfileLikes +
                ", completedMainLevels=" + completedMainLevels.size() +
                ", completedCreatorLevels=" + completedCreatorLevels.size() +
                ", favouriteCreatorLevels=" + favouriteCreatorLevels.size() +
                ", unlockedCosmetics=" + unlockedCosmetics.size() +
                ", unlockedAchievements=" + unlockedAchievements.size() +
                '}';
    }
}
