package com.unipi.alexandris.android.echotrialsonandroid.cosmetics;

import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.SoundID;
import com.unipi.alexandris.android.echotrialsonandroid.data.DeathCause;

public enum AvailableCosmetics {
    
    // ===== DEATH SOUNDS =====

    DEATH_SPIKE_DEFAULT("death_spike_default", CosmeticType.DEATH_SOUND, SoundID.DEATH_SPIKE_1, DeathCause.SPIKE),
    DEATH_SUFFOCATION_DEFAULT("death_suffocation_default", CosmeticType.DEATH_SOUND, SoundID.DEATH_SUFFOCATION_1, DeathCause.SUFFOCATION),
    DEATH_FALL_VARIANT_1("death_fall_variant_1", CosmeticType.DEATH_SOUND, SoundID.DEATH_FALL_1, DeathCause.FALL),
    DEATH_FALL_VARIANT_2("death_fall_variant_2", CosmeticType.DEATH_SOUND, SoundID.DEATH_FALL_2, DeathCause.FALL),
    DEATH_TAP_DEFAULT("death_tap_default", CosmeticType.DEATH_SOUND, SoundID.DEATH_FALL_2, DeathCause.TAP),
    

    // ===== DEATH PARTICLES =====
    PARTICLES_DEATH_DEFAULT("particles_death_default", CosmeticType.DEATH_PARTICLES, "default_explosion"),
    PARTICLES_DEATH_SPARKLES("particles_death_sparkles", CosmeticType.DEATH_PARTICLES, "sparkle_explosion"),
    PARTICLES_DEATH_SMOKE("particles_death_smoke", CosmeticType.DEATH_PARTICLES, "smoke_explosion"),


    // ===== PARTICLE TRAILS =====
    TRAIL_DEFAULT("trail_default", CosmeticType.PARTICLE_TRAIL, "sprite_trail"),
    TRAIL_NONE("trail_none", CosmeticType.PARTICLE_TRAIL, "none"),
    TRAIL_FIRE("trail_fire", CosmeticType.PARTICLE_TRAIL, "fire_trail"),
    TRAIL_ICE("trail_ice", CosmeticType.PARTICLE_TRAIL, "ice_trail"),


    // ===== PLAYER SPRITES =====
    PLAYER_DEFAULT("player_default", CosmeticType.PLAYER_SPRITE, "textures/player/default"),
    PLAYER_BLUE("player_blue", CosmeticType.PLAYER_SPRITE, "textures/player/blue"),
    PLAYER_RED("player_red", CosmeticType.PLAYER_SPRITE, "textures/player/red");
    
    private final String id;
    private final CosmeticType type;
    private final Object resource; // Can be SoundID, String, or other resource type
    private final DeathCause deathCause;
    
    // Constructor for death sounds
    AvailableCosmetics(String id, CosmeticType type, SoundID soundId, DeathCause deathCause) {
        this.id = id;
        this.type = type;
        this.resource = soundId;
        this.deathCause = deathCause;
    }
    
    // Constructor for other cosmetics
    @SuppressWarnings("unused")
    AvailableCosmetics(String id, CosmeticType type, Object resource) {
        this.id = id;
        this.type = type;
        this.resource = resource;
        this.deathCause = null;
    }
    
    // Constructor for non-sound cosmetics
    AvailableCosmetics(String id, CosmeticType type, String resourcePath) {
        this.id = id;
        this.type = type;
        this.resource = resourcePath;
        this.deathCause = null;
    }
    
    public String getId() { return id; }
    public CosmeticType getType() { return type; }
    public Object getResource() { return resource; }
    public DeathCause getDeathCause() { return deathCause; }

    public static AvailableCosmetics[] getCosmeticsOfType(CosmeticType type) {
        return java.util.Arrays.stream(values())
                .filter(cosmetic -> cosmetic.getType() == type)
                .toArray(AvailableCosmetics[]::new);
    }

    public static AvailableCosmetics[] getDeathSoundsForCause(DeathCause cause) {
        return java.util.Arrays.stream(values())
                .filter(cosmetic -> cosmetic.getType() == CosmeticType.DEATH_SOUND)
                .filter(cosmetic -> cosmetic.getDeathCause() == cause)
                .toArray(AvailableCosmetics[]::new);
    }

    @SuppressWarnings("unused")
    public static AvailableCosmetics findById(String id) {
        for (AvailableCosmetics cosmetic : values()) {
            if (cosmetic.getId().equals(id)) {
                return cosmetic;
            }
        }
        return null;
    }
}
