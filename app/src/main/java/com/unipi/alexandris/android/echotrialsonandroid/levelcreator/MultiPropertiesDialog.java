package com.unipi.alexandris.android.echotrialsonandroid.levelcreator;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.ButtonSoundHelper;

import java.util.List;
import java.util.Objects;

/**
 * Dialog for editing base physics properties of multiple selected objects.
 * Only exposes common base properties: x, y, size, velX, velY, widthMultiplier, heightMultiplier.
 * When a field is changed, it applies to all selected objects.
 */
public class MultiPropertiesDialog extends Dialog {

    private final List<GridObject> gridObjects;
    private final LevelCreatorActivity activity;
    
    // UI Elements
    private TextView groupIdText;
    private ImageButton groupUpBtn, groupDownBtn;
    
    // Button repeat functionality
    private final ButtonRepeatHelper buttonRepeatHelper = new ButtonRepeatHelper();

    @SuppressLint("SetTextI18n")
    public MultiPropertiesDialog(LevelCreatorActivity activity, List<GridObject> gridObjects) {
        super(activity);
        this.activity = activity;
        this.gridObjects = gridObjects;


        setContentView(R.layout.dialog_block_properties);
        
        // Set dialog size to 85% of screen, but respect max dimensions
        WindowManager.LayoutParams params = Objects.requireNonNull(getWindow()).getAttributes();
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        int targetWidth = (int) (screenWidth * 0.85);
        int targetHeight = (int) (screenHeight * 0.85);
        int maxWidth = (int) (activity.getResources().getDisplayMetrics().density * 480); // 480dp (block dialog max)
        int maxHeight = (int) (activity.getResources().getDisplayMetrics().density * 320); // 320dp (block dialog max)
        params.width = Math.min(targetWidth, maxWidth);
        params.height = Math.min(targetHeight, maxHeight);
        getWindow().setAttributes(params);

        // Update dialog title for multi-selection
        TextView dialogTitle = findViewById(R.id.dialogTitle);
        if (dialogTitle != null) {
            dialogTitle.setText("Multiple Objects Properties");
        }

        // Save current state to undo stack before making any changes
        activity.saveCanvasStateForced();

        // Initialize views and setup
        initializeViews();
        loadCurrentValues();
        setupButtonListeners();
    }
    
    private void initializeViews() {
        groupIdText = findViewById(R.id.groupIdText);
        groupUpBtn = findViewById(R.id.groupUpButton);
        groupDownBtn = findViewById(R.id.groupDownButton);
    }
    
    private void loadCurrentValues() {
        if (gridObjects.isEmpty()) {
            groupIdText.setText("-");
            return;
        }
        groupIdText.setText(getSharedValueOrDash(gridObjects, GridObject::getGroupId));
    }

    private String getSharedValueOrDash(List<GridObject> gridObjects, java.util.function.Function<GridObject, Integer> valueExtractor) {
        if (gridObjects.isEmpty()) return "-";
        
        GridObject firstObject = gridObjects.get(0);
        int firstValue = valueExtractor.apply(firstObject);
        
        // Check if all objects have the same value
        boolean allSameValue = gridObjects.stream()
            .allMatch(obj -> valueExtractor.apply(obj) == firstValue);
        
        if (allSameValue) {
            return String.valueOf(firstValue);
        } else {
            return "-";
        }
    }

    private int getCurrentValueOrMin(List<GridObject> gridObjects, java.util.function.Function<GridObject, Integer> valueExtractor, int minValue) {
        if (gridObjects.isEmpty()) return minValue;
        
        GridObject firstObject = gridObjects.get(0);
        int firstValue = valueExtractor.apply(firstObject);
        
        // Check if all objects have the same value
        boolean allSameValue = gridObjects.stream()
            .allMatch(obj -> valueExtractor.apply(obj) == firstValue);
        
        if (allSameValue) {
            return firstValue;
        } else {
            return minValue;
        }
    }
    

    
    @Override
    public void dismiss() {
        super.dismiss();
    }
    
    @Override
    public void cancel() {
        super.cancel();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonListeners() {
        // Group ID controls
        setupButtonWithRepeat(groupUpBtn, () -> adjustGroupId(1), () -> adjustGroupId(1));
        setupButtonWithRepeat(groupDownBtn, () -> adjustGroupId(-1), () -> adjustGroupId(-1));
        
        // Confirm button
        ImageButton confirmButton = findViewById(R.id.confirmButton2);
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
    
    private void adjustGroupId(int increment) {
        if (gridObjects.isEmpty()) return;
        
        // Get current value, or start from 0 if values differ
        int currentGroupId = getCurrentValueOrMin(gridObjects, GridObject::getGroupId, 0);
        int actualIncrement = getAcceleratedIncrement(increment);
        int newGroupId = Math.max(0, Math.min(1000, currentGroupId + actualIncrement));
        
        // Update all objects and their corresponding grid objects
        for (GridObject gridObject : gridObjects) {
            int oldGroupId = gridObject.getGroupId();
            gridObject.getSimpleObject().setGroupId(newGroupId);
            gridObject.setGroupId(newGroupId);
            
            // Update the grid cell mapping
            if (activity != null && activity.getGridView() != null) {
                LevelGridView gridView = activity.getGridView();
                int gridX = (int) (gridObject.getSimpleObject().getX() / gridView.getCellSize());
                int gridY = (int) (gridObject.getSimpleObject().getY() / gridView.getCellSize());
                gridView.changeObjectGroup(gridX, gridY, oldGroupId, newGroupId);
            }
        }
        
        // Update the display
        groupIdText.setText(String.valueOf(newGroupId));
        saveStateAndRefresh();
    }

    @Override
    public void show() {
        super.show();
        
        // Apply window animation directly to dialog
        Window window = getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.DialogSlideAnimation);
        }
    }

}


