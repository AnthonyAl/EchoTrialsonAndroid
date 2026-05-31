package com.unipi.alexandris.android.echotrialsonandroid.cosmetics;

import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.SoundFXManager;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.SoundID;
import com.unipi.alexandris.android.echotrialsonandroid.data.DeathCause;

/**
 * Manages cosmetic sound effects by integrating the cosmetic system with the sound system.
 * Routes sound requests through the player's equipped cosmetics before playing them.
 */
public class CosmeticSoundManager {
    
    private final SoundFXManager soundFXManager;
    
    public CosmeticSoundManager() {
        // Get the sound manager that should already be initialized by GameConstants
        this.soundFXManager = GameConstants.INSTANCE.getSoundManager();
        if (this.soundFXManager == null) {
            throw new IllegalStateException("SoundFXManager must be initialized before CosmeticSoundManager");
        }
        // Initialize equipped cosmetics to defaults for new players
        EquippedCosmetics.INSTANCE.resetToDefaults();
    }

    public void playDeathSound(DeathCause deathCause) {
        AvailableCosmetics equippedSound = EquippedCosmetics.INSTANCE.getEquippedDeathSound(deathCause);
        if (equippedSound != null && equippedSound.getResource() instanceof SoundID) {
            SoundID soundId = (SoundID) equippedSound.getResource();
            soundFXManager.playSound(soundId);
        }
    }
    
}
