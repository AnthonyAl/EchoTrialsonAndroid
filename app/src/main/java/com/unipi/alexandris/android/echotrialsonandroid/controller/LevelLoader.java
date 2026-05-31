package com.unipi.alexandris.android.echotrialsonandroid.controller;

import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.model.Block;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockCommon;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockWater;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockIce;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpikeCommon;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpikeCommonDown;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpikeCommonLeft;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpikeCommonRight;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpikeIce;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpikeIceDown;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpikeIceLeft;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpikeIceRight;
import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;

import java.util.*;
import java.util.stream.Collectors;
import android.util.Log;

import org.w3c.dom.Document;

/**
 * Loads and initializes game levels.
 * <br>
 * Currently loads hardcoded levels (LEVEL_A_I, test level).
 * Will be enhanced to load from XML/JSON level data files:
 * <br>
 * PLANNED STRUCTURE:
 * - assets/levels/LEVEL_A_I.xml (level definition)
 * - Platform positions, enemy spawns, item locations
 * - Camera bounds, background textures, audio tracks
 * - SimpleTrigger events, scripted sequences
 * <br>
 * MIGRATION PATH:
 * 1. Define XML schema for level data
 * 2. Create level data parser (XML → SimplePhysicsObject creation)
 * 3. Replace hardcoded levels with data-driven loading
 * 4. Level editor saves to XML format
 * <br>
 * This simplified version only loads LEVEL_A_I and test level for current development.
 * <br>
 * ✨ NEW: Post-loading block group unification
 * Call unifyBlockGroups() after level creation to optimize grouped blocks.
 */
public class LevelLoader {
    private static final String TAG = "LevelLoader";
    private final DataDecoder dataDecoder;
    private boolean enableBlockUnification = true;

    public LevelLoader(Camera camera) {
        this.dataDecoder = new DataDecoder(camera);
    }
    
    /**
     * Sets whether block unification should be enabled.
     * Block unification merges blocks of the same type and group for performance.
     * 
     * @param enable true to enable block unification (default), false to disable
     */
    public void setEnableBlockUnification(boolean enable) {
        this.enableBlockUnification = enable;
    }

    public void loadLevel(Document document, String ID) {
        GameConstants.INSTANCE.resetGlobalArea();
        GameConstants.INSTANCE.setPlayer(null);

        loadLevelFromXML(document, ID);

        if (enableBlockUnification) {
            unifyBlockGroups();
        } else {
            Log.i(TAG, "Block unification disabled - keeping individual blocks");
        }
    }

    private void loadLevelFromXML(Document document, String ID) {
        try {
            if (dataDecoder.decodeLevelFromXML(document))
                Log.i(TAG, "Successfully loaded level from XML: " + ID);

            else Log.e(TAG, "Failed to load level from XML: " + ID);
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading level from XML: " + e.getMessage());
        }
    }

    public void unifyBlockGroups() {
        // Get all blocks grouped by type and groupId
        Map<String, List<Block>> groups = GameConstants.INSTANCE.getObjectHandler().getObjects().stream()
            .filter(obj -> obj instanceof Block)
            .map(obj -> (Block) obj)
            .filter(block -> block.getGroupId() >= 0)
            .collect(Collectors.groupingBy(block -> 
                block.getGroupId() + ":" + block.getClass().getSimpleName()));
        
        Log.d(TAG, "🔄 UNIFY: Found " + groups.size() + " block groups for unification");
        for (Map.Entry<String, List<Block>> entry : groups.entrySet()) {
            Log.d(TAG, "🔄 UNIFY: Group '" + entry.getKey() + "' has " + entry.getValue().size() + " blocks");
        }

        groups.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .forEach(this::unifyGroup);

        GameConstants.INSTANCE.rebuildCollisionRegions(GameConstants.INSTANCE.getObjectHandler().getObjects());
    }

    private void unifyGroup(Map.Entry<String, List<Block>> entry) {
        List<Block> blocks = entry.getValue();
        // Merge all regions
        Region unified = new Region();
        blocks.forEach(block -> unified.op(block.getShapeArea(), Region.Op.UNION));
        // Create unified block
        Block prototype = blocks.get(0);
        Block newBlock = createUnifiedBlock(prototype, unified);
        if (newBlock == null) return;
        // Replace all individual blocks with unified one
        blocks.forEach(GameConstants.INSTANCE.getObjectHandler()::removeObject);
        GameConstants.INSTANCE.getObjectHandler().addObject(newBlock);
    }

    private Block createUnifiedBlock(Block prototype, Region region) {
        try {
                Block unified = new BlockCommon(region, GameConstants.INSTANCE.getObjectHandler(), prototype.getGroupId());
                if(prototype instanceof BlockCommon) {
                    unified = new BlockCommon(region, GameConstants.INSTANCE.getObjectHandler(), prototype.getGroupId());
                }
                else if(prototype instanceof BlockIce) {
                    unified = new BlockIce(region, GameConstants.INSTANCE.getObjectHandler(), prototype.getGroupId());
                }
                else if(prototype instanceof BlockWater) {
                    unified = new BlockWater(region, GameConstants.INSTANCE.getObjectHandler(), prototype.getGroupId());
                }
                else if(prototype instanceof BlockSpikeCommon) {
                    unified = new BlockSpikeCommon(region, GameConstants.INSTANCE.getObjectHandler(), prototype.getGroupId());
                }
                else if(prototype instanceof BlockSpikeCommonDown) {
                    unified = new BlockSpikeCommonDown(region, GameConstants.INSTANCE.getObjectHandler(), prototype.getGroupId());
                }
                else if(prototype instanceof BlockSpikeCommonLeft) {
                    unified = new BlockSpikeCommonLeft(region, GameConstants.INSTANCE.getObjectHandler(), prototype.getGroupId());
                }
                else if(prototype instanceof BlockSpikeCommonRight) {
                    unified = new BlockSpikeCommonRight(region, GameConstants.INSTANCE.getObjectHandler(), prototype.getGroupId());
                }
                else if(prototype instanceof BlockSpikeIce) {
                    unified = new BlockSpikeIce(region, GameConstants.INSTANCE.getObjectHandler(), prototype.getGroupId());
                }
                else if(prototype instanceof BlockSpikeIceDown) {
                    unified = new BlockSpikeIceDown(region, GameConstants.INSTANCE.getObjectHandler(), prototype.getGroupId());
                }
                else if(prototype instanceof BlockSpikeIceLeft) {
                    unified = new BlockSpikeIceLeft(region, GameConstants.INSTANCE.getObjectHandler(), prototype.getGroupId());
                }
                else if(prototype instanceof BlockSpikeIceRight) {
                    unified = new BlockSpikeIceRight(region, GameConstants.INSTANCE.getObjectHandler(), prototype.getGroupId());
                }
            
            unified.setGroupId(prototype.getGroupId());
            return unified;
        } catch (Exception e) {
            return null;
        }
    }
} 