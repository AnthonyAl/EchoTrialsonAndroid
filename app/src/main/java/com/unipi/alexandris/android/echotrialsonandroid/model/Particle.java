package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLSpriteBatch;
import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLTextureManager;

import java.util.Random;

/**
 * Represents individual particle objects in the game's particle system.
 * Each particle has its own physical properties, visual properties, and behavior.
 * Particle are used for visual effects like dust, water splashes, and explosions.
 */
public class Particle extends PhysicsObject {

    /** The color of this particle. */
    private final String color;

    /** Random number generator for particle variations. */
    private final Random random = new Random();

    /** Random position offset range. */
    private final int offset;

    /** Spawn shapeArea width. */
    private final int spawnOffsetX;

    /** Spawn shapeArea height. */
    private final int spawnOffsetY;

    private final boolean rigid;

    private final float bounceX;
    private final float bounceY;

    /** Random size variation range. */
    private final int sizeOffset;

    /** Lifetime counter for the particle. */
    private int counter;

    /** Maximum lifetime of the particle. */
    private final double time;

    /**
     * Constructs a new Particle with specified properties.
     * Initializes the particle with random variations within given ranges.
     *
     * @param x Initial X coordinate
     * @param y Initial Y coordinate
     * @param id Object identifier
     * @param handler Game object handler
     * @param colors Array of possible colors
     * @param size Base particle size
     * @param offset Position variation range
     * @param spawnOffsetX Spawn shapeArea width
     * @param spawnOffsetY Spawn shapeArea height
     * @param sizeOffset Size variation range
     * @param time Maximum lifetime
     * @param gravityX Horizontal gravity
     * @param gravityY Vertical gravity
     * @param velX Initial X velocity
     * @param velY Initial Y velocity
     */
    public Particle(float x, float y, ID id, ObjectHandler handler, String[] colors, int size,
                    int offset, int spawnOffsetX, int spawnOffsetY, int sizeOffset, double time,
                    double gravityX, double gravityY, double velX, double velY, float bounceX, float bounceY, boolean rigid) {
        super(x, y, size, handler, id, 0);

        // Sanitize inputs
        if(time < 0) time = 0;
        if(offset <= 0) offset = 1;
        if(spawnOffsetX <= 0) spawnOffsetX = 1;
        if(spawnOffsetY <= 0) spawnOffsetY = 1;

        // Initialize properties
        int i = random.nextInt(colors.length);
        this.color = colors[i];
        this.offset = offset;
        this.spawnOffsetX = spawnOffsetX;
        this.spawnOffsetY = spawnOffsetY;
        this.sizeOffset = sizeOffset;
        this.counter = 0;
        // FIX: Ensure particles have a minimum lifetime and use the time parameter directly
        this.time = time; // Use time directly, no random multiplication that could make particles immortal
        this.handler = handler;
        physics.setGravityX(gravityX);
        physics.setGravityY(gravityY);
        this.velX = velX;
        this.velY = velY;

        if(bounceX < 0) bounceX = 0;
        if(bounceX > 1) bounceX = 1;
        this.bounceX = bounceX;

        if(bounceY < 0) bounceY = 0;
        if(bounceY > 1) bounceY = 1;
        this.bounceY = bounceY;

        this.rigid = rigid;
        
    }

    @Override
    protected void onTick() {

        if(rigid) {
            //Update position through physics calculations
            Region blockCopy = new Region(GameConstants.INSTANCE.getGlobalRigidArea());
            physics.particleMovementPhysics(blockCopy);
            physics.particleSubDividePhysics(GameConstants.INSTANCE.getGlobalSpikesArea(), 5);
        }

        // Increment lifetime counter
        counter++;
        
        // Check if particle has expired
        if (counter >= time) {
            // Particle lifetime expired - remove it safely
            if (handler != null) {
                handler.removeObject(this);
            }
        }
    }

    @Override
    public void renderGL(GLSpriteBatch spriteBatch, Camera camera) {
        if (!isVisible() || spriteBatch == null) return;
        
        if(counter < time) {
            // Get texture manager and textures for solid color rendering
            GLTextureManager textureManager =
                spriteBatch.getTextureManager();
            if (textureManager == null) return;
            
            int whiteTexture = textureManager.getWhiteTexture();
            if (whiteTexture == -1) return;
            
            // Try to get a black texture first, fallback to white if not available
            int coloredTexture = textureManager.getColoredTexture(color);
            
            // Calculate random variations (same as Canvas version)
            int drawX = (int) x + random.nextInt(spawnOffsetX) - (spawnOffsetX / 2);
            int drawY = (int) y + random.nextInt(spawnOffsetY) - (spawnOffsetY / 2);
            int drawSize = size + random.nextInt(sizeOffset) - (sizeOffset / 2);
            
            // Ensure minimum size
            if (drawSize <= 0) drawSize = 1;
            
            // Validate texture IDs before drawing to prevent crashes
            if (coloredTexture > 0) {
                // Use black texture directly with per-vertex opacity (no tinting needed)
                spriteBatch.draw(coloredTexture, (float)drawX, (float)drawY, (float)drawSize, (float)drawSize, false, false,
                               1.0f, 1.0f, 1.0f, getOpacity());
            } else if (whiteTexture > 0) {
                // Fallback to white texture with black tint and per-vertex opacity
                spriteBatch.draw(whiteTexture, (float)drawX, (float)drawY, (float)drawSize, (float)drawSize, false, false,
                               0.0f, 0.0f, 0.0f, getOpacity());
            }
            // If both textures are invalid, skip rendering (prevents crash)
        }
        else {
            // Particle lifetime expired - remove it safely
            if (handler != null) {
                handler.removeObject(this);
            }
        }
    }

    public int getOffset() {
        return offset;
    }

    public float getBounceY() {
        return bounceY;
    }

    public float getBounceX() {
        return bounceX;
    }

    public boolean isRigid() {
        return rigid;
    }

    public String getColor() {
        return color;
    }

    public double getTime() {
        return time;
    }
}