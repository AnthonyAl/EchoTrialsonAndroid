package com.unipi.alexandris.android.echotrialsonandroid.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.SoundFXManager;
import com.unipi.alexandris.android.echotrialsonandroid.cosmetics.CosmeticSoundManager;
import com.unipi.alexandris.android.echotrialsonandroid.model.PhysicsObject;
import com.unipi.alexandris.android.echotrialsonandroid.model.Player;
import com.unipi.alexandris.android.echotrialsonandroid.model.Portal;
import com.unipi.alexandris.android.echotrialsonandroid.utility.LevelID;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.utility.SessionManager;

@SuppressWarnings("unused")
public enum GameConstants {
    @SuppressLint("StaticFieldLeak") INSTANCE;
    private final boolean DEBUG_MODE = false;
    // Screen dimensions
    private int SCREEN_WIDTH = 1280;
    private int SCREEN_HEIGHT = 720;
    
    // Game state
    private boolean GAME_PAUSED = false;
    private boolean SPEEDRUN_MODE = false;
    
    // Level data
    private LevelID currentLevel = null;
    private Region rigid = new Region();  // Collision shapeArea for solid blocks
    private Region ice = new Region();    // Collision shapeArea for ice surfaces
    private Region spikes = new Region();  // Collision shapeArea for spike blocks
    private Region water = new Region();  // Collision shapeArea for water areas
    
    // Initial player coordinates
    private int spawnX = 0;
    private int spawnY = 0;
    
    // Game object references
    private Player player = null;
    private Portal goal = null;
    private SoundFXManager soundManager;
    private CosmeticSoundManager cosmeticSoundManager;

    private long frameCounter = 0;

    private ObjectHandler handler;
    private boolean objectHandlerInitialized = false;
    private Context context;
    
    // GameHostActivity reference for player death handling
    private com.unipi.alexandris.android.echotrialsonandroid.view.GameHostActivity gameHostActivity = null;
    
    // User authentication data
    private String currentUserUid = null;

    public ObjectHandler getObjectHandler() {
        if (!objectHandlerInitialized) {
            initializeObjectHandler();
        }
        return handler;
    }

    public void initializeObjectHandler() {
        if (objectHandlerInitialized) {
            throw new IllegalStateException("ObjectHandler already initialized");
        }
        this.handler = new ObjectHandler();
        this.objectHandlerInitialized = true;
    }

    public void resetHandler() {
        handler = null;
        objectHandlerInitialized = false;
    }

    public void initializeSoundManagerIfNeeded(Context context) {
        // Initialize SoundFXManager first
        if (soundManager == null) {
            soundManager = new SoundFXManager(context.getApplicationContext());
        }
        
        // Then initialize CosmeticSoundManager (which depends on SoundFXManager)
        if (cosmeticSoundManager == null && SessionManager.INSTANCE.isInitialized()) {
            cosmeticSoundManager = new CosmeticSoundManager();
        }
    }

    public void rebuildCollisionRegions(java.util.List<PhysicsObject> allObjects) {
        // Clear all collision regions
        rigid.setEmpty();
        ice.setEmpty();
        water.setEmpty();
        spikes.setEmpty();
        
        // Rebuild from current blocks only
        for (PhysicsObject obj : allObjects) {
            if (obj instanceof com.unipi.alexandris.android.echotrialsonandroid.model.Block) {
                com.unipi.alexandris.android.echotrialsonandroid.model.Block blockObj = 
                    (com.unipi.alexandris.android.echotrialsonandroid.model.Block) obj;
                
                // Add to appropriate collision regions based on rigid type
                if (obj instanceof com.unipi.alexandris.android.echotrialsonandroid.model.BlockCommon) {
                    rigid.op(blockObj.getShapeArea(), Region.Op.UNION);
                } else if (obj instanceof com.unipi.alexandris.android.echotrialsonandroid.model.BlockIce) {
                    // Ice blocks are solid AND slippery
                    rigid.op(blockObj.getShapeArea(), Region.Op.UNION);
                    ice.op(blockObj.getShapeArea(), Region.Op.UNION);
                } else if (obj instanceof com.unipi.alexandris.android.echotrialsonandroid.model.BlockWater) {
                    water.op(blockObj.getShapeArea(), Region.Op.UNION);
                } else if (obj instanceof com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpike) {
                    // All spike blocks (common and ice, all orientations) go to spikes region
                    spikes.op(blockObj.getShapeArea(), Region.Op.UNION);
                }
            }
        }
        
        frameCounter++;
    }

    public boolean isDEBUG_MODE() {
        return DEBUG_MODE;
    }

    public int getScreenWidth() {
        return SCREEN_WIDTH;
    }

    public int getScreenHeight() {
        return SCREEN_HEIGHT;
    }

    public boolean isGamePaused() {
        return GAME_PAUSED;
    }

    public LevelID getCurrentLevel() {
        return currentLevel;
    }

    public Region getGlobalRigidArea() {
        return rigid;
    }

    public Region getGlobalIceArea() {
        return ice;
    }

    public Region getGlobalWaterArea() {
        return water;
    }

    public Region getGlobalSpikesArea() {
        return spikes;
    }

    public int getSpawnX() {
        return spawnX;
    }

    public int getSpawnY() {
        return spawnY;
    }

    public Player getPlayer() {
        return player;
    }

    public Portal getGoal() {
        return goal;
    }

    public long getFrameCounter() {
        return frameCounter;
    }

    public SoundFXManager getSoundManager() {
        return soundManager;
    }

    public void setSoundManager(SoundFXManager soundManager) {
        this.soundManager = soundManager;
    }
    
    public CosmeticSoundManager getCosmeticSoundManager() {
        return cosmeticSoundManager;
    }
    
    public void setCosmeticSoundManager(CosmeticSoundManager cosmeticSoundManager) {
        this.cosmeticSoundManager = cosmeticSoundManager;
    }

    public void setScreenWidth(int SCREEN_WIDTH) {
        this.SCREEN_WIDTH = SCREEN_WIDTH;
    }

    public void setScreenHeight(int SCREEN_HEIGHT) {
        this.SCREEN_HEIGHT = SCREEN_HEIGHT;
    }

    public void pause() {
        GAME_PAUSED = true;
    }

    public void unpause() {
        GAME_PAUSED = false;
    }
    
    public void setGamePaused(boolean paused) {
        GAME_PAUSED = paused;
    }

    public void toggleSpeedrunMode() {
        SPEEDRUN_MODE = !SPEEDRUN_MODE;
    }

    public void setCurrentLevel(LevelID currentLevel) {
        this.currentLevel = currentLevel;
    }

    public void setGlobalRigidArea(Region rigid) {
        this.rigid = rigid;
    }

    public void setGlobalIceArea(Region ice) {
        this.ice = ice;
    }

    public void setGlobalWaterArea(Region water) {
        this.water = water;
    }

    public void setGlobalSpikesArea(Region spikes) {
        this.spikes = spikes;
    }


    public void resetGlobalArea() {
        rigid.setEmpty();
        ice.setEmpty();
        water.setEmpty();
        spikes.setEmpty();
    }

    public void setSpawnX(int spawnX) {
        this.spawnX = spawnX;
    }

    public void setSpawnY(int spawnY) {
        this.spawnY = spawnY;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setGoal(Portal goal) {
        this.goal = goal;
    }

    public void setFrameCounter(long frameCounter) {
        this.frameCounter = frameCounter;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public com.unipi.alexandris.android.echotrialsonandroid.view.GameHostActivity getGameHostActivity() {
        return gameHostActivity;
    }

    public void setGameHostActivity(com.unipi.alexandris.android.echotrialsonandroid.view.GameHostActivity activity) {
        this.gameHostActivity = activity;
    }

    public String getCurrentUserUid() {
        return currentUserUid;
    }

    public void setCurrentUserUid(String userUid) {
        this.currentUserUid = userUid;
    }

    public void clearCurrentUserUid() {
        this.currentUserUid = null;
    }

    public boolean isUserSignedIn() {
        return currentUserUid != null && !currentUserUid.isEmpty();
    }
}