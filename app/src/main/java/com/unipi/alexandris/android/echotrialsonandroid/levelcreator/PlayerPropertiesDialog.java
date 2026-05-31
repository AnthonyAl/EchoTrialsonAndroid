package com.unipi.alexandris.android.echotrialsonandroid.levelcreator;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.SimplePlayer;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.ButtonSoundHelper;

import java.util.Objects;

/**
 * Dialog for editing player properties using button controls.
 * Uses dialog_player_properties.xml layout with up/down buttons for all values.
 */
public class PlayerPropertiesDialog extends Dialog {
    
    private final GridObject gridObject;
    private final SimplePlayer player;
    private final LevelCreatorActivity activity;

    // UI Elements
    private TextView sizeText, widthText, heightText, velXText, velYText, gravityText, groupIdText;
    private ImageButton sizeUpBtn, sizeDownBtn;
    private ImageButton widthUpBtn, widthDownBtn;
    private ImageButton heightUpBtn, heightDownBtn;
    private ImageButton speedUpBtn, speedDownBtn;
    private ImageButton jumpUpBtn, jumpDownBtn;
    private ImageButton gravityUpBtn, gravityDownBtn;
    private ImageButton groupUpBtn, groupDownBtn;
    
    // Button repeat functionality
    private final ButtonRepeatHelper buttonRepeatHelper = new ButtonRepeatHelper();
    
    // Value constraints
    private static final double DECIMAL_INCREMENT = 0.5;
    private static final double MULTIPLIER_INCREMENT = 0.1;
    private static final double GRAVITY_INCREMENT = 0.1;
    private static final int SIZE_INCREMENT = 1;
    private static final int SIZE_MAX = 100;
    private static final double MULTIPLIER_MAX = 10.0;
    private static final double SPEED_MAX = 50.0;
    private static final double GRAVITY_MAX = 25.0;
    

    
    public PlayerPropertiesDialog(LevelCreatorActivity activity, GridObject gridObject) {
        super(activity);
        this.activity = activity;
        this.gridObject = gridObject;
        this.player = (SimplePlayer) gridObject.getSimpleObject();
        

        
        setContentView(R.layout.dialog_player_properties);
        
        // Set dialog size to 80% of screen, but respect max dimensions
        WindowManager.LayoutParams params = Objects.requireNonNull(getWindow()).getAttributes();
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        int targetWidth = (int) (screenWidth * 0.85);
        int targetHeight = (int) (screenHeight * 0.85);
        int maxWidth = (int) (activity.getResources().getDisplayMetrics().density * 700); // 600dp
        int maxHeight = (int) (activity.getResources().getDisplayMetrics().density * 330); // 800dp
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
        sizeText = findViewById(R.id.sizeText);
        widthText = findViewById(R.id.widthText);
        heightText = findViewById(R.id.heightText);
        velXText = findViewById(R.id.velXText);
        velYText = findViewById(R.id.velYText);
        gravityText = findViewById(R.id.gravityText);
        groupIdText = findViewById(R.id.groupIdText);
        
        // Buttons with unique IDs
        sizeUpBtn = findViewById(R.id.sizeUpButton);
        sizeDownBtn = findViewById(R.id.sizeDownButton);
        widthUpBtn = findViewById(R.id.widthUpButton);
        widthDownBtn = findViewById(R.id.widthDownButton);
        heightUpBtn = findViewById(R.id.heightUpButton);
        heightDownBtn = findViewById(R.id.heightDownButton);
        speedUpBtn = findViewById(R.id.speedUpButton);
        speedDownBtn = findViewById(R.id.speedDownButton);
        jumpUpBtn = findViewById(R.id.jumpUpButton);
        jumpDownBtn = findViewById(R.id.jumpDownButton);
        gravityUpBtn = findViewById(R.id.gravityUpButton);
        gravityDownBtn = findViewById(R.id.gravityDownButton);
        groupUpBtn = findViewById(R.id.groupUpButton);
        groupDownBtn = findViewById(R.id.groupDownButton);
    }
    
    @SuppressLint("DefaultLocale")
    private void loadCurrentValues() {
        sizeText.setText(String.valueOf(player.getSize()));
        widthText.setText(String.valueOf((int)(player.getWidthMultiplier() * 10)));
        heightText.setText(String.valueOf((int)(player.getHeightMultiplier() * 10)));
        velXText.setText(String.format("%.1f", player.getSpeedX()));
        velYText.setText(String.format("%.1f", player.getSpeedY()));
        gravityText.setText(String.valueOf((int)(player.getGravity() * 10)));
        groupIdText.setText(String.valueOf(player.getGroupId()));
    }
    
    @SuppressLint("ClickableViewAccessibility")
    private void setupButtonListeners() {
        // Size controls
        setupButtonWithRepeat(sizeUpBtn, () -> adjustSize(SIZE_INCREMENT), () -> adjustSize(SIZE_INCREMENT));
        setupButtonWithRepeat(sizeDownBtn, () -> adjustSize(-SIZE_INCREMENT), () -> adjustSize(-SIZE_INCREMENT));
        
        // Width multiplier controls
        setupButtonWithRepeat(widthUpBtn, () -> adjustWidthMultiplier(MULTIPLIER_INCREMENT), () -> adjustWidthMultiplier(MULTIPLIER_INCREMENT));
        setupButtonWithRepeat(widthDownBtn, () -> adjustWidthMultiplier(-MULTIPLIER_INCREMENT), () -> adjustWidthMultiplier(-MULTIPLIER_INCREMENT));
        
        // Height multiplier controls
        setupButtonWithRepeat(heightUpBtn, () -> adjustHeightMultiplier(MULTIPLIER_INCREMENT), () -> adjustHeightMultiplier(MULTIPLIER_INCREMENT));
        setupButtonWithRepeat(heightDownBtn, () -> adjustHeightMultiplier(-MULTIPLIER_INCREMENT), () -> adjustHeightMultiplier(-MULTIPLIER_INCREMENT));
        
        // Speed controls
        setupButtonWithRepeat(speedUpBtn, () -> adjustSpeed(DECIMAL_INCREMENT), () -> adjustSpeed(DECIMAL_INCREMENT));
        setupButtonWithRepeat(speedDownBtn, () -> adjustSpeed(-DECIMAL_INCREMENT), () -> adjustSpeed(-DECIMAL_INCREMENT));
        
        // Jump controls
        setupButtonWithRepeat(jumpUpBtn, () -> adjustJump(DECIMAL_INCREMENT), () -> adjustJump(DECIMAL_INCREMENT));
        setupButtonWithRepeat(jumpDownBtn, () -> adjustJump(-DECIMAL_INCREMENT), () -> adjustJump(-DECIMAL_INCREMENT));
        
        // Gravity controls
        setupButtonWithRepeat(gravityUpBtn, () -> adjustGravity(GRAVITY_INCREMENT), () -> adjustGravity(GRAVITY_INCREMENT));
        setupButtonWithRepeat(gravityDownBtn, () -> adjustGravity(-GRAVITY_INCREMENT), () -> adjustGravity(-GRAVITY_INCREMENT));
        
        // Group ID controls
        setupButtonWithRepeat(groupUpBtn, () -> adjustGroupId(1), () -> adjustGroupId(1));
        setupButtonWithRepeat(groupDownBtn, () -> adjustGroupId(-1), () -> adjustGroupId(-1));
        
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
    
    private void adjustSize(int increment) {
        int actualIncrement = getAcceleratedIncrement(increment);
        int newSize = Math.max(0, Math.min(SIZE_MAX, player.getSize() + actualIncrement));
        player.setSize(newSize);
        sizeText.setText(String.valueOf(newSize));
        saveStateAndRefresh();
    }
    
    private void adjustWidthMultiplier(double increment) {
        double actualIncrement = getAcceleratedIncrement(increment);
        double newWidth = Math.max(0.1, Math.min(MULTIPLIER_MAX, player.getWidthMultiplier() + actualIncrement));
        player.setWidthMultiplier(newWidth);
        widthText.setText(String.valueOf((int)(newWidth * 10)));
        saveStateAndRefresh();
    }
    
    private void adjustHeightMultiplier(double increment) {
        double actualIncrement = getAcceleratedIncrement(increment);
        double newHeight = Math.max(0.1, Math.min(MULTIPLIER_MAX, player.getHeightMultiplier() + actualIncrement));
        player.setHeightMultiplier(newHeight);
        heightText.setText(String.valueOf((int)(newHeight * 10)));
        saveStateAndRefresh();
    }
    
    @SuppressLint("DefaultLocale")
    private void adjustSpeed(double increment) {
        double actualIncrement = getAcceleratedIncrement(increment);
        double newSpeed = Math.max(0.0, Math.min(SPEED_MAX, player.getSpeedX() + actualIncrement));
        player.setSpeedX(newSpeed);
        velXText.setText(String.format("%.1f", newSpeed));
        saveStateAndRefresh();
    }
    
    @SuppressLint("DefaultLocale")
    private void adjustJump(double increment) {
        double actualIncrement = getAcceleratedIncrement(increment);
        double newJump = Math.max(0.0, Math.min(SPEED_MAX, player.getSpeedY() + actualIncrement));
        player.setSpeedY(newJump);
        velYText.setText(String.format("%.1f", newJump));
        saveStateAndRefresh();
    }
    
    private void adjustGravity(double increment) {
        double actualIncrement = getAcceleratedIncrement(increment);
        double newGravity = Math.max(0.0, Math.min(GRAVITY_MAX, player.getGravity() + actualIncrement));
        player.setGravity(newGravity);
        gravityText.setText(String.valueOf((int)(newGravity * 10)));
        saveStateAndRefresh();
    }
    
    private void adjustGroupId(int increment) {
        int actualIncrement = getAcceleratedIncrement(increment);
        int newGroupId = Math.max(0, Math.min(1000, player.getGroupId() + actualIncrement));
        player.setGroupId(newGroupId);
        gridObject.setGroupId(newGroupId); // Update the grid object's group ID
        groupIdText.setText(String.valueOf(newGroupId));
        saveStateAndRefresh();
    }
    
    /**
     * Gets the accelerated increment based on how long the button has been held.
     * 
     * @param baseIncrement The base increment value
     * @return The accelerated increment value
     */
    private int getAcceleratedIncrement(int baseIncrement) {
        return buttonRepeatHelper.getAcceleratedIncrement(baseIncrement);
    }
    
    /**
     * Gets the accelerated increment for double values based on how long the button has been held.
     * 
     * @param baseIncrement The base increment value
     * @return The accelerated increment value
     */
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
        
        // Apply window animation directly to dialog
        Window window = getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.DialogSlideAnimation);
        }
    }
    
    @Override
    public void dismiss() {
        // No need to save state here - it was already saved when dialog opened
        super.dismiss();
    }
}
