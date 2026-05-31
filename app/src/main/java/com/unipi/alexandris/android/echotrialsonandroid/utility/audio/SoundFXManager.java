package com.unipi.alexandris.android.echotrialsonandroid.utility.audio;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.unipi.alexandris.android.echotrialsonandroid.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SoundFXManager {
    
    private static final String TAG = "SoundFXManager";

    private float masterVolume = 1.0f;
    private float musicVolume = 0.7f;
    private float sfxVolume = 0.8f;
    private final Map<SoundID, int[]> soundFileMap;
    private static final int MAX_FAILURES_PER_SOUND = 3;
    private MediaPlayer backgroundMusicPlayer;
    private final Context context;
    private final Random random;
    private SoundID currentBackgroundMusic = null;
    private final Map<SoundID, Long> lastPlayTime = new HashMap<>();
    private static final long SOUND_COOLDOWN_MS = 100;
    private final Map<SoundID, Integer> soundFailureCount = new HashMap<>();
    private final java.util.concurrent.BlockingQueue<SoundTask> soundQueue = new java.util.concurrent.LinkedBlockingQueue<>();
    private final Thread soundThread;
    private volatile boolean isShutdown = false;

    private static class SoundTask {
        final int resourceId;
        final float volume;
        final SoundID soundId;
        
        SoundTask(int resourceId, float volume, SoundID soundId) {
            this.resourceId = resourceId;
            this.volume = volume;
            this.soundId = soundId;
        }
    }

    public SoundFXManager(Context context) {
        this.context = context;
        this.random = new Random();
        
        // Initialize sound file mappings
        this.soundFileMap = initializeSoundFileMap();
        
        // Initialize background music player
        this.backgroundMusicPlayer = new MediaPlayer();
        this.backgroundMusicPlayer.setLooping(true);
        
        // Initialize dedicated sound thread
        this.soundThread = new Thread(this::processSoundQueue, "SoundFXThread");
        this.soundThread.setDaemon(true);
        this.soundThread.start();
    }
    
    /**
     * Processes the sound queue on the dedicated sound thread.
     * This method runs continuously until shutdown.
     * Enhanced with better error recovery and thread safety.
     */
    private void processSoundQueue() {
        Log.d(TAG, "Sound thread started");
        
        while (!isShutdown) {
            SoundTask task = null;
            try {
                // Wait for a sound task
                task = soundQueue.take();
                
                // Double-check shutdown after taking task
                if (isShutdown) {
                    Log.d(TAG, "Sound thread shutdown during task retrieval");
                    break;
                }
                
                // Validate task
                if (task == null) {
                    Log.w(TAG, "Received null sound task, skipping");
                    continue;
                }
                
                if (task.soundId == null) {
                    Log.w(TAG, "Received task with null soundId, skipping");
                    continue;
                }
                
                // Process the sound task - each sound gets its own MediaPlayer
                playSoundFileWithNewInstance(task.resourceId, task.volume, task.soundId);
                
                // Small delay to prevent overwhelming the audio system
                try {
                    Thread.sleep(1); // 1ms delay between sounds
                } catch (InterruptedException ie) {
                    if (isShutdown) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
            } catch (InterruptedException e) {
                if (isShutdown) {
                    Thread.currentThread().interrupt();
                    break;
                }
                Log.d(TAG, "Sound thread interrupted but continuing (not shutdown)");
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "Out of memory in sound thread, forcing GC and continuing", e);
                System.gc();
            } catch (Exception e) {
                Log.w(TAG, "Error processing sound task: " + (task != null ? task.soundId : "unknown"), e);
            }
        }
        
        Log.d(TAG, "Sound thread shutting down gracefully");
    }

    private Map<SoundID, int[]> initializeSoundFileMap() {
        Map<SoundID, int[]> map = new HashMap<>();
        
        // Death sounds - one sound per SoundID
        map.put(SoundID.DEATH_FALL_1, new int[]{R.raw.falling_1});
        map.put(SoundID.DEATH_FALL_2, new int[]{R.raw.falling_2});
        map.put(SoundID.DEATH_SPIKE_1, new int[]{R.raw.spike_damage_3});
        map.put(SoundID.DEATH_SUFFOCATION_1, new int[]{R.raw.spike_damage_3});

        // Movement sounds
        map.put(SoundID.JUMP, new int[]{R.raw.jump_1});
        map.put(SoundID.WALKING, new int[]{R.raw.walking});
        map.put(SoundID.SWIMMING, new int[]{R.raw.swimming1, R.raw.swimming2});
        
        // UI sounds
        map.put(SoundID.UI_BUTTON_CLICK, new int[]{R.raw.tap});
        
        // Game effects
        map.put(SoundID.PORTAL, new int[]{R.raw.portal});
        
        // Level completion
        map.put(SoundID.STAR_COLLECTED, new int[]{R.raw.star_unlocked});
        
        // Water effects
        map.put(SoundID.WATER_SPLASH, new int[]{R.raw.swimming1}); // Placeholder - TODO: Add actual water splash sound
        
        return map;
    }

    public void playSound(SoundID soundId) {
        if (soundId == null) {
            Log.w(TAG, "Attempted to play null sound SimpleID");
            return;
        }
        
        if (isShutdown) {
            Log.d(TAG, "Sound manager is shutdown, ignoring sound: " + soundId);
            return;
        }
        
        try {
            // Check if sound has failed too many times recently
            Integer failures = soundFailureCount.get(soundId);
            if (failures != null && failures >= MAX_FAILURES_PER_SOUND) {
                Log.w(TAG, "Sound " + soundId + " has failed " + failures + " times, temporarily disabled");
                return;
            }
            
            // Check cooldown to prevent overlapping sounds
            long currentTime = System.currentTimeMillis();
            Long lastTime = lastPlayTime.get(soundId);
            if (lastTime != null && (currentTime - lastTime) < SOUND_COOLDOWN_MS) {
                // Sound is still in cooldown, skip this play
                Log.v(TAG, "Sound " + soundId + " is on cooldown, skipping");
                return;
            }
            
            int[] files = soundFileMap.get(soundId);
            if (files == null || files.length == 0) {
                Log.w(TAG, "No sound files mapped for sound SimpleID: " + soundId);
                return;
            }
            
            // Validate sound files
            boolean hasValidFile = false;
            for (int file : files) {
                if (file != 0) {
                    hasValidFile = true;
                    break;
                }
            }
            
            if (!hasValidFile) {
                Log.w(TAG, "No valid sound files found for SimpleID: " + soundId);
                return;
            }
            
            // Select a random file if multiple are available
            int selectedFile = 0;
            int attempts = 0;
            while (selectedFile == 0 && attempts < files.length) {
                selectedFile = files[random.nextInt(files.length)];
                attempts++;
            }
            
            if (selectedFile == 0) {
                Log.w(TAG, "Could not find valid sound file for SimpleID: " + soundId);
                return;
            }
            
            // Calculate final volume
            float finalVolume = sfxVolume * masterVolume;
            if (finalVolume <= 0.0f) {
                Log.v(TAG, "Volume is zero, skipping sound: " + soundId);
                return;
            }
            
            // Update last play time
            lastPlayTime.put(soundId, currentTime);
            
            // Queue sound for processing on dedicated sound thread
            SoundTask task = new SoundTask(selectedFile, finalVolume, soundId);
            boolean queued = soundQueue.offer(task);
            
            if (!queued) {
                Log.w(TAG, "Sound queue full, dropping sound: " + soundId + " (queue size: " + soundQueue.size() + ")");
            } else {
                Log.v(TAG, "Queued sound: " + soundId + ", queue size: " + soundQueue.size());
            }
            
        } catch (Exception e) {
            Log.w(TAG, "Error in playSound for " + soundId, e);
        }
    }
    
    /**
     * Plays a sound file with a new MediaPlayer instance to avoid interruption.
     * This method creates a fresh MediaPlayer for each sound play, ensuring no conflicts.
     * Enhanced with better error handling and thread safety.
     */
    private void playSoundFileWithNewInstance(int resId, float volume, SoundID soundId) {
        if (resId == 0) {
            Log.w(TAG, "Invalid resource SimpleID: 0 for sound: " + soundId);
            return;
        }
        
        if (isShutdown) {
            Log.d(TAG, "Sound manager is shutdown, ignoring sound: " + soundId);
            return;
        }
        
        MediaPlayer player = null;
        try {
            // Create a new MediaPlayer instance for each sound to avoid interruption
            player = MediaPlayer.create(context, resId);
            if (player == null) {
                Log.e(TAG, "Failed to create MediaPlayer for sound: " + soundId + ", resource ID: " + resId);
                recordSoundFailure(soundId);
                return;
            }
            
            // Validate volume range
            float safeVolume = Math.max(0.0f, Math.min(1.0f, volume));
            if (safeVolume != volume) {
                Log.w(TAG, "Volume clamped from " + volume + " to " + safeVolume + " for sound: " + soundId);
            }
            
            // Set volume safely
            player.setVolume(safeVolume, safeVolume);
            
            // Create a final reference for listeners
            final MediaPlayer finalPlayer = player;
            
            // Set up completion listener to release the player after playback
            player.setOnCompletionListener(mp -> {
                try {
                    if (mp != null && mp == finalPlayer) {
                        mp.release();
                        Log.v(TAG, "MediaPlayer released for sound: " + soundId);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error releasing MediaPlayer for sound: " + soundId, e);
                }
            });
            
            // Set up error listener for cleanup
            player.setOnErrorListener((mp, what, extra) -> {
                try {
                    if (mp != null && mp == finalPlayer) {
                        mp.release();
                        Log.w(TAG, "MediaPlayer error for sound: " + soundId + ", what: " + what + ", extra: " + extra);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error releasing MediaPlayer on error for sound: " + soundId, e);
                }
                return true; // Error handled
            });
            
            // Check if we're still good to play
            if (isShutdown) {
                Log.d(TAG, "Sound manager shutdown during setup, releasing player for: " + soundId);
                player.release();
                return;
            }
            
            // Start playback
            player.start();
            Log.v(TAG, "Started new MediaPlayer for sound: " + soundId);
            
            // Reset failure count on successful playback
            recordSoundSuccess(soundId);
            
        } catch (IllegalStateException e) {
            Log.w(TAG, "IllegalStateException playing sound: " + soundId + " - MediaPlayer in invalid state", e);
            recordSoundFailure(soundId);
            if (player != null) {
                try { player.release(); } catch (Exception ex) { /* ignore */ }
            }
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "IllegalArgumentException playing sound: " + soundId + " - Invalid resource or parameters", e);
            recordSoundFailure(soundId);
            if (player != null) {
                try { player.release(); } catch (Exception ex) { /* ignore */ }
            }
        } catch (SecurityException e) {
            Log.w(TAG, "SecurityException playing sound: " + soundId + " - Permission denied", e);
            recordSoundFailure(soundId);
            if (player != null) {
                try { player.release(); } catch (Exception ex) { /* ignore */ }
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error playing sound: " + soundId + ", resource ID: " + resId, e);
            recordSoundFailure(soundId);
            if (player != null) {
                try { player.release(); } catch (Exception ex) { /* ignore */ }
            }
        }
    }

    private void recordSoundFailure(SoundID soundId) {
        if (soundId == null) return;
        
        Integer currentFailures = soundFailureCount.get(soundId);
        int newFailures = (currentFailures == null) ? 1 : currentFailures + 1;
        soundFailureCount.put(soundId, newFailures);
        
        if (newFailures >= MAX_FAILURES_PER_SOUND) {
            Log.w(TAG, "Sound " + soundId + " has reached maximum failures (" + newFailures + "), temporarily disabling");
        }
    }

    private void recordSoundSuccess(SoundID soundId) {
        if (soundId == null) return;
        
        // Reset failure count on successful playback
        soundFailureCount.remove(soundId);
    }

    public void playBackgroundMusic(SoundID soundId) {
        if (soundId == null) {
            Log.w(TAG, "Attempted to play null background music SimpleID");
            return;
        }
        
        int[] files = soundFileMap.get(soundId);
        if (files == null || files.length == 0) {
            Log.w(TAG, "No background music files mapped for sound SimpleID: " + soundId);
            return;
        }
        
        // Stop current background music
        stopBackgroundMusic();
        
        // Start new background music
        int selectedFile = files[random.nextInt(files.length)];
        
        try {
            backgroundMusicPlayer.reset();
            backgroundMusicPlayer = MediaPlayer.create(context, selectedFile);
            backgroundMusicPlayer.setLooping(true);
            backgroundMusicPlayer.setVolume(musicVolume * masterVolume, musicVolume * masterVolume);
            backgroundMusicPlayer.start();
            currentBackgroundMusic = soundId;
        } catch (Exception e) {
            Log.e(TAG, "Error playing background music: " + selectedFile, e);
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null && backgroundMusicPlayer.isPlaying()) {
            backgroundMusicPlayer.stop();
        }
        currentBackgroundMusic = null;
    }

    public void pauseBackgroundMusic() {
        if (backgroundMusicPlayer != null && backgroundMusicPlayer.isPlaying()) {
            backgroundMusicPlayer.pause();
        }
    }

    public void resumeBackgroundMusic() {
        if (backgroundMusicPlayer != null && !backgroundMusicPlayer.isPlaying()) {
            backgroundMusicPlayer.start();
        }
    }

    private void updateBackgroundMusicVolume() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setVolume(musicVolume * masterVolume, musicVolume * masterVolume);
        }
    }
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateBackgroundMusicVolume();
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateBackgroundMusicVolume();
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public SoundID getCurrentBackgroundMusic() {
        return currentBackgroundMusic;
    }

    public boolean isBackgroundMusicPlaying() {
        return backgroundMusicPlayer != null && backgroundMusicPlayer.isPlaying();
    }

    public void addSoundMapping(SoundID soundId, int... fileNames) {
        if (soundId != null && fileNames != null && fileNames.length > 0) {
            soundFileMap.put(soundId, fileNames);
        }
    }

    public void clearCache() {
        // No-op: Individual MediaPlayer instances auto-release after playback
    }

    public void release() {
        // Shutdown sound thread
        isShutdown = true;
        if (soundThread != null && soundThread.isAlive()) {
            soundThread.interrupt();
            try {
                soundThread.join(1000); // Wait up to 1 second for thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Clear sound queue
        soundQueue.clear();
        
        // Release cached sounds
        clearCache();
        
        // Release background music player
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.release();
            backgroundMusicPlayer = null;
        }
        
        currentBackgroundMusic = null;
    }
} 