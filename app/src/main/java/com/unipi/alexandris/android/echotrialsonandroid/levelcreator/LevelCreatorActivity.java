package com.unipi.alexandris.android.echotrialsonandroid.levelcreator;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.controller.DataEncoder;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.ButtonSoundHelper;
import com.unipi.alexandris.android.echotrialsonandroid.utility.UniversalDialogAnimator;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.*;
import com.unipi.alexandris.android.echotrialsonandroid.utility.LevelID;
import com.unipi.alexandris.android.echotrialsonandroid.view.CreatorLevelViewActivity;
import com.unipi.alexandris.android.echotrialsonandroid.view.CreatorMyLevelsActivity;
import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import android.util.Log;

/**
 * Main activity for the level creator functionality.
 * Manages the user interface components and coordinates interactions between
 * the grid view, palette items, toolbar buttons, and group selector.
 * <br>
 * This activity allows users to:
 * - Place various types of level objects on a grid
 * - Select different object types from a palette
 * - Toggle features like slide mode and color tinting
 * - Group objects for layered placement
 * - Save and clear level designs
 */
public class LevelCreatorActivity extends AppCompatActivity {

    private LevelGridView gridView;
    private PaletteItemView selectedPaletteItem;
    private ImageButton deleteButton;
    private boolean tintEnabled = false;
    private View propertiesButtonContainer;
    private View copyButtonContainer;
    private View pasteButtonContainer;
    private String currentLevelName;
    private GridObject selectedGridObject;
    private TextView selectedItemsText;
    private View progressLayout;
    private TextView progressText;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentLevelUid;
    private boolean isNewLevel;
    private static class ClipboardData {
        public final List<CopiedObject> objects;
        public final int width;
        public final int height;
        
        public ClipboardData(List<CopiedObject> objects, int width, int height) {
            this.objects = objects;
            this.width = width;
            this.height = height;
        }
    }

    private static class CopiedObject {
        public final LevelObject type;
        public final int relativeX; // Relative to selection top-left
        public final int relativeY; // Relative to selection top-left
        public final int groupId;
        public final SimplePhysicsObject originalSimpleObject;
        
        public CopiedObject(LevelObject type, int relativeX, int relativeY, int groupId, SimplePhysicsObject originalSimpleObject) {
            this.type = type;
            this.relativeX = relativeX;
            this.relativeY = relativeY;
            this.groupId = groupId;
            this.originalSimpleObject = originalSimpleObject;
        }
    }

    private ClipboardData clipboardData = null;
    private static class CanvasState {
        private final Map<String, GridCell> gridState;
        
        public CanvasState(Map<String, GridCell> currentGrid) {
            // Create deep copy of entire grid
            this.gridState = new HashMap<>();
            for (Map.Entry<String, GridCell> entry : currentGrid.entrySet()) {
                this.gridState.put(entry.getKey(), new GridCell(entry.getValue()));
            }
        }
        
        public Map<String, GridCell> getGridState() {
            return gridState;
        }

        public boolean isDifferentFrom(CanvasState other) {
            if (other == null) return true;
            if (this.gridState.size() != other.gridState.size()) return true;
            
            // Check if all cells and their contents are the same
            for (Map.Entry<String, GridCell> entry : this.gridState.entrySet()) {
                String key = entry.getKey();
                GridCell thisCell = entry.getValue();
                GridCell otherCell = other.gridState.get(key);
                
                if (otherCell == null) return true;
                if (thisCell.getAllObjects().size() != otherCell.getAllObjects().size()) return true;
                
                // Compare objects in cells (detailed comparison including properties)
                for (GridObject obj : thisCell.getAllObjects()) {
                    boolean found = false;
                    for (GridObject otherObj : otherCell.getAllObjects()) {
                        if (obj.getGroupId() == otherObj.getGroupId() && 
                            obj.getSimpleObject().getClass().equals(otherObj.getSimpleObject().getClass()) &&
                            areObjectPropertiesEqual(obj.getSimpleObject(), otherObj.getSimpleObject())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) return true;
                }
            }
            return false;
        }

        private static boolean areObjectPropertiesEqual(SimplePhysicsObject obj1, SimplePhysicsObject obj2) {
            if (obj1 == null || obj2 == null) return obj1 == obj2;
            
            // Compare basic properties
            if (Double.compare(obj1.getX(), obj2.getX()) != 0) return false;
            if (Double.compare(obj1.getY(), obj2.getY()) != 0) return false;
            if (obj1.getSize() != obj2.getSize()) return false;
            if (Double.compare(obj1.getVelX(), obj2.getVelX()) != 0) return false;
            if (Double.compare(obj1.getVelY(), obj2.getVelY()) != 0) return false;
            if (Double.compare(obj1.getWidthMultiplier(), obj2.getWidthMultiplier()) != 0) return false;
            if (Double.compare(obj1.getHeightMultiplier(), obj2.getHeightMultiplier()) != 0) return false;
            
            // Compare player-specific properties if both are players
            if (obj1 instanceof SimplePlayer && obj2 instanceof SimplePlayer) {
                SimplePlayer player1 = (SimplePlayer) obj1;
                SimplePlayer player2 = (SimplePlayer) obj2;
                if (Double.compare(player1.getSpeedX(), player2.getSpeedX()) != 0) return false;
                if (Double.compare(player1.getSpeedY(), player2.getSpeedY()) != 0) return false;
                if (Double.compare(player1.getGravity(), player2.getGravity()) != 0) return false;
            }
            
            // Compare trigger-specific properties if both are triggers
            if (obj1 instanceof SimpleTrigger && 
                obj2 instanceof SimpleTrigger) {
                
                SimpleTrigger trigger1 = 
                    (SimpleTrigger) obj1;
                SimpleTrigger trigger2 = 
                    (SimpleTrigger) obj2;
                
                // Compare common trigger properties
                if (trigger1.getDelay() != trigger2.getDelay()) return false;
                if (trigger1.getSpeed() != trigger2.getSpeed()) return false;
                // targetGroupId removed - triggers use their own groupId as target
                if (trigger1.getWR1() != trigger2.getWR1()) return false;
                if (trigger1.getWR2() != trigger2.getWR2()) return false;
                if (trigger1.getHR1() != trigger2.getHR1()) return false;
                if (trigger1.getHR2() != trigger2.getHR2()) return false;
                
                // Compare trigger-specific properties based on type
                if (trigger1 instanceof SimpleTriggerFade && 
                    trigger2 instanceof SimpleTriggerFade) {
                    
                    SimpleTriggerFade fade1 = 
                        (SimpleTriggerFade) trigger1;
                    SimpleTriggerFade fade2 = 
                        (SimpleTriggerFade) trigger2;
                    
                    if (Double.compare(fade1.getTargetOpacity(), fade2.getTargetOpacity()) != 0) return false;
                    return fade1.isRemoveCollisionAtZero() == fade2.isRemoveCollisionAtZero();
                    
                } else if (trigger1 instanceof SimpleTriggerGravity && 
                           trigger2 instanceof SimpleTriggerGravity) {
                    
                    SimpleTriggerGravity gravity1 = 
                        (SimpleTriggerGravity) trigger1;
                    SimpleTriggerGravity gravity2 = 
                        (SimpleTriggerGravity) trigger2;
                    
                    if (Double.compare(gravity1.getGravityStrength(), gravity2.getGravityStrength()) != 0) return false;
                    return gravity1.getGravityDirection() == gravity2.getGravityDirection();
                    
                } else if (trigger1 instanceof SimpleTriggerMovement && 
                           trigger2 instanceof SimpleTriggerMovement) {
                    
                    SimpleTriggerMovement movement1 = 
                        (SimpleTriggerMovement) trigger1;
                    SimpleTriggerMovement movement2 = 
                        (SimpleTriggerMovement) trigger2;
                    
                    if (Double.compare(movement1.getXSpeed(), movement2.getXSpeed()) != 0) return false;
                    if (Double.compare(movement1.getYSpeed(), movement2.getYSpeed()) != 0) return false;
                    return Double.compare(movement1.getGravityIntensity(), movement2.getGravityIntensity()) == 0;
                    
                } else if (trigger1 instanceof SimpleTriggerScale && 
                           trigger2 instanceof SimpleTriggerScale) {
                    
                    SimpleTriggerScale scale1 = 
                        (SimpleTriggerScale) trigger1;
                    SimpleTriggerScale scale2 = 
                        (SimpleTriggerScale) trigger2;
                    
                    if (Double.compare(scale1.getTargetHeight(), scale2.getTargetHeight()) != 0) return false;
                    return Double.compare(scale1.getTargetWidth(), scale2.getTargetWidth()) == 0;
                    
                } else if (trigger1 instanceof SimpleTriggerTranslate && 
                           trigger2 instanceof SimpleTriggerTranslate) {
                    
                    SimpleTriggerTranslate translate1 = 
                        (SimpleTriggerTranslate) trigger1;
                    SimpleTriggerTranslate translate2 = 
                        (SimpleTriggerTranslate) trigger2;
                    
                    if (translate1.getDirection() != translate2.getDirection()) return false;
                    return translate1.getBlockCount() == translate2.getBlockCount();
                }
                // Note: SimpleTriggerInversion has no specific properties beyond the common ones
            }
            
            return true;
        }
    }

    private final Stack<CanvasState> undoStack = new Stack<>();
    private final Stack<CanvasState> redoStack = new Stack<>();
    private static final int MAX_UNDO_HISTORY = 50;
    private DataEncoder dataEncode;
    private TextView objectLimitText;
    private LevelGridLoader levelGridLoader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_creator);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        // Get level UID from intent
        Intent intent = getIntent();
        currentLevelUid = intent.getStringExtra("level_uid");
        isNewLevel = intent.getBooleanExtra("is_new_level", true);
        
        // Load existing level data if not a new level
        if (!isNewLevel && currentLevelUid != null) {
            loadLevelFromFirestore();
        }
        
        // Initialize views
        gridView = findViewById(R.id.gridView);
        objectLimitText = findViewById(R.id.objectLimitText);
        selectedItemsText = findViewById(R.id.selectedItemsText);
        
        // Initialize progress overlay views
        progressLayout = findViewById(R.id.progressLayout);
        progressText = findViewById(R.id.progressText);
        progressBar = findViewById(R.id.progressBar3);
        
        // Set up grid modification callback for undo/redo
        gridView.setModificationCallback(this::saveCanvasState);
        
        // Set up object count update callback
        gridView.setObjectCountUpdateCallback(this::updateObjectCountDisplay);
        
        // Set up selection change callback for updating selected items text
        gridView.setSelectionChangeCallback(this::updateSelectedItemsDisplay);

        updateObjectCountDisplay();
        updateSelectedItemsDisplay();
        deleteButton = findViewById(R.id.btnDelete);
        deleteButton.clearColorFilter();

        setupToolbarButtons();
        initializePalette();
        GroupSelector groupSelector = findViewById(R.id.groupSelector);
        if (groupSelector != null) {
            groupSelector.setOnGroupSelectedListener(group -> {
                if (gridView != null) {
                    gridView.setCurrentGroup(group);
                }
            });
        }

        dataEncode = new DataEncoder(this);
        levelGridLoader = new LevelGridLoader(this);
        setupCameraHeightLimits();
        updateUndoRedoButtonStates();
    }

    private void initializePalette() {
        GridLayout paletteTray = findViewById(R.id.paletteTray);
        List<PaletteItemView> paletteItems = new ArrayList<>();

        for (LevelObject object : LevelObject.values()) {
            PaletteItemView paletteItem = new PaletteItemView(this, object);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dpToPx(36);
            params.height = dpToPx(36);
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            paletteItem.setLayoutParams(params);
            
            paletteItem.setOnClickListener(v -> selectPaletteItem((PaletteItemView) v));
            
            paletteTray.addView(paletteItem);
            paletteItems.add(paletteItem);
        }

        if (!paletteItems.isEmpty()) {
            selectPaletteItem(paletteItems.get(0));
        }
    }

    private void selectPaletteItem(PaletteItemView item) {
        // Deselect previous item
        if (selectedPaletteItem != null) {
            selectedPaletteItem.setSelected(false);
        }
        deleteButton.clearColorFilter();

        selectedPaletteItem = item;
        if (selectedPaletteItem != null) {
            selectedPaletteItem.setSelected(true);
        }

        gridView.setSelectedObject(item != null ? item.getLevelObject() : null);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupToolbarButtons() {
        // Top toolbar buttons (left side)
        ImageButton undoButton = findViewById(R.id.undoButton);
        ImageButton redoButton = findViewById(R.id.redoButton);
        ImageButton clearButton = findViewById(R.id.btnClear);
        ImageButton propertiesButton = findViewById(R.id.btnProperties);
        ImageButton copyButton = findViewById(R.id.btnCopy);
        ImageButton pasteButton = findViewById(R.id.btnPaste);
        
        // Top toolbar buttons (right side)
        ImageButton saveButton = findViewById(R.id.btnSave);
        ImageButton loadButton = findViewById(R.id.btnLoad);
        ImageButton tintButton = findViewById(R.id.btnTint);
        
        // Right sidebar buttons
        ImageButton slideButton = findViewById(R.id.btnSlide);
        ImageButton selectModeButton = findViewById(R.id.btnSelectMode);
        
        // Navigation buttons for palette
        ImageButton leftNavButton = findViewById(R.id.imageButton);
        ImageButton rightNavButton = findViewById(R.id.imageButton2);
        
        // Cancel button for back functionality
        ImageButton cancelButton = findViewById(R.id.btnCancel);
        
        // Get button containers for visibility management
        propertiesButtonContainer = findViewById(R.id.propertiesButtonContainer);
        copyButtonContainer = findViewById(R.id.copyButtonContainer);
        pasteButtonContainer = findViewById(R.id.pasteButtonContainer);
        
        ButtonSoundHelper.addClickSound(saveButton, v -> handleSave());
        ButtonSoundHelper.addClickSound(loadButton, v -> handleLoad());
        ButtonSoundHelper.addClickSound(clearButton, v -> handleClear());
        ButtonSoundHelper.addClickSound(deleteButton, v -> handleDelete());
        
        if (slideButton != null) {
            ButtonSoundHelper.addClickSound(slideButton, v -> toggleSlideMode());
        }
        
        if (tintButton != null) {
            ButtonSoundHelper.addClickSound(tintButton, v -> toggleTint());
        }
        
        // Set up navigation button click listeners
        if (leftNavButton != null) {
            ButtonSoundHelper.addClickSound(leftNavButton, v -> scrollPaletteRight()); // Forward button scrolls right
        }
        
        if (rightNavButton != null) {
            ButtonSoundHelper.addClickSound(rightNavButton, v -> scrollPaletteLeft()); // Back button scrolls left
        }
        
        // Snap-to-grid is always enabled now; button removed from layout
        
        if (propertiesButton != null) {
            ButtonSoundHelper.addClickSound(propertiesButton, v -> handleProperties());
            // Properties available only in selection mode or when a single object is explicitly selected in future
            propertiesButton.setEnabled(false);
            propertiesButton.setAlpha(0.4f);
        }

        if (selectModeButton != null) {
            ButtonSoundHelper.addClickSound(selectModeButton, v -> toggleSelectionMode(selectModeButton));
        }

        if (copyButton != null) {
            ButtonSoundHelper.addClickSound(copyButton, v -> handleCopy());
        }

        if (pasteButton != null) {
            ButtonSoundHelper.addClickSound(pasteButton, v -> handlePaste());
        }

        // New buttons (placeholders for now - functionality will be added later)
        if (undoButton != null) {
            ButtonSoundHelper.addClickSound(undoButton, v -> handleUndo());
        }
        if (redoButton != null) {
            ButtonSoundHelper.addClickSound(redoButton, v -> handleRedo());
        }
        
        // Set up cancel button with confirmation dialog
        if (cancelButton != null) {
            ButtonSoundHelper.addClickSound(cancelButton, v -> showExitConfirmationDialog());
        }

        View.OnTouchListener tapFeedback = createButtonTapFeedback();
        saveButton.setOnTouchListener(tapFeedback);
        loadButton.setOnTouchListener(tapFeedback);
        clearButton.setOnTouchListener(tapFeedback);
        deleteButton.setOnTouchListener(tapFeedback);
        if (slideButton != null) slideButton.setOnTouchListener(tapFeedback);
        if (tintButton != null) tintButton.setOnTouchListener(tapFeedback);
        if (propertiesButton != null) propertiesButton.setOnTouchListener(tapFeedback);
        if (selectModeButton != null) selectModeButton.setOnTouchListener(tapFeedback);
        if (copyButton != null) copyButton.setOnTouchListener(tapFeedback);
        if (pasteButton != null) pasteButton.setOnTouchListener(tapFeedback);
        if (undoButton != null) undoButton.setOnTouchListener(tapFeedback);
        if (redoButton != null) redoButton.setOnTouchListener(tapFeedback);
        if (cancelButton != null) cancelButton.setOnTouchListener(tapFeedback);

        if (propertiesButtonContainer != null) {
            propertiesButtonContainer.setVisibility(View.VISIBLE);
        }
        if (propertiesButton != null) {
            propertiesButton.setEnabled(false);
            propertiesButton.setAlpha(0.4f);
        }
    }

    private void handleSave() {
        if (gridView == null || dataEncode == null) {
            Toast.makeText(this, "Error: Grid view or data encoder not initialized", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentLevelUid == null) {
            Toast.makeText(this, "Error: No level UID specified", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress("Preparing to save...");

        new Thread(() -> {
            try {
                int totalObjects = gridView.countTotalObjectsInLevel();
                if (totalObjects > LevelGridView.MAX_OBJECT_COUNT) {
                    runOnUiThread(() -> {
                        hideProgress();
                        Toast.makeText(this, "Cannot save level - object count (" + totalObjects + ") exceeds maximum limit of " + LevelGridView.MAX_OBJECT_COUNT, Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                
                // Update progress text
                runOnUiThread(() -> updateProgressText("Processing level data..."));
                
                // Get SimpleModel grid data from the grid view
                Map<String, SimplePhysicsObject> simpleGridData = gridView.getSimpleGridData();
                int gridSize = gridView.getCellSize();
                
                // Find the actual player spawn position from the grid
                int[] playerSpawnPos = gridView.getPlayerSpawnPosition();
                int playerSpawnX, playerSpawnY;
                
                if (playerSpawnPos != null) {
                    playerSpawnX = playerSpawnPos[0];
                    playerSpawnY = playerSpawnPos[1];
                } else {
                    playerSpawnX = 5;
                    playerSpawnY = 10;
                    runOnUiThread(() -> Toast.makeText(this, "No player spawn found, using default position", Toast.LENGTH_SHORT).show());
                }

                String levelName = (currentLevelName != null && !currentLevelName.isEmpty()) 
                    ? currentLevelName 
                    : "Level_" + System.currentTimeMillis();
                
                // Update progress text
                runOnUiThread(() -> updateProgressText("Encoding level data..."));
                
                // Create level data for Firestore
                Map<String, Object> levelData = new HashMap<>();
                levelData.put("name", levelName);
                levelData.put("lastEdited", new Date());
                levelData.put("isPublic", false);
                levelData.put("isOpenSource", false);
                levelData.put("isVerified", false);
                levelData.put("targetCompletionTime", -1L);
                levelData.put("bestCompletionTime", -1L);
                
                // Update progress text
                runOnUiThread(() -> updateProgressText("Compressing level data..."));

                String encodedLevelData = dataEncode.encodeSimpleLevelToCompressedXML(LevelID.LEVEL_COMMUNITY_CREATED, simpleGridData, gridSize, playerSpawnX, playerSpawnY);
                if (encodedLevelData == null) {
                    runOnUiThread(() -> {
                        hideProgress();
                        Toast.makeText(this, "Failed to compress level data - save cancelled", Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                
                levelData.put("levelData", encodedLevelData);
                
                // Update progress text
                runOnUiThread(() -> updateProgressText("Uploading to cloud..."));
                
                // Save to Firestore
                String userId = GameConstants.INSTANCE.getCurrentUserUid();
                db.collection("users").document(userId)
                        .collection("levels")
                        .document(currentLevelUid)
                        .set(levelData)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Update progress text
                                updateProgressText("Save complete!");
                                
                                // Small delay to show completion message
                                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                    hideProgress();
                                    Toast.makeText(this, "Level saved successfully to cloud!", Toast.LENGTH_SHORT).show();
                                    // Navigate to CreatorMyLevelsActivity and finish
                                    Intent intent = new Intent(this, CreatorMyLevelsActivity.class);
                                    startActivity(intent);
                                    finish();
                                }, 500);
                            } else {
                                hideProgress();
                                Toast.makeText(this, "Failed to save level: " + 
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                                    Toast.LENGTH_LONG).show();
                            }
                        });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    hideProgress();
                    Toast.makeText(this, "Error saving level: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void loadLevelFromFirestore() {
        if (currentLevelUid == null || auth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: Cannot load level", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String userId = GameConstants.INSTANCE.getCurrentUserUid();
        System.out.println("🔍 Loading level data for UID: " + currentLevelUid);
        
        db.collection("users").document(userId)
                .collection("levels")
                .document(currentLevelUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        String levelDataString = doc.getString("levelData");
                        
                        // Load the level name from Firestore
                        currentLevelName = doc.getString("name");
                        if (currentLevelName == null || currentLevelName.isEmpty()) {
                            currentLevelName = "Unnamed Level";
                        }
                        
                        if (levelDataString != null && !levelDataString.isEmpty()) {
                            System.out.println("Found level data, attempting to load into grid...");
                            loadLevelDataIntoGrid(levelDataString);
                        } else {
                            System.out.println("No level data found in document");
                            Toast.makeText(this, "No level data found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        System.out.println("Failed to load level from cloud: " +
                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        Toast.makeText(this, "Failed to load level from cloud", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadLevelDataIntoGrid(String levelDataString) {
        try {
            
            // Use the LevelGridLoader to load the level data from string
            if (levelGridLoader != null && gridView != null) {
                boolean success = levelGridLoader.loadLevelFromString(levelDataString, gridView);
                
                if (success) {
                    System.out.println("Level data loaded successfully into grid");
                    Toast.makeText(this, "Level loaded successfully!", Toast.LENGTH_SHORT).show();
                    updateObjectCountDisplay(); // Update the object count display
                } else {
                    System.out.println("Failed to load level data into grid");
                    Toast.makeText(this, "Failed to load level data", Toast.LENGTH_SHORT).show();
                }
            } else {
                System.out.println("LevelGridLoader or GridView not initialized");
                Toast.makeText(this, "Error: Grid not initialized", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            System.out.println("Error loading level data into grid: " + e.getMessage());
            Toast.makeText(this, "Error loading level data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLoad() {
        if (levelGridLoader == null) {
            Toast.makeText(this, "Error: Level loader not initialized", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            int levelResourceId = R.raw.level_debug_spikes;
            
            boolean success = levelGridLoader.loadLevelToGrid(levelResourceId, gridView);
            
            if (success) {
                Toast.makeText(this, "Level loaded successfully from raw resources!", Toast.LENGTH_SHORT).show();
                updateObjectCountDisplay(); // Update count after loading
            } else {
                Toast.makeText(this, "Error loading level from raw resources. Check if level_debug_1.xml exists.", Toast.LENGTH_SHORT).show();
                Log.e("LevelCreatorActivity", "Level loading failed");
            }
            
        } catch (Exception e) {
            Log.e("LevelCreatorActivity", "Error loading level", e);
            Toast.makeText(this, "Error loading level: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleTint() {
        tintEnabled = !tintEnabled;
        gridView.setShowColors(tintEnabled);
        
        ImageButton tintButton = findViewById(R.id.btnTint);
        if (tintButton != null) {
            if (tintEnabled) {
                tintButton.setColorFilter(ContextCompat.getColor(this, R.color.activation_green_tint));
            } else {
                tintButton.clearColorFilter();
            }
        }
        
        // Update text visibility based on tint mode
        updateObjectCountDisplay();
        updateSelectedItemsDisplay();
    }

    private void toggleSlideMode() {
        ImageButton slideButton = findViewById(R.id.btnSlide);
        if (slideButton != null && gridView != null) {
            // Don't allow slide mode when selection mode is active
            if (gridView.isSelectionModeActive()) {
                return; // Silently ignore - no toast notification
            }
            
            // Toggle slide mode in grid view
            boolean slideMode = gridView.toggleSlideMode();
            
            // Update button appearance
            if (slideMode) {
                slideButton.setColorFilter(ContextCompat.getColor(this, R.color.activation_green_tint)); // Green activation color
            } else {
                slideButton.clearColorFilter(); // Remove color filter to show original icon
            }
        }
    }

    private void toggleSelectionMode(ImageButton selectModeButton) {
        if (gridView == null) return;
        boolean enabled = gridView.toggleMultiSelectMode();
        
        // Visual feedback for selection mode
        if (enabled) {
            selectModeButton.setColorFilter(ContextCompat.getColor(this, R.color.activation_green_tint)); // green when active
            // Dim palette and slide button when selection mode is active
            dimPaletteForSelectionMode(true);
            updateButtonStatesForSelectionMode(true);
            setPropertiesButtonState(true);
            showCopyPasteButtons();
            deleteButton.clearColorFilter();
        } else {
            selectModeButton.clearColorFilter();
            dimPaletteForSelectionMode(false);
            updateButtonStatesForSelectionMode(false);
            setPropertiesButtonState(false);
            hideCopyPasteButtons();
            if (gridView != null && gridView.getSelectedObject() == null) {
                deleteButton.setColorFilter(ContextCompat.getColor(this, R.color.activation_green_tint));
            }
        }
    }

    private void dimPaletteForSelectionMode(boolean dim) {
        GridLayout paletteTray = findViewById(R.id.paletteTray);
        if (paletteTray != null) {
            float alpha = dim ? 0.3f : 1.0f;
            paletteTray.setAlpha(alpha);
            paletteTray.setEnabled(!dim);

            for (int i = 0; i < paletteTray.getChildCount(); i++) {
                paletteTray.getChildAt(i).setEnabled(!dim);
            }
        }
    }

    private void updateButtonStatesForSelectionMode(boolean selectionModeActive) {
        ImageButton slideButton = findViewById(R.id.btnSlide);
        
        if (selectionModeActive) {
            // Dim slide button when selection mode is active
            if (slideButton != null) {
                slideButton.setAlpha(0.3f);
                slideButton.setEnabled(false);
                slideButton.clearColorFilter(); // Remove any active tint
                // Make sure slide mode is off
                if (gridView != null && gridView.isSlideModeActive()) {
                    gridView.toggleSlideMode(); // This will turn it off
                }
            }
        } else {
            // Restore slide button when selection mode is disabled
            if (slideButton != null) {
                slideButton.setAlpha(1.0f);
                slideButton.setEnabled(true);
            }
        }
    }

    private void setPropertiesButtonState(boolean enabled) {
        ImageButton propertiesButton = findViewById(R.id.btnProperties);
        if (propertiesButton != null) {
            propertiesButton.setEnabled(enabled);
            propertiesButton.setAlpha(enabled ? 1.0f : 0.4f);
        }
    }

    public void showPropertiesButton(GridObject gridObject) {
        selectedGridObject = gridObject;
        if (propertiesButtonContainer != null) {
            propertiesButtonContainer.setVisibility(View.VISIBLE);
            ImageButton propertiesButton = findViewById(R.id.btnProperties);
            if (propertiesButton != null) {
                propertiesButton.setEnabled(true);
                propertiesButton.setAlpha(1.0f);
            }
        }
    }

    public void hidePropertiesButton() {
        selectedGridObject = null;
        if (propertiesButtonContainer != null) {
            propertiesButtonContainer.setVisibility(View.VISIBLE);
            ImageButton propertiesButton = findViewById(R.id.btnProperties);
            if (propertiesButton != null) {
                propertiesButton.setEnabled(false);
                propertiesButton.setAlpha(0.4f);
            }
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void setupCameraHeightLimits() {
        if (gridView == null) return;

        double minHeight = getMinHeight();
        double maxHeight = 0; // Camera can't show beyond level bottom
        
        // Set the camera height limits in the grid view for visualization
        gridView.setCameraHeightLimits(minHeight, maxHeight);
        
        // Position the grid view to start near the min height for better editing
        // This ensures the level creator opens at a reasonable position
        // The grid will be positioned so the camera limits are visible
    }

    private double getMinHeight() {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        // Calculate camera height limits based on screen dimensions
        // The camera follows the player and has these constraints:
        // - Min height: 0 (camera can't go above the top of the level)
        // - Max height: levelHeight - screenHeight (camera can't show beyond level bottom)
        // For level creator, we'll use a reasonable level height
        // A typical level might be 50 blocks tall (50 * 48 = 2400 pixels)
        int levelHeight = 100 * 48; // 50 blocks * 48 pixels per block
        // Camera can't go above level top
        return levelHeight - screenHeight;
    }

    private void handleCopy() {
        if (gridView == null || !gridView.hasMultiSelection()) {
            return;
        }

        List<String> selectedCells = new ArrayList<>(gridView.getSelectedCellKeys());
        if (selectedCells.isEmpty()) {
            return;
        }
        
        // Calculate selection bounds
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        
        for (String key : selectedCells) {
            String[] coords = key.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
        
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        
        // Collect all objects from selected cells
        List<CopiedObject> copiedObjects = new ArrayList<>();
        
        for (String key : selectedCells) {
            String[] coords = key.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            GridCell cell = gridView.accessCell(x, y);
            
            // Copy objects based on current group mode
            if (gridView.getCurrentGroup() == -1) {
                // ALL mode: copy all objects from the cell
                for (GridObject obj : cell.getAllObjects()) {
                    LevelObject type = getLevelObjectType(obj);
                    if (type != null) {
                        int relativeX = x - minX;
                        int relativeY = y - minY;
                        copiedObjects.add(new CopiedObject(type, relativeX, relativeY, obj.getGroupId(), obj.getSimpleObject()));
                    }
                }
            } else {
                // Specific group: copy only objects from current group
                GridObject obj = cell.getObject(gridView.getCurrentGroup());
                if (obj != null) {
                    LevelObject type = getLevelObjectType(obj);
                    if (type != null) {
                        int relativeX = x - minX;
                        int relativeY = y - minY;
                        copiedObjects.add(new CopiedObject(type, relativeX, relativeY, obj.getGroupId(), obj.getSimpleObject()));
                    }
                }
            }
        }
        
        // Store in clipboard
        clipboardData = new ClipboardData(copiedObjects, width, height);
        
        // Enable paste button if we have data
        setPasteButtonState(!clipboardData.objects.isEmpty());
    }

    private void handlePaste() {
        if (gridView == null || clipboardData == null || clipboardData.objects.isEmpty()) {
            return; // Nothing to paste
        }
        
        if (!gridView.hasMultiSelection()) {
            return; // Need a selection to paste into
        }
        
        // Get the selection bounds to determine paste location
        List<String> selectedCells = gridView.getSelectedCellKeys();
        if (selectedCells.isEmpty()) {
            return;
        }
        
        // Find the top-left of the current selection (paste anchor point)
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        for (String key : selectedCells) {
            String[] coords = key.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
        }
        
        // Check for conflicts before pasting
        List<String> conflicts = new ArrayList<>();
        
        for (CopiedObject copiedObj : clipboardData.objects) {
            int pasteX = minX + copiedObj.relativeX;
            int pasteY = minY + copiedObj.relativeY;
            GridCell targetCell = gridView.accessCell(pasteX, pasteY);
            
            // Check if target cell already has an object in the same group
            if (targetCell.getObject(copiedObj.groupId) != null) {
                conflicts.add("(" + pasteX + "," + pasteY + " group " + copiedObj.groupId + ")");
            }
        }
        
        // Show conflict warning if any conflicts exist
        if (!conflicts.isEmpty()) {
            Toast.makeText(this, "Cannot paste - conflicts with existing objects", Toast.LENGTH_SHORT).show();
            return; // Don't paste if conflicts exist
        }
        
        // Check for player limit violation before pasting
        boolean containsPlayer = false;
        for (CopiedObject copiedObj : clipboardData.objects) {
            if (copiedObj.type == LevelObject.PLAYER_SPAWN) {
                containsPlayer = true;
                break;
            }
        }
        
        if (containsPlayer) {
            // Count existing players in the level
            int currentPlayerCount = gridView.countPlayersInLevel();
            if (currentPlayerCount >= 1) {
                Toast.makeText(this, "Cannot paste player - only one player allowed per level", Toast.LENGTH_SHORT).show();
                return; // Don't paste if player limit would be exceeded
            }
        }
        
        // Check for object count limit violation before pasting
        if (gridView.wouldExceedObjectLimit(clipboardData.objects.size())) {
            String status = gridView.getObjectCountStatus();
            Toast.makeText(this, "Cannot paste - would exceed object limit: " + status, Toast.LENGTH_SHORT).show();
            return; // Don't paste if object limit would be exceeded
        }
        
        // No conflicts - proceed with pasting
        saveCanvasState(); // Save state before pasting
        
        for (CopiedObject copiedObj : clipboardData.objects) {
            int pasteX = minX + copiedObj.relativeX;
            int pasteY = minY + copiedObj.relativeY;
            
            // Create a new object based on the copied object's type and properties
            SimplePhysicsObject newSimpleObject = SimpleObjectFactory.createSimpleObject(
                copiedObj.type, pasteX, pasteY, gridView.getCellSize(), copiedObj.groupId
            );
            
            // Copy additional properties from the original object (like player speed, etc.)
            copyObjectProperties(copiedObj.originalSimpleObject, newSimpleObject);
            
            // Create GridObject and add to the grid
            GridObject newGridObject = new GridObject(newSimpleObject);
            newGridObject.setGroupId(copiedObj.groupId);
            
            GridCell targetCell = gridView.accessCell(pasteX, pasteY);
            targetCell.addObject(newGridObject);
        }
        
        // Refresh the grid view
        gridView.invalidate();
        updateObjectCountDisplay(); // Update count after pasting
    }

    private void handleClear() {
        if (gridView != null) {
            saveCanvasState(); // Save state before clearing
            gridView.clearGrid();
            updateObjectCountDisplay(); // Update count after clearing
        }
    }

    private void handleProperties() {
        if (gridView == null) return;
        
        // Multi-select: if any cells are selected, determine appropriate dialog type
        if (gridView.hasMultiSelection()) {
            // Check if selection contains any objects before showing dialog
            if (gridView.hasObjectsInSelection()) {
                // Check if selection contains exactly one object - if so, use specialized dialog
                GridObject singleObject = getSingleSelectedObject();
                if (singleObject != null) {
                    // Single object in multi-selection - use specialized dialog
                    openSpecializedPropertiesDialog(singleObject);
                } else {
                    // Multiple objects - check if they are all triggers
                    List<GridObject> selectedGridObjects = getSelectedGridObjects();
                    if (!selectedGridObjects.isEmpty() && selectedGridObjects.size() == getSelectedObjectCount()) {
                        // Check if all selected objects are triggers
                        boolean allTriggers = selectedGridObjects.stream()
                            .allMatch(obj -> obj.getSimpleObject() instanceof SimpleTrigger);
                        
                        if (allTriggers) {
                            // All selected objects are triggers - check if they are the same type
                            boolean allSameTriggerType = areAllTriggersSameType(selectedGridObjects);
                            
                            if (allSameTriggerType) {
                                // All triggers are the same type - use the specific trigger dialog
                                GridObject firstTrigger = selectedGridObjects.get(0);
                                SimplePhysicsObject firstObject = firstTrigger.getSimpleObject();

                                if (firstObject instanceof SimpleTriggerFade) {
                                    TriggerFadePropertiesDialog dialog = new TriggerFadePropertiesDialog(this, selectedGridObjects);
                                    dialog.show();
                                } else if (firstObject instanceof SimpleTriggerGravity) {
                                    TriggerGravityPropertiesDialog dialog = new TriggerGravityPropertiesDialog(this, selectedGridObjects);
                                    dialog.show();
                                } else if (firstObject instanceof SimpleTriggerInversion) {
                                    TriggerInversionPropertiesDialog dialog = new TriggerInversionPropertiesDialog(this, selectedGridObjects);
                                    dialog.show();
                                } else if (firstObject instanceof SimpleTriggerMovement) {
                                    TriggerMovementPropertiesDialog dialog = new TriggerMovementPropertiesDialog(this, selectedGridObjects);
                                    dialog.show();
                                } else if (firstObject instanceof SimpleTriggerScale) {
                                    TriggerScalePropertiesDialog dialog = new TriggerScalePropertiesDialog(this, selectedGridObjects);
                                    dialog.show();
                                } else if (firstObject instanceof SimpleTriggerTranslate) {
                                    TriggerTranslatePropertiesDialog dialog = new TriggerTranslatePropertiesDialog(this, selectedGridObjects);
                                    dialog.show();
                                } else {
                                    // Fallback to single object dialog for other trigger types
                                    openSpecializedPropertiesDialog(firstTrigger);
                                }
                            } else {
                                // Different trigger types - use shared trigger dialog
                                SharedTriggerPropertiesDialog dialog = new SharedTriggerPropertiesDialog(this, selectedGridObjects);
                                dialog.show();
                            }
                        } else {
                            // Mixed object types or non-triggers - use general multi-properties dialog
                            MultiPropertiesDialog dialog = new MultiPropertiesDialog(this, selectedGridObjects);
                            dialog.show();
                        }
                    } else {
                        // Mixed object types or non-triggers - use general multi-properties dialog
                        MultiPropertiesDialog dialog = new MultiPropertiesDialog(this, selectedGridObjects);
                        dialog.show();
                    }
                }
            }
            // Silently do nothing if selection contains no objects (empty cells only)
            return;
        }
        
        // Single object selection: open specialized dialog based on object type
        if (selectedGridObject != null) {
            openSpecializedPropertiesDialog(selectedGridObject);
        }
        // Remove toast notification for empty selection - silently do nothing
    }

    private GridObject getSingleSelectedObject() {
        if (gridView == null || !gridView.hasMultiSelection()) {
            return null;
        }
        
        List<String> selectedCells = gridView.getSelectedCellKeys();
        GridObject foundObject = null;
        int objectCount = 0;
        
        for (String key : selectedCells) {
            String[] coords = key.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            GridCell cell = gridView.accessCell(x, y);
            
            // Count objects based on current group mode
            if (gridView.getCurrentGroup() == -1) {
                // ALL mode: count all objects in the cell
                Collection<GridObject> objects = cell.getAllObjects();
                objectCount += objects.size();
                if (objectCount == 1 && foundObject == null) {
                    foundObject = objects.iterator().next();
                } else if (objectCount > 1) {
                    return null; // More than one object
                }
            } else {
                // Specific group mode: check if cell has object in current group
                GridObject obj = cell.getObject(gridView.getCurrentGroup());
                if (obj != null) {
                    objectCount++;
                    if (objectCount == 1) {
                        foundObject = obj;
                    } else {
                        return null; // More than one object
                    }
                }
            }
        }
        
        return (objectCount == 1) ? foundObject : null;
    }

    private boolean areAllTriggersSameType(List<GridObject> selectedGridObjects) {
        if (selectedGridObjects.isEmpty()) {
            return true;
        }
        
        SimplePhysicsObject firstObject = selectedGridObjects.get(0).getSimpleObject();
        Class<?> firstType = firstObject.getClass();
        
        return selectedGridObjects.stream()
            .allMatch(obj -> obj.getSimpleObject().getClass() == firstType);
    }

    private List<GridObject> getSelectedGridObjects() {
        List<GridObject> gridObjects = new ArrayList<>();
        if (gridView == null || !gridView.hasMultiSelection()) {
            return gridObjects;
        }
        
        List<String> selectedCells = gridView.getSelectedCellKeys();
        for (String key : selectedCells) {
            String[] coords = key.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            GridCell cell = gridView.accessCell(x, y);
            
            // Get objects based on current group mode
            if (gridView.getCurrentGroup() == -1) {
                // ALL mode: get all objects in the cell
                Collection<GridObject> objects = cell.getAllObjects();
                gridObjects.addAll(objects);
            } else {
                // Specific group mode: get object in current group
                GridObject obj = cell.getObject(gridView.getCurrentGroup());
                if (obj != null) {
                    gridObjects.add(obj);
                }
            }
        }
        
        return gridObjects;
    }

    private List<SimpleTrigger> getSelectedTriggers() {
        List<SimpleTrigger> triggers = new ArrayList<>();
        if (gridView == null || !gridView.hasMultiSelection()) {
            return triggers;
        }
        
        List<String> selectedCells = gridView.getSelectedCellKeys();
        for (String key : selectedCells) {
            String[] coords = key.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            GridCell cell = gridView.accessCell(x, y);
            
            // Get objects based on current group mode
            if (gridView.getCurrentGroup() == -1) {
                // ALL mode: get all objects in the cell
                Collection<GridObject> objects = cell.getAllObjects();
                for (GridObject obj : objects) {
                    if (obj.getSimpleObject() instanceof SimpleTrigger) {
                        triggers.add((SimpleTrigger) obj.getSimpleObject());
                    }
                }
            } else {
                // Specific group mode: check if cell has object in current group
                GridObject obj = cell.getObject(gridView.getCurrentGroup());
                if (obj != null && obj.getSimpleObject() instanceof SimpleTrigger) {
                    triggers.add((SimpleTrigger) obj.getSimpleObject());
                }
            }
        }
        
        return triggers;
    }

    private int getSelectedObjectCount() {
        if (gridView == null || !gridView.hasMultiSelection()) {
            return 0;
        }
        
        int objectCount = 0;
        List<String> selectedCells = gridView.getSelectedCellKeys();
        for (String key : selectedCells) {
            String[] coords = key.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            GridCell cell = gridView.accessCell(x, y);
            
            // Count objects based on current group mode
            if (gridView.getCurrentGroup() == -1) {
                // ALL mode: count all objects in the cell
                Collection<GridObject> objects = cell.getAllObjects();
                objectCount += objects.size();
            } else {
                // Specific group mode: check if cell has object in current group
                GridObject obj = cell.getObject(gridView.getCurrentGroup());
                if (obj != null) {
                    objectCount++;
                }
            }
        }
        
        return objectCount;
    }

    public void openSpecializedPropertiesDialog(GridObject gridObject) {
        SimplePhysicsObject simpleObject = gridObject.getSimpleObject();
        
        // Determine dialog type based on object type
        if (simpleObject instanceof SimplePlayer) {
            // Player object - use new PlayerPropertiesDialog
            PlayerPropertiesDialog dialog = new PlayerPropertiesDialog(this, gridObject);
            dialog.show();
        } else if (simpleObject instanceof SimpleTriggerFade) {
            // Fade trigger - open specialized dialog
            TriggerFadePropertiesDialog dialog = new TriggerFadePropertiesDialog(this, gridObject);
            dialog.show();
        } else if (simpleObject instanceof SimpleTriggerGravity) {
            // Gravity trigger - open specialized dialog
            TriggerGravityPropertiesDialog dialog = new TriggerGravityPropertiesDialog(this, gridObject);
            dialog.show();
        
        } else if (simpleObject instanceof SimpleTriggerInversion) {
            // Inversion trigger - open specialized dialog
            TriggerInversionPropertiesDialog dialog = new TriggerInversionPropertiesDialog(this, gridObject);
            dialog.show();
        } else if (simpleObject instanceof SimpleTriggerMovement) {
            // Movement trigger - open specialized dialog
            TriggerMovementPropertiesDialog dialog = new TriggerMovementPropertiesDialog(this, gridObject);
            dialog.show();
        } else if (simpleObject instanceof SimpleTriggerScale) {
            // Scale trigger - open specialized dialog
            TriggerScalePropertiesDialog dialog = new TriggerScalePropertiesDialog(this, gridObject);
            dialog.show();
        } else if (simpleObject instanceof SimpleTriggerTranslate) {
            // Translate trigger - open specialized dialog
            TriggerTranslatePropertiesDialog dialog = new TriggerTranslatePropertiesDialog(this, gridObject);
            dialog.show();
        } else {
            // Block or other object - use new BlockPropertiesDialog
            BlockPropertiesDialog dialog = new BlockPropertiesDialog(this, gridObject, gridView);
            dialog.show();
        }
    }

    private void scrollPaletteLeft() {
        HorizontalScrollView paletteScroll = findViewById(R.id.paletteScroll);
        if (paletteScroll != null) {
            // Scroll left by a fixed amount
            int scrollAmount = 200; // Adjust this value as needed
            paletteScroll.smoothScrollBy(-scrollAmount, 0);
            
            // Scale animation for visual feedback
            ImageButton rightButton = findViewById(R.id.imageButton2);
            if (rightButton != null) {
                rightButton.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(100)
                    .withEndAction(() -> 
                        rightButton.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start())
                    .start();
            }
        }
    }

    private void scrollPaletteRight() {
        HorizontalScrollView paletteScroll = findViewById(R.id.paletteScroll);
        if (paletteScroll != null) {
            // Scroll right by a fixed amount
            int scrollAmount = 200; // Adjust this value as needed
            paletteScroll.smoothScrollBy(scrollAmount, 0);
            
            // Scale animation for visual feedback
            ImageButton leftButton = findViewById(R.id.imageButton);
            if (leftButton != null) {
                leftButton.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(100)
                    .withEndAction(() -> 
                        leftButton.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start())
                    .start();
            }
        }
    }

    private void handleDelete() {
        if (gridView == null) return;
        
        if (gridView.isMultiSelectModeActive()) {
            // Selection mode: delete all selected objects
            deleteSelectedObjects();
        } else {
            // Normal mode: toggle delete mode
            toggleDeleteMode();
        }
    }

    private void deleteSelectedObjects() {
        if (gridView == null || !gridView.hasMultiSelection()) {
            return;
        }
        
        saveCanvasState(); // Save state before deleting
        
        // Get all selected cell keys
        List<String> selectedCells = gridView.getSelectedCellKeys();
        
        for (String key : selectedCells) {
            String[] coords = key.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            GridCell cell = gridView.accessCell(x, y);
            
            // Delete objects based on current group mode
            if (gridView.getCurrentGroup() == -1) {
                // ALL mode: delete all objects from the cell
                List<GridObject> allObjects = new ArrayList<>(cell.getAllObjects());
                for (GridObject obj : allObjects) {
                    cell.removeObject(obj.getGroupId());
                }
            } else {
                // Specific group: delete only objects from current group
                cell.removeObject(gridView.getCurrentGroup());
            }
        }
        
        // Clear the selection after deletion
        gridView.clearSelection();
        
        // Refresh the grid view
        gridView.invalidate();
        
        // Update object count display after deleting selected items
        updateObjectCountDisplay();
    }

    private void toggleDeleteMode() {
        if (gridView.getSelectedObject() == null) {
            // Currently in delete mode - restore last selected object or select first available
            if (selectedPaletteItem != null) {
                selectedPaletteItem.setSelected(true);
                gridView.setSelectedObject(selectedPaletteItem.getLevelObject());
            } else {
                // No previous selection - select the first available palette item
                selectFirstPaletteItem();
            }
            deleteButton.clearColorFilter(); // Remove active color
        } else {
            // Switch to delete mode
            gridView.setSelectedObject(null); // null means delete mode
            // Clear palette selection
            if (selectedPaletteItem != null) {
                selectedPaletteItem.setSelected(false);
                selectedPaletteItem = null;
            }
            deleteButton.setColorFilter(ContextCompat.getColor(this, R.color.activation_green_tint)); // Active color
        }
    }

    private void selectFirstPaletteItem() {
        GridLayout paletteTray = findViewById(R.id.paletteTray);
        if (paletteTray != null && paletteTray.getChildCount() > 0) {
            // Find the first PaletteItemView child
            for (int i = 0; i < paletteTray.getChildCount(); i++) {
                if (paletteTray.getChildAt(i) instanceof PaletteItemView) {
                    PaletteItemView firstItem = (PaletteItemView) paletteTray.getChildAt(i);
                    selectPaletteItem(firstItem);
                    gridView.setSelectedObject(firstItem.getLevelObject());
                    break;
                }
            }
        }
    }

    public void saveCanvasState() {
        if (gridView == null) return;
        
        CanvasState newState = new CanvasState(gridView.getGridCellsSnapshot());
        
        // Compare with previous state to avoid duplicate saves for grid operations
        CanvasState lastState = undoStack.isEmpty() ? null : undoStack.peek();
        if (newState.isDifferentFrom(lastState)) {
            // Clear redo stack when making new changes after undo
            redoStack.clear();
            
            // Add new state to undo stack
            undoStack.push(newState);
            
            // Limit undo history size
            if (undoStack.size() > MAX_UNDO_HISTORY) {
                undoStack.removeElementAt(0); // Remove oldest state
            }
            
            // Update button states
            updateUndoRedoButtonStates();
        }
    }

    public void saveCanvasStateForced() {
        if (gridView == null) return;
        
        CanvasState newState = new CanvasState(gridView.getGridCellsSnapshot());
        
        // Clear redo stack when making new changes after undo
        redoStack.clear();
        
        // Add new state to undo stack
        undoStack.push(newState);
        
        // Limit undo history size
        if (undoStack.size() > MAX_UNDO_HISTORY) {
            undoStack.removeElementAt(0); // Remove oldest state
        }
        
        // Update button states
        updateUndoRedoButtonStates();
    }

    private void handleUndo() {
        if (undoStack.isEmpty()) return;
        
        // Save current state to redo stack first
        CanvasState currentState = new CanvasState(gridView.getGridCellsSnapshot());
        redoStack.push(currentState);
        
        // Get previous state and restore it
        CanvasState previousState = undoStack.pop();
        restoreCanvasState(previousState);
        
        // Clear selection to avoid empty grid selections
        gridView.clearSelection();
        
        // Update button states
        updateUndoRedoButtonStates();
    }

    private void handleRedo() {
        if (redoStack.isEmpty()) return;
        
        // Save current state to undo stack first
        CanvasState currentState = new CanvasState(gridView.getGridCellsSnapshot());
        undoStack.push(currentState);
        
        // Get next state and restore it
        CanvasState nextState = redoStack.pop();
        restoreCanvasState(nextState);
        
        // Clear selection to avoid empty grid selections
        gridView.clearSelection();
        
        // Update button states
        updateUndoRedoButtonStates();
    }

    private void restoreCanvasState(CanvasState state) {
        if (gridView == null || state == null) return;
        
        // Clear current grid
        gridView.clearGrid();
        
        // Restore all cells from the saved state
        for (Map.Entry<String, GridCell> entry : state.getGridState().entrySet()) {
            String[] coords = entry.getKey().split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            GridCell savedCell = entry.getValue();
            GridCell targetCell = gridView.accessCell(x, y);
            
            // Copy all objects from saved cell to target cell
            for (GridObject obj : savedCell.getAllObjects()) {
                targetCell.addObject(new GridObject(obj)); // Create copy to avoid reference issues
            }
        }
        
        gridView.invalidate();
        updateObjectCountDisplay(); // Update count after restoring state
    }

    private void updateUndoRedoButtonStates() {
        ImageButton undoButton = findViewById(R.id.undoButton);
        ImageButton redoButton = findViewById(R.id.redoButton);
        
        if (undoButton != null) {
            boolean canUndo = !undoStack.isEmpty();
            undoButton.setEnabled(canUndo);
            undoButton.setAlpha(canUndo ? 1.0f : 0.4f);
        }
        
        if (redoButton != null) {
            boolean canRedo = !redoStack.isEmpty();
            redoButton.setEnabled(canRedo);
            redoButton.setAlpha(canRedo ? 1.0f : 0.4f);
        }
    }

    public void showPlayerLimitWarning() {
        Toast.makeText(this, "Only one player spawn is allowed per level", Toast.LENGTH_SHORT).show();
    }

    public void showObjectLimitWarning() {
        String status = gridView.getObjectCountStatus();
        Toast.makeText(this, "Object limit reached: " + status, Toast.LENGTH_SHORT).show();
    }

    private void updateObjectCountDisplay() {
        if (objectLimitText == null || gridView == null) return;
        
        // Only show object count when tint mode is enabled
        if (tintEnabled) {
            int currentCount = gridView.countTotalObjectsInLevel();
            int maxCount = LevelGridView.MAX_OBJECT_COUNT;
            
            // Update text with current count / max count
            @SuppressLint("DefaultLocale") String displayText = String.format("%,d", currentCount);
            objectLimitText.setText(displayText);
            
            // Change color based on count - bright red when >9000
            if (currentCount > 9000) {
                objectLimitText.setTextColor(Color.rgb(255, 0, 0)); // Bright red
            } else {
                objectLimitText.setTextColor(Color.rgb(72, 31, 25)); // Original brown color #481F19
            }
            
            objectLimitText.setVisibility(View.VISIBLE);
        } else {
            objectLimitText.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateSelectedItemsDisplay() {
        if (selectedItemsText != null && gridView != null) {
            // Only show selected items count when tint mode is enabled
            if (tintEnabled) {
                int selectedCount = gridView.getSelectedCellKeys().size();
                selectedItemsText.setText("Selected: " + selectedCount);
                selectedItemsText.setVisibility(View.VISIBLE);
            } else {
                selectedItemsText.setVisibility(View.GONE);
            }
        }
    }

    private LevelObject getLevelObjectType(GridObject gridObject) {
        SimplePhysicsObject simpleObj = gridObject.getSimpleObject();
        String className = simpleObj.getClass().getSimpleName();
        
        // Map SimplePhysicsObject classes to LevelObject types
        switch (className) {
            case "SimpleBlockCommon":
                return LevelObject.BLOCK;
            case "SimpleBlockIce":
                return LevelObject.ICE_BLOCK;
            case "SimpleBlockWater":
                return LevelObject.WATER_BLOCK;
            case "SimpleBlockSpike":
            case "SimpleBlockSpikeCommon":
                return LevelObject.SPIKE_BLOCK;
            case "SimpleBlockSpikeCommonDown":
                return LevelObject.SPIKE_BLOCK_DOWN;
            case "SimpleBlockSpikeCommonLeft":
                return LevelObject.SPIKE_BLOCK_LEFT;
            case "SimpleBlockSpikeCommonRight":
                return LevelObject.SPIKE_BLOCK_RIGHT;
            case "SimpleBlockSpikeIce":
                return LevelObject.ICE_SPIKE;
            case "SimpleBlockSpikeIceDown":
                return LevelObject.ICE_SPIKE_DOWN;
            case "SimpleBlockSpikeIceLeft":
                return LevelObject.ICE_SPIKE_LEFT;
            case "SimpleBlockSpikeIceRight":
                return LevelObject.ICE_SPIKE_RIGHT;
            case "SimplePortal":
                return LevelObject.PORTAL;
            case "SimplePlayer":
                return LevelObject.PLAYER_SPAWN;
            case "SimpleTriggerFade":
                return LevelObject.TRIGGER_FADE;
            case "SimpleTriggerInversion":
                return LevelObject.TRIGGER_INVERT;
            case "SimpleTriggerMovement":
                return LevelObject.TRIGGER_MOVEMENT;
            case "SimpleTriggerScale":
                return LevelObject.TRIGGER_SCALE;
            case "SimpleTriggerGravity":
                return LevelObject.TRIGGER_GRAVITY;
            case "SimpleTriggerTranslate":
                return LevelObject.TRIGGER_TRANSLATE;
            default:
                return null; // Unknown type
        }
    }

    private void copyObjectProperties(SimplePhysicsObject source, SimplePhysicsObject target) {
        // Copy common properties that all SimplePhysicsObjects have
        target.setWidthMultiplier(source.getWidthMultiplier());
        target.setHeightMultiplier(source.getHeightMultiplier());
        target.setVelX(source.getVelX());
        target.setVelY(source.getVelY());
        
        // Copy type-specific properties using reflection or explicit casting
        if (source instanceof SimplePlayer && target instanceof SimplePlayer) {
            SimplePlayer sourcePlayer = (SimplePlayer) source;
            SimplePlayer targetPlayer = (SimplePlayer) target;
            
            targetPlayer.setSpeedX(sourcePlayer.getSpeedX());
            targetPlayer.setSpeedY(sourcePlayer.getSpeedY());
            targetPlayer.setGravity(sourcePlayer.getGravity());
        } else if (source instanceof SimpleTrigger && target instanceof SimpleTrigger) {
            SimpleTrigger sourceTrigger = (SimpleTrigger) source;
            SimpleTrigger targetTrigger = (SimpleTrigger) target;
            
            // Copy common trigger properties
            targetTrigger.setDelay(sourceTrigger.getDelay());
            targetTrigger.setSpeed(sourceTrigger.getSpeed());
            targetTrigger.setWR1(sourceTrigger.getWR1());
            targetTrigger.setWR2(sourceTrigger.getWR2());
            targetTrigger.setHR1(sourceTrigger.getHR1());
            targetTrigger.setHR2(sourceTrigger.getHR2());
            
            // Copy trigger-specific properties based on type
            if (sourceTrigger instanceof SimpleTriggerFade && targetTrigger instanceof SimpleTriggerFade) {
                SimpleTriggerFade sourceFade = (SimpleTriggerFade) sourceTrigger;
                SimpleTriggerFade targetFade = (SimpleTriggerFade) targetTrigger;
                
                targetFade.setTargetOpacity(sourceFade.getTargetOpacity());
                targetFade.setRemoveCollisionAtZero(sourceFade.isRemoveCollisionAtZero());
                
            } else if (sourceTrigger instanceof SimpleTriggerGravity && targetTrigger instanceof SimpleTriggerGravity) {
                SimpleTriggerGravity sourceGravity = (SimpleTriggerGravity) sourceTrigger;
                SimpleTriggerGravity targetGravity = (SimpleTriggerGravity) targetTrigger;
                
                targetGravity.setGravityStrength(sourceGravity.getGravityStrength());
                targetGravity.setGravityDirection(sourceGravity.getGravityDirection());
                
            } else if (sourceTrigger instanceof SimpleTriggerMovement && targetTrigger instanceof SimpleTriggerMovement) {
                SimpleTriggerMovement sourceMovement = (SimpleTriggerMovement) sourceTrigger;
                SimpleTriggerMovement targetMovement = (SimpleTriggerMovement) targetTrigger;
                
                targetMovement.setXSpeed(sourceMovement.getXSpeed());
                targetMovement.setYSpeed(sourceMovement.getYSpeed());
                targetMovement.setGravityIntensity(sourceMovement.getGravityIntensity());
                
            } else if (sourceTrigger instanceof SimpleTriggerScale && targetTrigger instanceof SimpleTriggerScale) {
                SimpleTriggerScale sourceScale = (SimpleTriggerScale) sourceTrigger;
                SimpleTriggerScale targetScale = (SimpleTriggerScale) targetTrigger;
                
                            targetScale.setTargetHeight(sourceScale.getTargetHeight());
            targetScale.setTargetWidth(sourceScale.getTargetWidth());
                
            } else if (sourceTrigger instanceof SimpleTriggerTranslate && targetTrigger instanceof SimpleTriggerTranslate) {
                SimpleTriggerTranslate sourceTranslate = (SimpleTriggerTranslate) sourceTrigger;
                SimpleTriggerTranslate targetTranslate = (SimpleTriggerTranslate) targetTrigger;
                
                targetTranslate.setDirection(sourceTranslate.getDirection());
                targetTranslate.setBlockCount(sourceTranslate.getBlockCount());
            }
        }
    }

    public static View.OnTouchListener createButtonTapFeedback() {
        return new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ScaleAnimation scaleDown = new ScaleAnimation(
                            1.0f, 0.9f, // X scale from 100% to 90%
                            1.0f, 0.9f, // Y scale from 100% to 90%
                            Animation.RELATIVE_TO_SELF, 0.5f, // Pivot X at center
                            Animation.RELATIVE_TO_SELF, 0.5f  // Pivot Y at center
                        );
                        scaleDown.setDuration(100); // Quick animation
                        scaleDown.setFillAfter(true); // Keep the scale
                        v.startAnimation(scaleDown);
                        break;
                        
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Scale back to normal when released
                        ScaleAnimation scaleUp = new ScaleAnimation(
                            0.9f, 1.0f, // X scale from 90% back to 100%
                            0.9f, 1.0f, // Y scale from 90% back to 100%
                            Animation.RELATIVE_TO_SELF, 0.5f, // Pivot X at center
                            Animation.RELATIVE_TO_SELF, 0.5f  // Pivot Y at center
                        );
                        scaleUp.setDuration(100); // Quick animation
                        scaleUp.setFillAfter(true); // Keep the scale
                        v.startAnimation(scaleUp);
                        break;
                }
                return false;
            }
        };
    }

    private void showCopyPasteButtons() {
        if (copyButtonContainer != null) {
            copyButtonContainer.setVisibility(View.VISIBLE);
        }
        if (pasteButtonContainer != null) {
            pasteButtonContainer.setVisibility(View.VISIBLE);
        }
    }

    private void hideCopyPasteButtons() {
        if (copyButtonContainer != null) {
            copyButtonContainer.setVisibility(View.GONE);
        }
        if (pasteButtonContainer != null) {
            pasteButtonContainer.setVisibility(View.GONE);
        }
    }

    private void setPasteButtonState(boolean enabled) {
        ImageButton pasteButton = findViewById(R.id.btnPaste);
        if (pasteButton != null) {
            pasteButton.setEnabled(enabled);
            pasteButton.setAlpha(enabled ? 1.0f : 0.4f);
        }
    }

    public LevelGridView getGridView() {
        return gridView;
    }

    private void showExitConfirmationDialog() {
        UniversalDialogAnimator.AnimatedDialog animatedDialog = UniversalDialogAnimator.showConfirmationDialog(
            this,
            "Dismiss changes and move back to Level View?",
            null, // No additional message
            () -> {
                // Yes button clicked - navigate back
                Intent intent = new Intent(this, CreatorLevelViewActivity.class);
                intent.putExtra("level_uid", currentLevelUid);
                intent.putExtra("is_private", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            },
            () -> {
                // No button clicked - just dismiss (handled automatically)
            }
        );
        
        // Show the animated dialog
        animatedDialog.show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showProgress(String initialText) {
        if (progressLayout == null || progressText == null || progressBar == null) {
            return;
        }
        
        // Set initial text
        progressText.setText(initialText);
        
        // Show the progress layout
        progressLayout.setVisibility(View.VISIBLE);
        progressLayout.setAlpha(0f);
        
        // Make the progress layout capture ALL touch events
        progressLayout.setClickable(true);
        progressLayout.setFocusable(true);
        progressLayout.setOnTouchListener((v, event) -> true);
        
        // Animate fade-in
        progressLayout.animate()
            .alpha(1.0f)
            .setDuration(300)
            .start();
    }

    private void hideProgress() {
        if (progressLayout == null) {
            return;
        }
        
        // Animate fade-out
        progressLayout.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction(() -> {
                progressLayout.setVisibility(View.GONE);
                // Remove the touch listener that was blocking interactions
                progressLayout.setOnTouchListener(null);
                progressLayout.setClickable(false);
                progressLayout.setFocusable(false);
            })
            .start();
    }

    private void updateProgressText(String newText) {
        if (progressText != null) {
            progressText.setText(newText);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showExitConfirmationDialog();
    }
} 