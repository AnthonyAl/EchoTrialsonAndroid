package com.unipi.alexandris.android.echotrialsonandroid.levelcreator;

import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleBlockCommon;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleBlockIce;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleBlockSpikeCommon;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleBlockSpikeIce;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleBlockWater;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimplePhysicsObject;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimplePlayer;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimplePortal;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleTriggerFade;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleTriggerGravity;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleTriggerInversion;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleTriggerMovement;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleTriggerScale;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleTriggerTranslate;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleBlockSpikeCommonDown;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleBlockSpikeCommonLeft;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleBlockSpikeCommonRight;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleBlockSpikeIceDown;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleBlockSpikeIceLeft;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleBlockSpikeIceRight;

/**
 * Factory class for creating SimpleModel instances from LevelObject types.
 * This class handles the conversion from level editor object types to
 * their corresponding SimpleModel representations.
 */
public class SimpleObjectFactory {

    public static SimplePhysicsObject createSimpleObject(LevelObject levelObject, int gridX, int gridY, int gridSize, int groupId) {
        // Convert grid coordinates to world coordinates
        double worldX = gridX * gridSize;
        double worldY = gridY * gridSize;
        
        switch (levelObject) {
            case BLOCK:
                return new SimpleBlockCommon(worldX, worldY, groupId);
            case ICE_BLOCK:
                return new SimpleBlockIce(worldX, worldY, groupId);
            case WATER_BLOCK:
                return new SimpleBlockWater(worldX, worldY, groupId);
            case SPIKE_BLOCK:
                return new SimpleBlockSpikeCommon(worldX, worldY, groupId);
            case SPIKE_BLOCK_DOWN:
                return new SimpleBlockSpikeCommonDown(worldX, worldY, groupId);
            case SPIKE_BLOCK_LEFT:
                return new SimpleBlockSpikeCommonLeft(worldX, worldY, groupId);
            case SPIKE_BLOCK_RIGHT:
                return new SimpleBlockSpikeCommonRight(worldX, worldY, groupId);
            case ICE_SPIKE:
                return new SimpleBlockSpikeIce(worldX, worldY, groupId);
            case ICE_SPIKE_DOWN:
                return new SimpleBlockSpikeIceDown(worldX, worldY, groupId);
            case ICE_SPIKE_LEFT:
                return new SimpleBlockSpikeIceLeft(worldX, worldY, groupId);
            case ICE_SPIKE_RIGHT:
                return new SimpleBlockSpikeIceRight(worldX, worldY, groupId);
            case PORTAL:
                return new SimplePortal(worldX, worldY, groupId);
            case PLAYER_SPAWN:
                return new SimplePlayer(worldX, worldY, groupId);
            case TRIGGER_FADE:
                return new SimpleTriggerFade((int)worldX, (int)worldY, groupId);
            case TRIGGER_INVERT:
                return new SimpleTriggerInversion((int)worldX, (int)worldY, groupId);
            case TRIGGER_MOVEMENT:
                return new SimpleTriggerMovement((int)worldX, (int)worldY, groupId);
            case TRIGGER_SCALE:
                return new SimpleTriggerScale((int)worldX, (int)worldY, groupId);
            case TRIGGER_GRAVITY:
                return new SimpleTriggerGravity((int)worldX, (int)worldY, groupId);
            case TRIGGER_TRANSLATE:
                return new SimpleTriggerTranslate((int)worldX, (int)worldY, groupId);
            default:
                // Default to common block if unknown type
                return new SimpleBlockCommon(worldX, worldY, groupId);
        }
    }

    public static SimplePhysicsObject createSimpleObject(LevelObject levelObject, double worldX, double worldY, int groupId) {
        switch (levelObject) {
            case BLOCK:
                return new SimpleBlockCommon(worldX, worldY, groupId);
            case ICE_BLOCK:
                return new SimpleBlockIce(worldX, worldY, groupId);
            case WATER_BLOCK:
                return new SimpleBlockWater(worldX, worldY, groupId);
            case SPIKE_BLOCK:
                return new SimpleBlockSpikeCommon(worldX, worldY, groupId);
            case SPIKE_BLOCK_DOWN:
                return new SimpleBlockSpikeCommonDown(worldX, worldY, groupId);
            case SPIKE_BLOCK_LEFT:
                return new SimpleBlockSpikeCommonLeft(worldX, worldY, groupId);
            case SPIKE_BLOCK_RIGHT:
                return new SimpleBlockSpikeCommonRight(worldX, worldY, groupId);
            case ICE_SPIKE:
                return new SimpleBlockSpikeIce(worldX, worldY, groupId);
            case ICE_SPIKE_DOWN:
                return new SimpleBlockSpikeIceDown(worldX, worldY, groupId);
            case ICE_SPIKE_LEFT:
                return new SimpleBlockSpikeIceLeft(worldX, worldY, groupId);
            case ICE_SPIKE_RIGHT:
                return new SimpleBlockSpikeIceRight(worldX, worldY, groupId);
            case PORTAL:
                return new SimplePortal(worldX, worldY, groupId);
            case PLAYER_SPAWN:
                return new SimplePlayer(worldX, worldY, groupId);
            case TRIGGER_FADE:
                return new SimpleTriggerFade((int)worldX, (int)worldY, groupId);
            case TRIGGER_INVERT:
                return new SimpleTriggerInversion((int)worldX, (int)worldY, groupId);
            case TRIGGER_MOVEMENT:
                return new SimpleTriggerMovement((int)worldX, (int)worldY, groupId);
            case TRIGGER_SCALE:
                return new SimpleTriggerScale((int)worldX, (int)worldY, groupId);
            case TRIGGER_GRAVITY:
                return new SimpleTriggerGravity((int)worldX, (int)worldY, groupId);
            case TRIGGER_TRANSLATE:
                return new SimpleTriggerTranslate((int)worldX, (int)worldY, groupId);
            default:
                // Default to common block if unknown type
                return new SimpleBlockCommon(worldX, worldY, groupId);
        }
    }
} 