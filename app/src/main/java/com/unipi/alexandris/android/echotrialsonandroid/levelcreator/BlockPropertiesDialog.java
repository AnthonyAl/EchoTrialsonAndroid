package com.unipi.alexandris.android.echotrialsonandroid.levelcreator;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.view.Window;


import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimplePhysicsObject;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.ButtonSoundHelper;

public class BlockPropertiesDialog extends Dialog {
    
    private final GridObject gridObject;
    private final SimplePhysicsObject block;
    private final LevelCreatorActivity activity;

    // UI Elements
    private TextView groupIdText;
    private ImageButton groupUpBtn, groupDownBtn;
    
    // Button repeat functionality
    private final ButtonRepeatHelper buttonRepeatHelper = new ButtonRepeatHelper();
    

    
    public BlockPropertiesDialog(LevelCreatorActivity activity, GridObject gridObject, LevelGridView gridView) {
        super(activity);
        this.activity = activity;
        this.gridObject = gridObject;
        this.block = gridObject.getSimpleObject();

        setContentView(R.layout.dialog_block_properties);
        Window window = getWindow();
        if(window == null) return;
        WindowManager.LayoutParams params = getWindow().getAttributes();
        int width = (int) (activity.getResources().getDisplayMetrics().density * 480);
        int height = (int) (activity.getResources().getDisplayMetrics().density * 320);
        params.width = width;
        params.height = height;
        getWindow().setAttributes(params);

        activity.saveCanvasStateForced();

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
        groupIdText.setText(String.valueOf(block.getGroupId()));
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
    
    private void adjustGroupId(int increment) {
        int actualIncrement = getAcceleratedIncrement(increment);
        int oldGroupId = block.getGroupId();
        int newGroupId = Math.max(0, Math.min(1000, oldGroupId + actualIncrement));
        block.setGroupId(newGroupId);
        gridObject.setGroupId(newGroupId);
        
        // Update the grid cell mapping
        if (activity != null && activity.getGridView() != null) {
            LevelGridView gridView = activity.getGridView();
            int gridX = (int) (block.getX() / gridView.getCellSize());
            int gridY = (int) (block.getY() / gridView.getCellSize());
            gridView.changeObjectGroup(gridX, gridY, oldGroupId, newGroupId);
        }
        
        groupIdText.setText(String.valueOf(newGroupId));
        saveStateAndRefresh();
    }

    private int getAcceleratedIncrement(int baseIncrement) {
        return buttonRepeatHelper.getAcceleratedIncrement(baseIncrement);
    }
    
    private void saveStateAndRefresh() {
        if (activity != null && activity.getGridView() != null) {
            activity.getGridView().invalidate();
        }
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
    
    @Override
    public void dismiss() {
        super.dismiss();
    }
}
