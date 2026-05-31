package com.unipi.alexandris.android.echotrialsonandroid.utility;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.HashMap;
import java.util.Map;

public class ResourceManager {
    private final Map<String, Bitmap> bitmapCache = new HashMap<>();
    private final Context context;

    public ResourceManager(Context context) {
        this.context = context;
    }

    public Bitmap loadBitmap(Context context, int resourceId) {
        try {
            // Load the bitmap from resources using BitmapFactory
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);

            if (bitmap == null) {
                throw new Resources.NotFoundException("Resource SimpleID " + resourceId + " could not be decoded");
            }

            return bitmap;
        } catch (Resources.NotFoundException e) {
            // Return a placeholder bitmap for development (red square)
            Bitmap placeholder = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
            placeholder.eraseColor(0xFFFF0000);
            return placeholder;
        }
    }

    public void clearCache() {
        for (Bitmap bitmap : bitmapCache.values()) {
            bitmap.recycle();
        }
        bitmapCache.clear();
    }

    public Context getContext() {
        return context;
    }
}