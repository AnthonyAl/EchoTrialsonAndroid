package com.unipi.alexandris.android.echotrialsonandroid.utility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.ButtonSoundHelper;

public class UniversalDialogAnimator {
    
    private static final String TAG = "UniversalDialogAnimator";

    public interface DialogContentProvider {
        View createDialogContent(Context context);

        void onDialogDismissed();
    }

    public static class AnimatedDialog {
        private final Activity activity;
        private final DialogContentProvider contentProvider;
        private ViewGroup rootView;
        private FrameLayout overlayContainer;
        private FrameLayout dialogContainer;
        private View dialogContent;
        private boolean isShowing = false;
        private boolean isAnimating = false;
        
        public AnimatedDialog(Activity activity, DialogContentProvider contentProvider) {
            this.activity = activity;
            this.contentProvider = contentProvider;
        }

        public void show() {
            if (isShowing || isAnimating) return;
            
            // Get the activity's root view
            rootView = activity.findViewById(android.R.id.content);
            if (rootView == null) return;
            
            // Create overlay container (full screen with semi-transparent background)
            overlayContainer = new FrameLayout(activity);
            overlayContainer.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ));
            overlayContainer.setBackgroundColor(Color.parseColor("#CC000000")); // Semi-transparent black
            overlayContainer.setClickable(true); // Prevent clicks from passing through
            
            // Create dialog container (centered with size constraints)
            dialogContainer = new FrameLayout(activity);
            
            // Calculate proper dialog sizing (similar to original AlertDialog sizing)
            android.util.DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
            float density = activity.getResources().getDisplayMetrics().density;
            
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;
            
            int targetWidth = (int) (screenWidth * 0.85); // 85% of screen width
            int targetHeight = (int) (screenHeight * 0.65); // 65% of screen height
            
            // Apply max constraints
            int maxWidth = (int) (density * 600); // 600dp max width
            int maxHeight = (int) (density * 400); // 400dp max height
            
            int finalWidth = Math.min(targetWidth, maxWidth);
            int finalHeight = Math.min(targetHeight, maxHeight);
            
            FrameLayout.LayoutParams dialogParams = new FrameLayout.LayoutParams(finalWidth, finalHeight);
            dialogParams.gravity = Gravity.CENTER;
            dialogContainer.setLayoutParams(dialogParams);
            
            // Create dialog content from provider
            dialogContent = contentProvider.createDialogContent(activity);
            if (dialogContent != null) {
                dialogContainer.addView(dialogContent);
            }
            
            // Add dialog container to overlay
            overlayContainer.addView(dialogContainer);
            
            // Add overlay to root view
            rootView.addView(overlayContainer);
            
            // Initially hide for animation
            overlayContainer.setVisibility(View.INVISIBLE);
            
            // Start slide-in animation
            isShowing = true;
            isAnimating = true;
            
            // Post to ensure layout is complete before animating
            overlayContainer.post(() -> {
                overlayContainer.setVisibility(View.VISIBLE);
                DialogAnimationHelper.addSlideInAnimation(dialogContainer);
                
                // Animation completed
                dialogContainer.postDelayed(() -> {
                    isAnimating = false;
                }, 400); // Match animation duration
            });
        }

        public void dismiss() {
            if (!isShowing || isAnimating) return;
            
            isAnimating = true;
            
            // Start slide-out animation with custom callback
            DialogAnimationHelper.addSlideOutAnimationWithCallback(dialogContainer, () -> {
                // Animation completed - now clean up
                if (overlayContainer != null && rootView != null) {
                    rootView.removeView(overlayContainer);
                }
                
                // Notify content provider
                contentProvider.onDialogDismissed();
                
                // Reset state
                isShowing = false;
                isAnimating = false;
                overlayContainer = null;
                dialogContainer = null;
                dialogContent = null;
            });
        }

        public boolean isShowing() {
            return isShowing;
        }
    }

    @SuppressLint("DiscouragedApi")
    public static AnimatedDialog showConfirmationDialog(Activity activity, String title, String message, 
            Runnable onYes, Runnable onNo) {
        
        AnimatedDialog[] dialogRef = new AnimatedDialog[1]; // Reference holder for button callbacks
        
        AnimatedDialog dialog = createFromLayout(activity,
            activity.getResources().getIdentifier("notice_dialog_confirmation", "layout", activity.getPackageName()),
            dialogView -> {
                // Set up the confirmation dialog content
                android.widget.TextView titleText = dialogView.findViewById(
                    activity.getResources().getIdentifier("levelNameText", "id", activity.getPackageName())
                );
                android.widget.TextView detailsText = dialogView.findViewById(
                    activity.getResources().getIdentifier("detailsText", "id", activity.getPackageName())
                );
                android.widget.Button yesButton = dialogView.findViewById(
                    activity.getResources().getIdentifier("yesButton", "id", activity.getPackageName())
                );
                android.widget.Button noButton = dialogView.findViewById(
                    activity.getResources().getIdentifier("noButton", "id", activity.getPackageName())
                );
                
                if (titleText != null) titleText.setText(title);
                if (detailsText != null) {
                    if (message != null && !message.isEmpty()) {
                        detailsText.setText(message);
                        detailsText.setVisibility(View.VISIBLE);
                    } else {
                        detailsText.setVisibility(View.GONE);
                    }
                }
                
                // Set up button click listeners with sounds and dialog dismissal
                if (yesButton != null) {
                    ButtonSoundHelper.addClickSound(yesButton, v -> {
                        if (onYes != null) onYes.run();
                        if (dialogRef[0] != null) dialogRef[0].dismiss();
                    });
                }
                if (noButton != null) {
                    ButtonSoundHelper.addClickSound(noButton, v -> {
                        if (onNo != null) onNo.run();
                        if (dialogRef[0] != null) dialogRef[0].dismiss();
                    });
                }
            },
            () -> {
                // Dialog dismissed
            }
        );
        
        dialogRef[0] = dialog; // Store reference for button callbacks
        return dialog;
    }

    public static AnimatedDialog createFromLayout(Activity activity, int layoutRes, 
            DialogSetupCallback setupCallback, Runnable dismissCallback) {
        return new AnimatedDialog(activity, new DialogContentProvider() {
            @Override
            public View createDialogContent(Context context) {
                View view = activity.getLayoutInflater().inflate(layoutRes, null);
                if (setupCallback != null) {
                    setupCallback.setupDialog(view);
                }
                return view;
            }
            
            @Override
            public void onDialogDismissed() {
                if (dismissCallback != null) {
                    dismissCallback.run();
                }
            }
        });
    }

    public interface DialogSetupCallback {
        void setupDialog(View dialogView);
    }
}
