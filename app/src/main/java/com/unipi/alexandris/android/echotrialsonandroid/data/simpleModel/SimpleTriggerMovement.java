package com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel;

@SuppressWarnings(value = "unused")
public class SimpleTriggerMovement extends SimpleTrigger {
    private double xSpeed = 1.0;
    private double ySpeed = 1.0;
    private double gravityIntensity = 1.0;

    public SimpleTriggerMovement(int x, int y, int groupId) {
        super(x, y, SimpleID.TriggerMovement, groupId);
    }

    public double getXSpeed() {
        return xSpeed;
    }

    public void setXSpeed(double xSpeed) {
        this.xSpeed = xSpeed;
    }

    public double getYSpeed() {
        return ySpeed;
    }

    public void setYSpeed(double ySpeed) {
        this.ySpeed = Math.max(0.0, ySpeed);
    }

    public double getGravityIntensity() {
        return gravityIntensity;
    }

    public void setGravityIntensity(double gravityIntensity) {
        this.gravityIntensity = Math.max(0.0, gravityIntensity);
    }
}
