package com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel;

@SuppressWarnings(value = "unused")
public abstract class SimplePhysicsObject {
    protected double x, y, velX = 0, velY = 0;
    protected int size;
    protected double width, height, widthMultiplier = 1.0, heightMultiplier = 1.0;
    protected SimpleID id;
    protected int groupId;
    protected boolean active = true, visible = true;
    protected boolean cancelUP = false,cancelDOWN = false,cancelLEFT = false,cancelRIGHT = false;

    public SimplePhysicsObject(double x, double y, int size, SimpleID id, int groupId) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.groupId = groupId;
        if(size < 0) size = 0;
        this.size = size;
        this.width = size * widthMultiplier;
        this.height = size * heightMultiplier;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getVelX() {
        return velX;
    }

    public void setVelX(double velX) {
        this.velX = velX;
    }

    public double getVelY() {
        return velY;
    }

    public void setVelY(double velY) {
        this.velY = velY;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
        width = size * widthMultiplier;
        height = size * heightMultiplier;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
        if(size == 0) size = 1;
        widthMultiplier = width / size;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        if(height < 0) height = 0;
        this.height = height;
        if(size == 0) size = 1;
        heightMultiplier = height / size;
    }

    public double getWidthMultiplier() {
        return widthMultiplier;
    }

    public void setWidthMultiplier(double widthMultiplier) {
        if(widthMultiplier < 0) widthMultiplier = 0;
        this.widthMultiplier = widthMultiplier;
        this.width = this.size * widthMultiplier;
    }

    public double getHeightMultiplier() {
        return heightMultiplier;
    }

    public void setHeightMultiplier(double heightMultiplier) {
        if(heightMultiplier < 0) heightMultiplier = 0;
        this.heightMultiplier = heightMultiplier;
        height = size * heightMultiplier;
    }

    public SimpleID getId() {
        return id;
    }

    public void setId(SimpleID id) {
        this.id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isCancelUP() {
        return cancelUP;
    }

    public void setCancelUP(boolean cancelUP) {
        this.cancelUP = cancelUP;
    }

    public boolean isCancelDOWN() {
        return cancelDOWN;
    }

    public void setCancelDOWN(boolean cancelDOWN) {
        this.cancelDOWN = cancelDOWN;
    }

    public boolean isCancelLEFT() {
        return cancelLEFT;
    }

    public void setCancelLEFT(boolean cancelLEFT) {
        this.cancelLEFT = cancelLEFT;
    }

    public boolean isCancelRIGHT() {
        return cancelRIGHT;
    }

    public void setCancelRIGHT(boolean cancelRIGHT) {
        this.cancelRIGHT = cancelRIGHT;
    }
}