package com.unipi.alexandris.android.echotrialsonandroid.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The SimpleID enum represents different types of game objects and elements.
 * Each enum value corresponds to a specific type of entity that can exist in the game world.
 * <br>
 * Each SimpleID includes an interaction mask defining which other object types it can interact with.
 * This provides a clean, immutable way to handle collision detection and object interactions.
 */
public enum ID {
    /** The player character controlled by the user */
    Player(List.of("BlockCommon", "BlockIce", "BlockWater", "BlockSpikeCommon", "BlockSpikeCommonDown", 
                   "BlockSpikeCommonLeft", "BlockSpikeCommonRight", "BlockSpikeIce", "BlockSpikeIceDown", 
                   "BlockSpikeIceLeft", "BlockSpikeIceRight", "Portal")),
    
    /** SimplePortal for level completion or teleportation */
    Portal(List.of( "Player")),
    
    /** SimpleForeground layer game elements */
    Foreground(new ArrayList<>()),
    
    /** SimpleBackground layer game elements */
    Background(new ArrayList<>()),
    
    /** Solid blocks that form the level structure */
    BlockCommon(List.of("Player", "Particles")),

    /** Ice blocks providing slippery surfaces */
    BlockIce(List.of("Player", "Particles")),
    
    /** Spike obstacles that damage the player on contact */
    BlockSpikeCommon(List.of("Player")),
    BlockSpikeCommonDown(List.of("Player")),
    BlockSpikeCommonLeft(List.of("Player")),
    BlockSpikeCommonRight(List.of("Player")),
    
    /** Ice spike blocks */
    BlockSpikeIce(List.of("Player")),
    BlockSpikeIceDown(List.of("Player")),
    BlockSpikeIceLeft(List.of("Player")),
    BlockSpikeIceRight(List.of("Player")),
    
    /** Water blocks affecting player movement and mechanics */
    BlockWater(List.of("Player")),
    
    /** Text objects for UI and world text */
    TextObject(new ArrayList<>()), // Text objects typically don't have physics interactions
    
    /** Visual particle effects */
    Particles(List.of("BlockCommon", "BlockIce")),
    
    // SimpleTrigger Types
    /** Scale trigger for size changes */
    TriggerScale(List.of("Player")),
    
    /** Fade trigger for visibility changes */
    TriggerFade(List.of("Player", "BlockCommon", "BlockIce", "BlockSpikeCommon", "BlockSpikeCommonDown", 
            "BlockSpikeCommonLeft", "BlockSpikeCommonRight", "BlockSpikeIce", "BlockSpikeIceDown", 
            "BlockSpikeIceLeft", "BlockSpikeIceRight", "Portal")),
    
    /** Gravity trigger for physics changes */
    TriggerGravity(List.of("Player")),
    
    /** Inversion trigger for control changes */
    TriggerInversion(List.of("Player")),
    
    /** Movement trigger for position changes */
    TriggerTranslate(List.of("BlockCommon", "BlockIce", "BlockSpikeCommon", "BlockSpikeCommonDown", 
            "BlockSpikeCommonLeft", "BlockSpikeCommonRight", "BlockSpikeIce", "BlockSpikeIceDown", 
            "BlockSpikeIceLeft", "BlockSpikeIceRight", "Portal")),
    
    /** Movement trigger for position changes */
    TriggerMovement(List.of("Player"));

    final private List<String> interactionMaskStringIds = new ArrayList<>();
    final private List<ID> interactionMask = new ArrayList<>();

    ID(List<String> interactionMasksStringIds) {
        // Lazy initialization of interactionMask
        this.interactionMaskStringIds.addAll(interactionMasksStringIds);
    }

    public List<ID> getInteractionMask() {
        // If the interactionMasks is empty, attempt to fill it out based on the interactionMaskStringIds
        // interactionMasks is not initialized in constructor because the Enum Objects are not yet initialized
        if (interactionMask.isEmpty()) {
            synchronized (this) { // Extra safety for paranoid threading
                if (interactionMask.isEmpty()) { // Double-check pattern
                    for (String id : interactionMaskStringIds) interactionMask.add(valueOf(id));
                }
            }
        }
        return new ArrayList<>(interactionMask);
    }

    public boolean canInteractWith(ID otherId) {
        return interactionMask.contains(otherId);
    }
} 