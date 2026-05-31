package com.unipi.alexandris.android.echotrialsonandroid.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.data.PlayerStatistics;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.ButtonSoundHelper;
import com.unipi.alexandris.android.echotrialsonandroid.levelcreator.LevelCreatorActivity;
import com.unipi.alexandris.android.echotrialsonandroid.utility.SessionManager;

public class TestStatsDialog extends Dialog {

    private final Context context;
    private final SessionManager sessionManager;

    // UI Components
    private EditText etStars;
    private EditText etCoins;
    private EditText etUltraLevels;
    private EditText etCreatorLevels;
    private EditText etProfileLikes;
    private Button btnLoadCurrent;
    private Button btnSaveToLocal;
    private Button btnClose;
    private ImageButton windowCloseButton;
    private TextView tvStatus;

    public TestStatsDialog(Context context, SessionManager sessionManager) {
        super(context);
        this.context = context;
        this.sessionManager = sessionManager;

        initializeDialog();
        setupViews();
        setupListeners();
        loadCurrentStats();
    }

    private void initializeDialog() {
        setContentView(R.layout.dialog_test_stats);
        setCancelable(true);
        setCanceledOnTouchOutside(false);

        // Set dialog dimensions using dp values like other dialogs
        WindowManager.LayoutParams params = getWindow().getAttributes();
        float density = context.getResources().getDisplayMetrics().density;
        
        // Use fixed sizes for test dialog
        int width = (int) (density * 500); // 500dp
        int height = (int) (density * 600); // 600dp
        
        params.width = width;
        params.height = height;
        params.gravity = Gravity.CENTER;
        getWindow().setAttributes(params);
    }

    private void setupViews() {
        etStars = findViewById(R.id.etStars);
        etCoins = findViewById(R.id.etCoins);
        etUltraLevels = findViewById(R.id.etUltraLevels);
        etCreatorLevels = findViewById(R.id.etCreatorLevels);
        etProfileLikes = findViewById(R.id.etProfileLikes);
        btnLoadCurrent = findViewById(R.id.btnLoadCurrent);
        btnSaveToLocal = findViewById(R.id.btnSaveToLocal);
        btnClose = findViewById(R.id.btnClose);
        windowCloseButton = findViewById(R.id.windowCloseButton);
        tvStatus = findViewById(R.id.tvStatus);
    }

    private void setupListeners() {
        // Apply touch animations to all buttons
        View.OnTouchListener touchListener = LevelCreatorActivity.createButtonTapFeedback();

        // Close button - follow established pattern
        if (windowCloseButton != null) {
            windowCloseButton.setOnTouchListener(LevelCreatorActivity.createButtonTapFeedback());
            ButtonSoundHelper.addClickSound(windowCloseButton, v -> dismiss());
        }

        // Action buttons
        btnLoadCurrent.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(btnLoadCurrent, v -> loadCurrentStats());

        btnSaveToLocal.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(btnSaveToLocal, v -> saveToLocal());

        btnClose.setOnTouchListener(touchListener);
        ButtonSoundHelper.addClickSound(btnClose, v -> dismiss());
    }

    private void loadCurrentStats() {
        // Force reload from file to get latest stats
        sessionManager.loadPlayerStatisticsFromFile();
        PlayerStatistics stats = sessionManager.getPlayerStatistics();
        if (stats != null) {
            etStars.setText(String.valueOf(stats.getStars()));
            etCoins.setText(String.valueOf(stats.getCoins()));
            etUltraLevels.setText(String.valueOf(stats.getUltraDifficultyLevelsCompleted()));
            etCreatorLevels.setText(String.valueOf(stats.getCreatorCompletedLevels()));
            etProfileLikes.setText(String.valueOf(stats.getTotalProfileLikes()));
            
            showStatus("Current stats loaded from local storage");
        } else {
            showStatus("No local stats found");
        }
    }

    private void saveToLocal() {
        try {
            PlayerStatistics stats = sessionManager.getPlayerStatistics();
            if (stats == null) {
                stats = new PlayerStatistics("test");
            }

            // Parse and set values from EditText fields
            stats.setStars(parseInt(etStars.getText().toString(), 0));
            stats.setCoins(parseInt(etCoins.getText().toString(), 0));
            stats.setUltraDifficultyLevelsCompleted(parseInt(etUltraLevels.getText().toString(), 0));
            stats.setCreatorCompletedLevels(parseInt(etCreatorLevels.getText().toString(), 0));
            stats.setTotalProfileLikes(parseInt(etProfileLikes.getText().toString(), 0));

            // Update timestamp
            stats.updateTimestamp();

            // Save to session manager
            sessionManager.updatePlayerStatistics(stats);
            
            showStatus("Stats saved to local storage successfully!");
        } catch (Exception e) {
            showStatus("Error saving stats: " + e.getMessage());
        }
    }

    private int parseInt(String text, int defaultValue) {
        if (text == null || text.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void showStatus(String message) {
        tvStatus.setText(message);
        tvStatus.setVisibility(View.VISIBLE);
        
        // Hide status after 3 seconds
        tvStatus.postDelayed(() -> {
            tvStatus.setVisibility(View.GONE);
        }, 3000);
    }

    @Override
    public void show() {
        super.show();
    }
    
    @Override
    public void dismiss() {
        super.dismiss();
    }
}
