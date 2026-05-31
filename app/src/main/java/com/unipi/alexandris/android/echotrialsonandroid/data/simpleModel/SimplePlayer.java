package com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel;

@SuppressWarnings(value = "unused")
public class SimplePlayer extends SimplePhysicsObject {

    private boolean inAir = false;
    private boolean onIce = false;
    private boolean inWater = false;
    private int health = 3;
    private int air = 100;
    private double speedX = 15.0, speedY = 20.0, gravity = 0.9;

    public SimplePlayer(double x, double y, int groupId) {
        super(x, y, 48, SimpleID.Player, groupId);
        // Set player-specific defaults
        setWidthMultiplier(1.0);
        setHeightMultiplier(1.3);
        // Update width and height based on multipliers
        setWidth(size * getWidthMultiplier());
        setHeight(size * getHeightMultiplier());
    }

    public double getGravity() {
        return gravity;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public double getSpeedY() {
        return speedY;
    }

    public void setSpeedY(double speedY) {
        this.speedY = speedY;
    }

    public double getSpeedX() {
        return speedX;
    }

    public void setSpeedX(double speedX) {
        this.speedX = speedX;
    }

    public int getAir() {
        return air;
    }

    public void setAir(int air) {
        this.air = air;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public boolean isInWater() {
        return inWater;
    }

    public void setInWater(boolean inWater) {
        this.inWater = inWater;
    }

    public boolean isOnIce() {
        return onIce;
    }

    public void setOnIce(boolean onIce) {
        this.onIce = onIce;
    }

    public boolean isInAir() {
        return inAir;
    }

    public void setInAir(boolean inAir) {
        this.inAir = inAir;
    }
}