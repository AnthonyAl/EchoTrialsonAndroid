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
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleTriggerFade;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TriggerFadePropertiesDialog extends Dialog {
    
    private final List<SimpleTriggerFade> triggers;
    private final List<GridObject> gridObjects;
    private final LevelCreatorActivity activity;
    
    // UI Elements
    private TextView delayText, speedText, groupIdText;
    private TextView leftText, rightText, topText, bottomText;
    private TextView opacityText, collisionText;
    private ImageButton delayUpBtn, delayDownBtn;
    private ImageButton speedUpBtn, speedDownBtn;
    private ImageButton groupUpBtn, groupDownBtn;
    private ImageButton leftUpBtn, leftDownBtn;
    private ImageButton rightUpBtn, rightDownBtn;
    private ImageButton topUpBtn, topDownBtn;
    private ImageButton bottomUpBtn, bottomDownBtn;
    private ImageButton opacityUpBtn, opacityDownBtn;
    private ImageButton collisionUpBtn, collisionDownBtn;
    
    // Button repeat functionality
    private final ButtonRepeatHelper buttonRepeatHelper = new ButtonRepeatHelper();
    
    // Value constraints
    private static final int INTEGER_INCREMENT = 1;
    private static final double OPACITY_INCREMENT = 1.0; // 1% increments
    private static final int DELAY_MAX = 100000;
    private static final int SPEED_MAX = 100000;
    private static final int BOUNDS_MAX = 1000;
    private static final double OPACITY_MAX = 100.0;
    private static final double OPACITY_MIN = 0.0;
    
    public TriggerFadePropertiesDialog(LevelCreatorActivity activity, GridObject gridObject) {
        super(activity);
        this.activity = activity;
        this.gridObjects = new ArrayList<>();
        this.gridObjects.add(gridObject);
        this.triggers = new ArrayList<>();
        this.triggers.add((SimpleTriggerFade) gridObject.getSimpleObject());
        
        setContentView(R.layout.dialog_fade_trigger_properties);
        
        // Set dialog size to 80% of screen, but respect max dimensions
        WindowManager.LayoutParams params = Objects.requireNonNull(getWindow()).getAttributes();
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        int targetWidth = (int) (screenWidth * 0.85);
        int targetHeight = (int) (screenHeight * 0.85);
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

    public TriggerFadePropertiesDialog(LevelCreatorActivity activity, List<GridObject> gridObjects) {
        super(activity);
        this.activity = activity;
        this.gridObjects = gridObjects;
        this.triggers = new ArrayList<>();
        for (GridObject gridObject : gridObjects) {
            this.triggers.add((SimpleTriggerFade) gridObject.getSimpleObject());
        }
        

        
        setContentView(R.layout.dialog_fade_trigger_properties);
        
        // Set dialog size to 80% of screen, but respect max dimensions
        WindowManager.LayoutParams params = Objects.requireNonNull(getWindow()).getAttributes();
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        int targetWidth = (int) (screenWidth * 0.85);
        int targetHeight = (int) (screenHeight * 0.85);
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
        opacityText = findViewById(R.id.opacityText);
        collisionText = findViewById(R.id.collisionText);
        
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
        opacityUpBtn = findViewById(R.id.opacityUpButton);
        opacityDownBtn = findViewById(R.id.opacityDownButton);
        collisionUpBtn = findViewById(R.id.collisionUpButton);
        collisionDownBtn = findViewById(R.id.collisionDownButton);
    }
    
    private void loadCurrentValues() {
        if (triggers.isEmpty()) return;
        
        // Check if all triggers have the same values for each property
        delayText.setText(getSharedValueOrDash(triggers, SimpleTrigger::getDelay));
        speedText.setText(getSharedValueOrDash(triggers, SimpleTrigger::getSpeed));
        groupIdText.setText(getSharedValueOrDash(triggers, SimplePhysicsObject::getGroupId));
        leftText.setText(getSharedValueOrDash(triggers, SimpleTrigger::getWR1));
        rightText.setText(getSharedValueOrDash(triggers, SimpleTrigger::getWR2));
        topText.setText(getSharedValueOrDash(triggers, SimpleTrigger::getHR1));
        bottomText.setText(getSharedValueOrDash(triggers, SimpleTrigger::getHR2));
        opacityText.setText(getSharedValueOrDash(triggers, trigger -> (int)(trigger.getTargetOpacity() * 100)));
        collisionText.setText(getSharedValueOrDash(triggers, trigger -> trigger.isRemoveCollisionAtZero() ? "YES" : "NO"));
    }

    private String getSharedValueOrDash(List<SimpleTriggerFade> triggers, java.util.function.Function<SimpleTriggerFade, Object> valueExtractor) {
        if (triggers.isEmpty()) return "-";
        
        SimpleTriggerFade firstTrigger = triggers.get(0);
        Object firstValue = valueExtractor.apply(firstTrigger);
        
        // Check if all triggers have the same value
        boolean allSameValue = triggers.stream()
            .allMatch(trigger -> {
                Object value = valueExtractor.apply(trigger);
                return value.equals(firstValue);
            });
        
        if (allSameValue) {
            return String.valueOf(firstValue);
        } else {
            return "-";
        }
    }

    private int getCurrentValueOrMin(List<SimpleTriggerFade> triggers, java.util.function.Function<SimpleTriggerFade, Integer> valueExtractor, int minValue) {
        if (triggers.isEmpty()) return minValue;
        
        SimpleTriggerFade firstTrigger = triggers.get(0);
        int firstValue = valueExtractor.apply(firstTrigger);
        
        boolean allSameValue = triggers.stream()
            .allMatch(trigger -> valueExtractor.apply(trigger) == firstValue);
        
        if (allSameValue) {
            return firstValue;
        } else {
            return minValue;
        }
    }

    private double getCurrentValueOrMinDouble(List<SimpleTriggerFade> triggers, java.util.function.Function<SimpleTriggerFade, Double> valueExtractor, double minValue) {
        if (triggers.isEmpty()) return minValue;
        
        SimpleTriggerFade firstTrigger = triggers.get(0);
        double firstValue = valueExtractor.apply(firstTrigger);
        
        boolean allSameValue = triggers.stream()
            .allMatch(trigger -> valueExtractor.apply(trigger) == firstValue);
        
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
        
        // Opacity controls
        setupButtonWithRepeat(opacityUpBtn, () -> adjustOpacity(OPACITY_INCREMENT), () -> adjustOpacity(OPACITY_INCREMENT));
        setupButtonWithRepeat(opacityDownBtn, () -> adjustOpacity(-OPACITY_INCREMENT), () -> adjustOpacity(-OPACITY_INCREMENT));
        
        // Collision controls (no acceleration needed for boolean toggle)
        collisionUpBtn.setOnTouchListener(LevelCreatorActivity.createButtonTapFeedback());
        collisionUpBtn.setOnClickListener(v -> toggleCollision());
        collisionDownBtn.setOnClickListener(v -> toggleCollision());
        
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
        if (triggers.isEmpty()) return;
        
        // Get current value, or start from 0 if values differ
        int currentDelay = getCurrentValueOrMin(triggers, SimpleTrigger::getDelay, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newDelay = Math.max(0, Math.min(DELAY_MAX, currentDelay + actualIncrement));
        
        for (SimpleTriggerFade trigger : triggers) {
            trigger.setDelay(newDelay);
        }
        
        delayText.setText(String.valueOf(newDelay));
        saveStateAndRefresh();
    }
    
    private void adjustSpeed(int increment) {
        if (triggers.isEmpty()) return;
        
        // Get current value, or start from 1 if values differ
        int currentSpeed = getCurrentValueOrMin(triggers, SimpleTrigger::getSpeed, 1);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newSpeed = Math.max(1, Math.min(SPEED_MAX, currentSpeed + actualIncrement));
        
        for (SimpleTriggerFade trigger : triggers) {
            trigger.setSpeed(newSpeed);
        }
        
        speedText.setText(String.valueOf(newSpeed));
        saveStateAndRefresh();
    }
    
    private void adjustGroupId(int increment) {
        if (triggers.isEmpty()) return;
        
        // Get current value, or start from 0 if values differ
        int currentGroupId = getCurrentValueOrMin(triggers, SimplePhysicsObject::getGroupId, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newGroupId = Math.max(0, Math.min(1000, currentGroupId + actualIncrement));
        
        // Update all triggers and their corresponding grid objects
        for (int i = 0; i < triggers.size(); i++) {
            SimpleTriggerFade trigger = triggers.get(i);
            GridObject gridObject = gridObjects.get(i);
            int oldGroupId = trigger.getGroupId();
            trigger.setGroupId(newGroupId);
            gridObject.setGroupId(newGroupId); // Update the grid object's group ID
            
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
        if (triggers.isEmpty()) return;
        
        // Get current value, or start from 0 if values differ
        int currentLeft = getCurrentValueOrMin(triggers, SimpleTrigger::getWR1, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newLeft = Math.max(0, Math.min(BOUNDS_MAX, currentLeft + actualIncrement));
        
        for (SimpleTriggerFade trigger : triggers) {
            trigger.setWR1(newLeft);
        }
        
        leftText.setText(String.valueOf(newLeft));
        saveStateAndRefresh();
    }
    
    private void adjustRightBound(int increment) {
        if (triggers.isEmpty()) return;
        
        // Get current value, or start from 0 if values differ
        int currentRight = getCurrentValueOrMin(triggers, SimpleTrigger::getWR2, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newRight = Math.max(0, Math.min(BOUNDS_MAX, currentRight + actualIncrement));
        
        for (SimpleTriggerFade trigger : triggers) {
            trigger.setWR2(newRight);
        }
        
        rightText.setText(String.valueOf(newRight));
        saveStateAndRefresh();
    }
    
    private void adjustTopBound(int increment) {
        if (triggers.isEmpty()) return;
        
        // Get current value, or start from 0 if values differ
        int currentTop = getCurrentValueOrMin(triggers, SimpleTrigger::getHR1, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newTop = Math.max(0, Math.min(BOUNDS_MAX, currentTop + actualIncrement));
        
        for (SimpleTriggerFade trigger : triggers) {
            trigger.setHR1(newTop);
        }
        
        topText.setText(String.valueOf(newTop));
        saveStateAndRefresh();
    }
    
    private void adjustBottomBound(int increment) {
        if (triggers.isEmpty()) return;
        
        // Get current value, or start from 0 if values differ
        int currentBottom = getCurrentValueOrMin(triggers, SimpleTrigger::getHR2, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newBottom = Math.max(0, Math.min(BOUNDS_MAX, currentBottom + actualIncrement));
        
        for (SimpleTriggerFade trigger : triggers) {
            trigger.setHR2(newBottom);
        }
        
        bottomText.setText(String.valueOf(newBottom));
        saveStateAndRefresh();
    }
    
    private void adjustOpacity(double increment) {
        if (triggers.isEmpty()) return;
        
        // Get current value, or start from 0 if values differ
        double currentOpacityPercent = getCurrentValueOrMinDouble(triggers, trigger -> trigger.getTargetOpacity() * 100, 0);
        double actualIncrement = getAcceleratedIncrement(increment);
        double newOpacityPercent = Math.max(OPACITY_MIN, Math.min(OPACITY_MAX, currentOpacityPercent + actualIncrement));
        
        for (SimpleTriggerFade trigger : triggers) {
            trigger.setTargetOpacity(newOpacityPercent / 100.0); // Convert back to 0.0-1.0 range
        }
        
        opacityText.setText(String.valueOf((int)newOpacityPercent));
        saveStateAndRefresh();
    }
    
    private void toggleCollision() {
        if (triggers.isEmpty()) return;
        
        // Get current value, or start from false if values differ
        boolean currentValue = triggers.get(0).isRemoveCollisionAtZero();
        boolean allSameValue = triggers.stream().allMatch(trigger -> trigger.isRemoveCollisionAtZero() == currentValue);
        
        boolean newValue;
        if (allSameValue) {
            newValue = !currentValue;
        } else {
            newValue = false; // If values differ, set to false
        }
        
        for (SimpleTriggerFade trigger : triggers) {
            trigger.setRemoveCollisionAtZero(newValue);
        }
        
        collisionText.setText(newValue ? "YES" : "NO");
        saveStateAndRefresh();
    }

    private int getAcceleratedIncrement(int baseIncrement) {
        return buttonRepeatHelper.getAcceleratedIncrement(baseIncrement);
    }

    private double getAcceleratedIncrement(double baseIncrement) {
        int acceleratedInt = buttonRepeatHelper.getAcceleratedIncrement((int)(baseIncrement * 10));
        return acceleratedInt / 10.0;
    }
    
    private void saveStateAndRefresh() {
        if (activity != null && activity.getGridView() != null) {
            activity.getGridView().invalidate();
        }
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
