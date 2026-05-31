package com.unipi.alexandris.android.echotrialsonandroid.utility;

import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLSpriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * SpriteTrailRenderer manages a trail of player sprites that follow behind the player.
 * The trail density increases with player speed and each trail sprite fades out over time.
 * Uses game ticks for timing instead of system time for consistent behavior.
 */
public class SpriteTrailRenderer {
    
    private static final int TRAIL_LIFETIME_TICKS = 10; // 1 second at 60 FPS
    private static final float MIN_SPEED_FOR_TRAIL = 2.0f; // Minimum speed to create trail
    private static final int MAX_TRAIL_DENSITY = 3; // Maximum trail spawn rate (every 1 tick at max speed - continuous)
    private static final int MIN_TRAIL_DENSITY = 1; // Minimum trail spawn rate (every 3 ticks at min speed - much more packed)
    
    private final List<TrailSprite> trailSprites;
    private int lastTrailSpawnTick;
    private int currentTick; // Game tick counter
    
    /**
     * Represents a single trail sprite with position, opacity, and timing information
     */
    private static class TrailSprite {
        public float x, y, width, height;
        public float opacity;
        public int creationTick;
        public int textureId;
        public boolean flipX, flipY;

        public TrailSprite(float x, float y, float width, float height, int textureId, boolean flipX, boolean flipY, int currentTick) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.textureId = textureId;
            this.flipX = flipX;
            this.flipY = flipY;
            this.opacity = 0.4f; // Start slightly transparent
            this.creationTick = currentTick;
        }

        public boolean update(int currentTick) {
            int age = currentTick - creationTick;
            
            if (age >= TRAIL_LIFETIME_TICKS) {
                return true; // Remove sprite
            }

            // Accelerating fade: use quadratic curve for faster fade at the start
            float ageRatio = (float) age / TRAIL_LIFETIME_TICKS;
            float fadeMultiplier = (1.0f - ageRatio) * (1.0f - ageRatio); // Quadratic fade (accelerating at start)
            this.opacity = 0.4f * fadeMultiplier;
            
            return false;
        }
    }
    
    public SpriteTrailRenderer() {
        this.trailSprites = new ArrayList<>();
        this.lastTrailSpawnTick = 0;
        this.currentTick = 0;
    }
    
    /**
     * Updates the trail system and potentially spawns a new trail sprite
     * @param playerX Current player X position
     * @param playerY Current player Y position
     * @param playerWidth Player sprite width
     * @param playerHeight Player sprite height
     * @param speedX Player horizontal speed (used for density calculation)
     * @param textureId Current player texture ID
     * @param flipX Whether to flip texture horizontally
     * @param flipY Whether to flip texture vertically
     */
    public void update(float playerX, float playerY, float playerWidth, float playerHeight, 
                      float speedX, int textureId, boolean flipX, boolean flipY) {
        
        currentTick++; // Increment our tick counter
        
        // Calculate trail spawn rate based on player speed
        float absSpeedX = Math.abs(speedX);
        if (absSpeedX >= MIN_SPEED_FOR_TRAIL) {
            // Interpolate between min and max density based on speed
            // Higher speed = lower interval (more frequent spawning)
            float speedRatio = Math.min(absSpeedX / 20.0f, 1.0f); // Cap at speed 20
            int spawnInterval = (int) (MIN_TRAIL_DENSITY - (speedRatio * (MIN_TRAIL_DENSITY - MAX_TRAIL_DENSITY)));
            
            // Check if it's time to spawn a new trail sprite
            if (currentTick - lastTrailSpawnTick >= spawnInterval) {
                spawnTrailSprite(playerX, playerY, playerWidth, playerHeight, textureId, flipX, flipY);
                lastTrailSpawnTick = currentTick;
            }
        }
        
        // Update existing trail sprites and remove expired ones
        // Remove expired sprite
        trailSprites.removeIf(sprite -> sprite.update(currentTick));
    }

    private void spawnTrailSprite(float x, float y, float width, float height, 
                                 int textureId, boolean flipX, boolean flipY) {
        TrailSprite newSprite = new TrailSprite(x, y, width, height, textureId, flipX, flipY, currentTick);
        trailSprites.add(newSprite);
    }

    public void render(GLSpriteBatch spriteBatch) {
        if (spriteBatch == null || trailSprites.isEmpty()) {
            return;
        }
        
        // Render trail sprites from oldest to newest (back to front)
        // Each sprite gets its own opacity using per-vertex color
        for (TrailSprite sprite : trailSprites) {
            // Use per-vertex color with sprite's individual opacity
            spriteBatch.draw(sprite.textureId, sprite.x, sprite.y, sprite.width, sprite.height, 
                           sprite.flipX, sprite.flipY, 1.0f, 1.0f, 1.0f, sprite.opacity);
        }
    }

    public void clear() {
        trailSprites.clear();
        lastTrailSpawnTick = currentTick; // Reset spawn timing
    }

    public int getTrailSpriteCount() {
        return trailSprites.size();
    }

    public void resetTicks() {
        currentTick = 0;
        lastTrailSpawnTick = 0;
        clear();
    }
}