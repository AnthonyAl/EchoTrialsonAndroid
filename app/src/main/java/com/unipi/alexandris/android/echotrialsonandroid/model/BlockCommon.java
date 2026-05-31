package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLSpriteBatch;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLTextureManager;

/**
 * Common block - the most basic obstruction.
 */
public class BlockCommon extends Block {

    public BlockCommon(double x, double y, ObjectHandler handler, int groupId) {
        super(x, y, handler, ID.BlockCommon, groupId);
    }
    
    public BlockCommon(Region region, ObjectHandler handler, int groupId) {
        super(region, handler, ID.BlockCommon, groupId);
    }
    
    @Override
    protected void drawTile(GLSpriteBatch spriteBatch,
                            GLTextureManager textureManager,
                            int x, int y) {
        
        int textureId = textureManager.getBrickTexture();
        if (textureId != -1) {
            // Draw brick texture with per-vertex opacity
            spriteBatch.draw(textureId, (float)x, (float)y, (float) width, (float) height, false, false,
                           1.0f, 1.0f, 1.0f, getOpacity());
        } else {
            // Fallback to gray with per-vertex opacity
            int whiteTexture = textureManager.getWhiteTexture();
            if (whiteTexture != -1) {
                spriteBatch.draw(whiteTexture, (float)x, (float)y, (float) width, (float) height, false, false,
                               0.5f, 0.5f, 0.5f, getOpacity());
            }
        }
    }
}

