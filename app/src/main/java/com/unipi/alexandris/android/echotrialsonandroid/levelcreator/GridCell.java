package com.unipi.alexandris.android.echotrialsonandroid.levelcreator;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single cell in the level creation grid.
 * Each cell can contain multiple objects, one for each group SimpleID.
 * This allows layering of objects from different groups on the same cell.
 * <br/>
 * The GridCell class serves as a container for grid objects, managing
 * their organization by group SimpleID and providing methods to access and
 * manipulate these objects.
 */
public class GridCell {
    /**
     * Map storing grid objects by their group SimpleID.
     * This allows each cell to contain multiple objects, one per group.
     * The key is the group SimpleID, and the value is the corresponding GridObject.
     */
    private Map<Integer, GridObject> objectsByGroup = new HashMap<>();
    
    /**
     * Default constructor.
     */
    public GridCell() {
        // Empty constructor
    }
    
    /**
     * Copy constructor for creating deep copies of GridCell objects.
     * Used for undo/redo functionality to preserve grid state.
     *
     * @param other The GridCell to copy from
     */
    public GridCell(GridCell other) {
        this.objectsByGroup = new HashMap<>();
        for (Map.Entry<Integer, GridObject> entry : other.objectsByGroup.entrySet()) {
            // Create a deep copy of each GridObject
            this.objectsByGroup.put(entry.getKey(), new GridObject(entry.getValue()));
        }
    }

    public void addObject(GridObject object) {
        objectsByGroup.put(object.getGroupId(), object);
    }

    public void removeObject(int groupId) {
        objectsByGroup.remove(groupId);
    }

    public GridObject getObject(int groupId) {
        return objectsByGroup.get(groupId);
    }

    public Collection<GridObject> getAllObjects() {
        return objectsByGroup.values();
    }

    public GridObject getTopObject() {
        if (objectsByGroup.isEmpty()) return null;
        return objectsByGroup.values().stream()
                .min(Comparator.comparingInt(GridObject::getGroupId))
                .orElse(null);
    }

    public boolean changeObjectGroup(int oldGroupId, int newGroupId) {
        GridObject object = objectsByGroup.get(oldGroupId);
        if (object == null) return false;

        objectsByGroup.remove(oldGroupId);
        object.setGroupId(newGroupId);
        object.getSimpleObject().setGroupId(newGroupId);
        objectsByGroup.put(newGroupId, object);
        
        return true;
    }
} 