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
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleTriggerInversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TriggerInversionPropertiesDialog extends Dialog {
    
    private final List<SimpleTriggerInversion> triggers;
    private final List<GridObject> gridObjects;
    private final LevelCreatorActivity activity;
    
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
    private static final int BOUNDS_MAX = 1000;
    
    public TriggerInversionPropertiesDialog(LevelCreatorActivity activity, GridObject gridObject) {
        super(activity);
        this.activity = activity;
        this.gridObjects = new ArrayList<>();
        this.gridObjects.add(gridObject);
        this.triggers = new ArrayList<>();
        this.triggers.add((SimpleTriggerInversion) gridObject.getSimpleObject());
        
        setContentView(R.layout.dialog_inversion_trigger_properties);
        
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

    public TriggerInversionPropertiesDialog(LevelCreatorActivity activity, List<GridObject> gridObjects) {
        super(activity);
        this.activity = activity;
        this.gridObjects = gridObjects;
        this.triggers = new ArrayList<>();
        for (GridObject gridObject : gridObjects) {
            this.triggers.add((SimpleTriggerInversion) gridObject.getSimpleObject());
        }
        
        setContentView(R.layout.dialog_inversion_trigger_properties);
        
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
        if (triggers.isEmpty()) return;
        
        // Check if all triggers have the same values for each property
        delayText.setText(getSharedValueOrDash(triggers, SimpleTrigger::getDelay));
        speedText.setText(getSharedValueOrDash(triggers, SimpleTrigger::getSpeed));
        groupIdText.setText(getSharedValueOrDash(triggers, SimplePhysicsObject::getGroupId));
        leftText.setText(getSharedValueOrDash(triggers, SimpleTrigger::getWR1));
        rightText.setText(getSharedValueOrDash(triggers, SimpleTrigger::getWR2));
        topText.setText(getSharedValueOrDash(triggers, SimpleTrigger::getHR1));
        bottomText.setText(getSharedValueOrDash(triggers, SimpleTrigger::getHR2));
    }

    private String getSharedValueOrDash(List<SimpleTriggerInversion> triggers, java.util.function.Function<SimpleTriggerInversion, Object> valueExtractor) {
        if (triggers.isEmpty()) return "-";
        
        SimpleTriggerInversion firstTrigger = triggers.get(0);
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

    private int getCurrentValueOrMin(List<SimpleTriggerInversion> triggers, java.util.function.Function<SimpleTriggerInversion, Integer> valueExtractor, int minValue) {
        if (triggers.isEmpty()) return minValue;
        
        SimpleTriggerInversion firstTrigger = triggers.get(0);
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
        
        for (SimpleTriggerInversion trigger : triggers) {
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
        
        for (SimpleTriggerInversion trigger : triggers) {
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
            SimpleTriggerInversion trigger = triggers.get(i);
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
        
        for (SimpleTriggerInversion trigger : triggers) {
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
        
        for (SimpleTriggerInversion trigger : triggers) {
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
        
        for (SimpleTriggerInversion trigger : triggers) {
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
        
        for (SimpleTriggerInversion trigger : triggers) {
            trigger.setHR2(newBottom);
        }
        bottomText.setText(String.valueOf(newBottom));
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
