package com.unipi.alexandris.android.echotrialsonandroid.model;

import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;

/**
 * SimpleTrigger that inverts directional controls.
 * Swaps left/right controls. Only affects the player.
 */
@SuppressWarnings(value = "unused")
public class TriggerInversion extends Trigger {

    public TriggerInversion(int x, int y, ObjectHandler handler, int delay, int speed, int WR1, int WR2, int HR1, int HR2, int groupId) {
        super(x, y, handler, ID.TriggerInversion, groupId, delay, speed, WR1, WR2, HR1, HR2);
    }

    @Override
    protected void action() {
        for(PhysicsObject target : getTargetObjects()) {
            if (!(target instanceof Player)) return;
            Player player = (Player) target;

            player.setSpeedX(-player.getSpeedX());

            deactivate();
        }
    }
}
