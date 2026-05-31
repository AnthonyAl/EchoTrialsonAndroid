package com.unipi.alexandris.android.echotrialsonandroid.levelcreator;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class ButtonRepeatHelper {
    
    // Button repeat functionality
    final Handler repeatHandler = new Handler(Looper.getMainLooper());
    final int REPEAT_DELAY = 100; // 100ms for fast repeat
    final int ACCELERATION_DELAY = 2000; // 2 seconds for acceleration
    final int HIGH_ACCELERATION_DELAY = 4000; // 4 seconds for high acceleration
    final int SUPER_ACCELERATION_DELAY = 10000; // 10 seconds for super acceleration
    final int ULTRA_ACCELERATION_DELAY = 20000; // 20 seconds for ultra acceleration
    Runnable repeatRunnable;
    long buttonPressStartTime;
    
    /**
     * Sets up a button with repeat functionality and acceleration.
     * This method matches the existing implementation in dialog classes.
     * 
     * @param button The button to set up
     * @param singleClick Action to perform on single click
     * @param repeatAction Action to perform on repeat (will receive accelerated increment)
     */
    @SuppressLint("ClickableViewAccessibility")
    public void setupButtonWithRepeat(ImageButton button, Runnable singleClick, Runnable repeatAction) {
        if (button == null) return;
        
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Apply scale animation from shared method
                        LevelCreatorActivity.createButtonTapFeedback().onTouch(v, event);
                        
                        buttonPressStartTime = System.currentTimeMillis();
                        singleClick.run();
                        repeatRunnable = new Runnable() {
                            @Override
                            public void run() {
                                repeatAction.run();
                                repeatHandler.postDelayed(this, REPEAT_DELAY);
                            }
                        };
                        repeatHandler.postDelayed(repeatRunnable, 500); // Start repeat after 500ms
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Apply scale animation from shared method
                        LevelCreatorActivity.createButtonTapFeedback().onTouch(v, event);
                        
                        if (repeatRunnable != null) {
                            repeatHandler.removeCallbacks(repeatRunnable);
                            repeatRunnable = null;
                        }
                        return true;
                }
                return false;
            }
        });
    }
    
    /**
     * Gets the accelerated increment based on how long the button has been held.
     * 
     * @param baseIncrement The base increment value
     * @return The accelerated increment value
     */
    public int getAcceleratedIncrement(int baseIncrement) {
        long currentTime = System.currentTimeMillis();
        long holdDuration = currentTime - buttonPressStartTime;

        if (holdDuration >= ULTRA_ACCELERATION_DELAY) {
            // After 20 seconds: 1000x increment
            return baseIncrement * 1000;
        }
        if (holdDuration >= SUPER_ACCELERATION_DELAY) {
            // After 10 seconds: 100x increment
            return baseIncrement * 100;
        } else if (holdDuration >= HIGH_ACCELERATION_DELAY) {
            // After 4 seconds: 10x increment
            return baseIncrement * 10;
        } else if (holdDuration >= ACCELERATION_DELAY) {
            // After 2 seconds: 5x increment
            return baseIncrement * 5;
        } else {
            // Normal increment
            return baseIncrement;
        }
    }
}
