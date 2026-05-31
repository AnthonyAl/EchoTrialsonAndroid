package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLSpriteBatch;

/**
 * Simple text display object for UI.
 */
public class TextObject extends PhysicsObject {
    private final String text;
    private final int textSize;
    private final Paint textPaint;

    public TextObject(double x, double y, ObjectHandler handler, String text, int textSize, int groupId) {
        super(x, y, 0, handler, ID.TextObject, groupId);
        this.text = text;
        this.textSize = textSize;
        
        // Setup text paint
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
    }
    
    @Override
    protected void onTick() {
        // Text doesn't need to update
    }

    @Override
    public void renderGL(GLSpriteBatch spriteBatch, Camera camera) {
        if (!isVisible() || spriteBatch == null || text == null || text.isEmpty()) return;
        
        // TODO: Text rendering in OpenGL requires GLTextRenderer integration
        // This would need to be handled by the UI system rather than individual objects
        // For now, this is a placeholder - text rendering is typically handled by GLUIRenderer
        
        // Text objects in game world are uncommon - most text is UI-based
        // If world-space text is needed, it would require:
        // 1. GLTextRenderer to generate text texture
        // 2. Render texture as sprite at world coordinates
        // 3. Handle text color, size, and positioning
    }
    
    @Override
    public Rect getBounds() {
        // Simple bounds for collision detection
        return new Rect((int)x, (int)y - textSize, (int)(x + textPaint.measureText(text)), (int)y);
    }
    
    @Override
    public Region getShapeArea() {
        return new Region(getBounds());
    }
} 