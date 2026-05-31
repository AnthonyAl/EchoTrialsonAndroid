package com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel;

@SuppressWarnings(value = "unused")
public class SimpleTriggerFade extends SimpleTrigger {
    private double targetOpacity = 0.0;
    private boolean removeCollisionAtZero = true;

    public SimpleTriggerFade(int x, int y, int groupId) {
        super(x, y, SimpleID.TriggerFade, groupId);
    }

    public double getTargetOpacity() {
        return targetOpacity;
    }

    public void setTargetOpacity(double targetOpacity) {
        this.targetOpacity = targetOpacity;
    }

    public boolean isRemoveCollisionAtZero() {
        return removeCollisionAtZero;
    }

    public void setRemoveCollisionAtZero(boolean removeCollisionAtZero) {
        this.removeCollisionAtZero = removeCollisionAtZero;
    }
}
