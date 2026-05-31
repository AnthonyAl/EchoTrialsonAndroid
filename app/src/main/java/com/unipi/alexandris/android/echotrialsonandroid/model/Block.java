package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.utility.TickRunnable;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLSpriteBatch;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLTextureManager;

/**
 * Simplified shape-agnostic block that uses Android Region for collision.
 * One unified rendering system for all block types.
 */
public abstract class Block extends PhysicsObject {
    protected Rect boundingRect;
    protected boolean uniform = true;
    protected TickRunnable onTick;
    protected double[] prevCenter = new double[2];
    
    /**
     * UNIFIED CONSTRUCTOR: Works for both individual blocks and unified regions
     */
    public Block(double x, double y, ObjectHandler handler, ID id, int groupId) {
        super(x, y, 48, handler, id, groupId);
        this.shapeArea = new Region((int)x, (int)y, (int)(x + width), (int)(y + height));
        updateCache();
    }

    public Block(Region region, ObjectHandler handler, ID id, int groupId) {
        super(region.getBounds().left, region.getBounds().top, 48, handler, id, groupId);
        this.shapeArea = new Region(region);
        updateCache();
    }
    
    private void updateCache() {
        this.boundingRect = shapeArea.getBounds();
        prevCenter[0] = boundingRect.centerX();
        prevCenter[1] = boundingRect.centerY();
    }

    @Override
    public void renderGL(GLSpriteBatch spriteBatch, Camera camera) {
        if (!isVisible() || spriteBatch == null) return;
        
        var textureManager = spriteBatch.getTextureManager();
        if (textureManager == null || shapeArea == null) return;
        
        Rect bounds = shapeArea.getBounds();
        if (bounds.isEmpty()) return;
        
        // Universal tile rendering - let subclass define texture
        renderTiles(spriteBatch, textureManager, bounds);
    }

    private void renderTiles(GLSpriteBatch spriteBatch,
                             GLTextureManager textureManager,
                             Rect bounds) {
        
        for (int y = bounds.top; y < bounds.bottom; y += (int) height) {
            for (int x = bounds.left; x < bounds.right; x += (int) width) {
                if (shapeArea.contains(x + 24, y + 24)) {
                    drawTile(spriteBatch, textureManager, x, y);
        }
            }
        }
    }

    protected abstract void drawTile(GLSpriteBatch spriteBatch,
                                     GLTextureManager textureManager,
                                     int x, int y);

    @Override
    public void setX(double x) {
        super.setX(x);
        updateShapeArea();
    }


    @Override
    public void setY(double y) {
        super.setY(y);
        updateShapeArea();
    }

    private void updateShapeArea() {
        if (shapeArea != null) {
            // Check if this is a unified block by looking at the region bounds vs position
            Rect regionBounds = shapeArea.getBounds();
            boolean isUnifiedBlock = (regionBounds.width() > width || regionBounds.height() > height);
            
            if (isUnifiedBlock) {
                // For unified blocks, translate the entire region to the new position
                int deltaX = (int)x - regionBounds.left;
                int deltaY = (int)y - regionBounds.top;
                
                if (deltaX != 0 || deltaY != 0) {
                    shapeArea.translate(deltaX, deltaY);
                }
                updateCache();
            } else {
                // For individual blocks, update both shapeArea and cache
                shapeArea.set((int)x, (int)y, (int)(x + width), (int)(y + height));
                updateCache();
            }
        }
    }

    @Override
    protected final void onTick() {
        physics.checkLethalInteractionCollision();
        if (uniform) {
            return;
        }
        
        if (onTick != null && onTick.isActive()) {
            onTick.run();
        } else {
            clearOnTick();
        }

        physics.blockMovementPhysics();
    }
    
    @Override
    public Rect getBounds() { return boundingRect; }
    
    @Override
    public Region getShapeArea() { return shapeArea; }
    
    public void setOnTick(TickRunnable onTick) {
        this.onTick = onTick;
        uniform = false;
    }

    protected void clearOnTick() {
        onTick = null;
        uniform = true;
    }

    public TickRunnable getOnTickTask() {
        return onTick;
    }

    public boolean isUniform() { return uniform; }

    @Override
    public boolean isVisibleInCamera(Camera camera) {
        if (camera == null) return true;
        Rect bounds = shapeArea.getBounds();
        int buffer = 100;
        return !(bounds.right < camera.getX() - buffer || 
                 bounds.left > camera.getX() + camera.getWidth() + buffer ||
                 bounds.bottom < camera.getY() - buffer || 
                 bounds.top > camera.getY() + camera.getHeight() + buffer);
    }
}