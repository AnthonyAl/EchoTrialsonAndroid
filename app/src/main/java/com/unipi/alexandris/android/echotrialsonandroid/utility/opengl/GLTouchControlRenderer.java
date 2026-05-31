package com.unipi.alexandris.android.echotrialsonandroid.utility.opengl;

import android.graphics.RectF;

import com.unipi.alexandris.android.echotrialsonandroid.model.Player;
import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;

/**
 * Simple OpenGL touch control renderer - just two bright yellow buttons.
 * Handles both rendering AND input processing directly.
 */
public class GLTouchControlRenderer {
    
    private final GLSpriteBatch spriteBatch;
    private final GLTextureManager textureManager;
    private final GLTextRenderer textRenderer;
    // Button areas
    private final RectF leftButtonArea = new RectF();
    private final RectF rightButtonArea = new RectF();
    
    // Right-side control areas
    private final RectF rightHalfArea = new RectF();  // Right half of screen for jump
    private final RectF rightTopArea = new RectF();   // Top half of right side for up
    private final RectF rightBottomArea = new RectF(); // Bottom half of right side for down
    
    // Button states
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean controlsEnabled = true;

    // Multi-touch tracking - track which finger controls which action
    private int leftButtonFingerId = -1;  // Finger ID controlling left button
    private int rightButtonFingerId = -1; // Finger ID controlling right button
    private int upFingerId = -1;          // Finger ID controlling up/jump
    private int downFingerId = -1;        // Finger ID controlling down
    
    // Screen tracking
    private int lastScreenWidth = 0;
    private int lastScreenHeight = 0;
    
    public GLTouchControlRenderer(GLSpriteBatch spriteBatch, GLTextureManager textureManager, GLTextRenderer textRenderer) {
        this.spriteBatch = spriteBatch;
        this.textureManager = textureManager;
        this.textRenderer = textRenderer;
    }
    
    /**
     * Updates button positions based on screen size.
     */
    private void updateButtonPositions(int screenWidth, int screenHeight) {
        int buttonSize = screenHeight / 6; // Nice big buttons
        int margin = buttonSize / 2;
        
        // Left button - bottom left
        float leftY = screenHeight - buttonSize - margin;
        leftButtonArea.set((float) margin, leftY, (float) margin + buttonSize, leftY + buttonSize);
        
        // Right button - next to left button
        float rightX = (float) margin + buttonSize + (float) margin / 2;
        float rightY = screenHeight - buttonSize - margin;
        rightButtonArea.set(rightX, rightY, rightX + buttonSize, rightY + buttonSize);
        
        // Right-side control areas
        float rightHalfX = screenWidth / 2f;
        rightHalfArea.set(rightHalfX, 0, screenWidth, screenHeight); // Right half of screen
        
        float screenMidY = screenHeight / 2f;
        rightTopArea.set(rightHalfX, 0, screenWidth, screenMidY); // Top half of right side
        rightBottomArea.set(rightHalfX, screenMidY, screenWidth, screenHeight); // Bottom half of right side
        
        lastScreenWidth = screenWidth;
        lastScreenHeight = screenHeight;
    }
    
    /**
     * Renders beautiful modern touch control buttons with rounded appearance.
     */
    public void render(int screenWidth, int screenHeight) {
        if (!controlsEnabled) return;
        
        // Update positions if needed
        if (screenWidth != lastScreenWidth || screenHeight != lastScreenHeight) {
            updateButtonPositions(screenWidth, screenHeight);
        }

        // Draw left button with arrow texture (flipped horizontally)
        if (!leftButtonArea.isEmpty()) {
            drawModernArrowButton(leftButtonArea, leftPressed, false);
        }
        
        // Draw right button with arrow texture (normal orientation)
        if (!rightButtonArea.isEmpty()) {
            drawModernArrowButton(rightButtonArea, rightPressed, true);
        }
    }
    
    /**
     * Draws a modern-looking button with circular background and arrow texture.
     */
    private void drawModernArrowButton(RectF buttonArea, boolean isPressed,
                                       boolean flipHorizontally) {
        int arrowTexture = textureManager.getArrowTexture();
        float buttonSize = buttonArea.width();
        
        // Calculate size effect when pressed (10% bigger when touched)
        float sizeMultiplier = isPressed ? 1.1f : 1.0f;
        float effectiveButtonSize = buttonSize * sizeMultiplier;

        
        // 2. Draw arrow texture with size effect
        if (arrowTexture != -1) {
            // Calculate arrow size (larger - 75% of effective button size)
            float arrowSize = effectiveButtonSize * 0.9f;
            float arrowX = buttonArea.centerX() - arrowSize / 2;
            float arrowY = buttonArea.centerY() - arrowSize / 2;

            // Apply per-vertex opacity based on press state (keep original colors)
            float opacity;
            if (isPressed) {
                opacity = 0.9f; // Slight transparency when pressed
                arrowSize = effectiveButtonSize;
                arrowX = buttonArea.centerX() - arrowSize / 2;
                arrowY = buttonArea.centerY() - arrowSize / 2;
            } else {
                opacity = 0.6f; // More transparency when not pressed
            }

            // Draw arrow with horizontal flip, size effect, and per-vertex opacity
            spriteBatch.draw(arrowTexture, arrowX, arrowY, arrowSize, arrowSize, flipHorizontally, false,
                           1.0f, 1.0f, 1.0f, opacity);
        }
    }
    
    /**
     * Processes touch input - returns true if handled.
     * Now properly tracks which finger controls which action.
     * Supports continuous jump holding and gravity-based directional controls.
     */
    public boolean processTouchInput(float touchX, float touchY, boolean isDown, int fingerId) {
        if (!controlsEnabled || GameConstants.INSTANCE.getPlayer() == null) return false;
        
        // Safety check for finger ID bounds
        if (fingerId < 0 || fingerId >= 10) {
            return false; // Ignore invalid finger IDs (max 10 fingers)
        }
        
        Player player = GameConstants.INSTANCE.getPlayer();

        if (player.isDead()) return false;
        
        if (isDown) {
            // Touch down - determine which action this finger controls
            // Since areas don't overlap, we can process all independently
            
            // Check walk buttons (left side)
            if (leftButtonArea.contains(touchX, touchY) && leftButtonFingerId == -1) {
                // This finger is now controlling left button
                leftButtonFingerId = fingerId;
                leftPressed = true;
                player.setPressLEFT(true);
            }
            
            if (rightButtonArea.contains(touchX, touchY) && rightButtonFingerId == -1) {
                // This finger is now controlling right button
                rightButtonFingerId = fingerId;
                rightPressed = true;
                player.setPressRIGHT(true);
            }
            
            // Check right-side controls (completely independent from walk buttons)
            if (player.getPhysics().getGravity() == 0) {
                // Zero gravity: use top/bottom areas of right side
                if (rightTopArea.contains(touchX, touchY) && upFingerId == -1) {
                    upFingerId = fingerId;
                    player.setPressUP(true);
                } else if (rightBottomArea.contains(touchX, touchY) && downFingerId == -1) {
                    downFingerId = fingerId;
                    player.setPressDOWN(true);
                }
            } else {
                // Normal gravity: any touch in right half = jump
                if (rightHalfArea.contains(touchX, touchY) && upFingerId == -1) {
                    upFingerId = fingerId;
                    player.setPressUP(true);
                }
            }
            
            // Return true if any action was processed
            return leftButtonArea.contains(touchX, touchY) || rightButtonArea.contains(touchX, touchY) || 
                   rightHalfArea.contains(touchX, touchY);
        } else {
            // Touch up - only release the action controlled by this specific finger
            boolean released = false;
            if (fingerId == leftButtonFingerId) {
                leftButtonFingerId = -1;
                leftPressed = false;
                player.setPressLEFT(false);
                released = true;
            }
            if (fingerId == rightButtonFingerId) {
                rightButtonFingerId = -1;
                rightPressed = false;
                player.setPressRIGHT(false);
                released = true;
            }
            if (fingerId == upFingerId) {
                upFingerId = -1;
                player.setPressUP(false);
                released = true;
            }
            if (fingerId == downFingerId) {
                downFingerId = -1;
                player.setPressDOWN(false);
                released = true;
            }
            
            return released;
        }
    }
    
    /**
     * Processes touch move - releases buttons if touch moves outside.
     * Now properly tracks which finger controls which action.
     * Supports gravity-based directional controls.
     */
    public void processTouchMove(float touchX, float touchY, int fingerId) {
        if (!controlsEnabled || GameConstants.INSTANCE.getPlayer() == null) return;
        
        Player player = GameConstants.INSTANCE.getPlayer();
        
        // SAFETY: Don't process input if player is dead
        if (player.isDead()) return;
        
        // Get player gravity to determine directional controls
        double playerGravity = player.getPhysics().getGravity();
        boolean isZeroGravity = Math.abs(playerGravity) < 0.1; // Consider gravity "zero" if very small
        
        // Only process movement for fingers that are controlling buttons
        if (fingerId == leftButtonFingerId) {
            // Release left button if touch moved outside
            if (!leftButtonArea.contains(touchX, touchY)) {
                leftButtonFingerId = -1;
                leftPressed = false;
                player.setPressLEFT(false);
            }
        }
        
        if (fingerId == rightButtonFingerId) {
            // Release right button if touch moved outside
            if (!rightButtonArea.contains(touchX, touchY)) {
                rightButtonFingerId = -1;
                rightPressed = false;
                player.setPressRIGHT(false);
            }
        }
        
        // Handle up/down controls based on gravity
        if (player.getPhysics().getGravity() == 0) {
            // Zero gravity: use area-based logic for right side only
            
            if (fingerId == upFingerId) {
                // Release up if touch moved outside top area of right side
                if (!rightTopArea.contains(touchX, touchY)) {
                    upFingerId = -1;
                    player.setPressUP(false);
                }
            }
            
            if (fingerId == downFingerId) {
                // Release down if touch moved outside bottom area of right side
                if (!rightBottomArea.contains(touchX, touchY)) {
                    downFingerId = -1;
                    player.setPressDOWN(false);
                }
            }
        } else {
            // Normal gravity: release jump if touch moved outside right half
            if (fingerId == upFingerId) {
                if (!rightHalfArea.contains(touchX, touchY)) {
                    upFingerId = -1;
                    player.setPressUP(false);
                }
            }
        }
        
        // Activate buttons if touch moved onto them (only if not already controlled by another finger)
        if (fingerId != leftButtonFingerId && leftButtonFingerId == -1 && leftButtonArea.contains(touchX, touchY)) {
            leftButtonFingerId = fingerId;
            leftPressed = true;
            player.setPressLEFT(true);
        }
        
        if (fingerId != rightButtonFingerId && rightButtonFingerId == -1 && rightButtonArea.contains(touchX, touchY)) {
            rightButtonFingerId = fingerId;
            rightPressed = true;
            player.setPressRIGHT(true);
        }
        
        // Activate jump controls if touch moved onto their areas (only if not already controlled by another finger)
        if (player.getPhysics().getGravity() == 0) {
            // Zero gravity: activate up/down if touch moved to appropriate areas
            if (fingerId != upFingerId && upFingerId == -1 && rightTopArea.contains(touchX, touchY)) {
                upFingerId = fingerId;
                player.setPressUP(true);
            }
            
            if (fingerId != downFingerId && downFingerId == -1 && rightBottomArea.contains(touchX, touchY)) {
                downFingerId = fingerId;
                player.setPressDOWN(true);
            }
        } else {
            // Normal gravity: activate jump if touch moved to right half
            if (fingerId != upFingerId && upFingerId == -1 && rightHalfArea.contains(touchX, touchY)) {
                upFingerId = fingerId;
                player.setPressUP(true);
            }
        }
    }
    
    /**
     * Updates the touch control renderer.
     * Currently no timer-based updates needed since we removed auto-release.
     */
    public void update(int deltaTime) {
        // No timer-based updates needed for continuous holding
    }
    
    /**
     * Releases all controls.
     */
    public void releaseAllControls() {
        if (GameConstants.INSTANCE.getPlayer() != null && !GameConstants.INSTANCE.getPlayer().isDead()) {
            leftPressed = false;
            rightPressed = false;
            leftButtonFingerId = -1;
            rightButtonFingerId = -1;
            upFingerId = -1;
            downFingerId = -1;
            GameConstants.INSTANCE.getPlayer().setPressLEFT(false);
            GameConstants.INSTANCE.getPlayer().setPressRIGHT(false);
            GameConstants.INSTANCE.getPlayer().setPressUP(false);
            GameConstants.INSTANCE.getPlayer().setPressDOWN(false);
        }
    }
    
    /**
     * Sets whether controls are enabled.
     */
    public void setControlsEnabled(boolean enabled) {
        this.controlsEnabled = enabled;
        if (!enabled) {
            releaseAllControls();
        }
    }
    
    /**
     * Gets whether controls are enabled.
     */
    public boolean isControlsEnabled() {
        return controlsEnabled;
    }
} 