package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.content.Context;
import android.graphics.Region;

import androidx.annotation.Nullable;

import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.SoundID;
import com.unipi.alexandris.android.echotrialsonandroid.data.DeathCause;
import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ParticleCreator;
import com.unipi.alexandris.android.echotrialsonandroid.cosmetics.DeathParticleHelper;
import com.unipi.alexandris.android.echotrialsonandroid.cosmetics.TrailHelper;

import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.*;
import com.unipi.alexandris.android.echotrialsonandroid.view.GameComponent;
import com.unipi.alexandris.android.echotrialsonandroid.view.GameHostActivity;

// TODO: Create the cosmetics enum? or list? in any case, cosmetics controller, and
// TODO: the cosmetics manager, that allows each account to have specific cosmetics for:
// TODO: --> animations
// TODO: --> death sound effects
// TODO: --> death particles
// TODO: Also implement the profile editor, from which the accounts can unlock & equip them.
// TODO: Finally, implement the achievement system, which provides the currency for cosmetics.
/**
 * The SimplePlayer class represents the character controlled by the user.
 * Handles player movement, physics, animations, and interactions with the game world.
 * Optimized for Android touch controls.
 */
public class Player extends PhysicsObject {
    public boolean death;

    // SimplePlayer state
    private boolean inAir = false;
    private boolean onIce = false;
    private boolean inWater = false;
    private boolean isOnGround = false;
    private boolean isTeleporting = false;
    private int air = 100;
    
    // Restart on death setting
    private boolean restartOnDeath = true;
    
    // Death system
    private int deathDelayCounter = 0;
    private DeathCause deathCause = null;
    
    // Cosmetic helpers
    private final DeathParticleHelper deathParticleHelper;
    private final TrailHelper trailHelper;
    
    
    // Camera reference for off-screen detection
    private Camera camera;
    
    // Animation variables
    private int animFrame = 0;
    private int animDelay = 0;
    private int direction = 1; // 1 for right, -1 for left

    public Player(double x, double y, ObjectHandler handler, Camera camera, int groupId) {
        super(x, y, 48, handler, ID.Player, groupId);
        // Initialize particle creator for death effects
        
        // Store camera reference for off-screen detection
        this.camera = camera;
        
        // Initialize cosmetic helpers
        this.deathParticleHelper = new DeathParticleHelper(new ParticleCreator(handler), physics);
        this.trailHelper = new TrailHelper();
    }
    

    
    @Override
    protected void onTick() {
        if (!isActive()) return;
        
        // Handle death animation and delay
        if (death) {
            // Stop all movement during death
            setPressLEFT(false);
            setPressRIGHT(false);
            setPressUP(false);
            setPressDOWN(false);
            
            // Make player invisible during death
            setVisible(false);
            
            // After death delay, show fail dialog or respawn
            if (deathDelayCounter++ > 90) {
                if(restartOnDeath) {
                    // Pause the game and show fail dialog
                    GameConstants.INSTANCE.setGamePaused(true);
                    // Show fail dialog through the activity
                    showFailDialog();
                    setActive(false); // Immediately deactivate this old player object
                    return; // Exit immediately to prevent state conflicts
                } else {
                    // Normal respawn - reset player state
                    respawn();
                    deathDelayCounter = 0;
                    death = false;
                    deathCause = null;
                    setVisible(true); // Make player visible again
                    GameConstants.INSTANCE.setGamePaused(false); // Resume game
                }
            }
            return; // Don't process physics while dead
        }
        
        // Reset death delay when alive
        deathDelayCounter = 0;
        
        // Update width and height based on size and multipliers (like original)
        width = size * widthMultiplier;
        height = size * heightMultiplier;
        

        // Skip physics processing during teleportation
        if (!isTeleporting) {
            // Create a copy of the collision region to avoid shared reference issues
            Region blockCopy = new Region(GameConstants.INSTANCE.getGlobalRigidArea());
            physics.actorMovementPhysics(blockCopy);
        }
        
        // Check for off-screen death (falling off the world)
        checkOffScreenDeath();

        // Check for water and ice collision for effects
        checkEnvironmentalEffects();
        
        // Update animation
        updateAnimation(velY);
        
    }

    private void createDeathParticles() {
        deathParticleHelper.createDeathParticles(x, y, width, height, velX, velY);
    }

    private void updateAnimation(double vely) {
        // TODO: COSMETICS: animations - sprite cosmetics can be integrated here later
        // EXACT copy of original direction logic
        if (isPressLEFT() && !isPressRIGHT()) {
            if(getSpeedX() >= 0)
                direction = -1; // Left
            else direction = 1;
        } else if (isPressRIGHT() && !isPressLEFT()) {
            if(getSpeedX() >= 0)
                direction = 1;  // Right
            else direction = -1;
        }
        
        animDelay++;

        int frameSpeed = (int) Math.abs(getSpeedX())*3;
        frameSpeed = 38 - Math.max(4, Math.min(34, frameSpeed)); // Clamp between 6 and 34
        
        // Fixed animation logic - check for jump first, regardless of vely
        if (isPressUP()) {
            animFrame = 1; // Jump animation when jumping
        } else if (Math.abs(velX) < 1) {
            // Idle when not actually moving horizontally (same logic as walking sound)
            animFrame = 0;
            animDelay = 0;
        } else {
            // Walking animation - only when actually moving
            if (animDelay > frameSpeed) {
                // Play walking sound when animation frame changes to "Move"
                if (animFrame != 2) {
                    if(isPlaySounds()) playWalkingSound();
                }
                animFrame = 2; // Move
            } else {
                animFrame = 1; // Jump
            }
            if (animDelay > frameSpeed*2) animDelay = 0;
        }
    }

    private void playWalkingSound() {
        // Only play if sound manager is available and player is on ground
        // All sounds are now asynchronous by default in SoundFXManager
        if (GameConstants.INSTANCE.getSoundManager() != null && isOnGround) {
            GameConstants.INSTANCE.getSoundManager().playSound(SoundID.WALKING);
        }
    }
    

    private void checkEnvironmentalEffects() {
        // Check for water and ice collision
        inWater = GameConstants.INSTANCE.getGlobalWaterArea().op(getBounds(), Region.Op.INTERSECT);
        onIce = GameConstants.INSTANCE.getGlobalIceArea().op(getBounds(), Region.Op.INTERSECT);
        
        // Handle water physics - air reduction
        if (inWater) {
            air--;
            if (air <= 0) {
                // Use takeDamage to ensure sound plays immediately
                takeDamage(DeathCause.SUFFOCATION); // Water death
                air = 30; // Reset air supply
            }
        } else {
            air = 100; // Refill air when not in water
        }
    }

    public void takeDamage(DeathCause deathCause) {
        if (death) return;
        
        // Stop walking sound when player dies
        
        // Play death sound IMMEDIATELY for instant feedback using cosmetic system
        if (playSounds && GameConstants.INSTANCE.getCosmeticSoundManager() != null) {
            GameConstants.INSTANCE.getCosmeticSoundManager().playDeathSound(deathCause);
        }
        
        // Create death particles (independent of sound playing)
        if (deathCause == DeathCause.SPIKE) {
            createDeathParticles();
        } else if (deathCause == DeathCause.TAP) {
            createDeathParticles();
        }

        death = true;
        this.deathCause = deathCause;
        deathDelayCounter = 0;
    }

    public void takeDamage(DeathCause deathCause, boolean playSounds) {
        if (death) return;

        // Stop walking sound when player dies

        // Play death sound IMMEDIATELY for instant feedback using cosmetic system
        if (playSounds && GameConstants.INSTANCE.getCosmeticSoundManager() != null) {
            GameConstants.INSTANCE.getCosmeticSoundManager().playDeathSound(deathCause);
        }
        
        // Create death particles (independent of sound playing)
        if (deathCause == DeathCause.SPIKE) {
            createDeathParticles();
        } else if (deathCause == DeathCause.TAP) {
            createDeathParticles();
        }

        death = true;
        this.deathCause = deathCause;
        deathDelayCounter = 0;
    }

    public boolean isDead() {
        return death;
    }

    public void respawn() {
        x = GameConstants.INSTANCE.getSpawnX();
        y = GameConstants.INSTANCE.getSpawnY();
        air = 100;
        physics.resetVelocity();
    }

    public boolean isInAir() {
        return inAir;
    }

    public void setInAir(boolean inAir) {
        this.inAir = inAir;
    }

    public boolean isInWater() {
        return inWater;
    }

    public int getAir() {
        return air;
    }

    public int getDirection() {
        return direction;
    }

    public boolean isOnGround() {
        return isOnGround;
    }

    public void setOnGround(boolean onGround) {
        this.isOnGround = onGround;
    }

    public boolean isTeleporting() {
        return isTeleporting;
    }

    public void setTeleporting(boolean teleporting) {
        this.isTeleporting = teleporting;
    }

    @Override
    public void renderGL(GLSpriteBatch spriteBatch, Camera camera) {
        if (!isVisible() || spriteBatch == null) return;
        
        // Get texture manager
        GLTextureManager textureManager =
            spriteBatch.getTextureManager();
        if (textureManager == null) return;
        
        // Use world coordinates directly - camera transform applied by view matrix
        // (Canvas version subtracts camera, but OpenGL applies camera transform in setupCameraMatrix)
        float drawX = (float)x;
        float drawY = (float)y;
        
        // Get player animation textures (same as Canvas version using GameConstants.gameImages.playerImages())
        int[] playerTextures = textureManager.getPlayerTextures();
        if (playerTextures != null && playerTextures.length > 0) {
            
            // Choose appropriate animation frame (EXACT same logic as Canvas version)
            int frameIndex = Math.min(animFrame % playerTextures.length, playerTextures.length - 1);
            // TODO: Integrate with Cosmetics for Player Sprites
            int playerTextureId = textureManager.getPlayerTexture(frameIndex);
            
            if (playerTextureId != -1) {
                // Handle direction flipping (EXACT same logic as Canvas version)
                // Canvas version: if (direction == 1) flip horizontally
                boolean flipX = direction == 1;
                boolean flipY = !(physics.getGravity() >= 0);
                
                // Update trail system with current player state
                trailHelper.update(drawX, drawY, (float)width, (float)height, physics.getSpeedX(), playerTextureId, flipX, flipY);
                
                // Render trail first (behind player)
                trailHelper.render(spriteBatch);
                
                // Then render player on top with per-vertex opacity
                spriteBatch.draw(playerTextureId, drawX, drawY, (float)width, (float)height, flipX, flipY,
                               1.0f, 1.0f, 1.0f, getOpacity());
            } else {
                // Fallback: draw colored rectangle using white texture + color tint
                drawPlayerFallback(spriteBatch, textureManager, drawX, drawY);
            }
        } else {
            // Fallback: draw colored rectangle using white texture + color tint
            drawPlayerFallback(spriteBatch, textureManager, drawX, drawY);
        }
    }

    private void drawPlayerFallback(GLSpriteBatch spriteBatch,
                                    GLTextureManager textureManager,
                                    float drawX, float drawY) {
        int whiteTexture = textureManager.getWhiteTexture();
        if (whiteTexture != -1) {
            // Light blue for player with per-vertex opacity (same as Canvas fallback)
            spriteBatch.draw(whiteTexture, drawX, drawY, (float)width, (float)height, false, false,
                           0.0f, 0.5f, 1.0f, getOpacity());
        }
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    private void checkOffScreenDeath() {
        if (camera == null) return;
        
        double marginX = 50;
        double marginY = 50;
        
        double cameraLeft = camera.getX();
        double cameraTop = camera.getY();
        double cameraRight = cameraLeft + camera.getWidth();
        double cameraBottom = cameraTop + camera.getHeight();
        
        if (x + width < cameraLeft - marginX || x > cameraRight + marginX ||
            y + height < cameraTop - marginY || y > cameraBottom + marginY) {
            
            if (!death) {
                // Use takeDamage to ensure sound plays immediately
                takeDamage(DeathCause.FALL);
                setVisible(false);
            }
        }
    }

    public boolean isOnIce() {
        return onIce;
    }

    public void setOnIce(boolean onIce) {
        this.onIce = onIce;
    }

    public DeathCause getDeathCause() {
        return deathCause;
    }

    public void setSpeedX(double speedX) {
        physics.setSpeedX(speedX);
    }

    public double getSpeedX() {
        return  physics.getSpeedX();
    }

    public void setSpeedY(double speedY) {
        physics.setSpeedY(speedY);
    }

    public void setGravity(double gravity) {
        physics.setGravity(gravity);
    }

    public boolean isRestartOnDeath() {
        return restartOnDeath;
    }

    public void setRestartOnDeath(boolean restartOnDeath) {
        this.restartOnDeath = restartOnDeath;
    }

    public void resetDeathState() {
        death = false;
        deathDelayCounter = 0;
        deathCause = null;
        setVisible(true);
        setActive(true);
        
        // Reset all input states
        setPressLEFT(false);
        setPressRIGHT(false);
        setPressUP(false);
        setPressDOWN(false);
        
        // Reset cancel flags (important for portal teleportation)
        setCancelLEFT(false);
        setCancelRIGHT(false);
        setCancelUP(false);
        setCancelDOWN(false);

        isTeleporting = false;
        
        // Reset touch controls to prevent stuck button states
        resetTouchControls();
        
        // Clear player trail on respawn
        trailHelper.clear();
        
    }

    private void resetTouchControls() {
        try {
            // Get the GameHostActivity and access the GameComponent
            GameHostActivity activity = 
                GameConstants.INSTANCE.getGameHostActivity();
            
            if (activity != null) {
                // Access the GameComponent through the activity
                GameComponent gameComponent = 
                    activity.findViewById(com.unipi.alexandris.android.echotrialsonandroid.R.id.game);
                
                if (gameComponent != null) {
                    // Get the GL surface view from the game component
                    GLGameSurfaceView glView =
                        gameComponent.getGLGameSurfaceView();
                    
                    if (glView != null && glView.getGLRenderer() != null && 
                        glView.getGLRenderer().getUIRenderer() != null) {
                        
                        GLTouchControlRenderer touchRenderer =
                            glView.getGLRenderer().getUIRenderer().getTouchControlRenderer();
                        
                        if (touchRenderer != null) {
                            touchRenderer.releaseAllControls();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore errors - touch controls will reset naturally on next touch
        }
    }

    private void showFailDialog() {
        try {
            // Try direct GameHostActivity reference first (most reliable)
            GameHostActivity activity = 
                GameConstants.INSTANCE.getGameHostActivity();
            
            if (activity != null) {
                // Use Handler to ensure this runs on the main thread
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    try {
                        activity.showFailDialog();
                    } catch (Exception ignored) {}
                });
                return;
            }
            
            // Fallback: Try context-based approach
            final GameHostActivity finalActivity = getGameHostActivity();

            if (finalActivity != null) {
                // Use Handler to ensure this runs on the main thread
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    try {
                        finalActivity.showFailDialog();
                    } catch (Exception ignored) {}
                });
            }
        } catch (Exception ignored) {}
    }

    @Nullable
    private static GameHostActivity getGameHostActivity() {
        Context context = GameConstants.INSTANCE.getContext();

        // Try multiple approaches to find GameHostActivity
        final GameHostActivity finalActivity;

        // Approach 1: Direct cast
        if (context instanceof GameHostActivity) {
            finalActivity = (GameHostActivity) context;
        }
        // Approach 2: Unwrap ContextThemeWrapper (with loop protection)
        else if (context instanceof android.view.ContextThemeWrapper) {
            Context baseContext = context;
            int unwrapCount = 0;
            GameHostActivity foundActivity = null;
            while (baseContext instanceof android.view.ContextThemeWrapper && unwrapCount < 10) {
                baseContext = ((android.view.ContextThemeWrapper) baseContext).getBaseContext();
                unwrapCount++;

                if (baseContext instanceof GameHostActivity) {
                    foundActivity = (GameHostActivity) baseContext;
                    break;
                }
            }
            finalActivity = foundActivity;
        } else {
            finalActivity = null;
        }
        return finalActivity;
    }

    public boolean getTeleporting() {
        return isTeleporting;
    }
}