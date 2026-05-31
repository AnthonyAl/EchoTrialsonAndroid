package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.graphics.Rect;
import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLSpriteBatch;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLTextureManager;

public class Background extends PhysicsObject {
    
    /**
     * Constructs a new SimpleBackground at the specified position.
     */
    public Background(double x, double y, ObjectHandler handler, int groupId) {
        super(x, y, 32, handler, ID.Background, groupId);
    }
    
    @Override
    protected void onTick() {
        // SimpleBackground doesn't need to update
    }

    @Override
    public void renderGL(GLSpriteBatch spriteBatch, Camera camera) {
        
        if (!isVisible() || spriteBatch == null) return;
        
        // Get texture manager
        GLTextureManager textureManager =
            spriteBatch.getTextureManager();
        if (textureManager == null) return;
        
        // Use world coordinates directly - camera transform applied by view matrix
        float drawX = (float)x;
        float drawY = (float)y;
        
        // Use background texture if needed for individual background objects
        int textureId = textureManager.getBackgroundTexture();
        if (textureId != -1) {
            // Draw background texture with per-vertex opacity
            spriteBatch.draw(textureId, drawX, drawY, size, size, false, false,
                           1.0f, 1.0f, 1.0f, getOpacity());
        }
    }
    
    @Override
    public Rect getBounds() {
        // SimpleBackground doesn't need collision detection
        return new Rect(0, 0, 0, 0);
    }
    
    @Override
    public Region getShapeArea() {
        // SimpleBackground doesn't need collision detection
        return new Region();
    }
} 