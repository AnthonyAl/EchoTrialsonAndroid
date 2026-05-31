package com.unipi.alexandris.android.echotrialsonandroid.levelcreator;

import android.graphics.Color;

import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.*;

/**
 * Represents an object placed on the level grid.
 * Each grid object has a SimpleModel instance and a group ID, which determines
 * its appearance, properties, and layering in the level editor.
 * <br/>
 * Grid objects serve as the fundamental data model for elements
 * placed in the level design. They combine SimpleModel data with
 * group assignment for multi-layered level editing.
 */
public class GridObject {

    private final SimplePhysicsObject simpleObject;

    private int groupId = 0;

    public GridObject(SimplePhysicsObject simpleObject) {
        this.simpleObject = simpleObject;
    }

     public GridObject(GridObject other) {
         if (other == null || other.simpleObject == null) {
             this.simpleObject = null;
             this.groupId = 0;
             return;
         }
         
         // Create a completely new SimplePhysicsObject instance of the same type
         LevelObject objectType = other.getType();
         SimplePhysicsObject originalObject = other.simpleObject;
         
         // Create new instance using the factory (just like when loading from file)
         this.simpleObject = SimpleObjectFactory.createSimpleObject(
             objectType,
             originalObject.getX(),
             originalObject.getY(),
             originalObject.getGroupId()
         );
         
         // Copy all properties manually to the new instance
         this.simpleObject.setVelX(originalObject.getVelX());
         this.simpleObject.setVelY(originalObject.getVelY());
         this.simpleObject.setSize(originalObject.getSize());
         this.simpleObject.setWidthMultiplier(originalObject.getWidthMultiplier());
         this.simpleObject.setHeightMultiplier(originalObject.getHeightMultiplier());
         
         // Copy type-specific properties
         if (originalObject instanceof SimplePlayer && this.simpleObject instanceof SimplePlayer) {
             SimplePlayer originalPlayer = (SimplePlayer) originalObject;
             SimplePlayer newPlayer = (SimplePlayer) this.simpleObject;
             newPlayer.setSpeedX(originalPlayer.getSpeedX());
             newPlayer.setSpeedY(originalPlayer.getSpeedY());
             newPlayer.setGravity(originalPlayer.getGravity());
         }
         
         // Copy trigger-specific properties
         if (originalObject instanceof SimpleTrigger && 
             this.simpleObject instanceof SimpleTrigger) {
             
             SimpleTrigger originalTrigger = 
                 (SimpleTrigger) originalObject;
             SimpleTrigger newTrigger = getSimpleTrigger(originalTrigger);

             // Copy trigger-specific properties based on type
             if (originalTrigger instanceof SimpleTriggerFade && 
                 newTrigger instanceof SimpleTriggerFade) {
                 
                 SimpleTriggerFade originalFade = 
                     (SimpleTriggerFade) originalTrigger;
                 SimpleTriggerFade newFade = 
                     (SimpleTriggerFade) newTrigger;
                 
                 newFade.setTargetOpacity(originalFade.getTargetOpacity());
                 newFade.setRemoveCollisionAtZero(originalFade.isRemoveCollisionAtZero());
                 
             } else if (originalTrigger instanceof SimpleTriggerGravity && 
                        newTrigger instanceof SimpleTriggerGravity) {
                 
                 SimpleTriggerGravity originalGravity = 
                     (SimpleTriggerGravity) originalTrigger;
                 SimpleTriggerGravity newGravity = 
                     (SimpleTriggerGravity) newTrigger;
                 
                 newGravity.setGravityStrength(originalGravity.getGravityStrength());
                 newGravity.setGravityDirection(originalGravity.getGravityDirection());
                 
             } else if (originalTrigger instanceof SimpleTriggerMovement && 
                        newTrigger instanceof SimpleTriggerMovement) {
                 
                 SimpleTriggerMovement originalMovement = 
                     (SimpleTriggerMovement) originalTrigger;
                 SimpleTriggerMovement newMovement = 
                     (SimpleTriggerMovement) newTrigger;
                 
                 newMovement.setXSpeed(originalMovement.getXSpeed());
                 newMovement.setYSpeed(originalMovement.getYSpeed());
                 newMovement.setGravityIntensity(originalMovement.getGravityIntensity());
                 
             } else if (originalTrigger instanceof SimpleTriggerScale && 
                        newTrigger instanceof SimpleTriggerScale) {
                 
                 SimpleTriggerScale originalScale = 
                     (SimpleTriggerScale) originalTrigger;
                 SimpleTriggerScale newScale = 
                     (SimpleTriggerScale) newTrigger;
                 
                             newScale.setTargetHeight(originalScale.getTargetHeight());
            newScale.setTargetWidth(originalScale.getTargetWidth());
                 
             } else if (originalTrigger instanceof SimpleTriggerTranslate && 
                        newTrigger instanceof SimpleTriggerTranslate) {
                 
                 SimpleTriggerTranslate originalTranslate = 
                     (SimpleTriggerTranslate) originalTrigger;
                 SimpleTriggerTranslate newTranslate = 
                     (SimpleTriggerTranslate) newTrigger;
                 
                 newTranslate.setDirection(originalTranslate.getDirection());
                 newTranslate.setBlockCount(originalTranslate.getBlockCount());
             }
             // Note: SimpleTriggerInversion has no specific properties beyond the common ones
         }
         
         this.groupId = other.groupId;
     }

    private SimpleTrigger getSimpleTrigger(SimpleTrigger originalTrigger) {
        SimpleTrigger newTrigger =
            (SimpleTrigger) this.simpleObject;

        // Copy common trigger properties
        newTrigger.setDelay(originalTrigger.getDelay());
        newTrigger.setSpeed(originalTrigger.getSpeed());
        // targetGroupId removed - triggers use their own groupId as target
        newTrigger.setWR1(originalTrigger.getWR1());
        newTrigger.setWR2(originalTrigger.getWR2());
        newTrigger.setHR1(originalTrigger.getHR1());
        newTrigger.setHR2(originalTrigger.getHR2());
        return newTrigger;
    }

    public SimplePhysicsObject getSimpleObject() {
        return simpleObject;
    }

    public LevelObject getType() {
        // Convert SimpleID to LevelObject for backward compatibility
        switch (simpleObject.getId()) {
            case BlockCommon:
                return LevelObject.BLOCK;
            case BlockIce:
                return LevelObject.ICE_BLOCK;
            case BlockWater:
                return LevelObject.WATER_BLOCK;
            case BlockSpikeCommon:
                return LevelObject.SPIKE_BLOCK;
            case BlockSpikeCommonDown:
                return LevelObject.SPIKE_BLOCK_DOWN;
            case BlockSpikeCommonLeft:
                return LevelObject.SPIKE_BLOCK_LEFT;
            case BlockSpikeCommonRight:
                return LevelObject.SPIKE_BLOCK_RIGHT;
            case BlockSpikeIce:
                return LevelObject.ICE_SPIKE;
            case BlockSpikeIceDown:
                return LevelObject.ICE_SPIKE_DOWN;
            case BlockSpikeIceLeft:
                return LevelObject.ICE_SPIKE_LEFT;
            case BlockSpikeIceRight:
                return LevelObject.ICE_SPIKE_RIGHT;
            case Portal:
                return LevelObject.PORTAL;
            case Player:
                return LevelObject.PLAYER_SPAWN;
            case TriggerFade:
                return LevelObject.TRIGGER_FADE;
            case TriggerInversion:
                return LevelObject.TRIGGER_INVERT;
            case TriggerMovement:
                return LevelObject.TRIGGER_MOVEMENT;
            case TriggerScale:
                return LevelObject.TRIGGER_SCALE;
            case TriggerGravity:
                return LevelObject.TRIGGER_GRAVITY;
            case TriggerTranslate:
                return LevelObject.TRIGGER_TRANSLATE;
            default:
                return LevelObject.BLOCK; // Default fallback
        }
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getColorForGroup() {
        float[] hsv = new float[3];
        Color.colorToHSV(getType().getColor(), hsv);
        
        // Modify saturation and brightness based on group ID
        hsv[1] = Math.min(1.0f, 0.5f + (groupId * 0.15f)); // Increase saturation with group
        hsv[2] = Math.max(0.4f, 1.0f - (groupId * 0.1f)); // Slightly decrease brightness
        
        return Color.HSVToColor(hsv);
    }
} 