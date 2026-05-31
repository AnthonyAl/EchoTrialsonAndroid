package com.unipi.alexandris.android.echotrialsonandroid.model;

import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;

/**
 * SimpleTrigger that changes gravity orientation.
 * Only affects the player.
 */
@SuppressWarnings(value = "unused")
public class TriggerGravity extends Trigger {

    public void setGravity(double gravityValue) {
        // This method is for compatibility - use setGravityStrength instead
        setGravityStrength(gravityValue);
    }
    
    public void setGravityStrength(double gravityStrength) {
        this.gravityStrength = gravityStrength;
    }
    
    public double getGravityStrength() {
        return gravityStrength;
    }

    /** Gravity direction options */
    public enum GravityDirection {
        UP, DOWN, LEFT, RIGHT
    }

    /** Target gravity direction */
    private GravityDirection gravityDirection = GravityDirection.DOWN;
    
    /** Gravity strength multiplier */
    private double gravityStrength = 1.0;

    public TriggerGravity(int x, int y, ObjectHandler handler, int delay, int speed, int WR1, int WR2, int HR1, int HR2, int groupId) {
        super(x, y, handler, ID.TriggerGravity, groupId, delay, speed, WR1, WR2, HR1, HR2);
    }

    @Override
    protected void action() {
        for(PhysicsObject target : getTargetObjects()) {
            if(!(target instanceof Player)) return;
            Player player = (Player) target;

            switch (gravityDirection) {
                case UP: {
                    player.setGravity(-gravityStrength);
                    deactivate();
                    break;
                }
                case DOWN: {
                    player.setGravity(gravityStrength);
                    deactivate();
                    break;
                }
                // TODO: gravity LEFT & RIGHT will be implemented in a future Title Update
            }
        }
    }

    /**
     * Gets the gravity direction
     */
    public GravityDirection getGravityDirection() {
        return gravityDirection;
    }

    /**
     * Sets the gravity direction
     */
    public void setGravityDirection(GravityDirection gravityDirection) {
        this.gravityDirection = gravityDirection;
    }
}
