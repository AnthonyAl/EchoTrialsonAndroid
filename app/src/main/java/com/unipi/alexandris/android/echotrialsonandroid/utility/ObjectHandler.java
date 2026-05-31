package com.unipi.alexandris.android.echotrialsonandroid.utility;

import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.model.*;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLSpriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * High-performance game object manager optimized for 120+ FPS.
 * Manages all game objects and scheduled tasks with minimal overhead.
 * Maintains separate lists by object type for efficient collision masking.
 */
public class ObjectHandler {
    
    /** List of all active game objects - LinkedList for fast add/remove */
    private final LinkedList<PhysicsObject> objects = new LinkedList<>();
    
    /** Pre-allocated list for rendering to avoid garbage collection */
    private final ArrayList<PhysicsObject> renderList = new ArrayList<>();
    
    /** Type-based object lists for efficient collision masking and access */
    private final Map<ID, List<PhysicsObject>> objectsByType = new EnumMap<>(ID.class);
    
    /** Map of scheduled tasks and their timing information */
    private final LinkedList<TickRunnable> scheduledTasks = new LinkedList<>();
    
    /** Whether the handler should perform visibility culling */
    private boolean enableCulling = true;
    
    /** Performance counters */
    private int visibleObjectCount = 0;
    private int totalObjectCount = 0;
    
    /** UID counter for assigning unique identifiers to objects */
    private int nextUid = 0;
    
    // Memory monitoring to prevent resource exhaustion
    private static final int MAX_TOTAL_OBJECTS = 5000; // Increased from 200
    private long lastMemoryCheck = 0;
    private static final long MEMORY_CHECK_INTERVAL = 5000; // Check every 5 seconds
    
    // Thread safety for rendering
    private volatile boolean isRendering = false;

    /**
     * Creates a new ObjectHandler and initializes type-based object lists.
     */
    public ObjectHandler() {
        // Initialize lists for each object type
        for (ID id : ID.values()) {
            objectsByType.put(id, new ArrayList<>());
        }
    }
    
    /**
     * High-performance update method with minimal overhead.
     * Only active objects are updated.
     */
    public void tick() {
        // Memory monitoring to prevent resource exhaustion
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMemoryCheck > MEMORY_CHECK_INTERVAL) {
            checkMemoryUsage();
            lastMemoryCheck = currentTime;
        }
        
        // Process scheduled tasks (optimized iteration)
        if (!scheduledTasks.isEmpty()) {
            for (int i = scheduledTasks.size() - 1; i >= 0; i--) {
                TickRunnable task = scheduledTasks.get(i);
                if (task.isActive()) {
                    task.run();
                } else {
                    scheduledTasks.remove(i);
                }
            }
        }
        
        // Update all active game objects (safe iteration to prevent ConcurrentModificationException)
        totalObjectCount = objects.size();
        

        
        // Create a copy of the list to iterate over safely
        List<PhysicsObject> objectsCopy;
        synchronized (objects) {
            objectsCopy = new ArrayList<>(objects);
        }
        
        for (PhysicsObject obj : objectsCopy) {
            if (obj != null) {
                // Always onTick triggers regardless of active state (they need to check for collision)
                boolean shouldTick = obj.isActive() || obj instanceof Trigger;
                
                if (shouldTick) {
                    try {
                        obj.tick();
                    } catch (Exception e) {
                        // Handle onTick error silently to maintain performance
                        android.util.Log.e("ObjectHandler", "Error ticking object: " + e.getMessage());
                    }
                }
            }
        }

        List<PhysicsObject> collisionObjectsCopy;
        synchronized (objects) {
            collisionObjectsCopy = new ArrayList<>(objects);
        }
        GameConstants.INSTANCE.rebuildCollisionRegions(collisionObjectsCopy);
    }
    
    /**
     * High-performance OpenGL rendering with same culling and optimizations as Canvas version.
     * Maintains exact same logic as render(Canvas, Camera) but uses OpenGL sprite batching.
     * 
     * @param spriteBatch The OpenGL sprite batch for efficient rendering
     * @param camera The camera providing view transformation
     */
    public void renderGL(GLSpriteBatch spriteBatch, Camera camera) {
        if (spriteBatch == null) return;

        // Set rendering flag to prevent object modifications during rendering
        isRendering = true;
        
        try {
            // Clear and populate render list (reuse to avoid GC) - SAME as Canvas version
            renderList.clear();
            visibleObjectCount = 0;
            
            // Create a thread-safe copy of objects for rendering
            List<PhysicsObject> objectsCopy;
            synchronized (objects) {
                objectsCopy = new ArrayList<>(objects);
            }
            
            // Fast culling and population of render list - SAME logic as Canvas version
            if (enableCulling && camera != null) {
                // Aggressive culling - only add objects that are definitely visible
                for (PhysicsObject obj : objectsCopy) {
                    if (obj != null && obj.isVisible()) {
                        if (obj.isVisibleInCamera(camera)) {
                            renderList.add(obj);
                            visibleObjectCount++;
                        }
                    }
                }
            } else {
                // No culling - add all visible objects
                for (PhysicsObject obj : objectsCopy) {
                    if (obj != null && obj.isVisible()) {
                        renderList.add(obj);
                        visibleObjectCount++;
                    }
                }
            }

            renderList.sort((o1, o2) -> {
                // Player always renders last (highest priority)
                if (o1 instanceof Player && !(o2 instanceof Player)) return 1;
                if (o2 instanceof Player && !(o1 instanceof Player)) return -1;
                
                // If both objects are in the same group, prioritize portals
                if (o1.getGroupId() == o2.getGroupId()) {
                    if (o1 instanceof Portal && !(o2 instanceof Portal)) return 1;
                    if (o2 instanceof Portal && !(o1 instanceof Portal)) return -1;
                }
                
                // For different groups or same type, sort by groupId (higher numbers render last)
                return Integer.compare(o2.getGroupId(), o1.getGroupId());
            });
            
            for (int i = 0; i < renderList.size(); i++) {
                PhysicsObject obj = renderList.get(i);
                try {
                    obj.renderGL(spriteBatch, camera); // Call SimplePhysicsObject.renderGL() instead of render()
                } catch (Exception e) {
                    // Handle render error silently to maintain performance
                }
            }
        } finally {
            // Reset rendering flag after rendering is complete
            isRendering = false;
        }
    }
    
    /**
     * Adds a new game object to the handler.
     * Assigns a unique UID to the object for reliable snapshot restoration.
     */
    public void addObject(PhysicsObject object) {
        if (object == null) return;
        
        // Allow object addition during rendering for particles and other effects
        // that need to be created immediately
        if (isRendering && object.getId() != ID.Particles) return;
        
        // Assign a unique UID to the object
        object.setUid(nextUid++);
        
        if (object instanceof Player && GameConstants.INSTANCE.getPlayer() == null) {
            GameConstants.INSTANCE.setPlayer((Player) object);
        } else if (object instanceof Portal) {
            GameConstants.INSTANCE.setGoal((Portal) object);
        }
        
        synchronized (objects) {
            objects.add(object);
        }
        
        ID objectId = object.getId();
        if (objectId != null) {
            List<PhysicsObject> typeList = objectsByType.get(objectId);
            if (typeList != null) {
                synchronized (typeList) {
                    typeList.add(object);
                }
            }
        }
    }

    public void removeObject(PhysicsObject object) {
        if (object == null) return;
        
        synchronized (objects) {
            objects.remove(object);
        }
        
        ID objectId = object.getId();
        if (objectId != null) {
            List<PhysicsObject> typeList = objectsByType.get(objectId);
            if (typeList != null) {
                synchronized (typeList) {
                    typeList.remove(object);
                }
            }
        }
    }

    public List<PhysicsObject> getObjectsView() {
        synchronized (objects) {
            return Collections.unmodifiableList(new ArrayList<>(objects));
        }
    }

    public List<PhysicsObject> getObjectsByType(ID objectType) {
        List<PhysicsObject> typeList = objectsByType.get(objectType);
        if (typeList == null) {
            return Collections.emptyList();
        }
        
        synchronized (typeList) {
            return List.copyOf(typeList);
        }
    }

    public int getObjectCountByType(ID objectType) {
        List<PhysicsObject> typeList = objectsByType.get(objectType);
        if (typeList == null) {
            return 0;
        }
        
        synchronized (typeList) {
            return typeList.size();
        }
    }

    public Player getPlayer() {
        List<PhysicsObject> players = getObjectsByType(ID.Player);
        return players.isEmpty() ? null : (Player) players.get(0);
    }

    public LinkedList<PhysicsObject> getObjects() {
        return objects;
    }

    public void clearObjects() {
        synchronized (objects) {
            objects.clear();
        }
        
        // Clear all type-based lists
        for (List<PhysicsObject> typeList : objectsByType.values()) {
            synchronized (typeList) {
                typeList.clear();
            }
        }
        
        // Clear special references
        GameConstants.INSTANCE.setPlayer(null);
        GameConstants.INSTANCE.setGoal(null);
    }
    public int getVisibleObjectCount() {
        return visibleObjectCount;
    }

    public int getTotalObjectCount() {
        return totalObjectCount;
    }

    public void setCullingEnabled(boolean enabled) {
        this.enableCulling = enabled;
    }

    public boolean isCullingEnabled() {
        return enableCulling;
    }

    public void runRepeating(TickRunnable task) {
        if (task != null) {
            scheduledTasks.add(task);
        }
    }

    public void removeTask(TickRunnable task) {
        scheduledTasks.remove(task);
    }

    public int getTaskCount() {
        return scheduledTasks.size();
    }

    public void clearAllTasks() {
        scheduledTasks.clear();
    }

    private void checkMemoryUsage() {
        int totalObjects = objects.size();
        int particleCount = getObjectCountByType(ID.Particles);
        
        if (totalObjects > MAX_TOTAL_OBJECTS) {
            if (particleCount > 50) {
                List<PhysicsObject> particlesToRemove = new ArrayList<>();
                for (PhysicsObject obj : objects) {
                    if (obj.getId() == ID.Particles && particlesToRemove.size() < (particleCount - 50)) {
                        particlesToRemove.add(obj);
                    }
                }
                
                for (PhysicsObject particle : particlesToRemove) {
                    removeObject(particle);
                }
            }
        }
    }
} 