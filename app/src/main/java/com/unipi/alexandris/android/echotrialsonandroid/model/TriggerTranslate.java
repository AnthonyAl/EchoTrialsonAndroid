package com.unipi.alexandris.android.echotrialsonandroid.model;

import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.utility.TickRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * SimpleTrigger that moves objects in a specific direction by a number of blocks.
 * Can affect blocks and potentially the player.
 */
@SuppressWarnings(value = "unused")
public class TriggerTranslate extends Trigger {

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    /** Movement direction */
    private Direction direction = Direction.RIGHT;
    
    /** Number of blocks to move */
    private int blockCount = 1;
    private final List<Double> targetX = new ArrayList<>();
    private final List<Double> targetY = new ArrayList<>();
    private final List<Double> delta = new ArrayList<>();

    public TriggerTranslate(int x, int y, ObjectHandler handler, int delay, int speed, int WR1, int WR2, int HR1, int HR2, int groupId) {
        super(x, y, handler, ID.TriggerTranslate, groupId, delay, speed, WR1, WR2, HR1, HR2);
    }

    @Override
    protected void action() {
        List<PhysicsObject> targets = getTargetObjects();

        int targetId = 0;
        for(PhysicsObject target : targets) {

            if(delta.size() < targets.size()) {

                delta.add(0.0);

                // Calculate total distance to move - use 48 (block size) not trigger size
                final int BLOCK_SIZE = 48;
                double totalDistance = blockCount * BLOCK_SIZE;
                // Calculate movement per frame based on speed (speed = frames to complete movement)
                delta.set(targetId, totalDistance / speed);
                
                switch (direction) {
                    case UP: {
                        delta.set(targetId, delta.get(targetId) * -1); // Negative for upward movement
                        targetX.add(target.getX());
                        targetY.add(target.getY() - (blockCount * BLOCK_SIZE)); // Target Y is above current Y
                        break;
                    }
                    case DOWN: {
                        targetX.add(target.getX());
                        targetY.add(target.getY() + (blockCount * BLOCK_SIZE)); // Target Y is below current Y
                        break;
                    }
                    case LEFT: {
                        delta.set(targetId, delta.get(targetId) * -1);
                        targetX.add(target.getX() - (blockCount * BLOCK_SIZE));
                        targetY.add(target.getY());
                        break;
                    }
                    case RIGHT: {
                        targetX.add(target.getX() + (blockCount * BLOCK_SIZE));
                        targetY.add(target.getY());
                        break;
                    }
                }
            }

            if(direction.equals(Direction.UP) || direction.equals(Direction.DOWN)) {
                // Check if movement is complete
                double currentY = target.getY();
                double targetYValue = targetY.get(targetId);
                double distanceToTarget = Math.abs(currentY - targetYValue);
                
                if (distanceToTarget < 1.0) {
                    target.setY(targetYValue);
                    // Reset block to uniform when movement is complete
                    if (target instanceof Block) {
                        target.setCancelUP(true);
                        target.setCancelDOWN(true);
                        target.setCancelLEFT(true);
                        target.setCancelRIGHT(true);
                        ((Block) target).clearOnTick();
                    }
                    deactivate();
                    return;
                }

                // Set block to non-uniform so it can interact with player during movement
                if (target instanceof Block) {
                    int finalTargetId = targetId;
                    ((Block) target).setOnTick(new TickRunnable(() -> {
                        double currentDelta = delta.get(finalTargetId);
                        target.setVelY(currentDelta);
                        
                        // Update cancel flags based on movement direction
                        if(currentDelta < 0) {
                            target.setCancelUP(false); // Allow upward movement
                            target.setCancelDOWN(true);
                        } else if(currentDelta > 0) {
                            target.setCancelUP(true);
                            target.setCancelDOWN(false); // Allow downward movement
                        }
                    }, 0, 0, -1, "TriggerTranslateBlock"));
                }
                else {
                    target.setY(target.getY() + delta.get(targetId));
                }
            }
            else if(direction.equals(Direction.LEFT) || direction.equals(Direction.RIGHT)) {
                // Check if movement is complete
                double currentX = target.getX();
                double targetXValue = targetX.get(targetId);
                double distanceToTarget = Math.abs(currentX - targetXValue);
                
                if (distanceToTarget < 1.0) {
                    target.setX(targetXValue);
                    // Reset block to uniform when movement is complete
                    if (target instanceof Block) {
                        target.setCancelUP(true);
                        target.setCancelDOWN(true);
                        target.setCancelLEFT(true);
                        target.setCancelRIGHT(true);
                        ((Block) target).clearOnTick();
                    }
                    deactivate();
                    return;
                }

                // Set block to non-uniform so it can interact with player during movement
                if (target instanceof Block) {
                    int finalTargetId1 = targetId;
                    ((Block) target).setOnTick(new TickRunnable(() -> {
                        double currentDelta = delta.get(finalTargetId1);
                        target.setVelX(currentDelta);
                        
                        // Update cancel flags based on movement direction
                        if(currentDelta < 0) {
                            target.setCancelLEFT(false); // Allow leftward movement
                            target.setCancelRIGHT(true);
                        } else if(currentDelta > 0) {
                            target.setCancelLEFT(true);
                            target.setCancelRIGHT(false); // Allow rightward movement
                        }
                    }, 0, 0, -1, "TriggerTranslateBlock"));
                }
                else target.setX(target.getX() + delta.get(targetId));
            }
            targetId++;
        }
    }

    /**
     * Gets the movement direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Sets the movement direction
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * Gets the number of blocks to move
     */
    public int getBlockCount() {
        return blockCount;
    }

    /**
     * Sets the number of blocks to move
     */
    public void setBlockCount(int blockCount) {
        this.blockCount = Math.max(1, blockCount);
    }

    // Compatibility methods for XML loading (calculate target position from direction/blocks)
    public double getTargetX() {
        switch (direction) {
            case LEFT: return -blockCount * 48.0;
            case RIGHT: return blockCount * 48.0;
            default: return 0.0;
        }
    }

    public double getTargetY() {
        switch (direction) {
            case UP: return -blockCount * 48.0;
            case DOWN: return blockCount * 48.0;
            default: return 0.0;
        }
    }

    public void setTargetX(double targetX) {
        if (targetX > 0) {
            direction = Direction.RIGHT;
            blockCount = (int) Math.round(targetX / 48.0);
        } else if (targetX < 0) {
            direction = Direction.LEFT;
            blockCount = (int) Math.round(-targetX / 48.0);
        }
    }

    public void setTargetY(double targetY) {
        if (targetY > 0) {
            direction = Direction.DOWN;
            blockCount = (int) Math.round(targetY / 48.0);
        } else if (targetY < 0) {
            direction = Direction.UP;
            blockCount = (int) Math.round(-targetY / 48.0);
        }
    }
}
