package com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel;

@SuppressWarnings(value = "unused")
public class SimpleTriggerScale extends SimpleTrigger {

    private double targetHeight = 1.0;
    private double targetWidth = 1.0;

    public SimpleTriggerScale(int x, int y, int groupId) {
        super(x, y, SimpleID.TriggerScale, groupId);
    }

    public double getTargetHeight() {
        return targetHeight;
    }

    public void setTargetHeight(double targetHeight) {
        this.targetHeight = Math.max(0.0, targetHeight);
    }

    public double getTargetWidth() {
        return targetWidth;
    }

    public void setTargetWidth(double targetWidth) {
        this.targetWidth = Math.max(0.0, targetWidth);
    }
}
