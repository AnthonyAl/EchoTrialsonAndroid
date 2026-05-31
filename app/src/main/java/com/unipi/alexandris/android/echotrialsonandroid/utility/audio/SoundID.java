package com.unipi.alexandris.android.echotrialsonandroid.utility.audio;

/**
 * Enum representing different sound effect IDs used throughout the game.
 * Each SimpleID corresponds to a specific type of sound that can be played.
 * 
 * <p>This enum serves as a centralized way to reference sounds and will be
 * used by the SoundFXManager to map sounds to their respective audio files.</p>
 * 
 * <p>Sounds are categorized by their purpose:
 * <ul>
 *   <li><b>DEATH</b>: Sounds played when the player dies</li>
 *   <li><b>MOVEMENT</b>: Sounds related to player movement</li>
 *   <li><b>COLLECTIBLE</b>: Sounds for collecting items</li>
 *   <li><b>BACKGROUND</b>: SimpleBackground music for different levels</li>
 *   <li><b>UI</b>: User interface sounds</li>
 *   <li><b>EFFECTS</b>: Various game effect sounds</li>
 * </ul></p>
 */
public enum SoundID {
    // Death sounds - individual sound files
    DEATH_FALL_1,        // falling_1.wav
    DEATH_FALL_2,        // falling_2.wav
    DEATH_SPIKE_1,       // spike_damage_3.wav (rename for consistency)
    DEATH_SUFFOCATION_1,
    
    // Movement sounds
    JUMP,                // SimplePlayer jumps
    LAND,                // SimplePlayer lands on ground
    WALKING,             // SimplePlayer walking on ground
    SWIMMING,            // SimplePlayer swimming in water
    SLIDE,               // SimplePlayer sliding on ice
    
    // Collectible sounds
    COLLECT_COIN,        // Collecting coins
    COLLECT_KEY,         // Collecting keys
    
    // SimpleBackground music
    BGM_LEVEL_1,         // Level 1 background music
    BGM_LEVEL_2,         // Level 2 background music
    BGM_LEVEL_3,         // Level 3 background music
    BGM_MENU,            // Main menu background music
    
    // UI sounds
    UI_BUTTON_CLICK,     // Button click sound
    UI_MENU_OPEN,        // Menu opening sound
    UI_MENU_CLOSE,       // Menu closing sound
    
    // Game effects
    EXPLOSION,           // Explosion effect
    PORTAL,              // Portal sound when player touches portal
    WATER_SPLASH,        // Water splash effect
    ICE_BREAK,           // Ice breaking sound
    
    // Level completion
    LEVEL_COMPLETE,      // Level completion sound
    GAME_OVER,           // Game over sound
    VICTORY,             // Victory sound
    STAR_COLLECTED;      // Star collection sound when stars are first unlocked
} 