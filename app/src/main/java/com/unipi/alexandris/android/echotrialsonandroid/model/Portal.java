package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.content.Context;
import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLSpriteBatch;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLTextureManager;
import com.unipi.alexandris.android.echotrialsonandroid.view.GameHostActivity;

/**
 * The SimplePortal class represents the goal/endpoint of a level.
 * When the player touches this object, they complete the level.
 */
public class Portal extends PhysicsObject {
    
    // Teleportation effect state
    private boolean isTeleporting = false;
    private Player teleportingPlayer = null;
    private double teleportStartTime = 0;
    private static final double TELEPORT_DURATION_MS = 1000; // 1 second teleportation effect

    /**
     * Constructs a new SimplePortal with specified position and handler.
     */
    public Portal(double x, double y, ObjectHandler handler, int groupId) {
        super(x, y, 64, handler, ID.Portal, groupId);
        setWidthMultiplier(2);
        setHeightMultiplier(2);
    }
    
    @Override
    protected void onTick() {
        // Handle teleportation effect if active
        if (isTeleporting && teleportingPlayer != null) {
            updateTeleportationEffect();
            return;
        }
        
        // Check for collision with all interactive objects (based on interaction mask)
        for (PhysicsObject target : getInteractiveObjects()) {
            if (getBounds().intersect(target.getBounds()) && target instanceof Player) {
                startTeleportationEffect((Player) target);
                break; // Only need one collision to start teleportation
            }
        }
    }

    private void startTeleportationEffect(Player player) {
        if (isTeleporting) return; // Already teleporting
        
        isTeleporting = true;
        teleportingPlayer = player;
        teleportStartTime = System.currentTimeMillis();
        
        // Disable player controls using cancel flags
        player.setCancelLEFT(true);
        player.setCancelRIGHT(true);
        player.setCancelUP(true);
        player.setCancelDOWN(true);

        
        // Play portal sound (hardcoded, not cosmetic)
        if (GameConstants.INSTANCE.getSoundManager() != null && player.isPlaySounds() && !player.getTeleporting()) {
            GameConstants.INSTANCE.getSoundManager().playSound(com.unipi.alexandris.android.echotrialsonandroid.utility.audio.SoundID.PORTAL);
        }
        
        // Disable physics processing during teleportation
        player.setTeleporting(true);
        player.setVelX(0);
        player.setVelY(0);
        // Stop player physics during teleportation
        double prevW = player.getWidth();
        double prevH = player.getHeight();
        player.setSize(48);
        player.setWidthMultiplier(1.0);
        player.setHeightMultiplier(1.3);

        double velW = player.getWidth() - prevW;
        double velH = player.getHeight() - prevH;

        if(player.getX() - (this.getX()-prevW/4) < 0) player.setX(player.getX() - velW);
        if(player.getY() - (this.getY()-prevH/4) < 0) player.setY(player.getY() - velH);

    }

    private void updateTeleportationEffect() {
        double currentTime = System.currentTimeMillis();
        double elapsedTime = currentTime - teleportStartTime;
        double progress = Math.min(elapsedTime / TELEPORT_DURATION_MS, 1.0);
        
        if (progress >= 1.0) {
            // Teleportation complete
            completeTeleportation();
            return;
        }
        
        // Calculate portal center (simple hit-box center)
        double portalCenterX = x + getWidth() / 2.0;
        double portalCenterY = y + getHeight() / 2.0;
        
        // Calculate target position
        double targetX = portalCenterX - teleportingPlayer.getWidth() / 2.0;
        double targetY = portalCenterY - teleportingPlayer.getHeight(); // Move player higher
        
        // Store initial position on first frame
        if (progress == 0.0) {
            // This will be handled by the first call, but we need initial positions
        }
        
        // Linear movement based on progress (0.0 to 1.0)
        // Calculate the starting position (where player was when teleportation began)
        double startX = teleportingPlayer.getX();
        double startY = teleportingPlayer.getY();
        
        // If this is not the first frame, we need to get the original start position
        // For simplicity, let's use a different approach: move directly towards target
        double distanceX = targetX - teleportingPlayer.getX();
        double distanceY = targetY - teleportingPlayer.getY();
        
        // Move a fixed amount each frame (linear movement)
        double moveDistance = 3.0; // pixels per frame
        double totalDistance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        
        double newX, newY;
        if (totalDistance <= moveDistance) {
            // Close enough - snap to target
            newX = targetX;
            newY = targetY;
        } else {
            // Move towards target at constant speed
            double moveRatio = moveDistance / totalDistance;
            newX = teleportingPlayer.getX() + distanceX * moveRatio;
            newY = teleportingPlayer.getY() + distanceY * moveRatio;
        }
        
        teleportingPlayer.setX(newX);
        teleportingPlayer.setY(newY);
        
        // Shrink player size based on progress
        double scale = 1.0 - progress; // 1.0 to 0.0
        teleportingPlayer.setWidthMultiplier(scale);
        teleportingPlayer.setHeightMultiplier(scale);
    }

    private void completeTeleportation() {
        // Re-enable physics for the player
        if (teleportingPlayer != null) {
            teleportingPlayer.setTeleporting(false);
        }
        
        isTeleporting = false;
        this.setActive(false);
        teleportingPlayer = null;
        
        // Show level completion dialog
        try {
            GameHostActivity activity = GameConstants.INSTANCE.getGameHostActivity();
            if (activity != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    try {
                        activity.showCompleteDialog();
                    } catch (Exception ignored) {}
                });
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void renderGL(GLSpriteBatch spriteBatch, Camera camera) {
        if (!isVisible() || spriteBatch == null) return;
        
        // Get texture manager
        GLTextureManager textureManager =
            spriteBatch.getTextureManager();
        if (textureManager == null) return;
        
        // Use world coordinates directly - camera transform applied by view matrix
        float drawX = (float)x;
        float drawY = (float)y;
        
        // Try to use portal texture first, fallback to colored rectangle
        int textureId = textureManager.getPortalTexture();
        if (textureId != -1) {
            // Draw with portal texture with per-vertex opacity (same visual as Canvas version)
            spriteBatch.draw(textureId, drawX, drawY, (float)getWidth(), (float)getHeight(), false, false,
                           1.0f, 1.0f, 1.0f, getOpacity());
        } else {
            // Fallback: draw colored rectangle using white texture + purple tint for portal
            int whiteTexture = textureManager.getWhiteTexture();
            if (whiteTexture != -1) {
                // Purple for portal with per-vertex opacity
                spriteBatch.draw(whiteTexture, drawX, drawY, (float)getWidth(), (float)getHeight(), false, false,
                               0.8f, 0.3f, 1.0f, getOpacity());
            }
        }
    }
} 