package com.unipi.alexandris.android.echotrialsonandroid.model;

import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;

/**
 * SimpleTrigger that modifies movement speeds.
 * Only affects the player.
 */
@SuppressWarnings(value = "unused")
public class TriggerMovement extends Trigger {

    /** Horizontal movement speed multiplier (0+ values) */
    private double xSpeed = 1.0;
    
    /** Jump strength multiplier (0+ values) */
    private double ySpeed = 1.0;
    
    /** Gravity intensity multiplier (0+ values) */
    private double gravityIntensity = 1.0;

    public TriggerMovement(int x, int y, ObjectHandler handler, int delay, int speed, int WR1, int WR2, int HR1, int HR2, int groupId) {
        super(x, y, handler, ID.TriggerMovement, groupId, delay, speed, WR1, WR2, HR1, HR2);
    }

    @Override
    protected void action() {
        for(PhysicsObject target : getTargetObjects()) {
            if (!(target instanceof Player)) return;
            Player player = (Player) target;


            player.setSpeedX(player.getSpeedX() < 0 ? -xSpeed : xSpeed);
            player.setSpeedY(ySpeed);
            player.setGravity(gravityIntensity);

            deactivate();
        }
    }

    /**
     * Gets the X speed multiplier
     */
    public double getXSpeed() {
        return xSpeed;
    }

    /**
     * Sets the X speed multiplier (cannot be negative)
     */
    public void setXSpeed(double xSpeed) {
        this.xSpeed = Math.max(0.0, xSpeed);
    }

    /**
     * Gets the Y speed (jump strength) multiplier
     */
    public double getYSpeed() {
        return ySpeed;
    }

    /**
     * Sets the Y speed (jump strength) multiplier (cannot be negative)
     */
    public void setYSpeed(double ySpeed) {
        this.ySpeed = Math.max(0.0, ySpeed);
    }

    /**
     * Gets the gravity intensity multiplier
     */
    public double getGravityIntensity() {
        return gravityIntensity;
    }

    /**
     * Sets the gravity intensity multiplier (cannot be negative)
     */
    public void setGravityIntensity(double gravityIntensity) {
        this.gravityIntensity = Math.max(0.0, gravityIntensity);
    }

    public void setSpeedY(double speedY) {
    }

    public void setSpeedX(double speedX) {

    }
}
