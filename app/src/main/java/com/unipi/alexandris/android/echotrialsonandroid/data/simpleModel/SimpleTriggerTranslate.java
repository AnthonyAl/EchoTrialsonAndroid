package com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel;

@SuppressWarnings(value = "unused")
public class SimpleTriggerTranslate extends SimpleTrigger {

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private Direction direction = Direction.RIGHT;
    private int blockCount = 1;

    public SimpleTriggerTranslate(int x, int y, int groupId) {
        super(x, y, SimpleID.TriggerTranslate, groupId);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getBlockCount() {
        return blockCount;
    }

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

    // Compatibility setters for XML loading (convert target position to direction/blocks)
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
