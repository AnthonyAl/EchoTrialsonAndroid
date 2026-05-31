package com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel;

@SuppressWarnings(value = "unused")
public class SimpleTriggerGravity extends SimpleTrigger {

    public enum GravityDirection {
        UP, DOWN, LEFT, RIGHT
    }

    private double gravityStrength;

    private GravityDirection gravityDirection = GravityDirection.DOWN;

    public SimpleTriggerGravity(int x, int y, int groupId) {
        super(x, y,SimpleID.TriggerGravity, groupId);
    }

    public double getGravityStrength() {
        return gravityStrength;
    }

    public void setGravityStrength(double gravityStrength) {
        this.gravityStrength = gravityStrength;
    }

    public GravityDirection getGravityDirection() {
        return gravityDirection;
    }

    public void setGravityDirection(GravityDirection gravityDirection) {
        this.gravityDirection = gravityDirection;
    }
}
