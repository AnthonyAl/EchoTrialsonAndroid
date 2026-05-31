package com.unipi.alexandris.android.echotrialsonandroid.model;


import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLSpriteBatch;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLTextureManager;

/**
 * Base class for spike blocks that can damage players and other objects.
 * 
 * <p>Features collision detection, damage dealing, and particle effects.
 * Spikes can be configured as lethal or non-lethal hazards.</p>
 * 
 * <p>Optimized collision detection prevents spam damage using cooldowns
 * and only checks the primary target (usually player) for performance.</p>
 */
public abstract class BlockSpike extends Block {

	public int texture;
	protected boolean lethal = true;
	
	// Damage cooldown to prevent multiple damage per spike contact
	private static final int DAMAGE_COOLDOWN_TICKS = 30; // 0.5 seconds at 60 FPS
	private int damageCooldown = 0;
	
	public BlockSpike(double x, double y, ID id, int groupId, ObjectHandler handler, int texture) {
		super(x, y, handler, id, groupId);
		this.texture = texture;

	}

	public BlockSpike(Region region, ID id, int groupId, ObjectHandler handler, int texture) {
		super(region, handler, id, groupId);
		this.texture = texture;
	}

	@Override
    protected void drawTile(GLSpriteBatch spriteBatch,
							GLTextureManager textureManager,
							int x, int y) {

        // Get the actual OpenGL texture ID from the texture manager
		int textureId = getTextureId(textureManager);



        if (textureId != -1) {
            // Draw spike texture with per-vertex opacity
            spriteBatch.draw(textureId, (float)x, (float)y, (float) width, (float) height, false, false,
                           1.0f, 1.0f, 1.0f, getOpacity());
        } else {
            // Fallback to red color with per-vertex opacity
            int whiteTexture = textureManager.getWhiteTexture();
            if (whiteTexture != -1) {
                spriteBatch.draw(whiteTexture, (float)x, (float)y, (float) width, (float) height, false, false,
                               1.0f, 0.2f, 0.2f, getOpacity());
            }
        }
	}

	private int getTextureId(GLTextureManager textureManager) {
		int textureId = -1;

		// Map the resource ID to the appropriate texture getter method
		if (texture == R.drawable.spikes) {
			textureId = textureManager.getSpikeTexture();
		} else if (texture == R.drawable.spikes_d) {
			textureId = textureManager.getSpikeTextureDown();
		} else if (texture == R.drawable.spikes_l) {
			textureId = textureManager.getSpikeTextureLeft();
		} else if (texture == R.drawable.spikes_r) {
			textureId = textureManager.getSpikeTextureRight();
		} else if (texture == R.drawable.ice_spikes) {
			textureId = textureManager.getSpikeIceTexture();
		} else if (texture == R.drawable.ice_spikes_d) {
			textureId = textureManager.getSpikeIceTextureDown();
		} else if (texture == R.drawable.ice_spikes_l) {
			textureId = textureManager.getSpikeIceTextureLeft();
		} else if (texture == R.drawable.ice_spikes_r) {
			textureId = textureManager.getSpikeIceTextureRight();
		}
		return textureId;
	}

	public int getDamageCooldown() {
		return damageCooldown;
	}

	public void setDamageCooldown() {
		this.damageCooldown = DAMAGE_COOLDOWN_TICKS;
	}
	public void stepDamageCooldown() {
		if(damageCooldown > 0) damageCooldown--;
	}

	/**
	 * Sets whether this spike is lethal.
	 */
	public void setLethal(boolean lethal) {
		this.lethal = lethal;
	}

	/**
	 * Checks if this spike is lethal.
	 */
	public boolean isLethal() {
		return lethal;
	}
	
	/**
	 * Resets the spike to its initial state.
	 * Used for game restart functionality.
	 */
	public void resetToInitialState() {
		damageCooldown = 0;
	}
}
