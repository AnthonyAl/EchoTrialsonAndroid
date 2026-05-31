package com.unipi.alexandris.android.echotrialsonandroid.model;


import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLSpriteBatch;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLTextureManager;
import java.util.List;
import com.unipi.alexandris.android.echotrialsonandroid.utility.TickRunnable;

/**
 * A trigger zone that activates game events when the player enters its shapeArea.
 * Supports delayed activation, repeated triggering, and customizable detection ranges.
 * Used to create interactive level mechanics and synchronized events.
 */
@SuppressWarnings(value = "unused")
public abstract class Trigger extends PhysicsObject {

	/** Delay in ticks before trigger action activates */
	private final int delay;

	/** Ticks between repeated trigger action activations */
	protected final int speed;

	/** Whether the SimplePlayer is still in range of the SimpleTrigger */
	private boolean engaged = false;

	/** Whether the SimpleTrigger can be activated by the SimplePlayer */
	private boolean active = false;

	/** Left range multiplier for trigger shapeArea */
	protected final int WR1;

	/** Right range multiplier for trigger shapeArea */
	protected final int WR2;

	/** Top range multiplier for trigger shapeArea */
	protected final int HR1;

	/** Bottom range multiplier for trigger shapeArea */
	protected final int HR2;

	private int clock = 0;

	/**
	 * Creates a trigger with custom ranges.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param id Object identifier
	 * @param delay Action Activation delay
	 * @param speed Action Repeat interval
	 * @param WR1 Left range
	 * @param WR2 Right range
	 * @param HR1 Top range
	 * @param HR2 Bottom range
	 */
	public Trigger(int x, int y, ObjectHandler handler, ID id, int groupId, int delay, int speed, int WR1, int WR2, int HR1, int HR2) {
		super(x, y, 48, handler, id, groupId);
		this.delay = delay;
		this.speed = speed;
		this.WR1 = WR1;
		this.WR2 = WR2;
		this.HR1 = HR1;
		this.HR2 = HR2;
	}

	protected abstract void action();

	/**
	 * Checks for player collision and activates trigger.
	 * <br>
	 * This finals the Override to disallow SimpleTrigger derivatives to alter the core behaviour.
	 */
	@Override
	protected final void onTick() {
		
		// Always check for collision first
		RectF triggerArea = getArea(WR1, HR1, WR2, HR2);
		RectF playerBounds = handler.getPlayer().getBoundsF();

		boolean hasCollision = RectF.intersects(triggerArea, playerBounds);

		if (hasCollision) {
			if(!engaged) {
				activate();
			}
		}

		// Only execute action if trigger is active and delay has passed
		if(!active) return;
		if(clock++ < delay) return;

		action();
	}

	protected final void activate() {
		active = true;
		engaged = true;
	}

	protected final void deactivate() {
		active = false;
	}

	/**
	 * OpenGL rendering method - renders debug visualization for triggers.
	 * Triggers are invisible in normal gameplay, only shown in debug mode.
	 * Shows trigger bounds outline in trigger-specific colors.
	 */
	@Override
	public void renderGL(GLSpriteBatch spriteBatch, Camera camera) {
		// Triggers are invisible in normal gameplay - only render in debug mode
		if (!isVisible() || spriteBatch == null || !GameConstants.INSTANCE.isDEBUG_MODE()) return;
		
		// Get texture manager
		GLTextureManager textureManager =
			spriteBatch.getTextureManager();
		if (textureManager == null) return;
		
		// Calculate trigger bounds in world coordinates
		float triggerX = (float)x;
		float triggerY = (float)y;
		float triggerSize = (float)size;
		
		// Calculate expanded bounds based on WR1/WR2/HR1/HR2
		float boundsLeft = triggerX - (WR1 * triggerSize);
		float boundsRight = triggerX + triggerSize + (WR2 * triggerSize);
		float boundsTop = triggerY - (HR1 * triggerSize);
		float boundsBottom = triggerY + triggerSize + (HR2 * triggerSize);
		
		// Draw trigger bounds outline using trigger-specific color
		int whiteTexture = textureManager.getWhiteTexture();
		if (whiteTexture != -1) {
			
			// Draw outline as a rectangle with stroke effect
			float stroke = 2.0f;
			
			// Draw four lines to create outline effect
			// Top line
			spriteBatch.draw(whiteTexture, boundsLeft, boundsTop, boundsRight - boundsLeft, stroke);
			// Bottom line
			spriteBatch.draw(whiteTexture, boundsLeft, boundsBottom - stroke, boundsRight - boundsLeft, stroke);
			// Left line
			spriteBatch.draw(whiteTexture, boundsLeft, boundsTop, stroke, boundsBottom - boundsTop);
			// Right line
			spriteBatch.draw(whiteTexture, boundsRight - stroke, boundsTop, stroke, boundsBottom - boundsTop);
		}
	}

	/**
	 * Gets trigger's basic collision bounds.
	 * @return Rectangle representing trigger zone
	 */
	@Override
	public final Rect getBounds() {
		return new Rect((int) x, (int) y, (int) x + size, (int) y + size);
	}

	@Override
	public final Region getShapeArea() {
		return new Region(getBounds());
	}
	
	/**
	 * Gets expanded collision bounds.
	 * @param a Expansion multiplier
	 * @return Expanded rectangle bounds
	 */
	public final Rect getBounds(int a) {
		a = a * size;
		return new Rect((int) x - a, (int) y - a, (int) x + size + a*2, (int)y + size + a*2);
	}

	/**
	 * Gets trigger's precise collision shapeArea.
	 * @return Elliptical shapeArea for basic detection
	 */
	public final RectF getBoundsF() {
		return new RectF((int) x, (int) y, (int) x + size, (int) y + size);
	}

	/**
	 * Gets trigger's detection shapeArea with custom ranges.
	 * @param w1 Left range
	 * @param h1 Top range
	 * @param w2 Right range
	 * @param h2 Bottom range
	 * @return Elliptical shapeArea with specified ranges
	 */
	public final RectF getArea(int w1, int h1, int w2, int h2) {
		w1 = w1 * size;
		h1 = h1 * size;
		w2 = w2 * size;
		h2 = h2 * size;
		return new RectF((int) x - w1, (int) y - h1, (int) x + size + w2, (int) y + size + h2);
	}

	/**
	 * Gets trigger activation delay.
	 * @return Delay in ticks
	 */
	public final int getDelay() {
		return delay;
	}

	/**
	 * Gets trigger repeat interval.
	 * @return Speed in ticks
	 */
	public final int getSpeed() {
		return speed;
	}



	public final boolean isActive() {
		return active;
	}
	
	/**
	 * Gets whether the trigger is currently engaged.
	 * @return true if engaged, false otherwise
	 */
	public final boolean isEngaged() {
		return engaged;
	}
	
	/**
	 * Gets the trigger's internal clock value.
	 * @return Current clock value
	 */
	public final int getClock() {
		return clock;
	}
	
	/**
	 * Resets the trigger to its initial state.
	 * Used for game restart functionality.
	 */
	public final void resetToInitialState() {
		engaged = false;
		active = false;
		clock = 0;
		
		// Clear any ongoing movement or animations for grouped objects
		clearGroupedObjectAnimations();
	}
	
	    /**
     * Clears any ongoing animations or movement for objects in this trigger's group.
     * This ensures that when the trigger is reset, any ongoing movement is stopped.
     */
    private void clearGroupedObjectAnimations() {
        List<PhysicsObject> targets = getTargetObjects();
        
        for (PhysicsObject target : targets) {
            // Clear any ongoing movement or animations
            if (target instanceof Block) {
                Block block = (Block) target;
                block.setCancelUP(true);
                block.setCancelDOWN(true);
                block.setCancelLEFT(true);
                block.setCancelRIGHT(true);
                
                // Get the current onTick task and cancel it properly
                TickRunnable currentTask = block.getOnTickTask();
                if (currentTask != null) {
                    currentTask.cancel();
                    // Also remove it from the ObjectHandler's scheduled tasks
                    if (handler != null) {
                        handler.removeTask(currentTask);
                    }
                }
                
                block.clearOnTick();
            }
            
            // Reset velocities to stop any ongoing movement
            target.setVelX(0);
            target.setVelY(0);
            target.setVelW(0);
            target.setVelH(0);
        }
    }
	
	/**
	 * Gets all objects in the target group for this trigger.
	 * Uses the ObjectHandler to efficiently find objects by group SimpleID.
	 * 
	 * @return List of objects in the target group
	 */
	protected java.util.List<PhysicsObject> getTargetObjects() {
		if (handler == null) {
			return java.util.Collections.emptyList();
		}
		
		java.util.List<PhysicsObject> targets = new java.util.ArrayList<>();
		
		// Get all object types and filter by group SimpleID
        for (PhysicsObject obj : this.getInteractiveObjects()) {
			if (obj.getGroupId() == groupId) {
				targets.add(obj);
			}
		}
		
		return targets;
	}

}
