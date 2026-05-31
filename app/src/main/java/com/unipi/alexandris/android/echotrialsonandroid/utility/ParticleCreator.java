package com.unipi.alexandris.android.echotrialsonandroid.utility;

import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.model.ID;
import com.unipi.alexandris.android.echotrialsonandroid.model.Particle;

/**
 * Manages the creation and spawning of particle effects.
 * Provides methods for creating particle systems with various properties.
 * Used for visual effects like dust, water splashes, explosions, etc.
 */
public class ParticleCreator {

    /** The game object handler for managing particle instances. */
    private final ObjectHandler handler;
    
    // Particle limits to prevent resource exhaustion
    private static final int MAX_PARTICLES = 1000; // Increased from 100
    private static final int MAX_PARTICLES_PER_SPAWN = 100; // Increased from 20

    /**
     * Constructs a new ParticleCreator with a reference to the game's object handler.
     *
     * @param handler The handler for managing game objects and particles
     */
    public ParticleCreator(ObjectHandler handler) {
        this.handler = handler;
    }

    /**
     * Spawns a complex particle system with customizable properties.
     * Creates multiple particles with specified behavior and appearance.
     *
     * @param num Number of particles to spawn
     * @param x X coordinate of spawn point
     * @param y Y coordinate of spawn point
     * @param colors Array of colors for particles
     * @param size Base size of particles
     * @param offset Random offset for particle positions
     * @param spawnOffsetX X-axis spawn shapeArea width
     * @param spawnOffsetY Y-axis spawn shapeArea height
     * @param sizeOffset Random variation in particle size
     * @param time Particle lifetime in seconds
     * @param gravityX Horizontal gravity effect
     * @param gravityY Vertical gravity effect
     * Spawns a complex particle system.
     */
    public void spawn(int num, float x, float y, String[] colors, int size, int offset,
                      int spawnOffsetX, int spawnOffsetY, int sizeOffset, double time,
                      double gravityX, double gravityY) {
        int currentParticles = handler.getObjectCountByType(ID.Particles);
        if (currentParticles >= MAX_PARTICLES) return;

        int particlesToSpawn = Math.min(num, MAX_PARTICLES_PER_SPAWN);
        particlesToSpawn = Math.min(particlesToSpawn, MAX_PARTICLES - currentParticles);
        
        Region emptyRegion = new Region();

        for(int i = 0; i < particlesToSpawn; i++) {
            handler.addObject(new Particle(
                    x, y, ID.Particles, handler, colors, size, offset,
                    spawnOffsetX, spawnOffsetY, sizeOffset, time,
                    gravityX, gravityY, 0, 0, 0, 0, false
            ));
        }
    }

    /**
     * Spawns a physics-based particle system with velocity and collision.
     * Creates particles that interact with obstacles and have initial velocity.
     *
     * @param num Number of particles to spawn
     * @param x X coordinate of spawn point
     * @param y Y coordinate of spawn point
     * @param colors Array of colors for particles
     * @param size Base size of particles
     * @param offset Random offset for particle positions
     * @param spawnOffsetX X-axis spawn shapeArea width
     * @param spawnOffsetY Y-axis spawn shapeArea height
     * @param sizeOffset Random variation in particle size
     * @param time Particle lifetime in seconds
     * @param gravityX Horizontal gravity effect
     * @param gravityY Vertical gravity effect
     * @param velX Initial X velocity
     * @param velY Initial Y velocity
     */
    public void spawn(int num, float x, float y, String[] colors, int size, int offset,
                      int spawnOffsetX, int spawnOffsetY, int sizeOffset, double time,
                      double gravityX, double gravityY, double velX, double velY, float bounceX, float bounceY, boolean rigid) {
        int currentParticles = handler.getObjectCountByType(ID.Particles);
        if (currentParticles >= MAX_PARTICLES) return;

        int particlesToSpawn = Math.min(num, MAX_PARTICLES_PER_SPAWN);
        particlesToSpawn = Math.min(particlesToSpawn, MAX_PARTICLES - currentParticles);
        
        for(int i = 0; i < particlesToSpawn; i++) {
            handler.addObject(new Particle(
                    x, y, ID.Particles, handler, colors, size, offset,
                    spawnOffsetX, spawnOffsetY, sizeOffset, time,
                    gravityX, gravityY, velX, velY, bounceX, bounceY, rigid
            ));
        }
    }

    /**
     * Spawns a simple single-color particle effect.
    * Simplified method for basic particle effects with minimal parameters.
     *
     * @param x X coordinate of spawn point
     * @param y Y coordinate of spawn point
     * @param color Color of the particle
     * @param offset Random offset for particle position
     * @param size Size of the particle
     * @param time Particle lifetime in seconds
     */
    public void spawn(float x, float y, String color, int offset, int size, double time) {
        String[] colors = {color};
        spawn(1, x, y, colors, size, offset, 0, 0, 0, time, 0, 0);
    }

    /**
     * Creates a color from RGB components.
     */
    public static int createColor(int r, int g, int b) {
        return android.graphics.Color.rgb(r, g, b);
    }
}