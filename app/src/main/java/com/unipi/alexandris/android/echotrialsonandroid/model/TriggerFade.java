package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.widget.Toast;

import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;

/**
 * SimpleTrigger that controls opacity of target objects.
 */
@SuppressWarnings(value = "unused")
public class TriggerFade extends Trigger {

    /** Target opacity value (0.0 = fully transparent, 1.0 = fully opaque) */
    private double targetOpacity = 0.0;
    
    /** Whether to remove collision when opacity reaches 0 */
    private boolean removeCollisionAtZero = true;

    private float delta = 0f;

    public TriggerFade(int x, int y, ObjectHandler handler, int delay, int speed, int WR1, int WR2, int HR1, int HR2, int groupId) {
        super(x, y, handler, ID.TriggerFade, groupId, delay, speed, WR1, WR2, HR1, HR2);
    }

    @Override
    protected void action() {
        for(PhysicsObject target : getTargetObjects()) {
            if(delta == 0f) {
                delta = (float) ((targetOpacity - target.getOpacity()) / speed);
            }
            if(target.getOpacity() == targetOpacity) {
                if(targetOpacity == 0 && removeCollisionAtZero) handler.removeObject(target);

                this.deactivate();
                return;
            }

            float new_opacity = target.getOpacity() + delta;

            if(Math.abs(new_opacity - targetOpacity) < 0.025) new_opacity = (float) targetOpacity;

            target.setOpacity(new_opacity);
        }
    }

    /**
     * Gets the target opacity value
     */
    public double getTargetOpacity() {
        return targetOpacity;
    }

    /**
     * Sets the target opacity value
     */
    public void setTargetOpacity(double targetOpacity) {
        this.targetOpacity = Math.max(0.0, Math.min(1.0, targetOpacity));
    }

    /**
     * Gets whether collision is removed when opacity reaches zero
     */
    public boolean isRemoveCollisionAtZero() {
        return removeCollisionAtZero;
    }

    /**
     * Sets whether to remove collision when opacity reaches zero
     */
    public void setRemoveCollisionAtZero(boolean removeCollisionAtZero) {
        this.removeCollisionAtZero = removeCollisionAtZero;
    }

    public void setOpacity(float opacity) {

    }
}
