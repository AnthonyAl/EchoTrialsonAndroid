package com.unipi.alexandris.android.echotrialsonandroid.utility;

/**
 * A task scheduler that executes a Runnable on a specified game onTick schedule.
 * Supports delayed tasks, periodic tasks, and limited repetition.
 */
public class TickRunnable implements Runnable {
    private final Runnable task;
    private final int period;
    private final int iterations;
    private int clock;
    private int cycles = 0;
    private boolean active = true;
    private String taskName;

    public TickRunnable(Runnable task, int delay, int period, int iterations) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        
        this.task = task;
        this.period = Math.max(0, period); // Ensure non-negative period
        this.iterations = iterations;
        this.clock = -Math.max(0, delay); // Ensure non-negative delay
        this.taskName = task.getClass().getSimpleName();
    }

    public TickRunnable(Runnable task, int delay, int period, int iterations, String taskName) {
        this(task, delay, period, iterations);
        this.taskName = taskName;
    }

    @Override
    public void run() {
        if (!active) return;
        
        clock++;
        
        // For one-time tasks with no period
        if (period == 0 && clock >= 0 && cycles == 0) {
            executeTask();
            cycles++;
            deactivateIfComplete();
            return;
        }
        
        // For periodic tasks
        if (clock >= 0 && period > 0 && clock % period == 0) {
            executeTask();
            cycles++;
            deactivateIfComplete();
        }
    }

    private void executeTask() {
        try {
            task.run();
        } catch (Exception e) {
            // Handle task error silently - will retry next cycle
        }
    }

    private void deactivateIfComplete() {
            if (iterations > 0 && cycles >= iterations) {
                active = false;
        }
    }

    public boolean isActive() {
        return active;
    }

    public void cancel() {
        this.active = false;
    }

    public int getExecutionCount() {
        return cycles;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}
