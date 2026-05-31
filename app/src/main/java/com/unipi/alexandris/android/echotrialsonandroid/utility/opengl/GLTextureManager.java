package com.unipi.alexandris.android.echotrialsonandroid.utility.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.unipi.alexandris.android.echotrialsonandroid.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages OpenGL textures for the game.
 * Provides the same resource access patterns as the original JavaFX BufferedImageLoader.
 * Handles texture loading, caching, and OpenGL texture management.
 */
public class GLTextureManager {
    
    private final Context context;
    private final Map<Integer, Integer> textureCache = new HashMap<>(); // Resource SimpleID -> OpenGL Texture SimpleID
    
    // Pre-loaded common textures for immediate access
    private int brickTexture = -1;
    private int iceTexture = -1;
    private int waterTexture = -1;
    private int backgroundTexture = -1;
    private final int[] spikeTexture = new int[4];
    private final int[] spikeIceTexture = new int[4];
    private final int[] playerTextures = new int[3]; // Idle, Jump, Move
    private int portalTexture = -1;
    private int whiteTexture = -1; // For solid color rendering
    private int arrowTexture = -1; // For touch control arrows
    private int blackTexture = -1;
    
    // Pre-created colored textures for particle system
    private int teal200Texture = -1;
    private int blackColorTexture = -1;
    private int redTexture = -1;
    private int greenTexture = -1;
    private int blueTexture = -1;
    private int yellowTexture = -1;
    private int cyanTexture = -1;
    private int magentaTexture = -1;
    private int orangeTexture = -1;
    private int pinkTexture = -1;
    private int brownTexture = -1;
    private int grayTexture = -1;
    private int lightGrayTexture = -1;
    private int darkGrayTexture = -1;
    private int vibrantRedTexture = -1;
    private int vibrantGreenTexture = -1;
    private int vibrantBlueTexture = -1;
    private int vibrantYellowTexture = -1;
    private int vibrantOrangeTexture = -1;
    private int vibrantPurpleTexture = -1;
    private int vibrantPinkTexture = -1;
    private int vibrantCyanTexture = -1;
    private int vibrantLimeTexture = -1;
    private int vibrantIndigoTexture = -1;
    private int vibrantTealTexture = -1;
    private int pastelRedTexture = -1;
    private int pastelGreenTexture = -1;
    private int pastelBlueTexture = -1;
    private int pastelYellowTexture = -1;
    private int pastelOrangeTexture = -1;
    private int pastelPurpleTexture = -1;
    private int pastelPinkTexture = -1;
    private int pastelCyanTexture = -1;
    private int pastelLimeTexture = -1;
    private int pastelPeachTexture = -1;
    private int pastelLavenderTexture = -1;
    private int neonRedTexture = -1;
    private int neonGreenTexture = -1;
    private int neonBlueTexture = -1;
    private int neonYellowTexture = -1;
    private int neonOrangeTexture = -1;
    private int neonPurpleTexture = -1;
    private int neonPinkTexture = -1;
    private int neonCyanTexture = -1;
    private int neonLimeTexture = -1;
    private int neonMagentaTexture = -1;
    private int fireRedTexture = -1;
    private int fireOrangeTexture = -1;
    private int fireYellowTexture = -1;
    private int fireWhiteTexture = -1;
    private int fireDarkRedTexture = -1;
    private int fireBrightOrangeTexture = -1;
    private int iceBlueTexture = -1;
    private int iceCyanTexture = -1;
    private int iceWhiteTexture = -1;
    private int iceLightBlueTexture = -1;
    private int iceDarkBlueTexture = -1;
    private int iceCrystalTexture = -1;
    private int forestGreenTexture = -1;
    private int grassGreenTexture = -1;
    private int leafGreenTexture = -1;
    private int earthBrownTexture = -1;
    private int sandYellowTexture = -1;
    private int skyBlueTexture = -1;
    private int oceanBlueTexture = -1;
    private int sunsetOrangeTexture = -1;
    private int sunsetPinkTexture = -1;
    private int sunsetPurpleTexture = -1;
    private int goldTexture = -1;
    private int silverTexture = -1;
    private int bronzeTexture = -1;
    private int copperTexture = -1;
    private int platinumTexture = -1;
    private int steelBlueTexture = -1;
    private int gunmetalTexture = -1;
    private int rainbowRedTexture = -1;
    private int rainbowOrangeTexture = -1;
    private int rainbowYellowTexture = -1;
    private int rainbowGreenTexture = -1;
    private int rainbowBlueTexture = -1;
    private int rainbowIndigoTexture = -1;
    private int rainbowVioletTexture = -1;
    private int halloweenOrangeTexture = -1;
    private int halloweenBlackTexture = -1;
    private int halloweenPurpleTexture = -1;
    private int halloweenGreenTexture = -1;
    private int halloweenRedTexture = -1;
    private int christmasRedTexture = -1;
    private int christmasGreenTexture = -1;
    private int christmasGoldTexture = -1;
    private int christmasSilverTexture = -1;
    private int christmasWhiteTexture = -1;
    private int easterPinkTexture = -1;
    private int easterLavenderTexture = -1;
    private int easterMintTexture = -1;
    private int easterYellowTexture = -1;
    private int easterBlueTexture = -1;
    private int easterPeachTexture = -1;
    private int transparentRedTexture = -1;
    private int transparentGreenTexture = -1;
    private int transparentBlueTexture = -1;
    private int transparentYellowTexture = -1;
    private int transparentOrangeTexture = -1;
    private int transparentPurpleTexture = -1;
    private int transparentWhiteTexture = -1;
    private int transparentBlackTexture = -1;
    private int electricBlueTexture = -1;
    private int poisonGreenTexture = -1;
    private int bloodRedTexture = -1;
    private int shadowBlackTexture = -1;
    private int lightningYellowTexture = -1;
    private int magicPurpleTexture = -1;
    private int healGreenTexture = -1;
    private int manaBlueTexture = -1;
    private int rageRedTexture = -1;
    private int stealthDarkTexture = -1;
    private int gradientStartRedTexture = -1;
    private int gradientEndRedTexture = -1;
    private int gradientStartBlueTexture = -1;
    private int gradientEndBlueTexture = -1;
    private int gradientStartGreenTexture = -1;
    private int gradientEndGreenTexture = -1;
    private int gradientStartYellowTexture = -1;
    private int gradientEndYellowTexture = -1;
    private int gradientStartPurpleTexture = -1;
    private int gradientEndPurpleTexture = -1;
    private int particleSparkleTexture = -1;
    private int particleSmokeTexture = -1;
    private int particleFireTexture = -1;
    private int particleIceTexture = -1;
    private int particleLightningTexture = -1;
    private int particlePoisonTexture = -1;
    private int particleHealTexture = -1;
    private int particleMagicTexture = -1;
    private int particleExplosionTexture = -1;
    private int particleTrailTexture = -1;
    private int particleGlowTexture = -1;
    private int particleDustTexture = -1;
    private int particleSteamTexture = -1;
    private int particleEnergyTexture = -1;
    private int particleDarknessTexture = -1;
    
    /**
     * Creates a new texture manager.
     */
    public GLTextureManager(Context context) {
        this.context = context;
        loadCommonTextures();
    }
    
    /**
     * Loads commonly used textures for immediate access.
     * This matches the texture loading pattern from the original JavaFX Game.Images record.
     */
    private void loadCommonTextures() {
        try {
            // Create white texture for solid color rendering
            whiteTexture = createWhiteTexture();
            
            // Create black texture for particle rendering
            blackTexture = createBlackTexture();
            
            // Create all colored textures for particle system
            createColoredTextures();
            
            // Load core game textures (same as original Game.gameImages)
            brickTexture = loadTexture(R.drawable.brick5);
            iceTexture = loadTexture(R.drawable.ice2);
            waterTexture = loadTexture(R.drawable.water3);
            spikeTexture[0] = loadTexture(R.drawable.spikes);
            spikeTexture[1] = loadTexture(R.drawable.spikes_d);
            spikeTexture[2] = loadTexture(R.drawable.spikes_l);
            spikeTexture[3] = loadTexture(R.drawable.spikes_r);
            spikeIceTexture[0] = loadTexture(R.drawable.ice_spikes);
            spikeIceTexture[1] = loadTexture(R.drawable.ice_spikes_d);
            spikeIceTexture[2] = loadTexture(R.drawable.ice_spikes_l);
            spikeIceTexture[3] = loadTexture(R.drawable.ice_spikes_r);
            backgroundTexture = loadTexture(R.drawable.bgrnd5);
            
            // Load player animation textures (matches GameImages.playerImages array)
            playerTextures[0] = loadTexture(R.drawable.idle);  // Idle animation
            playerTextures[1] = loadTexture(R.drawable.jump);  // Jump animation  
            playerTextures[2] = loadTexture(R.drawable.move);  // Move animation
            
            portalTexture = loadTexture(R.drawable.portal);
            
            // Load UI arrow texture for touch controls (with smooth filtering)
            arrowTexture = loadTexture(R.drawable.ui_arrow_moveb);

        } catch (Exception e) {
            System.err.println("Failed to load common textures: " + e.getMessage());
        }
    }

    /**
     * Loads a UI texture with smooth filtering (GL_LINEAR) for crisp UI elements.
     *
     * @param resourceId Android resource SimpleID
     * @return OpenGL texture SimpleID, or -1 if loading failed
     */
    public int loadUITexture(int resourceId) {
        // Check cache first (same pattern as original)
        if (textureCache.containsKey(resourceId)) {
            return textureCache.get(resourceId);
        }

        try {
            // Load bitmap from resources
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
            if (bitmap == null) {
                System.err.println("Failed to decode UI texture resource: " + resourceId);
                return -1;
            }

            // Generate OpenGL texture
            int[] textureIds = new int[1];
            GLES20.glGenTextures(1, textureIds, 0);
            int textureId = textureIds[0];

            // Bind and configure texture with smooth filtering for UI
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

            // Use LINEAR filtering for smooth UI elements (no pixelation)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            // Upload bitmap to GPU
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Clean up bitmap (GPU has the data now)
            bitmap.recycle();

            // Cache texture SimpleID (same pattern as original)
            textureCache.put(resourceId, textureId);

            return textureId;

        } catch (Exception e) {
            System.err.println("Failed to load UI texture " + resourceId + ": " + e.getMessage());
            return -1;
        }
    }

    /**
     * Loads a texture from Android resources and returns OpenGL texture SimpleID.
     * Uses NEAREST filtering for pixel-perfect game textures.
     * Maintains same caching behavior as original BufferedImageLoader.
     * 
     * @param resourceId Android resource SimpleID
     * @return OpenGL texture SimpleID, or -1 if loading failed
     */
    public int loadTexture(int resourceId) {
        // Check cache first (same pattern as original)
        if (textureCache.containsKey(resourceId)) {
            return textureCache.get(resourceId);
        }
        
        try {
            // Load bitmap from resources
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
            if (bitmap == null) {
                System.err.println("Failed to decode texture resource: " + resourceId);
                return -1;
            }
            
            // Generate OpenGL texture
            int[] textureIds = new int[1];
            GLES20.glGenTextures(1, textureIds, 0);
            int textureId = textureIds[0];
            
            // Bind and configure texture
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            
            // Set texture parameters for pixel-perfect 2D rendering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            
            // Upload bitmap to GPU
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            
            // Clean up bitmap (GPU has the data now)
            bitmap.recycle();
            
            // Cache texture SimpleID (same pattern as original)
            textureCache.put(resourceId, textureId);
            
            return textureId;
            
        } catch (Exception e) {
            System.err.println("Failed to load texture " + resourceId + ": " + e.getMessage());
            return -1;
        }
    }
    
    // Getters for common textures - matches original Game.gameImages access pattern
    
    /**
     * Gets the brick texture SimpleID (equivalent to Game.gameImages.brickImage()).
     */
    public int getBrickTexture() {
        return brickTexture;
    }
    
    /**
     * Gets the ice texture SimpleID (equivalent to Game.gameImages.iceImage()).
     */
    public int getIceTexture() {
        return iceTexture;
    }
    
    /**
     * Gets the water texture SimpleID (equivalent to Game.gameImages.waterImage()).
     */
    public int getWaterTexture() {
        return waterTexture;
    }

    public int[] getSpikeTextures() {
        return spikeTexture;
    }

    public int getSpikeTexture() {
        return spikeTexture[0]; // Default spike (up)
    }

    public int getSpikeTextureDown() {
        return spikeTexture[1]; // Down spike
    }

    public int getSpikeTextureLeft() {
        return spikeTexture[2]; // Left spike
    }

    public int getSpikeTextureRight() {
        return spikeTexture[3]; // Right spike
    }

    public int[] getSpikeIceTextures() {
        return spikeIceTexture;
    }

    public int getSpikeIceTexture() {
        return spikeIceTexture[0]; // Default ice spike (up)
    }

    public int getSpikeIceTextureDown() {
        return spikeIceTexture[1]; // Down ice spike
    }

    public int getSpikeIceTextureLeft() {
        return spikeIceTexture[2]; // Left ice spike
    }

    public int getSpikeIceTextureRight() {
        return spikeIceTexture[3]; // Right ice spike
    }
    
    /**
     * Gets the background texture SimpleID (equivalent to Game.gameImages.backgroundImage()).
     */
    public int getBackgroundTexture() {
        return backgroundTexture;
    }
    
    /**
     * Gets the player texture array (equivalent to GameImages.playerImages()).
     * Index 0: Idle, Index 1: Jump, Index 2: Move
     */
    public int[] getPlayerTextures() {
        return playerTextures;
    }
    
    /**
     * Gets a specific player texture by index.
     * @param index 0=Idle, 1=Jump, 2=Move
     */
    public int getPlayerTexture(int index) {
        if (index >= 0 && index < playerTextures.length) {
            return playerTextures[index];
        }
        return playerTextures[0]; // Default to idle
    }
    
    /**
     * Gets the portal texture SimpleID (equivalent to Game.gameImages.portalImages()[0]).
     */
    public int getPortalTexture() {
        return portalTexture;
    }
    
    /**
     * Gets the arrow texture SimpleID for touch control buttons.
     */
    public int getArrowTexture() {
        return arrowTexture;
    }
    
    /**
     * Gets the black texture for solid color rendering.
     */
    public int getBlackTexture() {
        return blackTexture;
    }

    /**
     * Gets a pre-created colored texture by color name.
     * This is much more efficient than creating textures on-demand.
     * 
     * @param colorName The name of the color (e.g., "teal_200", "fire_red", "particle_sparkle")
     * @return OpenGL texture ID, or -1 if color not found
     */
    public int getColoredTexture(String colorName) {
        switch (colorName) {
            // Original colors
            case "teal_200": return teal200Texture;
            case "black": return blackColorTexture;
            
            // Basic colors
            case "red": return redTexture;
            case "green": return greenTexture;
            case "blue": return blueTexture;
            case "yellow": return yellowTexture;
            case "cyan": return cyanTexture;
            case "magenta": return magentaTexture;
            case "orange": return orangeTexture;
            case "pink": return pinkTexture;
            case "brown": return brownTexture;
            case "gray": return grayTexture;
            case "light_gray": return lightGrayTexture;
            case "dark_gray": return darkGrayTexture;
            
            // Vibrant colors
            case "vibrant_red": return vibrantRedTexture;
            case "vibrant_green": return vibrantGreenTexture;
            case "vibrant_blue": return vibrantBlueTexture;
            case "vibrant_yellow": return vibrantYellowTexture;
            case "vibrant_orange": return vibrantOrangeTexture;
            case "vibrant_purple": return vibrantPurpleTexture;
            case "vibrant_pink": return vibrantPinkTexture;
            case "vibrant_cyan": return vibrantCyanTexture;
            case "vibrant_lime": return vibrantLimeTexture;
            case "vibrant_indigo": return vibrantIndigoTexture;
            case "vibrant_teal": return vibrantTealTexture;
            
            // Pastel colors
            case "pastel_red": return pastelRedTexture;
            case "pastel_green": return pastelGreenTexture;
            case "pastel_blue": return pastelBlueTexture;
            case "pastel_yellow": return pastelYellowTexture;
            case "pastel_orange": return pastelOrangeTexture;
            case "pastel_purple": return pastelPurpleTexture;
            case "pastel_pink": return pastelPinkTexture;
            case "pastel_cyan": return pastelCyanTexture;
            case "pastel_lime": return pastelLimeTexture;
            case "pastel_peach": return pastelPeachTexture;
            case "pastel_lavender": return pastelLavenderTexture;
            
            // Neon colors
            case "neon_red": return neonRedTexture;
            case "neon_green": return neonGreenTexture;
            case "neon_blue": return neonBlueTexture;
            case "neon_yellow": return neonYellowTexture;
            case "neon_orange": return neonOrangeTexture;
            case "neon_purple": return neonPurpleTexture;
            case "neon_pink": return neonPinkTexture;
            case "neon_cyan": return neonCyanTexture;
            case "neon_lime": return neonLimeTexture;
            case "neon_magenta": return neonMagentaTexture;
            
            // Fire colors
            case "fire_red": return fireRedTexture;
            case "fire_orange": return fireOrangeTexture;
            case "fire_yellow": return fireYellowTexture;
            case "fire_white": return fireWhiteTexture;
            case "fire_dark_red": return fireDarkRedTexture;
            case "fire_bright_orange": return fireBrightOrangeTexture;
            
            // Ice colors
            case "ice_blue": return iceBlueTexture;
            case "ice_cyan": return iceCyanTexture;
            case "ice_white": return iceWhiteTexture;
            case "ice_light_blue": return iceLightBlueTexture;
            case "ice_dark_blue": return iceDarkBlueTexture;
            case "ice_crystal": return iceCrystalTexture;
            
            // Nature colors
            case "forest_green": return forestGreenTexture;
            case "grass_green": return grassGreenTexture;
            case "leaf_green": return leafGreenTexture;
            case "earth_brown": return earthBrownTexture;
            case "sand_yellow": return sandYellowTexture;
            case "sky_blue": return skyBlueTexture;
            case "ocean_blue": return oceanBlueTexture;
            case "sunset_orange": return sunsetOrangeTexture;
            case "sunset_pink": return sunsetPinkTexture;
            case "sunset_purple": return sunsetPurpleTexture;
            
            // Metallic colors
            case "gold": return goldTexture;
            case "silver": return silverTexture;
            case "bronze": return bronzeTexture;
            case "copper": return copperTexture;
            case "platinum": return platinumTexture;
            case "steel_blue": return steelBlueTexture;
            case "gunmetal": return gunmetalTexture;
            
            // Rainbow colors
            case "rainbow_red": return rainbowRedTexture;
            case "rainbow_orange": return rainbowOrangeTexture;
            case "rainbow_yellow": return rainbowYellowTexture;
            case "rainbow_green": return rainbowGreenTexture;
            case "rainbow_blue": return rainbowBlueTexture;
            case "rainbow_indigo": return rainbowIndigoTexture;
            case "rainbow_violet": return rainbowVioletTexture;
            
            // Halloween colors
            case "halloween_orange": return halloweenOrangeTexture;
            case "halloween_black": return halloweenBlackTexture;
            case "halloween_purple": return halloweenPurpleTexture;
            case "halloween_green": return halloweenGreenTexture;
            case "halloween_red": return halloweenRedTexture;
            
            // Christmas colors
            case "christmas_red": return christmasRedTexture;
            case "christmas_green": return christmasGreenTexture;
            case "christmas_gold": return christmasGoldTexture;
            case "christmas_silver": return christmasSilverTexture;
            case "christmas_white": return christmasWhiteTexture;
            
            // Easter colors
            case "easter_pink": return easterPinkTexture;
            case "easter_lavender": return easterLavenderTexture;
            case "easter_mint": return easterMintTexture;
            case "easter_yellow": return easterYellowTexture;
            case "easter_blue": return easterBlueTexture;
            case "easter_peach": return easterPeachTexture;
            
            // Transparent colors
            case "transparent_red": return transparentRedTexture;
            case "transparent_green": return transparentGreenTexture;
            case "transparent_blue": return transparentBlueTexture;
            case "transparent_yellow": return transparentYellowTexture;
            case "transparent_orange": return transparentOrangeTexture;
            case "transparent_purple": return transparentPurpleTexture;
            case "transparent_white": return transparentWhiteTexture;
            case "transparent_black": return transparentBlackTexture;
            
            // Special effect colors
            case "electric_blue": return electricBlueTexture;
            case "poison_green": return poisonGreenTexture;
            case "blood_red": return bloodRedTexture;
            case "shadow_black": return shadowBlackTexture;
            case "lightning_yellow": return lightningYellowTexture;
            case "magic_purple": return magicPurpleTexture;
            case "heal_green": return healGreenTexture;
            case "mana_blue": return manaBlueTexture;
            case "rage_red": return rageRedTexture;
            case "stealth_dark": return stealthDarkTexture;
            
            // Gradient colors
            case "gradient_start_red": return gradientStartRedTexture;
            case "gradient_end_red": return gradientEndRedTexture;
            case "gradient_start_blue": return gradientStartBlueTexture;
            case "gradient_end_blue": return gradientEndBlueTexture;
            case "gradient_start_green": return gradientStartGreenTexture;
            case "gradient_end_green": return gradientEndGreenTexture;
            case "gradient_start_yellow": return gradientStartYellowTexture;
            case "gradient_end_yellow": return gradientEndYellowTexture;
            case "gradient_start_purple": return gradientStartPurpleTexture;
            case "gradient_end_purple": return gradientEndPurpleTexture;
            
            // Particle system colors
            case "particle_sparkle": return particleSparkleTexture;
            case "particle_smoke": return particleSmokeTexture;
            case "particle_fire": return particleFireTexture;
            case "particle_ice": return particleIceTexture;
            case "particle_lightning": return particleLightningTexture;
            case "particle_poison": return particlePoisonTexture;
            case "particle_heal": return particleHealTexture;
            case "particle_magic": return particleMagicTexture;
            case "particle_explosion": return particleExplosionTexture;
            case "particle_trail": return particleTrailTexture;
            case "particle_glow": return particleGlowTexture;
            case "particle_dust": return particleDustTexture;
            case "particle_steam": return particleSteamTexture;
            case "particle_energy": return particleEnergyTexture;
            case "particle_darkness": return particleDarknessTexture;
            
            default: return -1; // Color not found
        }
    }
    
    /**
     * Gets a texture by resource SimpleID, loading it if necessary.
     * Provides the same access pattern as original BufferedImageLoader.loadImage().
     * 
     * @param resourceId Android resource SimpleID
     * @return OpenGL texture SimpleID
     */
    public int getTexture(int resourceId) {
        return loadTexture(resourceId);
    }
    
    /**
     * Releases all loaded textures.
     * Should be called when the texture manager is no longer needed.
     */
    public void dispose() {
        // Delete all OpenGL textures
        for (int textureId : textureCache.values()) {
            GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
        }
        textureCache.clear();
        
        // Reset common texture IDs
        brickTexture = -1;
        iceTexture = -1;
        waterTexture = -1;
        backgroundTexture = -1;
        whiteTexture = -1;
        arrowTexture = -1;
        blackTexture = -1;
        Arrays.fill(playerTextures, -1);
        portalTexture = -1;
        
        // Reset all colored texture IDs
        teal200Texture = -1;
        blackColorTexture = -1;
        redTexture = -1;
        greenTexture = -1;
        blueTexture = -1;
        yellowTexture = -1;
        cyanTexture = -1;
        magentaTexture = -1;
        orangeTexture = -1;
        pinkTexture = -1;
        brownTexture = -1;
        grayTexture = -1;
        lightGrayTexture = -1;
        darkGrayTexture = -1;
        vibrantRedTexture = -1;
        vibrantGreenTexture = -1;
        vibrantBlueTexture = -1;
        vibrantYellowTexture = -1;
        vibrantOrangeTexture = -1;
        vibrantPurpleTexture = -1;
        vibrantPinkTexture = -1;
        vibrantCyanTexture = -1;
        vibrantLimeTexture = -1;
        vibrantIndigoTexture = -1;
        vibrantTealTexture = -1;
        pastelRedTexture = -1;
        pastelGreenTexture = -1;
        pastelBlueTexture = -1;
        pastelYellowTexture = -1;
        pastelOrangeTexture = -1;
        pastelPurpleTexture = -1;
        pastelPinkTexture = -1;
        pastelCyanTexture = -1;
        pastelLimeTexture = -1;
        pastelPeachTexture = -1;
        pastelLavenderTexture = -1;
        neonRedTexture = -1;
        neonGreenTexture = -1;
        neonBlueTexture = -1;
        neonYellowTexture = -1;
        neonOrangeTexture = -1;
        neonPurpleTexture = -1;
        neonPinkTexture = -1;
        neonCyanTexture = -1;
        neonLimeTexture = -1;
        neonMagentaTexture = -1;
        fireRedTexture = -1;
        fireOrangeTexture = -1;
        fireYellowTexture = -1;
        fireWhiteTexture = -1;
        fireDarkRedTexture = -1;
        fireBrightOrangeTexture = -1;
        iceBlueTexture = -1;
        iceCyanTexture = -1;
        iceWhiteTexture = -1;
        iceLightBlueTexture = -1;
        iceDarkBlueTexture = -1;
        iceCrystalTexture = -1;
        forestGreenTexture = -1;
        grassGreenTexture = -1;
        leafGreenTexture = -1;
        earthBrownTexture = -1;
        sandYellowTexture = -1;
        skyBlueTexture = -1;
        oceanBlueTexture = -1;
        sunsetOrangeTexture = -1;
        sunsetPinkTexture = -1;
        sunsetPurpleTexture = -1;
        goldTexture = -1;
        silverTexture = -1;
        bronzeTexture = -1;
        copperTexture = -1;
        platinumTexture = -1;
        steelBlueTexture = -1;
        gunmetalTexture = -1;
        rainbowRedTexture = -1;
        rainbowOrangeTexture = -1;
        rainbowYellowTexture = -1;
        rainbowGreenTexture = -1;
        rainbowBlueTexture = -1;
        rainbowIndigoTexture = -1;
        rainbowVioletTexture = -1;
        halloweenOrangeTexture = -1;
        halloweenBlackTexture = -1;
        halloweenPurpleTexture = -1;
        halloweenGreenTexture = -1;
        halloweenRedTexture = -1;
        christmasRedTexture = -1;
        christmasGreenTexture = -1;
        christmasGoldTexture = -1;
        christmasSilverTexture = -1;
        christmasWhiteTexture = -1;
        easterPinkTexture = -1;
        easterLavenderTexture = -1;
        easterMintTexture = -1;
        easterYellowTexture = -1;
        easterBlueTexture = -1;
        easterPeachTexture = -1;
        transparentRedTexture = -1;
        transparentGreenTexture = -1;
        transparentBlueTexture = -1;
        transparentYellowTexture = -1;
        transparentOrangeTexture = -1;
        transparentPurpleTexture = -1;
        transparentWhiteTexture = -1;
        transparentBlackTexture = -1;
        electricBlueTexture = -1;
        poisonGreenTexture = -1;
        bloodRedTexture = -1;
        shadowBlackTexture = -1;
        lightningYellowTexture = -1;
        magicPurpleTexture = -1;
        healGreenTexture = -1;
        manaBlueTexture = -1;
        rageRedTexture = -1;
        stealthDarkTexture = -1;
        gradientStartRedTexture = -1;
        gradientEndRedTexture = -1;
        gradientStartBlueTexture = -1;
        gradientEndBlueTexture = -1;
        gradientStartGreenTexture = -1;
        gradientEndGreenTexture = -1;
        gradientStartYellowTexture = -1;
        gradientEndYellowTexture = -1;
        gradientStartPurpleTexture = -1;
        gradientEndPurpleTexture = -1;
        particleSparkleTexture = -1;
        particleSmokeTexture = -1;
        particleFireTexture = -1;
        particleIceTexture = -1;
        particleLightningTexture = -1;
        particlePoisonTexture = -1;
        particleHealTexture = -1;
        particleMagicTexture = -1;
        particleExplosionTexture = -1;
        particleTrailTexture = -1;
        particleGlowTexture = -1;
        particleDustTexture = -1;
        particleSteamTexture = -1;
        particleEnergyTexture = -1;
        particleDarknessTexture = -1;
    }
    
    /**
     * Checks if a texture is loaded and valid.
     */
    public boolean isTextureLoaded(int resourceId) {
        return textureCache.containsKey(resourceId) && textureCache.get(resourceId) != -1;
    }
    
    /**
     * Gets the number of loaded textures.
     */
    public int getLoadedTextureCount() {
        return textureCache.size();
    }
    
    /**
     * Gets the white texture for solid color rendering.
     */
    public int getWhiteTexture() {
        return whiteTexture;
    }
    
    /**
     * Creates a 1x1 white texture for solid color rendering.
     */
    private int createWhiteTexture() {
        try {
            Bitmap whiteBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

            whiteBitmap.setPixel(0, 0, 0xFFFFFFFF);
            int[] textureIds = new int[1];
            GLES20.glGenTextures(1, textureIds, 0);
            int textureId = textureIds[0];
            if (textureId <= 0) {
                whiteBitmap.recycle();
                return -1;
            }
            
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, whiteBitmap, 0);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            
            whiteBitmap.recycle();
            return textureId;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Creates a 1x1 black texture for solid color rendering.
     */
    private int createBlackTexture() {
        try {
            Bitmap blackBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

            blackBitmap.setPixel(0, 0, 0xFF000000);
            int[] textureIds = new int[1];
            GLES20.glGenTextures(1, textureIds, 0);
            int textureId = textureIds[0];
            if (textureId <= 0) {
                blackBitmap.recycle();
                return -1;
            }
            
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, blackBitmap, 0);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            
            blackBitmap.recycle();
            return textureId;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Creates all colored textures at initialization for efficient particle rendering.
     */
    private void createColoredTextures() {
        try {
            // Original colors
            teal200Texture = createColoredTexture(Color.valueOf(Color.parseColor("#FF03DAC5")));
            blackColorTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF000000")));
            
            // Basic colors
            redTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF0000")));
            greenTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF00FF00")));
            blueTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF0000FF")));
            yellowTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFFF00")));
            cyanTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF00FFFF")));
            magentaTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF00FF")));
            orangeTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF8000")));
            pinkTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFC0CB")));
            brownTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF8B4513")));
            grayTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF808080")));
            lightGrayTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFD3D3D3")));
            darkGrayTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF404040")));
            
            // Vibrant colors
            vibrantRedTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF1744")));
            vibrantGreenTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF00E676")));
            vibrantBlueTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF2196F3")));
            vibrantYellowTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFEB3B")));
            vibrantOrangeTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF9800")));
            vibrantPurpleTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF9C27B0")));
            vibrantPinkTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFE91E63")));
            vibrantCyanTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF00BCD4")));
            vibrantLimeTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFCDDC39")));
            vibrantIndigoTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF3F51B5")));
            vibrantTealTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF009688")));
            
            // Pastel colors
            pastelRedTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFB3BA")));
            pastelGreenTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFB3FFB3")));
            pastelBlueTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFB3D9FF")));
            pastelYellowTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFFFB3")));
            pastelOrangeTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFD9B3")));
            pastelPurpleTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFE6B3FF")));
            pastelPinkTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFB3E6")));
            pastelCyanTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFB3FFFF")));
            pastelLimeTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFE6FFB3")));
            pastelPeachTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFE6B3")));
            pastelLavenderTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFE6E6FF")));
            
            // Neon colors
            neonRedTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF0066")));
            neonGreenTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF00FF66")));
            neonBlueTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF0066FF")));
            neonYellowTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFFF66")));
            neonOrangeTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF6600")));
            neonPurpleTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF6600FF")));
            neonPinkTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF0066")));
            neonCyanTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF00FFFF")));
            neonLimeTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF66FF00")));
            neonMagentaTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF00FF")));
            
            // Fire colors
            fireRedTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF2200")));
            fireOrangeTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF6600")));
            fireYellowTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFCC00")));
            fireWhiteTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFF5E6")));
            fireDarkRedTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFCC0000")));
            fireBrightOrangeTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF8800")));
            
            // Ice colors
            iceBlueTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF00CCFF")));
            iceCyanTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF66FFFF")));
            iceWhiteTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFF0F8FF")));
            iceLightBlueTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFB3E6FF")));
            iceDarkBlueTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF0066CC")));
            iceCrystalTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFE6F3FF")));
            
            // Nature colors
            forestGreenTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF228B22")));
            grassGreenTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF7CFC00")));
            leafGreenTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF32CD32")));
            earthBrownTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF8B4513")));
            sandYellowTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFF4A460")));
            skyBlueTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF87CEEB")));
            oceanBlueTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF006994")));
            sunsetOrangeTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF7F50")));
            sunsetPinkTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF69B4")));
            sunsetPurpleTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF9370DB")));
            
            // Metallic colors
            goldTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFD700")));
            silverTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFC0C0C0")));
            bronzeTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFCD7F32")));
            copperTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFB87333")));
            platinumTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFE5E4E2")));
            steelBlueTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF4682B4")));
            gunmetalTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF2C3539")));
            
            // Rainbow colors
            rainbowRedTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF0000")));
            rainbowOrangeTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF7F00")));
            rainbowYellowTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFFF00")));
            rainbowGreenTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF00FF00")));
            rainbowBlueTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF0000FF")));
            rainbowIndigoTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF4B0082")));
            rainbowVioletTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF9400D3")));
            
            // Halloween colors
            halloweenOrangeTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF6600")));
            halloweenBlackTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF1A1A1A")));
            halloweenPurpleTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF800080")));
            halloweenGreenTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF00FF00")));
            halloweenRedTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF0000")));
            
            // Christmas colors
            christmasRedTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFDC143C")));
            christmasGreenTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF228B22")));
            christmasGoldTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFD700")));
            christmasSilverTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFC0C0C0")));
            christmasWhiteTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFFAFA")));
            
            // Easter colors
            easterPinkTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFC0CB")));
            easterLavenderTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFE6E6FA")));
            easterMintTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFF5FFFA")));
            easterYellowTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFFFE0")));
            easterBlueTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFE0F6FF")));
            easterPeachTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFE5B4")));
            
            // Transparent colors
            transparentRedTexture = createColoredTexture(Color.valueOf(Color.parseColor("#80FF0000")));
            transparentGreenTexture = createColoredTexture(Color.valueOf(Color.parseColor("#8000FF00")));
            transparentBlueTexture = createColoredTexture(Color.valueOf(Color.parseColor("#800000FF")));
            transparentYellowTexture = createColoredTexture(Color.valueOf(Color.parseColor("#80FFFF00")));
            transparentOrangeTexture = createColoredTexture(Color.valueOf(Color.parseColor("#80FF8000")));
            transparentPurpleTexture = createColoredTexture(Color.valueOf(Color.parseColor("#80800080")));
            transparentWhiteTexture = createColoredTexture(Color.valueOf(Color.parseColor("#80FFFFFF")));
            transparentBlackTexture = createColoredTexture(Color.valueOf(Color.parseColor("#80000000")));
            
            // Special effect colors
            electricBlueTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF00FFFF")));
            poisonGreenTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF00FF00")));
            bloodRedTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF8B0000")));
            shadowBlackTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF1A1A1A")));
            lightningYellowTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFFF00")));
            magicPurpleTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF800080")));
            healGreenTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF00FF7F")));
            manaBlueTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF4169E1")));
            rageRedTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF0000")));
            stealthDarkTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF2F2F2F")));
            
            // Gradient colors
            gradientStartRedTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF0000")));
            gradientEndRedTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF8B0000")));
            gradientStartBlueTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF0000FF")));
            gradientEndBlueTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF000080")));
            gradientStartGreenTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF00FF00")));
            gradientEndGreenTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF008000")));
            gradientStartYellowTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFFF00")));
            gradientEndYellowTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF8000")));
            gradientStartPurpleTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF800080")));
            gradientEndPurpleTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF4B0082")));
            
            // Particle system colors
            particleSparkleTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFFFCC")));
            particleSmokeTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF808080")));
            particleFireTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF4400")));
            particleIceTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF00CCFF")));
            particleLightningTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFFF66")));
            particlePoisonTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF00FF66")));
            particleHealTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF66FF66")));
            particleMagicTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFCC66FF")));
            particleExplosionTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFF6600")));
            particleTrailTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFCCCCCC")));
            particleGlowTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFFFFF99")));
            particleDustTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFD2B48C")));
            particleSteamTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FFF5F5F5")));
            particleEnergyTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF00FFFF")));
            particleDarknessTexture = createColoredTexture(Color.valueOf(Color.parseColor("#FF2F2F2F")));
            
        } catch (Exception e) {
            System.err.println("Failed to create colored textures: " + e.getMessage());
        }
    }

    /**
     * Creates a 1x1 colored texture for solid color rendering.
     */
    private int createColoredTexture(Color color) {
        try {
            Bitmap coloredBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

            coloredBitmap.setPixel(0, 0, color.toArgb());
            int[] textureIds = new int[1];
            GLES20.glGenTextures(1, textureIds, 0);
            int textureId = textureIds[0];
            if (textureId <= 0) {
                coloredBitmap.recycle();
                return -1;
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, coloredBitmap, 0);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

            coloredBitmap.recycle();
            return textureId;
        } catch (Exception e) {
            return -1;
        }
    }
} 