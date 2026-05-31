package com.unipi.alexandris.android.echotrialsonandroid.cosmetics;

import com.unipi.alexandris.android.echotrialsonandroid.utility.ParticleCreator;
import com.unipi.alexandris.android.echotrialsonandroid.utility.PhysicsPlatformer;

public class DeathParticleHelper {
    
    private final ParticleCreator particleCreator;
    private final PhysicsPlatformer physics;
    
    public DeathParticleHelper(ParticleCreator particleCreator, PhysicsPlatformer physics) {
        this.particleCreator = particleCreator;
        this.physics = physics;
    }

    public void createDeathParticles(double x, double y, double width, double height, double velX, double velY) {
        if (particleCreator == null) return;
        
        // Get equipped death particle cosmetic
        AvailableCosmetics equippedParticles = EquippedCosmetics.INSTANCE.getEquippedCosmetic(CosmeticType.DEATH_PARTICLES);
        if (equippedParticles != null) {
            createDeathParticlesWithCosmetic(equippedParticles, x, y, width, height, velX, velY);
        } else {
            // Fallback to default particles
            createDefaultDeathParticles(x, y, width, height, velX, velY);
        }
    }

    private void createDeathParticlesWithCosmetic(AvailableCosmetics cosmetic, double x, double y, double width, double height, double velX, double velY) {
        String cosmeticId = cosmetic.getId();
        switch (cosmeticId) {
            case "particles_death_sparkles":
                createSparkleDeathParticles(x, y, width, height, velX, velY);
                break;
            case "particles_death_smoke":
                createSmokeDeathParticles(x, y, width, height, velX, velY);
                break;
            case "particles_death_default":
            default:
                createDefaultDeathParticles(x, y, width, height, velX, velY);
                break;
        }
    }

    private void createDefaultDeathParticles(double x, double y, double width, double height, double velX, double velY) {
        float centerX = (float)(x + width / 2);
        float centerY = (float)(y - 2);
        int particleSize = (int) (width+height)/2;

        for(int i = 0; i < 15; i++) {
            double particleVelX = Math.random() * 6 + 2;
            if(Math.random() < 0.5) particleVelX = -particleVelX;
            double particleVelY = Math.random() * 3 - 8;

            String[] colors = {"halloween_red"};
            try {
                particleCreator.spawn(2, centerX, centerY, colors, 15*particleSize/100, 3, 8,
                        3, 2, 85, -0.1, physics.getGravity()*0.5, particleVelX+velX*2, particleVelY+Math.abs(velY)*2, 0.6F,  0.2F, true);
            } catch (Exception ignored) {
            }
        }

        for(int i = 0; i < 15; i++) {
            double particleVelX = Math.random() * 4 + 1;
            if(Math.random() < 0.5) particleVelX = -particleVelX;
            double particleVelY = Math.random() * 5 - 12;

            String[] colors = {"halloween_black", "halloween_red"};
            try {
                particleCreator.spawn(3, centerX, centerY, colors, 20*particleSize/100, 3, 8,
                        3, 1, 85, 0.1, physics.getGravity()*0.8, particleVelX+velX*2, particleVelY+Math.abs(velY)*2, 0.6F,  0.2F, true);
            } catch (Exception ignored) {
            }
        }

        double particleVelY = Math.random() * 4 - 2;
        String[] colors = {"halloween_black", "halloween_red"};
        try {
            particleCreator.spawn(1, centerX, centerY, colors, 25*particleSize/100, 0, 0,
                    0, 0, 85, 0, physics.getGravity(), velX, particleVelY+Math.abs(velY)*2.3, 0.6F,  0.2F, true);
        } catch (Exception ignored) {
        }
    }


    // EXAMPLES:

    private void createSparkleDeathParticles(double x, double y, double width, double height, double velX, double velY) {
        float centerX = (float)(x + width / 2);
        float centerY = (float)(y - 2);
        int particleSize = (int) (width+height)/2;

        for(int i = 0; i < 20; i++) {
            double particleVelX = Math.random() * 8 + 2;
            if(Math.random() < 0.5) particleVelX = -particleVelX;
            double particleVelY = Math.random() * 6 - 10;

            String[] colors = {"christmas_gold", "christmas_white"};
            try {
                particleCreator.spawn(2, centerX, centerY, colors, 12*particleSize/100, 4, 12,
                        4, 3, 90, -0.05, physics.getGravity()*0.3, particleVelX+velX*2, particleVelY+Math.abs(velY)*2, 0.8F, 0.3F, true);
            } catch (Exception ignored) {
            }
        }
    }

    private void createSmokeDeathParticles(double x, double y, double width, double height, double velX, double velY) {
        float centerX = (float)(x + width / 2);
        float centerY = (float)(y - 2);
        int particleSize = (int) (width+height)/2;

        for(int i = 0; i < 25; i++) {
            double particleVelX = Math.random() * 4 + 1;
            if(Math.random() < 0.5) particleVelX = -particleVelX;
            double particleVelY = Math.random() * 8 - 15;

            String[] colors = {"silver", "halloween_black"};
            try {
                particleCreator.spawn(3, centerX, centerY, colors, 18*particleSize/100, 2, 10,
                        5, 2, 95, 0.05, physics.getGravity()*0.6, particleVelX+velX, particleVelY+Math.abs(velY)*1.5, 0.4F, 0.4F, true);
            } catch (Exception ignored) {
            }
        }
    }
}
