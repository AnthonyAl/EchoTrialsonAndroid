package com.unipi.alexandris.android.echotrialsonandroid.utility;

import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.AlphaAnimation;

import androidx.annotation.NonNull;

public class DialogAnimationHelper {

    private static final int ANIMATION_DURATION = 400; // 400ms for smooth effect

    public static void addSlideInAnimation(Dialog dialog) {
        if (dialog == null) return;
        
        Window window = dialog.getWindow();
        if (window == null) return;
        
        View decorView = window.getDecorView();

        // Create slide-in animation from top
        AnimationSet animationSet = getAnimationSetDown();

        // Configure animation properties
        animationSet.setDuration(ANIMATION_DURATION);
        animationSet.setInterpolator(new DecelerateInterpolator(2.0f)); // Slow ease-in
        animationSet.setFillAfter(true);
        
        // Apply animation to dialog
        decorView.startAnimation(animationSet);
    }

    @NonNull
    private static AnimationSet getAnimationSetDown() {
        TranslateAnimation slideIn = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,   // fromXDelta: no horizontal movement
            Animation.RELATIVE_TO_SELF, 0.0f,   // toXDelta: no horizontal movement
            Animation.RELATIVE_TO_SELF, -1.0f,  // fromYDelta: start from above screen
            Animation.RELATIVE_TO_SELF, 0.0f    // toYDelta: end at normal position
        );

        // Create fade-in animation
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);

        // Combine animations
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(slideIn);
        animationSet.addAnimation(fadeIn);
        return animationSet;
    }

    public static void addSlideInAnimation(View view) {
        if (view == null) return;
        
        // Create slide-in animation from top
        AnimationSet animationSet = getAnimationSetDown();

        // Configure animation properties
        animationSet.setDuration(ANIMATION_DURATION);
        animationSet.setInterpolator(new DecelerateInterpolator(2.0f)); // Slow ease-in
        animationSet.setFillAfter(true);
        
        // Apply animation to view
        view.startAnimation(animationSet);
    }

    public static Animation createSlideOutAnimation(Runnable onAnimationEnd) {
        // Create slide-out animation to top
        AnimationSet animationSet = getAnimationSetUp();

        // Configure animation properties (faster for dismissal)
        animationSet.setDuration(ANIMATION_DURATION / 2); // 200ms for quick dismissal
        animationSet.setInterpolator(new DecelerateInterpolator(1.0f)); // Quick acceleration
        animationSet.setFillAfter(true);
        
        // Set animation listener if callback provided
        if (onAnimationEnd != null) {
            animationSet.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                
                @Override
                public void onAnimationEnd(Animation animation) {
                    onAnimationEnd.run();
                }
                
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }
        
        return animationSet;
    }

    @NonNull
    private static AnimationSet getAnimationSetUp() {
        TranslateAnimation slideOut = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,   // fromXDelta: no horizontal movement
            Animation.RELATIVE_TO_SELF, 0.0f,   // toXDelta: no horizontal movement
            Animation.RELATIVE_TO_SELF, 0.0f,   // fromYDelta: start at normal position
            Animation.RELATIVE_TO_SELF, -1.0f   // toYDelta: end above screen
        );

        // Create fade-out animation
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);

        // Combine animations
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(slideOut);
        animationSet.addAnimation(fadeOut);
        return animationSet;
    }

    public static void addSlideInAnimationWithDelay(Dialog dialog, long delayMs) {
        if (dialog == null) return;
        
        Window window = dialog.getWindow();
        if (window == null) return;
        
        View decorView = window.getDecorView();

        // Initially hide the dialog
        decorView.setAlpha(0.0f);
        decorView.setTranslationY(-decorView.getHeight());
        
        // Animate after delay
        decorView.postDelayed(() -> addSlideInAnimation(dialog), delayMs);
    }

    public static void addSlideOutAnimationAndDismiss(Dialog dialog) {
        if (dialog == null) return;
        
        Window window = dialog.getWindow();
        if (window == null) {
            dialog.dismiss();
            return;
        }
        
        View decorView = window.getDecorView();

        // Create slide-out animation
        Animation slideOutAnimation = createSlideOutAnimation(dialog::dismiss);
        decorView.startAnimation(slideOutAnimation);
    }

    public static void addSlideOutAnimationAndHide(View view) {
        if (view == null) return;
        
        // Create slide-out animation
        Animation slideOutAnimation = createSlideOutAnimation(() -> view.setVisibility(View.GONE));
        view.startAnimation(slideOutAnimation);
    }

    public static void addSlideOutAnimationWithCallback(View view, Runnable onComplete) {
        if (view == null) return;
        
        // Create slide-out animation with custom callback
        Animation slideOutAnimation = createSlideOutAnimation(onComplete);
        view.startAnimation(slideOutAnimation);
    }
}
