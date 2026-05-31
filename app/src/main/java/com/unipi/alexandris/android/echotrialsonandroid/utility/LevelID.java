package com.unipi.alexandris.android.echotrialsonandroid.utility;

import com.unipi.alexandris.android.echotrialsonandroid.R;

import java.io.Serializable;

/**
 * The LevelID enum represents unique identifiers for all levels in Echo Trials.
 * Each level is identified by a group (e.g., "A", "B", "C") and a part (e.g., "I", "II", "III").
 * Target completion times are in seconds for star calculation.
 */
public enum LevelID implements Serializable {
    /** Debug level */
    LEVEL_COMMUNITY_CREATED("COMMUNITY", "I", -1, -1), // 60 seconds target for community levels
    LEVEL_DEBUG_I("DEBUG", "I", R.raw.level_debug_1, 30),
    LEVEL_DEBUG_II("DEBUG", "II", R.raw.level_debug_2, 45),
    LEVEL_DEBUG_III("DEBUG", "III", R.raw.level_debug_3, 60),
    LEVEL_DEBUG_IV("DEBUG", "IV", R.raw.level_debug_4, 75),
    LEVEL_DEBUG_V("DEBUG", "V", R.raw.level_debug_5, 90),
    LEVEL_DEBUG_VI("DEBUG", "VI", R.raw.level_debug_6, 120),
    LEVEL_DEBUG_SPIKES("DEBUG", "VI", R.raw.level_debug_spikes, 40),
    LEVEL_MAIN_MENU_I("MAIN_MENU", "I", R.raw.level_main_menu_1,-1),
    
    // Challenge Levels - Pits (A)
    LEVEL_A_I("A", "I", R.raw.level_a_1, 10),     // Pits Challenge Level 1
    LEVEL_A_II("A", "II", R.raw.level_a_2, 10),   // Pits Challenge Level 2
    LEVEL_A_III("A", "III", R.raw.level_a_3, 10), // Pits Challenge Level 3
    LEVEL_A_IV("A", "IV", R.raw.level_a_4, 10),   // Pits Challenge Level 4
    LEVEL_A_V("A", "V", R.raw.level_a_5, 10),     // Pits Challenge Level 5
    
    // Challenge Levels - Spikes (B)
    LEVEL_B_I("B", "I", R.raw.level_b_1, 10),     // Spikes Challenge Level 1
    LEVEL_B_II("B", "II", R.raw.level_b_2, 10),   // Spikes Challenge Level 2
    LEVEL_B_III("B", "III", R.raw.level_b_3, 10), // Spikes Challenge Level 3
    LEVEL_B_IV("B", "IV", R.raw.level_b_4, 10),   // Spikes Challenge Level 4
    LEVEL_B_V("B", "V", R.raw.level_b_5, 15),     // Spikes Challenge Level 5
    
    // Challenge Levels - Push (C)
    LEVEL_C_I("C", "I", R.raw.level_c_1, 5),     // Push Challenge Level 1
    LEVEL_C_II("C", "II", R.raw.level_c_2, 5),   // Push Challenge Level 2
    LEVEL_C_III("C", "III", R.raw.level_c_3, 20), // Push Challenge Level 3
    LEVEL_C_IV("C", "IV", R.raw.level_c_4, 10),   // Push Challenge Level 4
    LEVEL_C_V("C", "V", R.raw.level_c_5, 10),     // Push Challenge Level 5
    
    // Challenge Levels - Gravity (D)
    LEVEL_D_I("D", "I", R.raw.level_a_1, 30),     // Gravity Challenge Level 1
    LEVEL_D_II("D", "II", R.raw.level_debug_6, 45),   // Gravity Challenge Level 2
    LEVEL_D_III("D", "III", R.raw.level_a_1, 60), // Gravity Challenge Level 3
    LEVEL_D_IV("D", "IV", R.raw.level_debug_6, 75),   // Gravity Challenge Level 4
    LEVEL_D_V("D", "V", R.raw.level_a_1, 90),     // Gravity Challenge Level 5
    
    // Challenge Levels - Agility (E)
    LEVEL_E_I("E", "I", R.raw.level_a_1, 30),     // Agility Challenge Level 1
    LEVEL_E_II("E", "II", R.raw.level_debug_6, 45),   // Agility Challenge Level 2
    LEVEL_E_III("E", "III", R.raw.level_a_1, 60), // Agility Challenge Level 3
    LEVEL_E_IV("E", "IV", R.raw.level_debug_6, 75),   // Agility Challenge Level 4
    LEVEL_E_V("E", "V", R.raw.level_a_1, 90);     // Agility Challenge Level 5

    /** The group this level belongs to (e.g., "A", "B", "TEST", "MAIN") */
    public final String group;
    
    /** The part number of this level within its group (e.g., "I", "II", "III") */
    public final String part;

    public final int resourceId;
    
    /** Target completion time in seconds for star calculation */
    public final long targetCompletionTime;

    /**
     * Constructs a new LevelID with the specified group, part, resource ID, and target completion time.
     */
    LevelID(String group, String part, int resourceId, long targetCompletionTime) {
        this.group = group;
        this.part = part;
        this.resourceId = resourceId;
        this.targetCompletionTime = targetCompletionTime;
    }

    /**
     * Retrieves a LevelID by its exact name.
     */
    public static LevelID getByName(String name) {
        for(LevelID levelID : LevelID.values()) 
            if(levelID.name().equals(name)) 
                return levelID;
        return null;
    }
} 