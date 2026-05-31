package com.unipi.alexandris.android.echotrialsonandroid.cosmetics;

@SuppressWarnings("unused")
public enum CosmeticType {

    PLAYER_SPRITE("Player Sprite", "Change the player's appearance"),
    DEATH_SOUND("Death Sound", "Change the sound played when the player dies"),
    DEATH_PARTICLES("Death Particles", "Change the particle effect when the player dies"),
    PARTICLE_TRAIL("Particle Trail", "Change the trail particles that follow the player"),
    
    // Future expandable categories (examples)
    // JUMP_SOUND("Jump Sound", "Change the sound played when the player jumps"),
    // LANDING_SOUND("Landing Sound", "Change the sound played when the player lands"),
    // UI_SOUND_PACK("UI Sound Pack", "Change the user interface sound effects"),
    // BACKGROUND_MUSIC("Background Music", "Change the background music theme");
    ;
    
    private final String displayName;
    private final String description;
    
    CosmeticType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isVisual() {
        return this == PLAYER_SPRITE || this == DEATH_PARTICLES || this == PARTICLE_TRAIL;
    }

    public boolean isAudio() {
        return this == DEATH_SOUND;
    }

    public boolean isParticleEffect() {
        return this == DEATH_PARTICLES || this == PARTICLE_TRAIL;
    }
}
