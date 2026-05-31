package com.unipi.alexandris.android.echotrialsonandroid.levelcreator;

import android.graphics.Color;

import com.unipi.alexandris.android.echotrialsonandroid.R;

/**
 * Enumeration representing different types of objects that can be placed in the level.
 * Each object type has associated color, display name, and texture resource.
 * <br>
 * This enum provides the foundation for all placeable elements in the level editor,
 * defining their visual appearance and identifying properties.
 */
public enum LevelObject {
    BLOCK(Color.rgb(255, 0, 0), "SimpleBlock", R.drawable.brick5),
    ICE_BLOCK(Color.rgb(200, 255, 255), "Ice SimpleBlock", R.drawable.ice2),
    WATER_BLOCK(Color.rgb(0, 200, 255), "Water SimpleBlock", R.drawable.water4),
    PORTAL(Color.rgb(33, 0, 127), "SimplePortal", R.drawable.portal),
    PLAYER_SPAWN(Color.rgb(0, 0, 0), "SimplePlayer Spawn", R.drawable.idle),
    ICE_SPIKE(Color.rgb(38, 127, 0), "Ice Spike", R.drawable.ice_spikes),
    ICE_SPIKE_DOWN(Color.rgb(38, 127, 0), "Ice Spike Down", R.drawable.ice_spikes_d),
    ICE_SPIKE_LEFT(Color.rgb(38, 127, 0), "Ice Spike Left", R.drawable.ice_spikes_l),
    ICE_SPIKE_RIGHT(Color.rgb(38, 127, 0), "Ice Spike Right", R.drawable.ice_spikes_r),
    SPIKE_BLOCK(Color.rgb(0, 255, 0), "Spike SimpleBlock", R.drawable.spikes),
    SPIKE_BLOCK_DOWN(Color.rgb(0, 255, 0), "Spike Down", R.drawable.spikes_d),
    SPIKE_BLOCK_LEFT(Color.rgb(0, 255, 0), "Spike Left", R.drawable.spikes_l),
    SPIKE_BLOCK_RIGHT(Color.rgb(0, 255, 0), "Spike Right", R.drawable.spikes_r),
    TRIGGER_FADE(Color.rgb(227, 219, 0), "Fade Trigger", R.drawable.trigger_fade),
    TRIGGER_INVERT(Color.rgb(0, 255, 109), "Invert Trigger", R.drawable.trigger_invert),
    TRIGGER_MOVEMENT(Color.rgb(248, 0, 84), "Movement Trigger", R.drawable.trigger_velocity),
    TRIGGER_SCALE(Color.rgb(165, 0, 255), "Scale Trigger", R.drawable.trigger_scale),
    TRIGGER_GRAVITY(Color.rgb(0, 96, 255), "Gravity Trigger", R.drawable.trigger_gravity),
    TRIGGER_TRANSLATE(Color.rgb(0, 255, 221), "Translate Trigger", R.drawable.trigger_translate);
    private final int color;
    private final String displayName;
    private final int textureResourceId;

    LevelObject(int color, String displayName, int textureResourceId) {
        this.color = color;
        this.displayName = displayName;
        this.textureResourceId = textureResourceId;
    }

    public int getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }
    public int getTextureResourceId() {
        return textureResourceId;
    }
} 