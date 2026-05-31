package com.unipi.alexandris.android.echotrialsonandroid.data;

import com.unipi.alexandris.android.echotrialsonandroid.model.*;
import com.unipi.alexandris.android.echotrialsonandroid.utility.*;
import com.unipi.alexandris.android.echotrialsonandroid.controller.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GameStateSnapshot stores the initial state of all game objects for safe restart functionality.
 * This allows the game to be restarted without interfering with OpenGL rendering or object references.
 * <br/>
 * The snapshot contains:
 * - Object positions, velocities, and properties
 * - Player-specific state (health, air, etc.)
 * - Game constants state (spawn position, etc.)
 * - Object relationships and references
 */
@SuppressWarnings("unused")
public class GameStateSnapshot {

    public static class ObjectSnapshot {
        public final double x, y, velX, velY, velW, velH;
        public final int size;
        public final double width, height, widthMultiplier, heightMultiplier;
        public final boolean active, visible;
        public final float opacity;
        public final boolean playSounds;
        public final int groupId;
        public final int uid;
        public final String objectType;
        
        public ObjectSnapshot(PhysicsObject obj) {
            this.x = obj.getX();
            this.y = obj.getY();
            this.velX = obj.getVelX();
            this.velY = obj.getVelY();
            this.velW = obj.getVelW();
            this.velH = obj.getVelH();
            this.size = obj.getSize();
            this.width = obj.getWidth();
            this.height = obj.getHeight();
            this.widthMultiplier = obj.getWidthMultiplier();
            this.heightMultiplier = obj.getHeightMultiplier();
            this.active = obj.isActive();
            this.visible = obj.isVisible();
            this.opacity = obj.getOpacity();
            this.playSounds = obj.isPlaySounds();
            this.groupId = obj.getGroupId();
            this.uid = obj.getUid();
            this.objectType = obj.getClass().getSimpleName();
        }
    }

    public static class TriggerSnapshot extends ObjectSnapshot {
        public final boolean engaged;
        public final boolean active;
        public final int clock;
        
        public TriggerSnapshot(Trigger trigger) {
            super(trigger);
            this.engaged = trigger.isEngaged();
            this.active = trigger.isActive();
            this.clock = trigger.getClock();
        }
    }

    public static class SpikeSnapshot extends ObjectSnapshot {
        public final int damageCooldown;
        
        public SpikeSnapshot(BlockSpike spike) {
            super(spike);
            this.damageCooldown = spike.getDamageCooldown();
        }
    }

    public static class PlayerSnapshot extends ObjectSnapshot {
        public final boolean inAir, onIce, inWater;
        public final int air;
        public final boolean restartOnDeath;
        public final double speedX, speedY, gravity;
        
        public PlayerSnapshot(Player player) {
            super(player);
            this.inAir = player.isInAir();
            this.onIce = player.isOnIce();
            this.inWater = player.isInWater();
            this.air = player.getAir();
            this.restartOnDeath = player.isRestartOnDeath();
            this.speedX = player.getSpeedX();
            this.speedY = player.getPhysics().getSpeedY();
            this.gravity = player.getPhysics().getGravity();
        }
    }

    private final List<ObjectSnapshot> objectSnapshots = new ArrayList<>();
    private final Map<Integer, ObjectSnapshot> objectSnapshotsByUid = new HashMap<>(); // Map by UID for reliable matching
    private PlayerSnapshot playerSnapshot = null;
    private final int spawnX, spawnY;
    private final String currentLevel;
    

    public GameStateSnapshot(ObjectHandler handler) {
        // Store spawn position and current level
        this.spawnX = GameConstants.INSTANCE.getSpawnX();
        this.spawnY = GameConstants.INSTANCE.getSpawnY();
        this.currentLevel = GameConstants.INSTANCE.getCurrentLevel() != null ? GameConstants.INSTANCE.getCurrentLevel().name() : "UNKNOWN";

        List<PhysicsObject> objects = handler.getObjectsView();
        
        for (PhysicsObject obj : objects) {
            
            if (obj instanceof Player) {
                this.playerSnapshot = new PlayerSnapshot((Player) obj);
                objectSnapshots.add(playerSnapshot);
                objectSnapshotsByUid.put(playerSnapshot.uid, playerSnapshot);
            } else if (obj instanceof Trigger) {
                TriggerSnapshot triggerSnapshot = new TriggerSnapshot((Trigger) obj);
                objectSnapshots.add(triggerSnapshot);
                objectSnapshotsByUid.put(triggerSnapshot.uid, triggerSnapshot);
            } else if (obj instanceof BlockSpike) {
                SpikeSnapshot spikeSnapshot = new SpikeSnapshot((BlockSpike) obj);
                objectSnapshots.add(spikeSnapshot);
                objectSnapshotsByUid.put(spikeSnapshot.uid, spikeSnapshot);
            } else if (obj instanceof Block) {
                ObjectSnapshot snapshot = new ObjectSnapshot(obj);
                objectSnapshots.add(snapshot);
                objectSnapshotsByUid.put(snapshot.uid, snapshot);
            } else {
                ObjectSnapshot snapshot = new ObjectSnapshot(obj);
                objectSnapshots.add(snapshot);
                objectSnapshotsByUid.put(snapshot.uid, snapshot);
            }
        }
    }

    public void restore(ObjectHandler handler) {
        GameConstants.INSTANCE.setGamePaused(true);
        
        try {
            List<PhysicsObject> currentObjects = handler.getObjectsView();

            for (PhysicsObject currentObj : currentObjects) {
                if (currentObj != null) {
                    int uid = currentObj.getUid();
                    ObjectSnapshot snapshot = objectSnapshotsByUid.get(uid);
                    
                    if (snapshot != null) {
                        restoreObjectState(currentObj, snapshot);
                    }
                }
            }

            if (playerSnapshot != null) {
                Player player = handler.getPlayer();
                if (player != null) {
                    restorePlayerState(player, playerSnapshot);
                }
            }

            restoreTriggerStates(handler);
            restoreSpikeStates(handler);
            
            // Restore game constants
            GameConstants.INSTANCE.setSpawnX(spawnX);
            GameConstants.INSTANCE.setSpawnY(spawnY);
            resetCameraPosition();

            if (playerSnapshot != null) {
                Player player = handler.getPlayer();
                if (player != null) {
                    player.death = false;
                    player.setVisible(true);
                    player.setActive(true);
                    player.resetDeathState();
                }
            }
            
        } finally {
            GameConstants.INSTANCE.setGamePaused(false);
        }
    }

    private void restoreObjectState(PhysicsObject obj, ObjectSnapshot snapshot) {
        obj.setX(snapshot.x);
        obj.setY(snapshot.y);
        obj.setVelX(snapshot.velX);
        obj.setVelY(snapshot.velY);
        obj.setVelW(snapshot.velW);
        obj.setVelH(snapshot.velH);
        obj.setSize(snapshot.size);
        obj.setWidth(snapshot.width);
        obj.setHeight(snapshot.height);
        obj.setWidthMultiplier(snapshot.widthMultiplier);
        obj.setHeightMultiplier(snapshot.heightMultiplier);
        obj.setActive(snapshot.active);
        obj.setVisible(snapshot.visible);
        obj.setOpacity(snapshot.opacity);
        obj.setPlaySounds(snapshot.playSounds);
        obj.setGroupId(snapshot.groupId);
    }

    private void restorePlayerState(Player player, PlayerSnapshot snapshot) {
        restoreObjectState(player, snapshot);

        player.setInAir(snapshot.inAir);
        player.setOnIce(snapshot.onIce);
        player.setRestartOnDeath(snapshot.restartOnDeath);
        player.setSpeedX(snapshot.speedX);
        player.setSpeedY(snapshot.speedY);
        player.setGravity(snapshot.gravity);

        player.getPhysics().resetVelocity();
    }

    private void restoreTriggerStates(ObjectHandler handler) {
        List<PhysicsObject> currentObjects = handler.getObjectsView();
        
        for (PhysicsObject currentObj : currentObjects) {
            if (currentObj instanceof Trigger) {
                int uid = currentObj.getUid();
                ObjectSnapshot snapshot = objectSnapshotsByUid.get(uid);
                
                if (snapshot instanceof TriggerSnapshot) {
                    Trigger trigger = 
                        (Trigger) currentObj;
                    TriggerSnapshot triggerSnapshot = (TriggerSnapshot) snapshot;
                    
                    restoreTriggerState(trigger, triggerSnapshot);
                }
            }
        }
    }

    private void restoreTriggerState(Trigger trigger, TriggerSnapshot snapshot) {
        restoreObjectState(trigger, snapshot);
        trigger.resetToInitialState();
    }

    private void restoreSpikeStates(ObjectHandler handler) {
        List<PhysicsObject> currentObjects = handler.getObjectsView();
        
        for (PhysicsObject currentObj : currentObjects) {
            if (currentObj instanceof BlockSpike) {
                int uid = currentObj.getUid();
                ObjectSnapshot snapshot = objectSnapshotsByUid.get(uid);
                
                if (snapshot instanceof SpikeSnapshot) {
                    BlockSpike spike = 
                        (BlockSpike) currentObj;
                    SpikeSnapshot spikeSnapshot = (SpikeSnapshot) snapshot;
                    
                    restoreSpikeState(spike, spikeSnapshot);
                }
            }
        }
    }

    private void restoreSpikeState(BlockSpike spike, SpikeSnapshot snapshot) {
        restoreObjectState(spike, snapshot);
        spike.resetToInitialState();
    }

    private void resetCameraPosition() {
        try {
            Camera camera =
                GameController.GAME.getCamera();
            
            if (camera != null) {
                double cameraX = spawnX - camera.getWidth() / 2.0;
                double cameraY = spawnY - camera.getHeight() / 2.0;
                
                // Apply bounds checking to prevent camera from going out of bounds
                // Use more reasonable bounds that allow the camera to move to spawn position
                double minX = -5000; // Allow negative X for levels that start off-screen
                double maxX = 10000; // Adjust based on your level width
                double minY = 0;
                double maxY = 10000; // Adjust based on your level height
                
                // Clamp camera position within bounds
                cameraX = Math.max(minX, Math.min(maxX - camera.getWidth(), cameraX));
                cameraY = Math.max(minY, Math.min(maxY - camera.getHeight(), cameraY));
                
                camera.setX(cameraX);
                camera.setY(cameraY);
            }
        } catch (Exception ignored) {} // Handle camera reset error silently
    }

    public int getObjectCount() {
        return objectSnapshots.size();
    }

    public PlayerSnapshot getPlayerSnapshot() {
        return playerSnapshot;
    }

    public int getSpawnX() {
        return spawnX;
    }

    public int getSpawnY() {
        return spawnY;
    }

    public String getCurrentLevel() {
        return currentLevel;
    }
}
