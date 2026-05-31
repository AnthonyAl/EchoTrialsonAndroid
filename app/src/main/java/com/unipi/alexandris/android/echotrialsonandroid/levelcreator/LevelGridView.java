package com.unipi.alexandris.android.echotrialsonandroid.levelcreator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Custom view that displays and handles interaction with the level creation grid.
 * This view provides a grid-based interface for placing, manipulating, and visualizing
 * level objects across multiple layered groups.
 * <br>
 * Features include:
 * - Infinite panning and zooming
 * - Group-based object layering
 * - Support for multiple object types
 * - Grid line visualization
 * - Object textures with optional color tinting
 * - Smooth scrolling and scaling
 * - Long-press properties editing
 * - Slide mode for continuous object placement
 */
public class LevelGridView extends View {

    private static final int DEFAULT_CELL_SIZE = 48;
    public static final int MAX_OBJECT_COUNT = 10000;
    private static final int VISIBLE_CELLS_PADDING = 2;
    private final Paint gridPaint;
    private double cameraMinHeight = -1;
    private double cameraMaxHeight = -1;
    private final Paint highlightPaint;
    private final Map<String, GridCell> gridCells = new HashMap<>();
    public interface GridModificationCallback {
        void onGridModified();
    }
    public interface SelectionChangeCallback {
        void onSelectionChanged();
    }
    public interface ObjectCountUpdateCallback {
        void onObjectCountChanged();
    }
    private GridModificationCallback modificationCallback;
    private SelectionChangeCallback selectionChangeCallback;
    private ObjectCountUpdateCallback objectCountUpdateCallback;
    private boolean isSwipeInProgress = false;
    private float scale = 1.0f;
    private float translateX = 0f;
    private float translateY = 0f;
    private LevelObject selectedObject = null;
    private final GestureDetector gestureDetector;

    private final ScaleGestureDetector scaleDetector;
    private int longPressX = -1;
    private int longPressY = -1;
    private boolean multiSelectMode = false;
    private boolean isSelecting = false;
    private int selectStartGridX = -1;
    private int selectStartGridY = -1;
    private int selectEndGridX = -1;
    private int selectEndGridY = -1;
    private final Set<String> selectedCells = new HashSet<>();
    private boolean movingSelection = false;
    private int moveStartGridX = -1;
    private int moveStartGridY = -1;
    private int moveDeltaGridX = 0;
    private int moveDeltaGridY = 0;
	private boolean selectionMoveCandidate = false;
    private final StringBuilder keyBuilder = new StringBuilder(10);
    private final Bitmap backgroundTexture;
    private final Map<LevelObject, Bitmap> objectTextures = new HashMap<>();
    private int lastTouchX = -1;
    private int lastTouchY = -1;
    private boolean playerPlaced = false;
    private boolean slideMode = false;
    private boolean showColors = false;
    private int currentGroup = -1;
    private static final float INACTIVE_GROUP_ALPHA = 0.3f;
    private boolean snapToGridEnabled = true;
    private GridObject draggedObject = null;
    private int[] draggedObjectOriginalPosition = null;
    private boolean isDragging = false;
    private static final int DRAG_THRESHOLD = 20;
    private float selectionDownX = 0f;
    private float selectionDownY = 0f;
    private int downGridX = -1;
    private int downGridY = -1;
    
    /**
     * Constructs a new LevelGridView.
     * Initializes all required components, loads textures, and sets up gesture handling.
     *
     * @param context The application context
     * @param attrs Attribute set from XML layout file
     */
    public LevelGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        gridPaint = new Paint();
        gridPaint.setColor(Color.GRAY);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1);

        Paint cellPaint = new Paint();
        cellPaint.setStyle(Paint.Style.FILL);
        
        highlightPaint = new Paint();
        highlightPaint.setColor(Color.YELLOW);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(3);

        gestureDetector = new GestureDetector(context, new GestureListener());
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        
        setFocusable(true);

        backgroundTexture = BitmapFactory.decodeResource(getResources(), R.drawable.bgrnd5);

        loadObjectTextures();
    }

    private String getCellKey(int x, int y) {
        return x + "," + y;
    }

    private GridCell getCell(int x, int y) {
        String key = getCellKey(x, y);
        GridCell cell = gridCells.get(key);
        if (cell == null) {
            cell = new GridCell();
            gridCells.put(key, cell);
        }
        return cell;
    }

    private void loadObjectTextures() {
        for (LevelObject obj : LevelObject.values()) {
            int resourceId = obj.getTextureResourceId();
            if (resourceId != 0) {
                Bitmap texture = BitmapFactory.decodeResource(getResources(), resourceId);
                objectTextures.put(obj, texture);
            }
        }
    }

    @Override
    @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        canvas.save();
        
        // Apply transformations - simplified to avoid edge artifacts
        canvas.translate(translateX, translateY);
        canvas.scale(scale, scale);
        
        // Calculate visible bounds first
        float viewLeft = -translateX / scale;
        float viewTop = -translateY / scale;
        float viewRight = (getWidth() / scale) + viewLeft;
        float viewBottom = (getHeight() / scale) + viewTop;
        
        // BACKGROUND COLOR - replaced with texture
        if (backgroundTexture != null) {
            // Create a shader for tiling the background
            BitmapShader shader = new BitmapShader(
                backgroundTexture, 
                Shader.TileMode.REPEAT, 
                Shader.TileMode.REPEAT
            );
            Paint bgPaint = new Paint();
            bgPaint.setShader(shader);
            
            // Draw the tiled background
            canvas.drawRect(
                viewLeft, viewTop,
                viewRight, viewBottom,
                bgPaint
            );
        } else {
            // Fallback color if texture loading fails
            canvas.drawColor(Color.rgb(255, 254, 207));
        }
        
        // Calculate grid bounds
        int startX = (int)Math.floor(viewLeft / DEFAULT_CELL_SIZE) - VISIBLE_CELLS_PADDING;
        int startY = (int)Math.floor(viewTop / DEFAULT_CELL_SIZE) - VISIBLE_CELLS_PADDING;
        int endX = (int)Math.ceil(viewRight / DEFAULT_CELL_SIZE) + VISIBLE_CELLS_PADDING;
        int endY = (int)Math.ceil(viewBottom / DEFAULT_CELL_SIZE) + VISIBLE_CELLS_PADDING;
        
        // Draw cells
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                float left = x * DEFAULT_CELL_SIZE;
                float top = y * DEFAULT_CELL_SIZE;
                
                GridCell cell = getCell(x, y);
                Collection<GridObject> objects = cell.getAllObjects();
                
                // Sort objects by group ID (higher numbers drawn first - lower numbers on top, matching runtime)
                List<GridObject> sortedObjects = new ArrayList<>(objects);
                sortedObjects.sort((o1, o2) -> Integer.compare(o2.getGroupId(), o1.getGroupId()));
                
                for (GridObject obj : sortedObjects) {
                    boolean isActiveGroup = currentGroup == -1 || obj.getGroupId() == currentGroup;
                    float alpha = isActiveGroup ? 1.0f : INACTIVE_GROUP_ALPHA;
                    
                    // Draw texture
                    Paint texturePaint = new Paint();
                    texturePaint.setAlpha((int)(255 * alpha));
                    Bitmap texture = objectTextures.get(obj.getType());
                    if (texture != null) {
                        canvas.drawBitmap(texture, null,
                            new RectF(left, top, left + DEFAULT_CELL_SIZE, top + DEFAULT_CELL_SIZE),
                            texturePaint);
                        
                        // Draw color tint only if enabled
                        if (showColors) {
                            Paint tintPaint = new Paint();
                            tintPaint.setColor(obj.getColorForGroup());
                            tintPaint.setAlpha((int)(100 * alpha));
                            canvas.drawRect(left, top, left + DEFAULT_CELL_SIZE, top + DEFAULT_CELL_SIZE, tintPaint);
                        }
                    }
                    
                    // Draw trigger bounds outline if this is a trigger object and tint mode is enabled
                    if (obj.getSimpleObject() instanceof SimpleTrigger && showColors) {
                        SimpleTrigger trigger =
                            (SimpleTrigger) obj.getSimpleObject();
                        
                        // Calculate trigger bounds in world coordinates
                        float triggerX = (float) trigger.getX();
                        float triggerY = (float) trigger.getY();
                        float triggerSize = (float) trigger.getSize();
                        
                        // Calculate expanded bounds based on WR1/WR2/HR1/HR2
                        float boundsLeft = triggerX - (trigger.getWR1() * triggerSize);
                        float boundsRight = triggerX + triggerSize + (trigger.getWR2() * triggerSize);
                        float boundsTop = triggerY - (trigger.getHR1() * triggerSize);
                        float boundsBottom = triggerY + triggerSize + (trigger.getHR2() * triggerSize);
                        
                        // Draw thick outline using trigger's color
                        Paint boundsPaint = new Paint();
                        boundsPaint.setColor(obj.getColorForGroup());
                        boundsPaint.setStyle(Paint.Style.STROKE);
                        boundsPaint.setStrokeWidth(6f); // Thicker outline
                        boundsPaint.setAlpha((int)(200 * alpha)); // Semi-transparent
                        
                        canvas.drawRect(boundsLeft, boundsTop, boundsRight, boundsBottom, boundsPaint);
                        
                    }
                }
                
                // Draw grid lines
                canvas.drawRect(left, top, left + DEFAULT_CELL_SIZE, top + DEFAULT_CELL_SIZE, gridPaint);
                
                // Draw highlight: single-selection or multi-selected cells
                if (!selectedCells.isEmpty()) {
                    // OPTIMIZED: Use cached StringBuilder for key creation
                    keyBuilder.setLength(0);
                    keyBuilder.append(x).append(",").append(y);
                    if (selectedCells.contains(keyBuilder.toString())) {
                        canvas.drawRect(left, top, left + DEFAULT_CELL_SIZE, top + DEFAULT_CELL_SIZE, highlightPaint);
                    }
                } else if (x == longPressX && y == longPressY) {
                    canvas.drawRect(left, top, left + DEFAULT_CELL_SIZE, top + DEFAULT_CELL_SIZE, highlightPaint);
                }
            }
        }
        
        // Draw camera height limit lines if set
        if (cameraMinHeight >= 0 && cameraMaxHeight >= 0) {
            @SuppressLint("DrawAllocation") Paint limitPaint = new Paint();
            limitPaint.setColor(Color.RED);
            limitPaint.setStrokeWidth(3f);
            limitPaint.setStyle(Paint.Style.STROKE);
            
            // Convert world coordinates to screen coordinates
            float minY = (float)cameraMinHeight;
            float maxY = (float)cameraMaxHeight;
            
            // Draw both lines in red - infinite width
            canvas.drawLine(-getWidth() * 10, minY, getWidth() * 10, minY, limitPaint);
            canvas.drawLine(-getWidth() * 10, maxY, getWidth() * 10, maxY, limitPaint);
            
            // Add black overlay for out-of-bounds areas
            Paint overlayPaint = new Paint();
            overlayPaint.setColor(Color.BLACK);
            overlayPaint.setAlpha(80); // Semi-transparent black
            
            // Draw overlay above max height (top area)
            canvas.drawRect(-getWidth() * 10, -getHeight() * 10, getWidth() * 10, maxY, overlayPaint);
            
            // Draw overlay below min height (bottom area)
            canvas.drawRect(-getWidth() * 10, minY, getWidth() * 10, getHeight() * 10, overlayPaint);
            
            // Add labels - position them outside the grid area to avoid interference
            Paint textPaint = new Paint();
            textPaint.setColor(Color.RED);
            textPaint.setTextSize(20f);
            textPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("MIN HEIGHT", -getWidth() * 9, minY - 10, textPaint);
            canvas.drawText("MAX HEIGHT", -getWidth() * 9, maxY - 10, textPaint);
        }
        
        // Draw selection rectangle overlay while selecting
        if (multiSelectMode && isSelecting) {
            int minX = Math.min(selectStartGridX, selectEndGridX);
            int minY = Math.min(selectStartGridY, selectEndGridY);
            int maxX = Math.max(selectStartGridX, selectEndGridX);
            int maxY = Math.max(selectStartGridY, selectEndGridY);
            Paint rectPaint = new Paint(highlightPaint);
            rectPaint.setAlpha(120);
            float left = minX * DEFAULT_CELL_SIZE;
            float top = minY * DEFAULT_CELL_SIZE;
            float right = (maxX + 1) * DEFAULT_CELL_SIZE;
            float bottom = (maxY + 1) * DEFAULT_CELL_SIZE;
            canvas.drawRect(left, top, right, bottom, rectPaint);
        }
        
        // Draw ghost trail for moving objects
        if (multiSelectMode && movingSelection && (moveDeltaGridX != 0 || moveDeltaGridY != 0)) {
            Paint ghostPaint = new Paint();
            ghostPaint.setAlpha(128); // 50% transparency
            
            // Draw ghost objects at their new positions
            for (String selectedKey : selectedCells) {
                String[] coords = selectedKey.split(",");
                int origX = Integer.parseInt(coords[0]);
                int origY = Integer.parseInt(coords[1]);
                
                // Calculate new position
                int newX = origX + moveDeltaGridX;
                int newY = origY + moveDeltaGridY;
                
                float ghostLeft = newX * DEFAULT_CELL_SIZE;
                float ghostTop = newY * DEFAULT_CELL_SIZE;
                
                GridCell originalCell = getCell(origX, origY);
                Collection<GridObject> objectsToMove;
                
                if (currentGroup == -1) {
                    // ALL mode: move all objects from the cell
                    objectsToMove = originalCell.getAllObjects();
                } else {
                    // Specific group: move only that group's object
                    GridObject specificObj = originalCell.getObject(currentGroup);
                    objectsToMove = (specificObj != null) ? 
                        Collections.singletonList(specificObj) : 
                        Collections.emptyList();
                }
                
                // Sort objects by group ID (same as main rendering)
                List<GridObject> sortedGhostObjects = new ArrayList<>(objectsToMove);
                sortedGhostObjects.sort((o1, o2) -> Integer.compare(o2.getGroupId(), o1.getGroupId()));
                
                for (GridObject obj : sortedGhostObjects) {
                    // Draw ghost texture
                    Bitmap texture = objectTextures.get(obj.getType());
                    if (texture != null) {
                        canvas.drawBitmap(texture, null,
                            new RectF(ghostLeft, ghostTop, ghostLeft + DEFAULT_CELL_SIZE, ghostTop + DEFAULT_CELL_SIZE),
                            ghostPaint);
                        
                        // Draw ghost color tint if enabled
                        if (showColors) {
                            Paint ghostTintPaint = new Paint();
                            ghostTintPaint.setColor(obj.getColorForGroup());
                            ghostTintPaint.setAlpha(50); // Even more transparent for tint
                            canvas.drawRect(ghostLeft, ghostTop, ghostLeft + DEFAULT_CELL_SIZE, ghostTop + DEFAULT_CELL_SIZE, ghostTintPaint);
                        }
                    }
                }
            }
        }

        // Draw dragged object if any
        if (isDragging && draggedObject != null) {
            float x = (float)draggedObject.getSimpleObject().getX();
            float y = (float)draggedObject.getSimpleObject().getY();
            float size = DEFAULT_CELL_SIZE;
            
            // Draw with semi-transparency to show it's being dragged
            Paint dragPaint = new Paint();
            dragPaint.setAlpha(180);
            
            Bitmap texture = objectTextures.get(draggedObject.getType());
            if (texture != null) {
                canvas.drawBitmap(texture, null,
                    new RectF(x, y, x + size, y + size),
                    dragPaint);
                
                // Draw color tint if enabled
                if (showColors) {
                    Paint tintPaint = new Paint();
                    tintPaint.setColor(draggedObject.getColorForGroup());
                    tintPaint.setAlpha(60);
                    canvas.drawRect(x, y, x + size, y + size, tintPaint);
                }
            }
            
            // Draw drag indicator
            Paint indicatorPaint = new Paint();
            indicatorPaint.setColor(Color.YELLOW);
            indicatorPaint.setStrokeWidth(3f);
            indicatorPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(x, y, x + size, y + size, indicatorPaint);
        }
        
        canvas.restore();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Mass-move handling in selection mode
        if (multiSelectMode && movingSelection) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE: {
                    int[] grid = screenToGrid(event.getX(), event.getY());
                    moveDeltaGridX = grid[0] - moveStartGridX;
                    moveDeltaGridY = grid[1] - moveStartGridY;
                    invalidate();
                    return true;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    // Apply movement
                    if (moveDeltaGridX != 0 || moveDeltaGridY != 0) {
                        moveSelectionBy(moveDeltaGridX, moveDeltaGridY);
                    }
                    movingSelection = false;
                    moveStartGridX = moveStartGridY = -1;
                    moveDeltaGridX = moveDeltaGridY = 0;
                    selectionMoveCandidate = false;
                    invalidate();
                    return true;
                }
            }
        }

        // If multi-select mode is enabled, use selection gestures when not moving selection
        if (multiSelectMode) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    int[] grid = screenToGrid(event.getX(), event.getY());
                    downGridX = grid[0];
                    downGridY = grid[1];
                    selectionDownX = event.getX();
                    selectionDownY = event.getY();
                    String key = grid[0] + "," + grid[1];
                    
                    // Prevent selection of cells that are too far outside the visible area
                    // This prevents the X=0 selection bug when grid is panned
                    float viewLeft = -translateX / scale;
                    float viewRight = (getWidth() / scale) + viewLeft;
                    float cellLeft = grid[0] * DEFAULT_CELL_SIZE;
                    float cellRight = (grid[0] + 1) * DEFAULT_CELL_SIZE;
                    
                    // Only allow selection if the cell is within or near the visible area
                    // Add extra padding to prevent edge cases
                    float padding = DEFAULT_CELL_SIZE * 3;
                    if (cellRight < viewLeft - padding || cellLeft > viewRight + padding) {
                        return false; // Ignore touches outside visible area
                    }
                    
                    // If tapping on a selected cell, mark as candidate for movement
                    if (selectedCells.contains(key)) {
                        selectionMoveCandidate = true;
                        isSelecting = false;
                        break; // Let gesture detector handle this for long press
                    } else {
                        // Tapping on unselected cell - immediately select it
                        selectedCells.clear();
                        toggleCellSelection(grid[0], grid[1]);
                        selectionMoveCandidate = false;
                        isSelecting = false;
                        invalidate();
                        return true; // Don't let gesture detector handle this
                    }
                }
                case MotionEvent.ACTION_MOVE: {
                    // Only start rectangle selection if we're not a movement candidate
                    if (!selectionMoveCandidate && !isSelecting) {
                        float dx = Math.abs(event.getX() - selectionDownX);
                        float dy = Math.abs(event.getY() - selectionDownY);
                        if (dx > DRAG_THRESHOLD || dy > DRAG_THRESHOLD) {
                            // Start rectangle selection
                            isSelecting = true;
                            selectStartGridX = downGridX;
                            selectStartGridY = downGridY;
                            int[] grid = screenToGrid(event.getX(), event.getY());
                            selectEndGridX = grid[0];
                            selectEndGridY = grid[1];
                            updateSelectedCells();
                            invalidate();
                        }
                    } else if (isSelecting) {
                        int[] grid = screenToGrid(event.getX(), event.getY());
                        selectEndGridX = grid[0];
                        selectEndGridY = grid[1];
                        updateSelectedCells();
                        invalidate();
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    boolean wasSelecting = isSelecting;
                    isSelecting = false;
                    if (wasSelecting) {
                        // Rectangle selection complete - filter to only include cells with objects
                        // (Single tap selection allows empty cells, but multi-selection filters them)
                        filterSelectedCellsToObjectsOnly();
                    }
                    selectionMoveCandidate = false;
                    downGridX = downGridY = -1;
                    invalidate();
                    return true;
                }
            }
        }

        // Handle drag and drop first
        if (isDragging) {
            return handleDragEvent(event);
        }
        
        // Handle slide mode
        if (slideMode) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Start of swipe - save state once
                    if (!isSwipeInProgress && modificationCallback != null) {
                        modificationCallback.onGridModified();
                    }
                    isSwipeInProgress = true;
                    placeObjectAtTouch(event.getX(), event.getY());
                    return true;
                case MotionEvent.ACTION_MOVE:
                    placeObjectAtTouch(event.getX(), event.getY());
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // End of swipe - trigger callback to update object count display
                    isSwipeInProgress = false;
                    if (modificationCallback != null) {
                        modificationCallback.onGridModified();
                    }
                    return true;
            }
            return super.onTouchEvent(event);
        }
        
        // Handle normal mode with gesture detection
        boolean scaleHandled = scaleDetector.onTouchEvent(event);
        boolean gestureHandled = gestureDetector.onTouchEvent(event);
        return scaleHandled || gestureHandled || super.onTouchEvent(event);
    }

    private boolean handleDragEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                // Update drag position
                int[] gridCoords = screenToGrid(event.getX(), event.getY());
                // Always snap to grid (free positioning removed)
                draggedObject.getSimpleObject().setX(gridCoords[0] * DEFAULT_CELL_SIZE);
                draggedObject.getSimpleObject().setY(gridCoords[1] * DEFAULT_CELL_SIZE);
                // Update selection outline to follow the dragged object
                longPressX = gridCoords[0];
                longPressY = gridCoords[1];
                invalidate();
                return true;
                
            case MotionEvent.ACTION_UP:
                // End drag
                if (draggedObject != null && draggedObjectOriginalPosition != null) {
                    // Notify callback to save state BEFORE modification (for undo/redo)
                    if (modificationCallback != null) {
                        modificationCallback.onGridModified();
                    }
                    
                    // Update the grid cell structure
                    GridCell originalCell = getCell(draggedObjectOriginalPosition[0], draggedObjectOriginalPosition[1]);
                    if(draggedObject.getType() == LevelObject.PLAYER_SPAWN) {
                        playerPlaced = false;
                    }
                    originalCell.removeObject(draggedObject.getGroupId());
                    
                    int[] newGridCoords = screenToGrid(event.getX(), event.getY());
                    GridCell newCell = getCell(newGridCoords[0], newGridCoords[1]);
                    newCell.addObject(draggedObject);
                    
                    // Keep properties button visible for the dropped object
                    showPropertiesButton(draggedObject);
                    // Keep the selection highlight at the new cell
                    longPressX = newGridCoords[0];
                    longPressY = newGridCoords[1];
                    
                    // Reset drag state
                    draggedObject = null;
                    draggedObjectOriginalPosition = null;
                    isDragging = false;
                    
                    // Notify callback to update object count display AFTER modification
                    if (objectCountUpdateCallback != null) {
                        objectCountUpdateCallback.onObjectCountChanged();
                    }
                }
                return true;
                
            case MotionEvent.ACTION_CANCEL:
                // Cancel drag and restore original position
                if (draggedObject != null && draggedObjectOriginalPosition != null) {
                    draggedObject.getSimpleObject().setX(draggedObjectOriginalPosition[0] * DEFAULT_CELL_SIZE);
                    draggedObject.getSimpleObject().setY(draggedObjectOriginalPosition[1] * DEFAULT_CELL_SIZE);
                    draggedObject = null;
                    draggedObjectOriginalPosition = null;
                    isDragging = false;
                    hidePropertiesButton();
                }
                return true;
        }
        return false;
    }

    private void placeObjectAtTouch(float x, float y) {
        int[] gridCoords = screenToGrid(x, y);
        if (gridCoords[0] != lastTouchX || gridCoords[1] != lastTouchY) {
            placeObject(gridCoords[0], gridCoords[1]);
            lastTouchX = gridCoords[0];
            lastTouchY = gridCoords[1];
        }
    }

    public void setSelectedObject(LevelObject object) {
        this.selectedObject = object;
    }

    public LevelObject getSelectedObject() {
        return this.selectedObject;
    }

    private int[] screenToGrid(float screenX, float screenY) {
        // Convert screen coordinates to world coordinates
        float worldX = (screenX - translateX) / scale;
        float worldY = (screenY - translateY) / scale;
        
        // Convert world coordinates to grid coordinates
        int gridX = (int)Math.floor(worldX / DEFAULT_CELL_SIZE);
        int gridY = (int)Math.floor(worldY / DEFAULT_CELL_SIZE);
        
        return new int[]{gridX, gridY};
    }

    private void placeObject(int gridX, int gridY) {
        // Don't allow placement when in selection mode
        if (multiSelectMode) {
            return;
        }
        
        // Notify callback to save state BEFORE modification (for undo/redo)
        if (modificationCallback != null && !isSwipeInProgress) {
            modificationCallback.onGridModified();
        }
        
        GridCell cell = getCell(gridX, gridY);
        if (selectedObject == null) {
            // Delete mode - only delete from current group
            if (currentGroup == -1) {
                // In ALL mode, delete the top object
                GridObject topObject = cell.getTopObject();
                if (topObject != null) {
                    cell.removeObject(topObject.getGroupId());
                }
            } else {
                cell.removeObject(currentGroup);
            }
        } else {
            // Check for player limit before placing
            if (selectedObject == LevelObject.PLAYER_SPAWN) {
                int playerCount = countPlayersInLevel();
                if (playerCount >= 1) {
                    // Notify the activity about the player limit
                    if (getContext() instanceof LevelCreatorActivity) {
                        ((LevelCreatorActivity) getContext()).showPlayerLimitWarning();
                    }
                    return; // Don't place the player
                }
            }
            
            // Check for object count limit before placing
            if (wouldExceedObjectLimit(1)) {
                // Notify the activity about the object limit
                if (getContext() instanceof LevelCreatorActivity) {
                    ((LevelCreatorActivity) getContext()).showObjectLimitWarning();
                }
                return; // Don't place the object
            }
            
            // Place mode - create SimpleModel instance with group ID
            int groupId = (currentGroup == -1) ? 0 : currentGroup;
            SimplePhysicsObject simpleObject = SimpleObjectFactory.createSimpleObject(selectedObject, gridX, gridY, DEFAULT_CELL_SIZE, groupId);
            GridObject newObject = new GridObject(simpleObject);
            newObject.setGroupId(groupId);
            cell.addObject(newObject);
        }
        
        // Notify callback to update object count display AFTER modification
        if (objectCountUpdateCallback != null) {
            objectCountUpdateCallback.onObjectCountChanged();
        }
        
        invalidate();
    }

    public int countPlayersInLevel() {
        int playerCount = 0;
        // Iterate through all existing grid cells (grid is infinite via HashMap)
        for (GridCell cell : gridCells.values()) {
            for (GridObject obj : cell.getAllObjects()) {
                if (obj.getSimpleObject() instanceof SimplePlayer) {
                    playerCount++;
                }
            }
        }
        return playerCount;
    }

    public int countTotalObjectsInLevel() {
        int totalCount = 0;
        // Iterate through all existing grid cells (grid is infinite via HashMap)
        for (GridCell cell : gridCells.values()) {
            totalCount += cell.getAllObjects().size();
        }
        return totalCount;
    }

    public boolean wouldExceedObjectLimit(int objectCount) {
        return countTotalObjectsInLevel() + objectCount > MAX_OBJECT_COUNT;
    }

    @SuppressLint("DefaultLocale")
    public String getObjectCountStatus() {
        int currentCount = countTotalObjectsInLevel();
        return String.format("%,d / %,d", currentCount, MAX_OBJECT_COUNT);
    }

    private void clearHighlight() {
        if (longPressX != -1 || longPressY != -1) {
            longPressX = -1;
            longPressY = -1;
            invalidate();
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            translateX -= distanceX;
            translateY -= distanceY;
            invalidate();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (multiSelectMode) {
                // Selection is handled in onTouchEvent for immediate feedback
                return true;
            } else {
                // In non-select mode, do placement/delete
                int[] gridCoords = screenToGrid(e.getX(), e.getY());
                placeObject(gridCoords[0], gridCoords[1]);
                clearHighlight();
                return true;
            }
        }

        @Override
        public void onLongPress(MotionEvent e) {
            int[] gridCoords = screenToGrid(e.getX(), e.getY());
            if (multiSelectMode) {
                String key = gridCoords[0] + "," + gridCoords[1];
                if (selectedCells.contains(key)) {
                    // Begin moving the current selection
                    movingSelection = true;
                    moveStartGridX = gridCoords[0];
                    moveStartGridY = gridCoords[1];
                    moveDeltaGridX = moveDeltaGridY = 0;
                    invalidate();
                }
                return;
            }
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float lastFocusX;
        private float lastFocusY;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            lastFocusX = detector.getFocusX();
            lastFocusY = detector.getFocusY();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float oldScale = scale;
            
            // Update scale with limits
            scale *= detector.getScaleFactor();
            scale = Math.max(0.5f, Math.min(scale, 5.0f));
            
            // Adjust translation to keep the focus point in the same place
            if (scale != oldScale) {
                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();
                
                // Calculate the focus point movement
                float focusDeltaX = focusX - lastFocusX;
                float focusDeltaY = focusY - lastFocusY;
                
                // Update translation to maintain focus point
                translateX += focusDeltaX - (focusX - translateX) * (scale / oldScale - 1);
                translateY += focusDeltaY - (focusY - translateY) * (scale / oldScale - 1);
                
                lastFocusX = focusX;
                lastFocusY = focusY;
            }
            
            invalidate();
            return true;
        }
    }

    public void setSlideMode(boolean enabled) {
        slideMode = enabled;
        invalidate();
    }

    public void setShowColors(boolean enabled) {
        showColors = enabled;
        invalidate();
    }

    public void setCurrentGroup(int group) {
        this.currentGroup = group;
        invalidate();
    }

    public Map<String, SimplePhysicsObject> getSimpleGridData() {
        Map<String, SimplePhysicsObject> gridData = new HashMap<>();

        int totalObjects = 0;
        for (Map.Entry<String, GridCell> entry : gridCells.entrySet()) {
            String key = entry.getKey();
            GridCell cell = entry.getValue();
            
            // Get ALL objects from the cell (not just the top object)
            Collection<GridObject> allObjects = cell.getAllObjects();
            for (GridObject gridObject : allObjects) {
                // Create unique key for each object: "x,y:groupId"
                String uniqueKey = key + ":" + gridObject.getGroupId();
                gridData.put(uniqueKey, gridObject.getSimpleObject());
                totalObjects++;
                

            }
        }
        

        return gridData;
    }

    public int[] getPlayerSpawnPosition() {
        for (Map.Entry<String, GridCell> entry : gridCells.entrySet()) {
            String key = entry.getKey();
            GridCell cell = entry.getValue();
            
            // Check all objects in the cell for PLAYER_SPAWN
            for (GridObject obj : cell.getAllObjects()) {
                if (obj.getType() == LevelObject.PLAYER_SPAWN) {
                    // Parse the key "x,y" to get coordinates
                    String[] coords = key.split(",");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    public int getCellSize() {
        return DEFAULT_CELL_SIZE;
    }

    public void setCameraHeightLimits(double minHeight, double maxHeight) {
        // Convert world coordinates to grid coordinates for visualization
        this.cameraMinHeight = minHeight;
        this.cameraMaxHeight = maxHeight;
        invalidate();
    }

    public void clearGrid() {
        gridCells.clear();
        invalidate();
    }

    public Map<String, GridCell> getGridCellsSnapshot() {
        return gridCells;
    }

    public void setModificationCallback(GridModificationCallback callback) {
        this.modificationCallback = callback;
    }

    public void setSelectionChangeCallback(SelectionChangeCallback callback) {
        this.selectionChangeCallback = callback;
    }

    public void setObjectCountUpdateCallback(ObjectCountUpdateCallback callback) {
        this.objectCountUpdateCallback = callback;
    }

    public void addObjectToGrid(int gridX, int gridY, SimplePhysicsObject simpleObject) {
        GridCell cell = getCell(gridX, gridY);
        GridObject gridObject = new GridObject(simpleObject);
        gridObject.setGroupId(simpleObject.getGroupId());
        cell.addObject(gridObject);
        invalidate();
    }

    public boolean changeObjectGroup(int gridX, int gridY, int oldGroupId, int newGroupId) {
        GridCell cell = getCell(gridX, gridY);
        return cell.changeObjectGroup(oldGroupId, newGroupId);
    }

    public void setCellSize(int cellSize) {
        // Note: This is a placeholder for future implementation
        // Currently the cell size is fixed at DEFAULT_CELL_SIZE
        // This method can be expanded to support dynamic cell sizes
    }

    public void setSnapToGrid(boolean enabled) {
        // Snap-to-grid is now always on. Keep API for compatibility.
        snapToGridEnabled = true;
    }

    public boolean toggleSlideMode() {
        // Don't allow slide mode when in selection mode
        if (multiSelectMode) {
            return false;
        }
        slideMode = !slideMode;
        return slideMode;
    }

    public boolean isSelectionModeActive() {
        return multiSelectMode;
    }
    public boolean isSlideModeActive() {
        return slideMode;
    }
    public boolean hasMultiSelection() {
        return multiSelectMode && !selectedCells.isEmpty();
    }
    public boolean isMultiSelectModeActive() {
        return multiSelectMode;
    }
    public boolean hasObjectsInSelection() {
        if (!hasMultiSelection()) return false;
        
        for (String key : selectedCells) {
            String[] coords = key.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            GridCell cell = getCell(x, y);
            
            if (currentGroup == -1) {
                // ALL mode: check if cell has any objects
                if (!cell.getAllObjects().isEmpty()) {
                    return true;
                }
            } else {
                // Specific group mode: check if cell has objects in current group
                if (cell.getObject(currentGroup) != null) {
                    return true;
                }
            }
        }
        return false; // No objects found in selection
    }

    public List<String> getSelectedCellKeys() {
        return new ArrayList<>(selectedCells);
    }

    public void clearSelection() {
        selectedCells.clear();
        invalidate();
        
        // Notify activity of selection change
        if (selectionChangeCallback != null) {
            selectionChangeCallback.onSelectionChanged();
        }
    }

    public void showPropertiesButton(GridObject gridObject) {
        if (getContext() instanceof LevelCreatorActivity) {
            ((LevelCreatorActivity) getContext()).showPropertiesButton(gridObject);
        }
    }

    public void hidePropertiesButton() {
        if (getContext() instanceof LevelCreatorActivity) {
            // Keep properties button visible but disabled when nothing selected
            ((LevelCreatorActivity) getContext()).hidePropertiesButton();
        }
    }

    private void startDrag(GridObject obj, int gridX, int gridY) {
        draggedObject = obj;
        draggedObjectOriginalPosition = new int[]{gridX, gridY};
        isDragging = true;
        
        // Show properties button for the dragged object
        showPropertiesButton(obj);
    }

    public boolean toggleMultiSelectMode() {
        multiSelectMode = !multiSelectMode;
        isSelecting = false;
        selectedCells.clear();
        
        // Disable slide mode when entering selection mode
        if (multiSelectMode) {
            slideMode = false;
        }
        
        invalidate();
        // Never hide properties button; activity manages enable/disable state
        
        // Notify activity of selection change
        if (selectionChangeCallback != null) {
            selectionChangeCallback.onSelectionChanged();
        }
        
        return multiSelectMode;
    }

    private void updateSelectedCells() {
        selectedCells.clear();
        if (selectStartGridX == -1 || selectStartGridY == -1 || selectEndGridX == -1 || selectEndGridY == -1) return;
        int minX = Math.min(selectStartGridX, selectEndGridX);
        int minY = Math.min(selectStartGridY, selectEndGridY);
        int maxX = Math.max(selectStartGridX, selectEndGridX);
        int maxY = Math.max(selectStartGridY, selectEndGridY);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                keyBuilder.setLength(0); // Reset for reuse
                keyBuilder.append(x).append(",").append(y);
                selectedCells.add(keyBuilder.toString());
            }
        }
        if (selectionChangeCallback != null) {
            selectionChangeCallback.onSelectionChanged();
        }
    }

    private void filterSelectedCellsToObjectsOnly() {
        selectedCells.removeIf(cellKey -> {
            String[] coords = cellKey.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            GridCell cell = getCell(x, y);
            
            if (currentGroup == -1) {
                return cell.getAllObjects().isEmpty();
            } else {
                return cell.getObject(currentGroup) == null;
            }
        });

        if (selectionChangeCallback != null) {
            selectionChangeCallback.onSelectionChanged();
        }
    }

    private void moveSelectionBy(int dx, int dy) {
        if (dx == 0 && dy == 0) return;

        if (modificationCallback != null) {
            modificationCallback.onGridModified();
        }

        List<String> originalKeys = new ArrayList<>(selectedCells);

        List<ObjectMoveInfo> moveInfos = new ArrayList<>();
        for (String key : originalKeys) {
            String[] coords = key.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            GridCell cell = getCell(x, y);
            
            Collection<GridObject> objectsToMove;
            if (currentGroup == -1) {
                objectsToMove = new ArrayList<>(cell.getAllObjects()); // Copy to avoid modification issues
            } else {
                GridObject obj = cell.getObject(currentGroup);
                objectsToMove = (obj != null) ? Collections.singletonList(obj) : Collections.emptyList();
            }
            
            for (GridObject obj : objectsToMove) {
                int oldGX = (int) Math.floor(obj.getSimpleObject().getX() / DEFAULT_CELL_SIZE);
                int oldGY = (int) Math.floor(obj.getSimpleObject().getY() / DEFAULT_CELL_SIZE);
                int newGX = oldGX + dx;
                int newGY = oldGY + dy;
                moveInfos.add(new ObjectMoveInfo(obj, oldGX, oldGY, newGX, newGY));
            }
        }

        for (ObjectMoveInfo info : moveInfos) {
            GridCell oldCell = getCell(info.oldGX, info.oldGY);
            if(info.object.getType() == LevelObject.PLAYER_SPAWN) {
                playerPlaced = false;
            }
            oldCell.removeObject(info.object.getGroupId());
        }

        for (ObjectMoveInfo info : moveInfos) {
            info.object.getSimpleObject().setX(info.newGX * DEFAULT_CELL_SIZE);
            info.object.getSimpleObject().setY(info.newGY * DEFAULT_CELL_SIZE);
            getCell(info.newGX, info.newGY).addObject(info.object);
        }

        List<String> newKeys = new ArrayList<>();
        for (String key : originalKeys) {
            String[] coords = key.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            keyBuilder.setLength(0); // Reset for reuse
            keyBuilder.append(x + dx).append(",").append(y + dy);
            newKeys.add(keyBuilder.toString());
        }
        selectedCells.clear();
        selectedCells.addAll(newKeys);

        filterSelectedCellsToObjectsOnly();
        
        invalidate();

        if (objectCountUpdateCallback != null) {
            objectCountUpdateCallback.onObjectCountChanged();
        }
        
        // Notify activity of selection change
        if (selectionChangeCallback != null) {
            selectionChangeCallback.onSelectionChanged();
        }
    }

    private static class ObjectMoveInfo {
        final GridObject object;
        final int oldGX, oldGY, newGX, newGY;
        
        ObjectMoveInfo(GridObject object, int oldGX, int oldGY, int newGX, int newGY) {
            this.object = object;
            this.oldGX = oldGX;
            this.oldGY = oldGY;
            this.newGX = newGX;
            this.newGY = newGY;
        }
    }

    private void toggleCellSelection(int gridX, int gridY) {
        keyBuilder.setLength(0);
        keyBuilder.append(gridX).append(",").append(gridY);
        String key = keyBuilder.toString();
        
        GridCell cell = getCell(gridX, gridY);

        boolean isMultiSelection = !selectedCells.isEmpty();
        
        if (selectedCells.contains(key)) {
            selectedCells.remove(key);
        } else {
            if (isMultiSelection) {
                boolean hasRelevantObjects;
                if (currentGroup == -1) {
                    hasRelevantObjects = !cell.getAllObjects().isEmpty();
                } else {
                    hasRelevantObjects = cell.getObject(currentGroup) != null;
                }
                
                if (hasRelevantObjects) {
                    selectedCells.add(key);
                }
            } else {
                selectedCells.add(key);
            }
        }

        if (selectionChangeCallback != null) {
            selectionChangeCallback.onSelectionChanged();
        }
    }


    public int getCurrentGroup() { return currentGroup; }
    public GridCell accessCell(int x, int y) { return getCell(x, y); }
} 