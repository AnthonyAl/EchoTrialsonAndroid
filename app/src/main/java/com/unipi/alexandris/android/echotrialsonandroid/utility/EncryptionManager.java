package com.unipi.alexandris.android.echotrialsonandroid.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * EncryptionManager handles AES-256 encryption for player statistics.
 * Uses simple AES-256-CBC encryption that works reliably on all devices.
 * Provides robust protection against casual data tampering.
 */
public class EncryptionManager {
    
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16; // 128 bits for AES
    private static final int KEY_LENGTH = 32; // 256 bits for AES-256
    private static final String KEY_PREF = "encryption_key";

    private final SharedPreferences keyPrefs;
    private SecretKey secretKey;
    
    public EncryptionManager(Context context) {
        this.keyPrefs = context.getSharedPreferences("encryption_keys", Context.MODE_PRIVATE);
        ensureKeyExists();
    }

    private void ensureKeyExists() {
        if (!keyPrefs.contains(KEY_PREF)) {
            // Generate a random 32-byte key for AES-256
            byte[] key = new byte[KEY_LENGTH];
            new SecureRandom().nextBytes(key);
            String encodedKey = Base64.encodeToString(key, Base64.DEFAULT);
            keyPrefs.edit().putString(KEY_PREF, encodedKey).apply();
        }
        
        // Load the key
        String encodedKey = keyPrefs.getString(KEY_PREF, null);
        if (encodedKey != null) {
            byte[] keyBytes = Base64.decode(encodedKey, Base64.DEFAULT);
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
        }
    }

    public String encrypt(String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            // Generate random IV
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);
            
            // Initialize cipher for encryption
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            
            // Encrypt the data
            byte[] encryptedData = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and encrypted data
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
            
            // Return Base64 encoded result
            return Base64.encodeToString(combined, Base64.DEFAULT);
            
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            // Decode Base64
            byte[] combined = Base64.decode(encryptedText, Base64.DEFAULT);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[IV_LENGTH];
            byte[] encryptedData = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encryptedData, 0, encryptedData.length);
            
            // Initialize cipher for decryption
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            
            // Decrypt the data
            byte[] decryptedData = cipher.doFinal(encryptedData);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    public boolean isKeyAvailable() {
        return keyPrefs.contains(KEY_PREF) && secretKey != null;
    }

    public void clearKey() {
        try {
            keyPrefs.edit().remove(KEY_PREF).apply();
            secretKey = null;
        } catch (Exception e) {
        }
    }
}
