package com.unipi.alexandris.android.echotrialsonandroid.cosmetics;

import com.unipi.alexandris.android.echotrialsonandroid.data.DeathCause;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public enum EquippedCosmetics {
    INSTANCE;
    
    // Background mode flag - when true, randomly selects from ALL available cosmetics
    private boolean isBackgroundMode = false;
    private final List<AvailableCosmetics> equippedSpikeDeathSounds = new ArrayList<>();
    private final List<AvailableCosmetics> equippedSuffocationDeathSounds = new ArrayList<>();
    private final List<AvailableCosmetics> equippedFallDeathSounds = new ArrayList<>();
    private final List<AvailableCosmetics> equippedTapDeathSounds = new ArrayList<>();
    
    private AvailableCosmetics equippedDeathParticles = AvailableCosmetics.PARTICLES_DEATH_DEFAULT;
    private AvailableCosmetics equippedParticleTrail = AvailableCosmetics.TRAIL_DEFAULT; // Default sprite trail
    private AvailableCosmetics equippedPlayerSprite = AvailableCosmetics.PLAYER_DEFAULT;

    EquippedCosmetics() {
        equippedSpikeDeathSounds.add(AvailableCosmetics.DEATH_SPIKE_DEFAULT);
        equippedSuffocationDeathSounds.add(AvailableCosmetics.DEATH_SUFFOCATION_DEFAULT);
        equippedFallDeathSounds.add(AvailableCosmetics.DEATH_FALL_VARIANT_1);
    }

    public void setBackgroundMode(boolean enabled) {
        this.isBackgroundMode = enabled;
    }

    @SuppressWarnings("unused")
    public boolean isBackgroundMode() {
        return isBackgroundMode;
    }
    
    /**
     * Gets the equipped death sound for a specific death cause
     * For sound cosmetics, randomly selects from all available cosmetics of that death cause
     * For TAP death, randomly chooses between spike and fall sounds
     */
     public AvailableCosmetics getEquippedDeathSound(DeathCause deathCause) {
         ThreadLocalRandom random = ThreadLocalRandom.current();
         
         // BACKGROUND MODE: Randomly select from ALL available cosmetics
         if (isBackgroundMode) {
             return getRandomDeathSoundFromAllAvailable(deathCause, random);
         }
         
         // NORMAL MODE: Use equipped cosmetics
         switch (deathCause) {
             case SPIKE:
                 // Randomly select from equipped spike death sounds
                 if (!equippedSpikeDeathSounds.isEmpty()) {
                     return equippedSpikeDeathSounds.get(random.nextInt(equippedSpikeDeathSounds.size()));
                 }
                 return AvailableCosmetics.DEATH_SPIKE_DEFAULT;

             case SUFFOCATION:
                 // Randomly select from equipped spike death sounds
                 if (!equippedSuffocationDeathSounds.isEmpty()) {
                     return equippedSuffocationDeathSounds.get(random.nextInt(equippedSuffocationDeathSounds.size()));
                 }
                 return AvailableCosmetics.DEATH_SUFFOCATION_DEFAULT;
                 
             case FALL:
                 // Randomly select from equipped fall death sounds
                 if (!equippedFallDeathSounds.isEmpty()) {
                     return equippedFallDeathSounds.get(random.nextInt(equippedFallDeathSounds.size()));
                 }
                 return AvailableCosmetics.DEATH_FALL_VARIANT_1;
                 
             case TAP:
                 // Special case: TAP death randomly selects from ALL equipped death sounds
                 // In normal mode, TAP should return null since it's only for background mode
                 List<AvailableCosmetics> allEquippedDeathSounds = getAllEquippedDeathSounds();
                 if (!allEquippedDeathSounds.isEmpty()) {
                     return allEquippedDeathSounds.get(random.nextInt(allEquippedDeathSounds.size()));
                 }
                 return null;
                
            default:
                return getEquippedDeathSound(DeathCause.FALL);
        }
    }

    private AvailableCosmetics getRandomDeathSoundFromAllAvailable(DeathCause deathCause, ThreadLocalRandom random) {
        switch (deathCause) {
            case SPIKE:
                AvailableCosmetics[] spikeCosmetics = AvailableCosmetics.getDeathSoundsForCause(DeathCause.SPIKE);
                if (spikeCosmetics.length > 0) {
                    return spikeCosmetics[random.nextInt(spikeCosmetics.length)];
                }
                return AvailableCosmetics.DEATH_SPIKE_DEFAULT;
                
            case FALL:
                AvailableCosmetics[] fallCosmetics = AvailableCosmetics.getDeathSoundsForCause(DeathCause.FALL);
                if (fallCosmetics.length > 0) {
                    return fallCosmetics[random.nextInt(fallCosmetics.length)];
                }
                return AvailableCosmetics.DEATH_FALL_VARIANT_1;
                
            case SUFFOCATION:
                AvailableCosmetics[] suffocationCosmetics = AvailableCosmetics.getDeathSoundsForCause(DeathCause.SUFFOCATION);
                if (suffocationCosmetics.length > 0) {
                    return suffocationCosmetics[random.nextInt(suffocationCosmetics.length)];
                }
                return AvailableCosmetics.DEATH_SUFFOCATION_DEFAULT;
                
            case TAP:
                AvailableCosmetics[] allDeathSounds = AvailableCosmetics.getCosmeticsOfType(CosmeticType.DEATH_SOUND);
                if (allDeathSounds.length > 0) {
                    return allDeathSounds[random.nextInt(allDeathSounds.length)];
                }
                return AvailableCosmetics.DEATH_FALL_VARIANT_1;
                
            default:
                return AvailableCosmetics.DEATH_FALL_VARIANT_1;
        }
    }

    public AvailableCosmetics getEquippedCosmetic(CosmeticType type) {
        if (isBackgroundMode) {
            return getRandomCosmeticFromAllAvailable(type);
        }
        
        // NORMAL MODE: Use equipped cosmetics
        switch (type) {
            case DEATH_SOUND:
                // For death sounds, we need to know the cause: return null to force using getEquippedDeathSound
                return null;
            case DEATH_PARTICLES:
                return equippedDeathParticles;
            case PARTICLE_TRAIL:
                return equippedParticleTrail;
            case PLAYER_SPRITE:
                return equippedPlayerSprite;
            default:
                return null;
        }
    }

    private AvailableCosmetics getRandomCosmeticFromAllAvailable(CosmeticType type) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        AvailableCosmetics[] availableCosmetics = AvailableCosmetics.getCosmeticsOfType(type);
        
        if (availableCosmetics.length > 0) {
            return availableCosmetics[random.nextInt(availableCosmetics.length)];
        }

        switch (type) {
            case DEATH_PARTICLES:
                return AvailableCosmetics.PARTICLES_DEATH_DEFAULT;
            case PARTICLE_TRAIL:
                return AvailableCosmetics.TRAIL_NONE;
            case PLAYER_SPRITE:
                return AvailableCosmetics.PLAYER_DEFAULT;
            default:
                return null;
        }
    }

    @SuppressWarnings("unused")
    public void equipCosmetic(AvailableCosmetics cosmetic) {
        if (cosmetic == null) return;
        
        switch (cosmetic.getType()) {
            case DEATH_SOUND:
                if (cosmetic.getDeathCause() == DeathCause.SPIKE) {
                    if (!equippedSpikeDeathSounds.contains(cosmetic)) {
                        equippedSpikeDeathSounds.add(cosmetic);
                    }
                } else if (cosmetic.getDeathCause() == DeathCause.FALL) {
                    if (!equippedFallDeathSounds.contains(cosmetic)) {
                        equippedFallDeathSounds.add(cosmetic);
                    }
                } else if (cosmetic.getDeathCause() == DeathCause.SUFFOCATION) {
                    if (!equippedSuffocationDeathSounds.contains(cosmetic)) {
                        equippedSuffocationDeathSounds.add(cosmetic);
                    }
                } else if (cosmetic.getDeathCause() == DeathCause.TAP) {
                    if (!equippedTapDeathSounds.contains(cosmetic)) {
                        equippedTapDeathSounds.add(cosmetic);
                    }
                }
                break;
            case DEATH_PARTICLES:
                equippedDeathParticles = cosmetic;
                break;
            case PARTICLE_TRAIL:
                equippedParticleTrail = cosmetic;
                break;
            case PLAYER_SPRITE:
                equippedPlayerSprite = cosmetic;
                break;
        }
    }

    @SuppressWarnings("unused")
    public void unequipDeathSound(AvailableCosmetics cosmetic) {
        if (cosmetic == null || cosmetic.getType() != CosmeticType.DEATH_SOUND) return;

        if (cosmetic.getDeathCause() == DeathCause.SPIKE) {
            equippedSpikeDeathSounds.remove(cosmetic);
        } else if (cosmetic.getDeathCause() == DeathCause.FALL) {
            equippedFallDeathSounds.remove(cosmetic);
        } else if (cosmetic.getDeathCause() == DeathCause.SUFFOCATION) {
            equippedSuffocationDeathSounds.remove(cosmetic);
        } else if (cosmetic.getDeathCause() == DeathCause.TAP) {
            equippedTapDeathSounds.remove(cosmetic);
        }
    }

    @SuppressWarnings("unused")
    public void unequipAllDeathSounds(DeathCause deathCause) {
        switch (deathCause) {
            case SPIKE:
                equippedSpikeDeathSounds.clear();
                equippedSpikeDeathSounds.add(AvailableCosmetics.DEATH_SPIKE_DEFAULT);
                break;
            case FALL:
                equippedFallDeathSounds.clear();
                equippedFallDeathSounds.add(AvailableCosmetics.DEATH_FALL_VARIANT_1);
                break;
            case SUFFOCATION:
                equippedSuffocationDeathSounds.clear();
                equippedSuffocationDeathSounds.add(AvailableCosmetics.DEATH_SUFFOCATION_DEFAULT);
                break;
            case TAP:
                equippedTapDeathSounds.clear();
                break;
        }
    }

    @SuppressWarnings("unused")
    public void unequipCosmetic(CosmeticType type) {
        switch (type) {
            case DEATH_PARTICLES:
                equippedDeathParticles = AvailableCosmetics.PARTICLES_DEATH_DEFAULT;
                break;
            case PARTICLE_TRAIL:
                equippedParticleTrail = AvailableCosmetics.TRAIL_NONE;
                break;
            case PLAYER_SPRITE:
                equippedPlayerSprite = AvailableCosmetics.PLAYER_DEFAULT;
                break;
            case DEATH_SOUND:
                break;
        }
    }
    

    private List<AvailableCosmetics> getAllEquippedDeathSounds() {
        List<AvailableCosmetics> allEquippedSounds = new ArrayList<>();

        allEquippedSounds.addAll(equippedSpikeDeathSounds);
        allEquippedSounds.addAll(equippedFallDeathSounds);
        allEquippedSounds.addAll(equippedSuffocationDeathSounds);
        allEquippedSounds.addAll(equippedTapDeathSounds);
        
        return allEquippedSounds;
    }

// FUTURE IMPLEMENTATION:
//    private List<AvailableCosmetics> getAllUnlockedDeathSounds() {
//        // For now, return all equipped death sounds as "unlocked"
//        // TODO: Later, replace with actual unlocked cosmetics from PlayerStatistics
//        // List<String> unlockedIds = playerStats.getUnlockedCosmetics();
//        // return unlockedIds.stream()
//        //     .map(AvailableCosmetics::findById)
//        //     .filter(cosmetic -> cosmetic != null && cosmetic.getType() == CosmeticType.DEATH_SOUND)
//        //     .collect(Collectors.toList());
//
//        return getAllEquippedDeathSounds();
//    }

    @SuppressWarnings("unused")
    public List<AvailableCosmetics> getAllEquippedCosmetics() {
        List<AvailableCosmetics> equipped = new ArrayList<>();
        
        // Add all equipped death sounds
        equipped.addAll(equippedSpikeDeathSounds);
        equipped.addAll(equippedFallDeathSounds);
        equipped.addAll(equippedSuffocationDeathSounds);
        equipped.addAll(equippedTapDeathSounds);
        
        // Add single equipped cosmetics
        equipped.add(equippedDeathParticles);
        equipped.add(equippedParticleTrail);
        equipped.add(equippedPlayerSprite);
        
        return equipped;
    }

    public void resetToDefaults() {
        equippedSpikeDeathSounds.clear();
        equippedSpikeDeathSounds.add(AvailableCosmetics.DEATH_SPIKE_DEFAULT);
        
        equippedFallDeathSounds.clear();
        equippedFallDeathSounds.add(AvailableCosmetics.DEATH_FALL_VARIANT_1);

        equippedSuffocationDeathSounds.clear();
        equippedSuffocationDeathSounds.add(AvailableCosmetics.DEATH_SUFFOCATION_DEFAULT);
        
        equippedTapDeathSounds.clear();

        equippedDeathParticles = AvailableCosmetics.PARTICLES_DEATH_DEFAULT;
        equippedParticleTrail = AvailableCosmetics.TRAIL_DEFAULT;
        equippedPlayerSprite = AvailableCosmetics.PLAYER_DEFAULT;
    }
}
