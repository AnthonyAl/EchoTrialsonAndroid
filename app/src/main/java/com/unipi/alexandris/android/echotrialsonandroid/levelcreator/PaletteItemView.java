package com.unipi.alexandris.android.echotrialsonandroid.levelcreator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import androidx.annotation.NonNull;

@SuppressLint("ViewConstructor")
public class PaletteItemView extends View {
    private final Paint paint;
    private final LevelObject levelObject;
    private boolean isSelected;
    private static final int SELECTED_BORDER_WIDTH = 4;
    private Bitmap texture;

    public PaletteItemView(Context context, LevelObject object) {
        super(context);
        this.levelObject = object;
        
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        
        // Set fixed size for palette items
        setMinimumWidth(32);
        setMinimumHeight(32);
        
        // Load texture
        if (object.getTextureResourceId() != 0) {
            texture = BitmapFactory.decodeResource(getResources(), object.getTextureResourceId());
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        int padding = isSelected ? SELECTED_BORDER_WIDTH : 2;
        
        // Draw selection border if selected
        if (isSelected) {
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(SELECTED_BORDER_WIDTH);
            canvas.drawRect((float) SELECTED_BORDER_WIDTH /2, (float) SELECTED_BORDER_WIDTH /2,
                    width - (float) SELECTED_BORDER_WIDTH /2, height - (float) SELECTED_BORDER_WIDTH /2, paint);
        }
        
        // Draw only texture, no tint
        if (texture != null) {
            // Calculate centerInside scaling to maintain aspect ratio
            float textureWidth = texture.getWidth();
            float textureHeight = texture.getHeight();
            float viewWidth = width - padding * 2;
            float viewHeight = height - padding * 2;
            
            float scale = Math.min(viewWidth / textureWidth, viewHeight / textureHeight);
            float scaledWidth = textureWidth * scale;
            float scaledHeight = textureHeight * scale;
            
            // Center the scaled image
            float left = padding + (viewWidth - scaledWidth) / 2;
            float top = padding + (viewHeight - scaledHeight) / 2;
            float right = left + scaledWidth;
            float bottom = top + scaledHeight;
            
            canvas.drawBitmap(texture, null, 
                new RectF(left, top, right, bottom), 
                new Paint());
        }
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        invalidate();
    }
    public LevelObject getLevelObject() {
        return levelObject;
    }
} 