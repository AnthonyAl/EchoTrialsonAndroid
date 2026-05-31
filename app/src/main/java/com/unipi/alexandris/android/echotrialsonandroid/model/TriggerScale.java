package com.unipi.alexandris.android.echotrialsonandroid.model;

import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;

/**
 * SimpleTrigger that changes object size.
 * Can affect any object.
 */
@SuppressWarnings(value = "unused")
public class TriggerScale extends Trigger {

    /** Target height multiplier */
    private double targetHeight = 1.0;
    
    /** Target width multiplier */
    private double targetWidth = 1.0;

    private double deltaHeight = 0.0;
    private double deltaWidth = 0.0;

    public TriggerScale(int x, int y, ObjectHandler handler, int delay, int speed, int WR1, int WR2, int HR1, int HR2, int groupId) {
        super(x, y, handler, ID.TriggerScale, groupId, delay, speed, WR1, WR2, HR1, HR2);
    }

    @Override
    protected void action() {
        for(PhysicsObject target : getTargetObjects()) {
            if (!(target instanceof Player)) return;
            Player player = (Player) target;

            if (deltaHeight == 0.0 && deltaWidth == 0.0) {
                // Calculate deltas for smooth scaling
                double currentHeight = player.getHeightMultiplier();
                double currentWidth = player.getWidthMultiplier();

                deltaHeight = (targetHeight - currentHeight) / speed;
                deltaWidth = (targetWidth - currentWidth) / speed;
            }

            // Apply scaling
            double newHeight = player.getHeightMultiplier();
            double newWidth = player.getWidthMultiplier();

            if (Math.abs(newHeight - targetHeight) >= 0.01){newHeight = player.getHeightMultiplier() + deltaHeight;}
            else newHeight = targetHeight;
            if (Math.abs(newWidth - targetWidth) >= 0.01) {newWidth = player.getWidthMultiplier() + deltaWidth;}
            else newWidth = targetWidth;

            // Check if scaling is complete
            if (Math.abs(newHeight - targetHeight) < 0.01 && Math.abs(newWidth - targetWidth) < 0.01) {
                player.setHeightMultiplier(targetHeight);
                player.setWidthMultiplier(targetWidth);
                deactivate();
                return;
            }

            player.setHeightMultiplier(newHeight);
            player.setWidthMultiplier(newWidth);
        }
    }

    /**
     * Gets the target height multiplier
     */
    public double getTargetHeight() {
        return targetHeight;
    }

    /**
     * Sets the target height multiplier (cannot be negative)
     */
    public void setTargetHeight(double targetHeight) {
        this.targetHeight = Math.max(0.0, targetHeight);
    }

    /**
     * Gets the target width multiplier
     */
    public double getTargetWidth() {
        return targetWidth;
    }

    /**
     * Sets the target width multiplier (cannot be negative)
     */
    public void setTargetWidth(double targetWidth) {
        this.targetWidth = Math.max(0.0, targetWidth);
    }


}
