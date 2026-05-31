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
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimpleTriggerTranslate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TriggerTranslatePropertiesDialog extends Dialog {
    
    private final List<SimpleTriggerTranslate> triggers;
    private final List<GridObject> gridObjects;
    private final LevelCreatorActivity activity;
    
    // UI Elements
    private TextView delayText, speedText, groupIdText;
    private TextView leftText, rightText, topText, bottomText;
    private TextView blocksText, directionText;
    private ImageButton delayUpBtn, delayDownBtn;
    private ImageButton speedUpBtn, speedDownBtn;
    private ImageButton groupUpBtn, groupDownBtn;
    private ImageButton leftUpBtn, leftDownBtn;
    private ImageButton rightUpBtn, rightDownBtn;
    private ImageButton topUpBtn, topDownBtn;
    private ImageButton bottomUpBtn, bottomDownBtn;
    private ImageButton blocksUpBtn, blocksDownBtn;
    private ImageButton directionUpBtn, directionDownBtn;
    
    // Button repeat functionality
    private final ButtonRepeatHelper buttonRepeatHelper = new ButtonRepeatHelper();
    
    // Value constraints
    private static final int INTEGER_INCREMENT = 1;
    private static final int DELAY_MAX = 100000;
    private static final int SPEED_MAX = 100000;
    private static final int BOUNDS_MAX = 1000;
    private static final int BLOCKS_MAX = 100000;
    private static final int BLOCKS_MIN = 1;
    
    public TriggerTranslatePropertiesDialog(LevelCreatorActivity activity, GridObject gridObject) {
        super(activity);
        this.activity = activity;
        this.gridObjects = new ArrayList<>();
        this.gridObjects.add(gridObject);
        this.triggers = new ArrayList<>();
        this.triggers.add((SimpleTriggerTranslate) gridObject.getSimpleObject());
        
        setContentView(R.layout.dialog_translate_trigger_properties);
        
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

    public TriggerTranslatePropertiesDialog(LevelCreatorActivity activity, List<GridObject> gridObjects) {
        super(activity);
        this.activity = activity;
        this.gridObjects = gridObjects;
        this.triggers = new ArrayList<>();
        for (GridObject gridObject : gridObjects) {
            this.triggers.add((SimpleTriggerTranslate) gridObject.getSimpleObject());
        }
        
        setContentView(R.layout.dialog_translate_trigger_properties);
        
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
        blocksText = findViewById(R.id.blocksText);
        directionText = findViewById(R.id.directionText);
        
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
        blocksUpBtn = findViewById(R.id.blocksUpButton);
        blocksDownBtn = findViewById(R.id.blocksDownButton);
        directionUpBtn = findViewById(R.id.directionUpButton);
        directionDownBtn = findViewById(R.id.directionDownButton);
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
        blocksText.setText(getSharedValueOrDash(triggers, SimpleTriggerTranslate::getBlockCount));
        directionText.setText(getSharedValueOrDash(triggers, trigger -> trigger.getDirection().name()));
    }

    private String getSharedValueOrDash(List<SimpleTriggerTranslate> triggers, java.util.function.Function<SimpleTriggerTranslate, Object> valueExtractor) {
        if (triggers.isEmpty()) return "-";
        
        SimpleTriggerTranslate firstTrigger = triggers.get(0);
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

    private int getCurrentValueOrMin(List<SimpleTriggerTranslate> triggers, java.util.function.Function<SimpleTriggerTranslate, Integer> valueExtractor, int minValue) {
        if (triggers.isEmpty()) return minValue;
        
        SimpleTriggerTranslate firstTrigger = triggers.get(0);
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
        
        // Blocks controls
        setupButtonWithRepeat(blocksUpBtn, () -> adjustBlocks(INTEGER_INCREMENT), () -> adjustBlocks(INTEGER_INCREMENT));
        setupButtonWithRepeat(blocksDownBtn, () -> adjustBlocks(-INTEGER_INCREMENT), () -> adjustBlocks(-INTEGER_INCREMENT));
        
        // Direction controls
        setupButtonWithRepeat(directionUpBtn, () -> adjustDirection(1), () -> adjustDirection(1));
        setupButtonWithRepeat(directionDownBtn, () -> adjustDirection(-1), () -> adjustDirection(-1));
        
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
        
        for (SimpleTriggerTranslate trigger : triggers) {
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
        
        for (SimpleTriggerTranslate trigger : triggers) {
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
            SimpleTriggerTranslate trigger = triggers.get(i);
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
        
        for (SimpleTriggerTranslate trigger : triggers) {
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
        
        for (SimpleTriggerTranslate trigger : triggers) {
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
        
        for (SimpleTriggerTranslate trigger : triggers) {
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
        
        for (SimpleTriggerTranslate trigger : triggers) {
            trigger.setHR2(newBottom);
        }
        bottomText.setText(String.valueOf(newBottom));
        saveStateAndRefresh();
    }
    
    private void adjustBlocks(int increment) {
        if (triggers.isEmpty()) return;
        int currentBlocks = getCurrentValueOrMin(triggers, SimpleTriggerTranslate::getBlockCount, BLOCKS_MIN);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newBlocks = Math.max(BLOCKS_MIN, Math.min(BLOCKS_MAX, currentBlocks + actualIncrement));
        
        for (SimpleTriggerTranslate trigger : triggers) {
            trigger.setBlockCount(newBlocks);
        }
        blocksText.setText(String.valueOf(newBlocks));
        saveStateAndRefresh();
    }
    
    private void adjustDirection(int increment) {
        if (triggers.isEmpty()) return;
        
        // Get current direction from first trigger
        final SimpleTriggerTranslate.Direction firstDirection = triggers.get(0).getDirection();
        
        // Check if all triggers have the same direction
        boolean allSameDirection = triggers.stream()
            .allMatch(trigger -> trigger.getDirection() == firstDirection);
        
        // Determine starting direction
        SimpleTriggerTranslate.Direction currentDirection;
        if (!allSameDirection) {
            // If directions differ, start from UP
            currentDirection = SimpleTriggerTranslate.Direction.UP;
        } else {
            currentDirection = firstDirection;
        }
        
        // Cycle through directions: UP -> RIGHT -> DOWN -> LEFT -> UP
        SimpleTriggerTranslate.Direction newDirection;
        switch (currentDirection) {
            case UP:
                newDirection = increment > 0 ? SimpleTriggerTranslate.Direction.RIGHT : SimpleTriggerTranslate.Direction.LEFT;
                break;
            case RIGHT:
                newDirection = increment > 0 ? SimpleTriggerTranslate.Direction.DOWN : SimpleTriggerTranslate.Direction.UP;
                break;
            case DOWN:
                newDirection = increment > 0 ? SimpleTriggerTranslate.Direction.LEFT : SimpleTriggerTranslate.Direction.RIGHT;
                break;
            case LEFT:
                newDirection = increment > 0 ? SimpleTriggerTranslate.Direction.UP : SimpleTriggerTranslate.Direction.DOWN;
                break;
            default:
                newDirection = SimpleTriggerTranslate.Direction.UP;
        }
        
        for (SimpleTriggerTranslate trigger : triggers) {
            trigger.setDirection(newDirection);
        }
        directionText.setText(newDirection.name());
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
