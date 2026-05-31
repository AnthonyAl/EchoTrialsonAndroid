package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;


import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLSpriteBatch;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLTextureManager;

import java.util.ArrayList;
import java.util.Random;

/**
 * Manages the foreground layer of game levels.
 * Renders decorative elements and visual effects that appear in front of other game objects.
 * Uses a tile-based system with support for multiple texture types and random variations.
 */
public class Foreground extends PhysicsObject {

	/** Composite image containing all foreground elements */
	private Bitmap foreground;

	/** Collection of tile maps defining foreground layout */
	private ArrayList<char[][]> maps;

	/** Collection of textures used for different tile types */
	private ArrayList<Bitmap> images;

	public Foreground(int x, int y, ObjectHandler handler, int groupId, ArrayList<Bitmap> images, ArrayList<char[][]> maps, int multiplier, int width, int height) {
		super(x, y, 0, handler, ID.Foreground, groupId);
		this.maps = maps;
		this.images = images;

		// Create a mutable bitmap for the foreground
		foreground = Bitmap.createBitmap(width * multiplier, height * multiplier, Bitmap.Config.ARGB_8888);
		Random r = new Random();

		// Create a canvas to draw on the bitmap
		Canvas canvas = new Canvas(foreground);
		Paint paint = new Paint();
		paint.setColor(Color.argb(0, 255, 0, 0)); // Transparent red

		// Create a matrix for scaling the bitmaps
		Matrix matrix = new Matrix();

		for(char[][] map : maps) {
			for(int i = 0; i < map.length; i++) {
				for(int j = 0; j < map[i].length; j++) {
					// Random scaling factors, similar to the original
					int rW = r.nextInt(3) - 1; // -1, 0, or 1
					int rH = r.nextInt(3) - 1; // -1, 0, or 1

					if(rW == 0) rW = 1;
					if(rH == 0) rH = 1;

					int adjX = 0;
					int adjY = 0;
					if(rW < 0) adjX = -rW * multiplier;
					if(rH < 0) adjY = -rH * multiplier;

					// Draw the appropriate texture based on tile type
					if(map[i][j] == 'b' && !images.isEmpty()) {
						// Scale the bitmap if needed
						if (rW != 1 || rH != 1) {
							matrix.reset();
							matrix.setScale(rW, rH);
							Bitmap scaledBitmap = Bitmap.createBitmap(images.get(0),
									0, 0,
									images.get(0).getWidth(),
									images.get(0).getHeight(),
									matrix, true);
							canvas.drawBitmap(scaledBitmap, i * multiplier + adjX, j * multiplier + adjY, paint);

							// Recycle the scaled bitmap to avoid memory leaks
							if (scaledBitmap != images.get(0)) {
								scaledBitmap.recycle();
							}
						} else {
							canvas.drawBitmap(images.get(0), i * multiplier + adjX, j * multiplier + adjY, paint);
						}
					}
					if(map[i][j] == 'i' && images.size() > 1) {
						canvas.drawBitmap(images.get(1), i * multiplier, j * multiplier, paint);
					}
					if(map[i][j] == 'w' && images.size() > 2) {
						canvas.drawBitmap(images.get(2), i * multiplier, j * multiplier, paint);
					}
					if(map[i][j] == 'g') {
						canvas.drawRect(i * multiplier, j * multiplier,
								(i + 1) * multiplier, (j + 1) * multiplier, paint);
					}
				}
			}
		}
		// No need to dispose Canvas in Android
	}

	/**
	 * Updates foreground state.
	 * SimpleForeground is static and requires no updates.
	 */
	@Override
	protected void onTick() {
		// No update needed for static foreground
	}

	@Override
	public void renderGL(GLSpriteBatch spriteBatch, Camera camera) {
		
		if (!isVisible() || spriteBatch == null || foreground == null) return;
		
		// Get texture manager
		GLTextureManager textureManager =
			spriteBatch.getTextureManager();
		if (textureManager == null) return;
		
		// Use world coordinates directly - camera transform applied by view matrix
		float drawX = (float)x;
		float drawY = (float)y;
		
		// TODO: Convert composite foreground bitmap to OpenGL texture for efficient rendering
		// For now, this is a placeholder - foreground rendering needs special handling in OpenGL
		// The composite bitmap would need to be uploaded as a texture and rendered as a single quad
	}

	@Override
	public Rect getBounds() {
		return null; // No collision
	}

	@Override
	public Region getShapeArea() {
		return null;
	}

	public void setBounds(Rect bounds) {
		// Not applicable for SimpleForeground
	}

	public Path getPath() {
		return null; // No collision
	}

	public void dispose() {
		if (foreground != null && !foreground.isRecycled()) {
			foreground.recycle();
			foreground = null;
		}
	}
}