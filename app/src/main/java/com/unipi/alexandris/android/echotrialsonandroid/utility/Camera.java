package com.unipi.alexandris.android.echotrialsonandroid.utility;

import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.model.PhysicsObject;

/**
 * The Camera class handles the viewport movement and positioning in the game.
 * It follows the player smoothly and ensures the view stays within
 * the level boundaries.
 */
public class Camera {

    private double x;
    private double y;
    private int screenWidth = 0;
    private int screenHeight = 0;
    private double levelWidth = 5000*48;
    private double levelHeight = 100*48;
    private double levelMinX = -100*48;
    private double levelMaxX = 5000*48;
    private float multiplier = 1f;
    private int debugFrameCount = 0;

    public Camera(double x, double y, int screenWidth, int screenHeight, float multiplier) {
        this.x = x;
        this.y = y;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        if(multiplier > 0f) this.multiplier = multiplier;
    }

    public void tick(PhysicsObject object) {
        if (object == null) return;
        
        // Calculate target camera position to center the player on screen
        // Camera position represents the top-left corner of the viewport
        // Use the same dimensions as getWidth() and getHeight() (with multiplier)
        double targetX = object.getX() + (double) object.getSize() / 2 - (double) (screenWidth * multiplier) / 3;
        double targetY = object.getY() + (double) object.getSize() / 2 - (double) (screenHeight * multiplier) / 2;
        
        // Enforce level boundaries to keep camera within valid range
        if (levelWidth > 0 && levelHeight > 0) {
            // X-axis: Allow camera to follow player infinitely (no X boundaries)
            // This prevents player from dying when moving left or right of level bounds
            
            // Y-axis boundaries only: camera can't go beyond level height
            // Top boundary: camera can't be negative
            if (targetY < 0) {
                targetY = 0;
            }
            // Bottom boundary: camera can't show beyond level height  
            // Allow camera to lose track of player sooner (follow less on minY)
            else if (targetY + (screenHeight * multiplier) > levelHeight*0.8) {
                targetY = levelHeight*0.8 - (screenHeight * multiplier);
                // If level is smaller than screen, center it
                if (targetY < 0) {
                    targetY = 0;
                }
            }
        }
        
        // IMPROVED: Use direct assignment for precise tracking instead of lerp to avoid drift
        // The original Canvas version might have used lerp, but direct assignment prevents accumulation errors
        if (GameConstants.INSTANCE.isDEBUG_MODE()) {
            // Use direct assignment in debug mode for precise tracking
            x = targetX;
            y = targetY;
        } else {
            // Use smooth camera movement (linear interpolation) for normal gameplay
            double lerpFactor = 0.15; // Slightly faster lerp to reduce lag
            x += (targetX - x) * lerpFactor;
            y += (targetY - y) * lerpFactor;
        }
    }

    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }
    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }
    public int getWidth() {
        return (int)(screenWidth * multiplier);
    }
    public int getHeight() {
        return (int)(screenHeight * multiplier);
    }
    
    /**
     * Sets the level bounds for camera movement.
     * This allows the camera to follow the player within the actual level boundaries.
     *
     * @param minX The minimum X coordinate of the level
     * @param maxX The maximum X coordinate of the level
     * @param minY The minimum Y coordinate of the level
     * @param maxY The maximum Y coordinate of the level
     */
    public void setLevelBounds(double minX, double maxX, double minY, double maxY) {
        this.levelMinX = minX;
        this.levelMaxX = maxX;
        this.levelWidth = maxX - minX;
        this.levelHeight = maxY - minY;
    }
} 