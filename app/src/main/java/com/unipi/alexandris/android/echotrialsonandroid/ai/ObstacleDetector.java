package com.unipi.alexandris.android.echotrialsonandroid.ai;

import android.graphics.Region;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpike;
import com.unipi.alexandris.android.echotrialsonandroid.model.PhysicsObject;
import com.unipi.alexandris.android.echotrialsonandroid.model.Player;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.utility.TickRunnable;

import java.util.List;

/**
 * AI system that detects obstacles ahead of the player and determines optimal jumping timing.
 * Continuously moves right and jumps to avoid walls, spikes, and holes.
 */
public class ObstacleDetector {
    
    private final Player player;
    private final ObjectHandler objectHandler;
    private static final double BASE_DETECTION_DISTANCE = 200;
    private static final double VELOCITY_MULTIPLIER = 2.0;
    private static final double PLAYER_HEIGHT = 48;
    private boolean isJumping = false;
    private int jumpCooldownFrames = 0;
    private static final int JUMP_COOLDOWN_FRAMES = 30;
    private TickRunnable currentJumpReleaseTask = null;
    private TickRunnable scheduledJumpTask = null;
    
    public ObstacleDetector(Player player, ObjectHandler objectHandler) {
        this.player = player;
        this.objectHandler = objectHandler;
    }

    public void update() {
        if (player == null || objectHandler == null) return;

        if (jumpCooldownFrames > 0) {
            jumpCooldownFrames--;
        }

        player.setPressRIGHT(true);
        player.setPressLEFT(false);

        if (!isJumping && jumpCooldownFrames <= 0) {
            int jumpDelay = shouldJumpWithTiming();
            if (jumpDelay >= 0) {
                if (jumpDelay == 0) {
                    performJump();
                } else {
                    scheduleJump(jumpDelay);
                }
            }
        }
    }

    private int shouldJumpWithTiming() {
        double playerX = player.getX();
        double playerY = player.getY();
        double playerVelX = player.getSpeedX();
        double playerVelY = player.getVelY();

        double detectionDistance = BASE_DETECTION_DISTANCE + (playerVelX * VELOCITY_MULTIPLIER);
        detectionDistance = Math.max(detectionDistance, 200);
        Region globalRigid = GameConstants.INSTANCE.getGlobalRigidArea();
        List<PhysicsObject> allObjects = objectHandler.getObjects();

        double wallDistance = getWallDistance(playerX, playerY, detectionDistance, globalRigid);
        if (wallDistance > 0) {
            return calculateJumpTiming(wallDistance, playerVelX, playerVelY);
        }

        double holeDistance = getHoleDistance(playerX, playerY, detectionDistance, globalRigid);
        if (holeDistance > 0) {
            return calculateJumpTiming(holeDistance, playerVelX, playerVelY);
        }

        double spikeDistance = getSpikeDistance(playerX, playerY, detectionDistance, allObjects);
        if (spikeDistance > 0) {
            return calculateJumpTiming(spikeDistance, playerVelX, playerVelY);
        }
        
        return -1;
    }

    private double getWallDistance(double playerX, double playerY, double detectionDistance, Region globalRigid) {
        for (int distance = 10; distance <= detectionDistance; distance += 10) {
            Region detectionArea = new Region(
                (int)(playerX + distance - 10), 
                (int)(playerY - PLAYER_HEIGHT), 
                (int)(playerX + distance), 
                (int)(playerY + PLAYER_HEIGHT)
            );
            
            Region intersection = new Region();
            intersection.op(detectionArea, globalRigid, Region.Op.INTERSECT);
            
            if (!intersection.isEmpty()) {
                return distance;
            }
        }
        return 0;
    }

    private double getHoleDistance(double playerX, double playerY, double detectionDistance, Region globalRigid) {
        for (int distance = 10; distance <= detectionDistance; distance += 10) {
            Region groundDetectionArea = new Region(
                (int)(playerX + distance - 10), 
                (int)(playerY + PLAYER_HEIGHT), 
                (int)(playerX + distance), 
                (int)(playerY + PLAYER_HEIGHT + 50)
            );
            
            Region intersection = new Region();
            intersection.op(groundDetectionArea, globalRigid, Region.Op.INTERSECT);
            
            if (intersection.isEmpty()) {
                return distance;
            }
        }
        return 0;
    }

    private double getSpikeDistance(double playerX, double playerY, double detectionDistance, List<PhysicsObject> allObjects) {
        double closestSpikeDistance = Double.MAX_VALUE;
        
        for (PhysicsObject obj : allObjects) {
            if (obj instanceof BlockSpike) {
                BlockSpike spike = (BlockSpike) obj;

                if (spike.getX() > playerX && spike.getX() <= playerX + detectionDistance) {
                    if (spike.getY() <= playerY + PLAYER_HEIGHT && 
                        spike.getY() + spike.getHeight() >= playerY) {
                        
                        double distance = spike.getX() - playerX;
                        if (distance < closestSpikeDistance) {
                            closestSpikeDistance = distance;
                        }
                    }
                }
            }
        }
        
        return closestSpikeDistance == Double.MAX_VALUE ? 0 : closestSpikeDistance;
    }

    private int calculateJumpTiming(double obstacleDistance, double playerVelX, double playerVelY) {
        double ticksToObstacle = obstacleDistance / Math.max(playerVelX, 1);

        if (playerVelY > 0) {
            ticksToObstacle -= playerVelY * 0.5;
        } else if (playerVelY < 0) {
            ticksToObstacle += Math.abs(playerVelY) * 0.2;
        }

        int optimalJumpDelay = (int) Math.max(0, ticksToObstacle - 5);

        return Math.min(optimalJumpDelay, 20);
    }

    private void scheduleJump(int delayTicks) {
        if (scheduledJumpTask != null) {
            objectHandler.removeTask(scheduledJumpTask);
        }
        
        scheduledJumpTask = new TickRunnable(
            () -> {
                performJump();
                scheduledJumpTask = null;
            }, delayTicks, 0, 1, "ScheduledJumpTask");
        objectHandler.runRepeating(scheduledJumpTask);
    }
    

    private void performJump() {
        if (jumpCooldownFrames > 0) {
            return;
        }
        
        isJumping = true;
        jumpCooldownFrames = JUMP_COOLDOWN_FRAMES;

        double playerVelX = player.getSpeedX();
        int jumpDurationFrames = (int) Math.max(6, Math.min(18, 9 + (playerVelX * 0.5)));

        if (currentJumpReleaseTask != null) {
            objectHandler.removeTask(currentJumpReleaseTask);
        }

        player.setPressUP(true);

        currentJumpReleaseTask = new TickRunnable(
            () -> {
                player.setPressUP(false);
                isJumping = false;
                currentJumpReleaseTask = null;
            }, jumpDurationFrames, 0, 1, "JumpReleaseTask");
        objectHandler.runRepeating(currentJumpReleaseTask);
    }

    public void reset() {
        if (currentJumpReleaseTask != null) {
            objectHandler.removeTask(currentJumpReleaseTask);
            currentJumpReleaseTask = null;
        }

        if (scheduledJumpTask != null) {
            objectHandler.removeTask(scheduledJumpTask);
            scheduledJumpTask = null;
        }

        player.setPressUP(false);
        isJumping = false;
        jumpCooldownFrames = 0;
    }

    public void cleanup() {
        if (currentJumpReleaseTask != null) {
            objectHandler.removeTask(currentJumpReleaseTask);
            currentJumpReleaseTask = null;
        }
        
        if (scheduledJumpTask != null) {
            objectHandler.removeTask(scheduledJumpTask);
            scheduledJumpTask = null;
        }

        player.setPressUP(false);
        player.setPressRIGHT(false);
        player.setPressLEFT(false);

        isJumping = false;
        jumpCooldownFrames = 0;
    }
}
