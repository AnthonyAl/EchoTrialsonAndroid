package com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel;

@SuppressWarnings(value = "unused")
public class SimplePortal extends SimplePhysicsObject {

    public SimplePortal(double x, double y, int groupId) {
        super(x, y, 48, SimpleID.Portal, groupId);
        // Set portal-specific defaults to match Portal class
        setWidthMultiplier(1.2);
        setHeightMultiplier(1.5);
        // Update width and height based on multipliers
        setWidth(size * getWidthMultiplier());
        setHeight(size * getHeightMultiplier());
    }
} 