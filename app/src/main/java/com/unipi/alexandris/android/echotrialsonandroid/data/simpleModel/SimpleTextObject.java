package com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel;

import android.graphics.Color;

@SuppressWarnings(value = "unused")
public class SimpleTextObject extends SimplePhysicsObject {
    private String text;
    private int textSize;
    private Color textColor;

    public SimpleTextObject(double x, double y, int groupId) {
        super(x, y, 0, SimpleID.TextObject, groupId);
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Color getTextColor() {
        return textColor;
    }
}