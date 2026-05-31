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
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleTriggerMovement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TriggerMovementPropertiesDialog extends Dialog {
    
    private final List<SimpleTriggerMovement> triggers;
    private final List<GridObject> gridObjects;
    private final LevelCreatorActivity activity;
    
    // UI Elements
    private TextView delayText, speedText, groupIdText;
    private TextView leftText, rightText, topText, bottomText;
    private TextView velXText, velYText;
    private ImageButton delayUpBtn, delayDownBtn;
    private ImageButton speedUpBtn, speedDownBtn;
    private ImageButton groupUpBtn, groupDownBtn;
    private ImageButton leftUpBtn, leftDownBtn;
    private ImageButton rightUpBtn, rightDownBtn;
    private ImageButton topUpBtn, topDownBtn;
    private ImageButton bottomUpBtn, bottomDownBtn;
    private ImageButton velXUpBtn, velXDownBtn;
    private ImageButton velYUpBtn, velYDownBtn;
    
    // Button repeat functionality
    private final ButtonRepeatHelper buttonRepeatHelper = new ButtonRepeatHelper();
    
    // Value constraints (same as player dialog)
    private static final int INTEGER_INCREMENT = 1;
    private static final double DECIMAL_INCREMENT = 0.5;
    private static final int DELAY_MAX = 100000;
    private static final int SPEED_MAX = 100000;
    private static final int BOUNDS_MAX = 1000;
    private static final double SPEED_MAX_VALUE = 50.0;
    private static final double SPEED_MIN_VALUE = 0.0;
    
    public TriggerMovementPropertiesDialog(LevelCreatorActivity activity, GridObject gridObject) {
        super(activity);
        this.activity = activity;
        this.gridObjects = new ArrayList<>();
        this.gridObjects.add(gridObject);
        this.triggers = new ArrayList<>();
        this.triggers.add((SimpleTriggerMovement) gridObject.getSimpleObject());
        
        setContentView(R.layout.dialog_movement_trigger_properties);
        
        // Set dialog size to 85% of screen, but respect max dimensions
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

    public TriggerMovementPropertiesDialog(LevelCreatorActivity activity, List<GridObject> gridObjects) {
        super(activity);
        this.activity = activity;
        this.gridObjects = gridObjects;
        this.triggers = new ArrayList<>();
        for (GridObject gridObject : gridObjects) {
            this.triggers.add((SimpleTriggerMovement) gridObject.getSimpleObject());
        }
        
        setContentView(R.layout.dialog_movement_trigger_properties);
        
        // Set dialog size to 85% of screen, but respect max dimensions
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
        velXText = findViewById(R.id.velXText);
        velYText = findViewById(R.id.velYText);
        
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
        velXUpBtn = findViewById(R.id.targetSpeedUpButton);
        velXDownBtn = findViewById(R.id.targetSpeedDownButton);
        velYUpBtn = findViewById(R.id.jumpUpButton);
        velYDownBtn = findViewById(R.id.jumpDownButton);
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
        velXText.setText(getSharedValueOrDash(triggers, trigger -> String.format("%.1f", trigger.getXSpeed())));
        velYText.setText(getSharedValueOrDash(triggers, trigger -> String.format("%.1f", trigger.getYSpeed())));
    }

    private String getSharedValueOrDash(List<SimpleTriggerMovement> triggers, java.util.function.Function<SimpleTriggerMovement, Object> valueExtractor) {
        if (triggers.isEmpty()) return "-";
        
        SimpleTriggerMovement firstTrigger = triggers.get(0);
        Object firstValue = valueExtractor.apply(firstTrigger);
        
        // Check if all triggers have the same value
        boolean allSameValue = triggers.stream()
            .allMatch(trigger -> valueExtractor.apply(trigger).equals(firstValue));
        
        if (allSameValue) {
            return String.valueOf(firstValue);
        } else {
            return "-";
        }
    }

    private int getCurrentValueOrMin(List<SimpleTriggerMovement> triggers, java.util.function.Function<SimpleTriggerMovement, Integer> valueExtractor, int minValue) {
        if (triggers.isEmpty()) return minValue;
        
        SimpleTriggerMovement firstTrigger = triggers.get(0);
        int firstValue = valueExtractor.apply(firstTrigger);
        
        // Check if all triggers have the same value
        boolean allSameValue = triggers.stream()
            .allMatch(trigger -> valueExtractor.apply(trigger) == firstValue);
        
        if (allSameValue) {
            return firstValue;
        } else {
            return minValue;
        }
    }

    private double getCurrentValueOrMinDouble(List<SimpleTriggerMovement> triggers, java.util.function.Function<SimpleTriggerMovement, Double> valueExtractor, double minValue) {
        if (triggers.isEmpty()) return minValue;
        
        SimpleTriggerMovement firstTrigger = triggers.get(0);
        double firstValue = valueExtractor.apply(firstTrigger);
        
        // Check if all triggers have the same value
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
        setupButtonWithRepeat(groupUpBtn, () -> adjustGroupId(INTEGER_INCREMENT), () -> adjustGroupId(INTEGER_INCREMENT));
        setupButtonWithRepeat(groupDownBtn, () -> adjustGroupId(-INTEGER_INCREMENT), () -> adjustGroupId(-INTEGER_INCREMENT));
        
        // Bounds controls
        setupButtonWithRepeat(leftUpBtn, () -> adjustLeftBound(INTEGER_INCREMENT), () -> adjustLeftBound(INTEGER_INCREMENT));
        setupButtonWithRepeat(leftDownBtn, () -> adjustLeftBound(-INTEGER_INCREMENT), () -> adjustLeftBound(-INTEGER_INCREMENT));
        
        setupButtonWithRepeat(rightUpBtn, () -> adjustRightBound(INTEGER_INCREMENT), () -> adjustRightBound(INTEGER_INCREMENT));
        setupButtonWithRepeat(rightDownBtn, () -> adjustRightBound(-INTEGER_INCREMENT), () -> adjustRightBound(-INTEGER_INCREMENT));
        
        setupButtonWithRepeat(topUpBtn, () -> adjustTopBound(INTEGER_INCREMENT), () -> adjustTopBound(INTEGER_INCREMENT));
        setupButtonWithRepeat(topDownBtn, () -> adjustTopBound(-INTEGER_INCREMENT), () -> adjustTopBound(-INTEGER_INCREMENT));
        
        setupButtonWithRepeat(bottomUpBtn, () -> adjustBottomBound(INTEGER_INCREMENT), () -> adjustBottomBound(INTEGER_INCREMENT));
        setupButtonWithRepeat(bottomDownBtn, () -> adjustBottomBound(-INTEGER_INCREMENT), () -> adjustBottomBound(-INTEGER_INCREMENT));
        
        // Walk speed controls (X Speed)
        setupButtonWithRepeat(velXUpBtn, () -> adjustVelX(DECIMAL_INCREMENT), () -> adjustVelX(DECIMAL_INCREMENT));
        setupButtonWithRepeat(velXDownBtn, () -> adjustVelX(-DECIMAL_INCREMENT), () -> adjustVelX(-DECIMAL_INCREMENT));
        
        // Jump force controls (Y Speed)
        setupButtonWithRepeat(velYUpBtn, () -> adjustVelY(DECIMAL_INCREMENT), () -> adjustVelY(DECIMAL_INCREMENT));
        setupButtonWithRepeat(velYDownBtn, () -> adjustVelY(-DECIMAL_INCREMENT), () -> adjustVelY(-DECIMAL_INCREMENT));
        
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

    private int getAcceleratedIncrement(int baseIncrement) {
        return buttonRepeatHelper.getAcceleratedIncrement(baseIncrement);
    }

    private double getAcceleratedIncrement(double baseIncrement) {
        return buttonRepeatHelper.getAcceleratedIncrement((int)(baseIncrement * 10)) / 10.0;
    }
    
    private void saveStateAndRefresh() {
        if (activity != null && activity.getGridView() != null) {
            activity.getGridView().invalidate();
        }
    }
    
    private void adjustDelay(int increment) {
        if (triggers.isEmpty()) return;
        int currentDelay = getCurrentValueOrMin(triggers, SimpleTrigger::getDelay, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newDelay = Math.max(0, Math.min(DELAY_MAX, currentDelay + actualIncrement));
        
        for (SimpleTriggerMovement trigger : triggers) {
            trigger.setDelay(newDelay);
        }
        delayText.setText(String.valueOf(newDelay));
        saveStateAndRefresh();
    }
    
    private void adjustSpeed(int increment) {
        if (triggers.isEmpty()) return;
        int currentSpeed = getCurrentValueOrMin(triggers, SimpleTrigger::getSpeed, 1);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newSpeed = Math.max(1, Math.min(SPEED_MAX, currentSpeed + actualIncrement));
        
        for (SimpleTriggerMovement trigger : triggers) {
            trigger.setSpeed(newSpeed);
        }
        speedText.setText(String.valueOf(newSpeed));
        saveStateAndRefresh();
    }
    
    private void adjustGroupId(int increment) {
        if (triggers.isEmpty()) return;
        int currentGroupId = getCurrentValueOrMin(triggers, SimplePhysicsObject::getGroupId, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newGroupId = Math.max(0, Math.min(1000, currentGroupId + actualIncrement));
        
        for (int i = 0; i < triggers.size(); i++) {
            SimpleTriggerMovement trigger = triggers.get(i);
            GridObject gridObject = gridObjects.get(i);
            int oldGroupId = trigger.getGroupId();
            trigger.setGroupId(newGroupId);
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
        if (triggers.isEmpty()) return;
        int currentLeft = getCurrentValueOrMin(triggers, SimpleTrigger::getWR1, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newLeft = Math.max(0, Math.min(BOUNDS_MAX, currentLeft + actualIncrement));
        
        for (SimpleTriggerMovement trigger : triggers) {
            trigger.setWR1(newLeft);
        }
        leftText.setText(String.valueOf(newLeft));
        saveStateAndRefresh();
    }
    
    private void adjustRightBound(int increment) {
        if (triggers.isEmpty()) return;
        int currentRight = getCurrentValueOrMin(triggers, SimpleTrigger::getWR2, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newRight = Math.max(0, Math.min(BOUNDS_MAX, currentRight + actualIncrement));
        
        for (SimpleTriggerMovement trigger : triggers) {
            trigger.setWR2(newRight);
        }
        rightText.setText(String.valueOf(newRight));
        saveStateAndRefresh();
    }
    
    private void adjustTopBound(int increment) {
        if (triggers.isEmpty()) return;
        int currentTop = getCurrentValueOrMin(triggers, SimpleTrigger::getHR1, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newTop = Math.max(0, Math.min(BOUNDS_MAX, currentTop + actualIncrement));
        
        for (SimpleTriggerMovement trigger : triggers) {
            trigger.setHR1(newTop);
        }
        topText.setText(String.valueOf(newTop));
        saveStateAndRefresh();
    }
    
    private void adjustBottomBound(int increment) {
        if (triggers.isEmpty()) return;
        int currentBottom = getCurrentValueOrMin(triggers, SimpleTrigger::getHR2, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newBottom = Math.max(0, Math.min(BOUNDS_MAX, currentBottom + actualIncrement));
        
        for (SimpleTriggerMovement trigger : triggers) {
            trigger.setHR2(newBottom);
        }
        bottomText.setText(String.valueOf(newBottom));
        saveStateAndRefresh();
    }
    
    @SuppressLint("DefaultLocale")
    private void adjustVelX(double increment) {
        if (triggers.isEmpty()) return;
        double currentVelX = getCurrentValueOrMinDouble(triggers, SimpleTriggerMovement::getXSpeed, SPEED_MIN_VALUE);
        double actualIncrement = getAcceleratedIncrement(increment);
        double newVelX = Math.max(SPEED_MIN_VALUE, Math.min(SPEED_MAX_VALUE, currentVelX + actualIncrement));
        
        for (SimpleTriggerMovement trigger : triggers) {
            trigger.setXSpeed(newVelX);
        }
        velXText.setText(String.format("%.1f", newVelX));
        saveStateAndRefresh();
    }
    
    @SuppressLint("DefaultLocale")
    private void adjustVelY(double increment) {
        if (triggers.isEmpty()) return;
        double currentVelY = getCurrentValueOrMinDouble(triggers, SimpleTriggerMovement::getYSpeed, SPEED_MIN_VALUE);
        double actualIncrement = getAcceleratedIncrement(increment);
        double newVelY = Math.max(SPEED_MIN_VALUE, Math.min(SPEED_MAX_VALUE, currentVelY + actualIncrement));
        
        for (SimpleTriggerMovement trigger : triggers) {
            trigger.setYSpeed(newVelY);
        }
        velYText.setText(String.format("%.1f", newVelY));
        saveStateAndRefresh();
    }
    
    @Override
    public void show() {
        super.show();
        Window window = getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.DialogSlideAnimation);
        }
    }
}
