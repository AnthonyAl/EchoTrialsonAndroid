package com.unipi.alexandris.android.echotrialsonandroid.cosmetics;

import com.unipi.alexandris.android.echotrialsonandroid.utility.SpriteTrailRenderer;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLSpriteBatch;

public class TrailHelper {
    
    private final SpriteTrailRenderer spriteTrailRenderer;
    // TODO: Add particle trail renderers when implemented
    
    public TrailHelper() {
        this.spriteTrailRenderer = new SpriteTrailRenderer();
    }

    public void update(double x, double y, double width, double height, double speedX, int textureId, boolean flipX, boolean flipY) {
        // Get equipped trail cosmetic
        AvailableCosmetics equippedTrail = EquippedCosmetics.INSTANCE.getEquippedCosmetic(CosmeticType.PARTICLE_TRAIL);
        
        if (equippedTrail != null) {
            updateTrailWithCosmetic(equippedTrail, x, y, width, height, speedX, textureId, flipX, flipY);
        } else {
            updateSpriteTrail(x, y, width, height, speedX, textureId, flipX, flipY);
        }
    }

    public void render(GLSpriteBatch spriteBatch) {
        AvailableCosmetics equippedTrail = EquippedCosmetics.INSTANCE.getEquippedCosmetic(CosmeticType.PARTICLE_TRAIL);
        
        if (equippedTrail != null) {
            renderTrailWithCosmetic(equippedTrail, spriteBatch);
        } else {
            renderSpriteTrail(spriteBatch);
        }
    }

    public void clear() {
        spriteTrailRenderer.clear();
        // TODO: Clear particle trails when implemented
    }

    private void updateTrailWithCosmetic(AvailableCosmetics cosmetic, double x, double y, double width, double height, double speedX, int textureId, boolean flipX, boolean flipY) {
        String cosmeticId = cosmetic.getId();
        
        switch (cosmeticId) {
            case "trail_none":
                break;
            case "trail_default":
                updateSpriteTrail(x, y, width, height, speedX, textureId, flipX, flipY);
                break;
            case "trail_fire":
                // TODO: Implement fire particle trail
                updateSpriteTrail(x, y, width, height, speedX, textureId, flipX, flipY);
                break;
            case "trail_ice":
                // TODO: Implement ice particle trail
                updateSpriteTrail(x, y, width, height, speedX, textureId, flipX, flipY);
                break;
            default:
                updateSpriteTrail(x, y, width, height, speedX, textureId, flipX, flipY);
                break;
        }
    }

    private void renderTrailWithCosmetic(AvailableCosmetics cosmetic, GLSpriteBatch spriteBatch) {
        String cosmeticId = cosmetic.getId();
        
        switch (cosmeticId) {
            case "trail_none":
                break;
            case "trail_default":
                renderSpriteTrail(spriteBatch);
                break;
            case "trail_fire":
                // TODO: Implement fire particle trail rendering
                renderSpriteTrail(spriteBatch);
                break;
            case "trail_ice":
                // TODO: Implement ice particle trail rendering
                renderSpriteTrail(spriteBatch);
                break;
            default:
                renderSpriteTrail(spriteBatch);
                break;
        }
    }

    private void updateSpriteTrail(double x, double y, double width, double height, double speedX, int textureId, boolean flipX, boolean flipY) {
        spriteTrailRenderer.update((float)x, (float)y, (float)width, (float)height, (float)speedX, textureId, flipX, flipY);
    }

    private void renderSpriteTrail(GLSpriteBatch spriteBatch) {
        spriteTrailRenderer.render(spriteBatch);
    }
}
