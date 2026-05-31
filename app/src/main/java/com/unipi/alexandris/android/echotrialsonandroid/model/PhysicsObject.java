package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLSpriteBatch;
import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.utility.PhysicsPlatformer;

/**
 * Abstract base class for all game objects.
 * Provides common functionality for position, movement, and collision detection.
 * 
 * <h3>Core Fields:</h3>
 * <ul>
 *   <li><b>Position & Size:</b> x, y (double), size (int), width, height (double)</li>
 *   <li><b>Scaling:</b> widthMultiplier, heightMultiplier (double) - for size modifications</li>
 *   <li><b>Identity:</b> id (SimpleID), groupId (int), uid (int) - object type, group classification, and unique identifier</li>
 *   <li><b>State:</b> active, visible (boolean) - object lifecycle and rendering state</li>
 * </ul>
 * 
 * <h3>Input Handling:</h3>
 * <ul>
 *   <li><b>Movement Input:</b> pressUP, pressDOWN, pressLEFT, pressRIGHT (boolean)</li>
 *   <li><b>Input Cancellation:</b> cancelUP, cancelDOWN, cancelLEFT, cancelRIGHT (boolean)</li>
 * </ul>
 * 
 * <h3>Collision & Rendering:</h3>
 * <ul>
 *   <li><b>Bounds:</b> bounds (RectF) - cached rectangular collision area</li>
 *   <li><b>Shape:</b> shapeArea (Region) - precise collision region for complex shapes</li>
 *   <li><b>Handler:</b> handler (ObjectHandler) - reference to game object manager</li>
 * </ul>
 * 
 * <h3>Usage Patterns:</h3>
 * <ul>
 *   <li><b>Physics:</b> Use double precision (x, y, width, height) for smooth movement</li>
 *   <li><b>Rendering:</b> Cast to float when passing to OpenGL methods</li>
 *   <li><b>Collision:</b> Use getBounds() for simple checks, getRegion() for precise detection</li>
 *   <li><b>Input:</b> Set press* flags for movement, cancel* flags to block input</li>
 * </ul>
 */
public abstract class PhysicsObject {
    protected double x, y, velX, velY, velW = 0, velH = 0;
    protected int size;
    protected boolean playSounds = true;
    protected float opacity = 1f;
    protected double width, height, widthMultiplier = 1.0, heightMultiplier = 1.0;
    protected ID id;
    protected int groupId;
    protected int uid = -1; // Unique identifier assigned by ObjectHandler

    protected final PhysicsPlatformer physics;

    protected boolean active = true, visible = true;
    protected boolean pressUP, pressDOWN, pressLEFT, pressRIGHT;
    protected boolean cancelUP = false,cancelDOWN = false,cancelLEFT = false,cancelRIGHT = false;
    protected final RectF bounds = new RectF();
    protected Region shapeArea;
    protected ObjectHandler handler;
    protected boolean sizeBound = false;
    protected boolean heightBound = false;
    protected boolean widthBound = false;

    public PhysicsObject(double x, double y, int size, ObjectHandler handler, ID id, int groupId) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.groupId = groupId;
        this.handler = handler;
        this.physics = new PhysicsPlatformer(this, GameConstants.INSTANCE.getSoundManager());

        if(size < 0) size = 0;
        this.size = size;
        this.width = size * widthMultiplier;
        this.height = size * heightMultiplier;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getUid() {
        return uid;
    }

    public java.util.List<PhysicsObject> getInteractiveObjects() {
        if (id == null) {
            return java.util.Collections.emptyList();
        }
        
        java.util.List<PhysicsObject> interactive = new java.util.ArrayList<>();
        for (ID interactableType : id.getInteractionMask()) {
            interactive.addAll(handler.getObjectsByType(interactableType));
        }
        return interactive;
    }

    /**
     * Updates the game object's state each game onTick.
     * Must be implemented by subclasses to define object behavior.
     * This method is called once per frame for active objects.
     */
    protected abstract void onTick();

    public final void tick() {
        onTick();

        // Reset size velocity values at the end of the tick
        velW = 0;
        velH = 0;
        setWidthBound(false);
        setHeightBound(false);
        setSizeBound(false);
    }


    public abstract void renderGL(GLSpriteBatch spriteBatch, Camera camera);

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Rect getBounds() {
        return new Rect((int) x, (int) y, (int) (x + width), (int) (y + height));
    };

    public RectF getBoundsF() {
        return new RectF((float) x, (float) y, (float) (x + width), (float) (y + height));
    }

    public Region getShapeArea() {
        return new Region(getBounds());
    };

    public boolean isVisibleInCamera(Camera camera) {
        if (camera == null) return true;
        
        // Add buffer zone to ensure objects at screen edges are rendered
        int buffer = 100; // 100-pixel buffer zone around screen edges
        
        float objRight = (float)(x + size);
        float objBottom = (float)(y + size);
        
        return !(objRight < camera.getX() - buffer || 
                 x > camera.getX() + camera.getWidth() + buffer ||
                 objBottom < camera.getY() - buffer || 
                 y > camera.getY() + camera.getHeight() + buffer);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
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

    public double getVelW() {
        return velW;
    }

    public void setVelW(double velW) {
        this.velW = velW;
    }

    public double getVelH() {
        return velH;
    }

    public void setVelH(double velH) {
        this.velH = velH;
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        if(sizeBound) {
            velH = 0;
            velW = 0;
            return;
        }
        if(size < 0) size = 0;
        this.size = size;
        setWidth(size * widthMultiplier);
        setHeight(size * heightMultiplier);
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public void setWidth(double width) {
        if(widthBound) {
            velW = 0;
            return;
        }
        if(width < 0) width = 0;
        this.velW = width - this.width;
        this.width = width;
        if(size == 0) size = 1;
        widthMultiplier = width / size;
    }

    public void setHeight(double height) {
        if(heightBound) {
            velH = 0;
            return;
        }
        if(height < 0) height = 0;
        this.velH = height - this.height;
        this.height = height;
        if(size == 0) size = 1;
        heightMultiplier = height / size;
    }

    public double getWidthMultiplier() {
        return widthMultiplier;
    }

    public void setWidthMultiplier(double widthMultiplier) {
        if(widthBound) {
            velW = 0;
            return;
        }
        if(widthMultiplier < 0) widthMultiplier = 0;
        this.widthMultiplier = widthMultiplier;
        setWidth(this.size * widthMultiplier);
    }

    public double getHeightMultiplier() {
        return heightMultiplier;
    }

    public void setHeightMultiplier(double heightMultiplier) {
        if(heightBound) {
            velH = 0;
            return;
        }
        if(heightMultiplier < 0) heightMultiplier = 0;
        this.heightMultiplier = heightMultiplier;
        setHeight(this.size * heightMultiplier);
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public void setCancelUP(boolean cancel) {
        this.cancelUP = cancel;
    }

    public void setCancelDOWN(boolean cancel) {
        this.cancelDOWN = cancel;
    }

    public void setCancelLEFT(boolean cancel) {
        this.cancelLEFT = cancel;
    }

    public void setCancelRIGHT(boolean cancel) {
        this.cancelRIGHT = cancel;
    }

    public boolean isCancelUP() {
        return cancelUP;
    }

    public boolean isCancelDOWN() {
        return cancelDOWN;
    }

    public boolean isCancelLEFT() {
        return cancelLEFT;
    }

    public boolean isCancelRIGHT() {
        return cancelRIGHT;
    }

    public boolean isPressUP() {
        return pressUP && !cancelUP;
    }

    public void setPressUP(boolean pressUP) {
        this.pressUP = pressUP;
    }

    public boolean isPressLEFT() {
        return pressLEFT && !cancelLEFT;
    }

    public void setPressLEFT(boolean pressLEFT) {
        this.pressLEFT = pressLEFT;
    }
    public boolean isPressDOWN() {
        return pressDOWN && !cancelDOWN;
    }

    public void setPressDOWN(boolean pressDOWN) {
        this.pressDOWN = pressDOWN;
    }

    public boolean isPressRIGHT() {
        return pressRIGHT && !cancelRIGHT;
    }

    public void setPressRIGHT(boolean pressRIGHT) {
        this.pressRIGHT = pressRIGHT;
    }

    public boolean isSizeBound() {
        return sizeBound;
    }

    public void setSizeBound(boolean sizeBound) {
        this.sizeBound = sizeBound;
    }

    public boolean isHeightBound() {
        return heightBound;
    }

    public void setHeightBound(boolean heightBound) {
        this.heightBound = heightBound;
    }

    public boolean isWidthBound() {
        return widthBound;
    }

    public void setWidthBound(boolean widthBound) {
        this.widthBound = widthBound;
    }

    public boolean isPlaySounds() {
        return playSounds;
    }

    public void setPlaySounds(boolean playSounds) {
        this.playSounds = playSounds;
    }

    public double getTrueHeight() {
        return (bounds.bottom - bounds.top) / height;
    }

    public double getTrueWidth() {
        return (bounds.right - bounds.left) / width;
    }

    public PhysicsPlatformer getPhysics() {
        return physics;
    }
}