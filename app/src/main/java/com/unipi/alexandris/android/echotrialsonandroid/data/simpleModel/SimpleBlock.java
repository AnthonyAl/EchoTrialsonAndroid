package com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel;

@SuppressWarnings(value = "unused")
public abstract class SimpleBlock extends SimplePhysicsObject {

    public SimpleBlock(double x, double y, SimpleID id, int groupId) {
        super(x, y, 48, id, groupId);
        // Set block-specific defaults
        setWidthMultiplier(1.0);
        setHeightMultiplier(1.0);
        // Update width and height based on multipliers
        setWidth(size * getWidthMultiplier());
        setHeight(size * getHeightMultiplier());
    }
} 