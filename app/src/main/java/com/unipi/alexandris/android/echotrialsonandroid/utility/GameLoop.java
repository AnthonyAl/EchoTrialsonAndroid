package com.unipi.alexandris.android.echotrialsonandroid.utility;

public class GameLoop extends Thread {
    private final int targetFPS;
    private final Runnable tick;
    private volatile boolean running = true;
    private volatile boolean stopped = false;
    private int actualFPS = 0;

    public GameLoop(int fps, Runnable tickMethod) {
        this.targetFPS = fps;
        this.tick = tickMethod;
    }

    @Override
    public void run() {
        final double nsPerTick = 1_000_000_000.0 / targetFPS;
        long lastTime = System.nanoTime();
        double delta = 0;

        long fpsTimer = System.currentTimeMillis();
        int frames = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;

            while (delta >= 1 && running) {
                try {
                    tick.run(); // call your Activity.tick()
                } catch (Exception e) {
                    // Prevent tick exceptions from crashing the loop
                    System.err.println("GameLoop tick error: " + e.getMessage());
                }
                delta--;
                frames++;
            }

            // crude FPS counter
            if (System.currentTimeMillis() - fpsTimer >= 1000) {
                actualFPS = frames;
                frames = 0;
                fpsTimer += 1000;
            }

            try {
                Thread.sleep(1); // yield CPU
            } catch (InterruptedException e) {
                running = false;
                break;
            }
        }
        
        // Mark as stopped
        stopped = true;
    }

    public void stopLoop() {
        if (!running) return; // Already stopped
        
        running = false;
        this.interrupt();
        
        // Wait for the thread to actually stop (with timeout)
        try {
            this.join(1000); // Wait up to 1 second for thread to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Force interrupt if still alive
        if (this.isAlive()) {
            System.err.println("GameLoop thread did not stop gracefully, forcing interrupt");
            this.interrupt();
        }
    }

    public int getActualFPS() {
        return actualFPS;
    }

    public boolean isStopped() {
        return stopped;
    }

    public boolean isRunning() {
        return running && !stopped;
    }
}
