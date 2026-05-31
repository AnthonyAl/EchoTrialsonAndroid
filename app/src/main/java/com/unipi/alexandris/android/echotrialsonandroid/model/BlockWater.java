package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLSpriteBatch;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLTextureManager;

/**
 * Water block that changes player physics.
 */
public class BlockWater extends Block {
    
    public BlockWater(double x, double y, ObjectHandler handler, int groupId) {
        super(x, y, handler, ID.BlockWater, groupId);
    }
    
    public BlockWater(Region region, ObjectHandler handler, int groupId) {
        super(region, handler, ID.BlockWater, groupId);
    }
    
    @Override
    protected void drawTile(GLSpriteBatch spriteBatch,
                            GLTextureManager textureManager,
                            int x, int y) {
        
        int textureId = textureManager.getWaterTexture();
        if (textureId != -1) {
            // Draw water texture with per-vertex opacity
            spriteBatch.draw(textureId, (float)x, (float)y, (float) width, (float) height, false, false,
                           1.0f, 1.0f, 1.0f, getOpacity());
        } else {
            // Fallback to blue with per-vertex opacity
            int whiteTexture = textureManager.getWhiteTexture();
            if (whiteTexture != -1) {
                spriteBatch.draw(whiteTexture, (float)x, (float)y, (float) width, (float) height, false, false,
                               0.2f, 0.5f, 1.0f, getOpacity());
            }
        }
    }
} 