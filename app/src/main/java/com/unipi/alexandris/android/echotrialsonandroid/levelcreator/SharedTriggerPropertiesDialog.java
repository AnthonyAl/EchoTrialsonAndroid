package com.unipi.alexandris.android.echotrialsonandroid.levelcreator;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.ButtonSoundHelper;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimplePhysicsObject;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleTrigger;

import java.util.List;
import java.util.Objects;

/**
 * Dialog for editing shared trigger properties using button controls.
 * Uses dialog_shared_trigger_properties.xml layout with up/down buttons for all values.
 */
public class SharedTriggerPropertiesDialog extends Dialog {
    
    private final LevelCreatorActivity activity;
    private final List<GridObject> gridObjects;
    
    // UI Elements
    private TextView delayText, speedText, groupIdText;
    private TextView leftText, rightText, topText, bottomText;
    private ImageButton delayUpBtn, delayDownBtn;
    private ImageButton speedUpBtn, speedDownBtn;
    private ImageButton groupUpBtn, groupDownBtn;
    private ImageButton leftUpBtn, leftDownBtn;
    private ImageButton rightUpBtn, rightDownBtn;
    private ImageButton topUpBtn, topDownBtn;
    private ImageButton bottomUpBtn, bottomDownBtn;
    
    // Button repeat functionality
    private final ButtonRepeatHelper buttonRepeatHelper = new ButtonRepeatHelper();
    
    // Value constraints
    private static final int INTEGER_INCREMENT = 1;
    private static final int DELAY_MAX = 100000;
    private static final int SPEED_MAX = 100000;
    private static final int BOUNDS_MAX = 100;
    
    public SharedTriggerPropertiesDialog(LevelCreatorActivity activity, List<GridObject> gridObjects) {
        super(activity);
        this.activity = activity;
        this.gridObjects = gridObjects;
        
        setContentView(R.layout.dialog_shared_trigger_properties);
        
        // Set dialog size to 80% of screen, but respect max dimensions
        WindowManager.LayoutParams params = Objects.requireNonNull(getWindow()).getAttributes();
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        int targetWidth = (int) (screenWidth * 0.8);
        int targetHeight = (int) (screenHeight * 0.8);
        int maxWidth = (int) (activity.getResources().getDisplayMetrics().density * 600); // 600dp
        int maxHeight = (int) (activity.getResources().getDisplayMetrics().density * 800); // 800dp
        params.width = Math.min(targetWidth, maxWidth);
        params.height = Math.min(targetHeight, maxHeight);
        getWindow().setAttributes(params);
        
        // Save current state to undo stack before making any changes
        activity.saveCanvasStateForced();

        initializeViews();
        loadCurrentValues();
        setupButtonListeners();
    }
    
    private void initializeViews() {
        // Text views
        delayText = findViewById(R.id.delayText);
        speedText = findViewById(R.id.speedText);
        groupIdText = findViewById(R.id.groupIdText);
        leftText = findViewById(R.id.leftText);
        rightText = findViewById(R.id.rightText);
        topText = findViewById(R.id.topText);
        bottomText = findViewById(R.id.bottomText);
        
        // Buttons
        delayUpBtn = findViewById(R.id.delayUpButton);
        delayDownBtn = findViewById(R.id.delayDownButton);
        speedUpBtn = findViewById(R.id.speedUpButton);
        speedDownBtn = findViewById(R.id.speedDownButton);
        groupUpBtn = findViewById(R.id.groupUpButton);
        groupDownBtn = findViewById(R.id.groupDownButton);
        leftUpBtn = findViewById(R.id.leftUpButton);
        leftDownBtn = findViewById(R.id.leftDownButton);
        rightUpBtn = findViewById(R.id.rightUpButton);
        rightDownBtn = findViewById(R.id.rightDownButton);
        topUpBtn = findViewById(R.id.topUpButton);
        topDownBtn = findViewById(R.id.topDownButton);
        bottomUpBtn = findViewById(R.id.bottomUpButton);
        bottomDownBtn = findViewById(R.id.bottomDownButton);
    }
    
    private void loadCurrentValues() {
        if (gridObjects.isEmpty()) return;
        
        // Check if all triggers have the same values for each property
        delayText.setText(getSharedValueOrDash(gridObjects, SimpleTrigger::getDelay));
        speedText.setText(getSharedValueOrDash(gridObjects, SimpleTrigger::getSpeed));
        groupIdText.setText(getSharedValueOrDash(gridObjects, SimplePhysicsObject::getGroupId));
        leftText.setText(getSharedValueOrDash(gridObjects, SimpleTrigger::getWR1));
        rightText.setText(getSharedValueOrDash(gridObjects, SimpleTrigger::getWR2));
        topText.setText(getSharedValueOrDash(gridObjects, SimpleTrigger::getHR1));
        bottomText.setText(getSharedValueOrDash(gridObjects, SimpleTrigger::getHR2));
    }

    private String getSharedValueOrDash(List<GridObject> gridObjects, java.util.function.Function<SimpleTrigger, Integer> valueExtractor) {
        if (gridObjects.isEmpty()) return "-";
        
        SimpleTrigger firstTrigger = (SimpleTrigger) gridObjects.get(0).getSimpleObject();
        int firstValue = valueExtractor.apply(firstTrigger);
        
        // Check if all triggers have the same value
        boolean allSameValue = gridObjects.stream()
            .allMatch(gridObject -> {
                SimpleTrigger trigger = (SimpleTrigger) gridObject.getSimpleObject();
                return valueExtractor.apply(trigger) == firstValue;
            });
        
        if (allSameValue) {
            return String.valueOf(firstValue);
        } else {
            return "-";
        }
    }

    private int getCurrentValueOrMin(List<GridObject> gridObjects, java.util.function.Function<SimpleTrigger, Integer> valueExtractor, int minValue) {
        if (gridObjects.isEmpty()) return minValue;
        
        SimpleTrigger firstTrigger = (SimpleTrigger) gridObjects.get(0).getSimpleObject();
        int firstValue = valueExtractor.apply(firstTrigger);
        
        // Check if all triggers have the same value
        boolean allSameValue = gridObjects.stream()
            .allMatch(gridObject -> {
                SimpleTrigger trigger = (SimpleTrigger) gridObject.getSimpleObject();
                return valueExtractor.apply(trigger) == firstValue;
            });
        
        if (allSameValue) {
            return firstValue;
        } else {
            return minValue;
        }
    }
    
    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonListeners() {
        // Delay controls
        setupButtonWithRepeat(delayUpBtn, () -> adjustDelay(INTEGER_INCREMENT), () -> adjustDelay(INTEGER_INCREMENT));
        setupButtonWithRepeat(delayDownBtn, () -> adjustDelay(-INTEGER_INCREMENT), () -> adjustDelay(-INTEGER_INCREMENT));
        
        // Speed controls
        setupButtonWithRepeat(speedUpBtn, () -> adjustSpeed(INTEGER_INCREMENT), () -> adjustSpeed(INTEGER_INCREMENT));
        setupButtonWithRepeat(speedDownBtn, () -> adjustSpeed(-INTEGER_INCREMENT), () -> adjustSpeed(-INTEGER_INCREMENT));
        
        // Group ID controls
        setupButtonWithRepeat(groupUpBtn, () -> adjustGroupId(1), () -> adjustGroupId(1));
        setupButtonWithRepeat(groupDownBtn, () -> adjustGroupId(-1), () -> adjustGroupId(-1));
        
        // Bounds controls
        setupButtonWithRepeat(leftUpBtn, () -> adjustLeftBound(INTEGER_INCREMENT), () -> adjustLeftBound(INTEGER_INCREMENT));
        setupButtonWithRepeat(leftDownBtn, () -> adjustLeftBound(-INTEGER_INCREMENT), () -> adjustLeftBound(-INTEGER_INCREMENT));
        
        setupButtonWithRepeat(rightUpBtn, () -> adjustRightBound(INTEGER_INCREMENT), () -> adjustRightBound(INTEGER_INCREMENT));
        setupButtonWithRepeat(rightDownBtn, () -> adjustRightBound(-INTEGER_INCREMENT), () -> adjustRightBound(-INTEGER_INCREMENT));
        
        setupButtonWithRepeat(topUpBtn, () -> adjustTopBound(INTEGER_INCREMENT), () -> adjustTopBound(INTEGER_INCREMENT));
        setupButtonWithRepeat(topDownBtn, () -> adjustTopBound(-INTEGER_INCREMENT), () -> adjustTopBound(-INTEGER_INCREMENT));
        
        setupButtonWithRepeat(bottomUpBtn, () -> adjustBottomBound(INTEGER_INCREMENT), () -> adjustBottomBound(INTEGER_INCREMENT));
        setupButtonWithRepeat(bottomDownBtn, () -> adjustBottomBound(-INTEGER_INCREMENT), () -> adjustBottomBound(-INTEGER_INCREMENT));
        
        // Confirm button
        ImageButton confirmButton = findViewById(R.id.confirmButton);
        if (confirmButton != null) {
            confirmButton.setOnTouchListener(LevelCreatorActivity.createButtonTapFeedback());
            ButtonSoundHelper.addClickSound(confirmButton, v -> {
                saveStateAndRefresh();
                dismiss();
            });
        }
    }
    
    private void setupButtonWithRepeat(ImageButton button, Runnable singleClick, Runnable repeatAction) {
        buttonRepeatHelper.setupButtonWithRepeat(button, singleClick, repeatAction);
    }
    
    private void adjustDelay(int increment) {
        if (gridObjects.isEmpty()) return;
        
        // Get current value, or start from 0 if values differ
        int currentDelay = getCurrentValueOrMin(gridObjects, SimpleTrigger::getDelay, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newDelay = Math.max(0, Math.min(DELAY_MAX, currentDelay + actualIncrement));
        
        for (GridObject gridObject : gridObjects) {
            SimpleTrigger trigger = (SimpleTrigger) gridObject.getSimpleObject();
            trigger.setDelay(newDelay);
        }
        
        delayText.setText(String.valueOf(newDelay));
        saveStateAndRefresh();
    }
    
    private void adjustSpeed(int increment) {
        if (gridObjects.isEmpty()) return;
        
        // Get current value, or start from 1 if values differ
        int currentSpeed = getCurrentValueOrMin(gridObjects, SimpleTrigger::getSpeed, 1);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newSpeed = Math.max(1, Math.min(SPEED_MAX, currentSpeed + actualIncrement));
        
        for (GridObject gridObject : gridObjects) {
            SimpleTrigger trigger = (SimpleTrigger) gridObject.getSimpleObject();
            trigger.setSpeed(newSpeed);
        }
        
        speedText.setText(String.valueOf(newSpeed));
        saveStateAndRefresh();
    }
    
    private void adjustGroupId(int increment) {
        if (gridObjects.isEmpty()) return;
        
        // Get current value, or start from 0 if values differ
        int currentGroupId = getCurrentValueOrMin(gridObjects, SimplePhysicsObject::getGroupId, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newGroupId = Math.max(0, Math.min(1000, currentGroupId + actualIncrement));
        
        // Update all triggers and their corresponding grid objects
        for (GridObject gridObject : gridObjects) {
            SimpleTrigger trigger = (SimpleTrigger) gridObject.getSimpleObject();
            int oldGroupId = trigger.getGroupId();
            trigger.setGroupId(newGroupId);
            
            // Update the GridObject's group ID
            gridObject.setGroupId(newGroupId);
            
            // Update the grid cell mapping
            if (activity != null && activity.getGridView() != null) {
                LevelGridView gridView = activity.getGridView();
                int gridX = (int) (trigger.getX() / gridView.getCellSize());
                int gridY = (int) (trigger.getY() / gridView.getCellSize());
                gridView.changeObjectGroup(gridX, gridY, oldGroupId, newGroupId);
            }
        }
        
        groupIdText.setText(String.valueOf(newGroupId));
        saveStateAndRefresh();
    }
    
    private void adjustLeftBound(int increment) {
        if (gridObjects.isEmpty()) return;
        
        // Get current value, or start from 0 if values differ
        int currentLeft = getCurrentValueOrMin(gridObjects, SimpleTrigger::getWR1, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newLeft = Math.max(0, Math.min(BOUNDS_MAX, currentLeft + actualIncrement));
        
        for (GridObject gridObject : gridObjects) {
            SimpleTrigger trigger = (SimpleTrigger) gridObject.getSimpleObject();
            trigger.setWR1(newLeft);
        }
        
        leftText.setText(String.valueOf(newLeft));
        saveStateAndRefresh();
    }
    
    private void adjustRightBound(int increment) {
        if (gridObjects.isEmpty()) return;
        
        // Get current value, or start from 0 if values differ
        int currentRight = getCurrentValueOrMin(gridObjects, SimpleTrigger::getWR2, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newRight = Math.max(0, Math.min(BOUNDS_MAX, currentRight + actualIncrement));
        
        for (GridObject gridObject : gridObjects) {
            SimpleTrigger trigger = (SimpleTrigger) gridObject.getSimpleObject();
            trigger.setWR2(newRight);
        }
        
        rightText.setText(String.valueOf(newRight));
        saveStateAndRefresh();
    }
    

    private void adjustTopBound(int increment) {
        if (gridObjects.isEmpty()) return;
        
        // Get current value, or start from 0 if values differ
        int currentTop = getCurrentValueOrMin(gridObjects, SimpleTrigger::getHR1, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newTop = Math.max(0, Math.min(BOUNDS_MAX, currentTop + actualIncrement));
        
        for (GridObject gridObject : gridObjects) {
            SimpleTrigger trigger = (SimpleTrigger) gridObject.getSimpleObject();
            trigger.setHR1(newTop);
        }
        
        topText.setText(String.valueOf(newTop));
        saveStateAndRefresh();
    }
    
    private void adjustBottomBound(int increment) {
        if (gridObjects.isEmpty()) return;
        
        // Get current value, or start from 0 if values differ
        int currentBottom = getCurrentValueOrMin(gridObjects, SimpleTrigger::getHR2, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newBottom = Math.max(0, Math.min(BOUNDS_MAX, currentBottom + actualIncrement));
        
        for (GridObject gridObject : gridObjects) {
            SimpleTrigger trigger = (SimpleTrigger) gridObject.getSimpleObject();
            trigger.setHR2(newBottom);
        }
        
        bottomText.setText(String.valueOf(newBottom));
        saveStateAndRefresh();
    }
    
    private void saveStateAndRefresh() {
        // Don't save to undo stack on every change - only on dismiss
        // Just refresh the UI
        if (activity != null && activity.getGridView() != null) {
            activity.getGridView().invalidate();
        }
    }
    private int getAcceleratedIncrement(int baseIncrement) {
        return buttonRepeatHelper.getAcceleratedIncrement(baseIncrement);
    }
    
    @Override
    public void show() {
        super.show();

        Window window = getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.DialogSlideAnimation);
        }
    }
    
    @Override
    public void dismiss() {
        super.dismiss();
    }
}
