package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLSpriteBatch;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLTextureManager;

/**
 * Ice block that provides slippery surfaces.
 */
public class BlockIce extends Block {
    
    public BlockIce(double x, double y, ObjectHandler handler, int groupId) {
        super(x, y, handler, ID.BlockIce, groupId);
    }
    
    public BlockIce(Region region, ObjectHandler handler, int groupId) {
        super(region, handler, ID.BlockIce, groupId);
    }
    
    @Override
    protected void drawTile(GLSpriteBatch spriteBatch,
                            GLTextureManager textureManager,
                            int x, int y) {
        
        int textureId = textureManager.getIceTexture();
        if (textureId != -1) {
            // Draw ice texture with per-vertex opacity
            spriteBatch.draw(textureId, (float)x, (float)y, (float) width, (float) height, false, false,
                           1.0f, 1.0f, 1.0f, getOpacity());
        } else {
            // Fallback to light blue with per-vertex opacity
            int whiteTexture = textureManager.getWhiteTexture();
            if (whiteTexture != -1) {
                spriteBatch.draw(whiteTexture, (float)x, (float)y, (float) width, (float) height, false, false,
                               0.7f, 0.9f, 1.0f, getOpacity());
            }
        }
    }
} 