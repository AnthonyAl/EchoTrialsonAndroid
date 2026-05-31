package com.unipi.alexandris.android.echotrialsonandroid.utility;


import android.graphics.Rect;
import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.data.DeathCause;
import com.unipi.alexandris.android.echotrialsonandroid.model.Block;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpike;
import com.unipi.alexandris.android.echotrialsonandroid.model.Player;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.SoundFXManager;
import com.unipi.alexandris.android.echotrialsonandroid.utility.audio.SoundID;
import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.model.Particle;
import com.unipi.alexandris.android.echotrialsonandroid.model.PhysicsObject;

/**
 * The PhysicsPlatformer class handles physics simulation for platforming mechanics.
 * This is a 1-to-1 adaptation of the original JavaFX PhysicsPlatformer.
 * It manages:
 * <ul>
 *   <li>Movement physics with collision detection</li>
 *   <li>Jumping and wall-jumping mechanics</li>
 *   <li>Swimming and water physics</li>
 *   <li>Surface-specific behavior (ice, blocks)</li>
 * </ul>
 * This class is central to the game's platforming mechanics and player movement.
 */
@SuppressWarnings(value = "unused")
public class PhysicsPlatformer {

    private final ParticleCreator particleCreator;
    /** Wall-jump flags for left and right walls. */
	private boolean wjl = true, wjr = true;
	
	/** Current velocity components. */
	private double velx = 0, vely = 0;
	
	/** Return array for movement physics calculations. */
	private final double[] ret = new double[4];
	
	/** Return array for swimming physics calculations. */
	private final double[] ret2 = new double[5];

    private final PhysicsObject object;
	
	/** Horizontal movement speed. */
	private double speedX = 5;
	
	/** Vertical movement speed (jump height). */
	private double speedY = 15;
	
	/** Gravity strength and direction. */
	private double gravity = 1;

    private double gravityX = 0;

    private double gravityY = 1;
	
	/** Flag indicating if last jump was from ice surface. */
	private boolean jumpedFromIce = false;
	
	/** Sound manager for jump effects. */
	private final SoundFXManager soundManager;

    private TickRunnable controlReleaseTask;
	

    public PhysicsPlatformer(PhysicsObject object, SoundFXManager soundManager) {
        this.object = object;
        this.soundManager = soundManager;
        this.particleCreator = new ParticleCreator(GameConstants.INSTANCE.getObjectHandler());
    }


    public void checkLethalInteractionCollision() {
        // TODO: switch this interaction to happen on player-side
        if(object.getSize() == 0) return;
        if(!(object instanceof BlockSpike)) return;
        BlockSpike spike = (BlockSpike) object;
        spike.stepDamageCooldown();

        // Check collision with all interactive objects (based on interaction mask)
        for (PhysicsObject target : spike.getInteractiveObjects()) {
            // Proper collision check using bounds intersection with temporary region
            Rect targetBounds = target.getBounds();
            // Create a temporary region for intersection test to avoid modifying the original
            Region tempRegion = new Region(spike.getShapeArea());
            tempRegion.op(targetBounds, Region.Op.INTERSECT);

            if (!tempRegion.isEmpty()) {
                // Only apply damage if cooldown is not active
                if (spike.getDamageCooldown() <= 0) {
                    // Object touched spike - apply damage
                    if (target instanceof Player && spike.isLethal()) {
                        ((Player) target).takeDamage(DeathCause.SPIKE);
                    }
                    // Start damage cooldown to prevent repeated damage
                    spike.setDamageCooldown();
                }
            }
        }
    }

    public void blockMovementPhysics() {
        if(object.getSize() == 0) return;
        if(!(object instanceof Block)) return;
        Block block = (Block) object;

        // Calculate movement based on current velocity (not random offset)
        velx = block.getVelX();
        vely = block.getVelY();

        // Calculate intended new position
        double newX = block.getX() + velx;
        double newY = block.getY() + vely;

        if((!block.isCancelLEFT() && velx < 0) || (!block.isCancelRIGHT() && velx > 0)) {
            block.setX(newX);
        }
        if((!block.isCancelUP() && vely < 0) || (!block.isCancelDOWN() && vely > 0)) {
            block.setY(newY);
        }

        for (PhysicsObject target : block.getInteractiveObjects()) {
            applyCollisionOnTarget(target);
        }
    }

    /**
     * Applies collision effects to any interactive object.
     * This generalizes the previous player-specific collision logic.
     */
    protected final void applyCollisionOnTarget(PhysicsObject target) {
        if(object instanceof BlockSpike) return;
        if(!(object instanceof Block)) return;
        Block block = (Block) object;

        Region up = new Region(getRectangle(target.getX() + 3, target.getY() - vely - 10,
                target.getWidth() - 6, 10 - vely));

        Region down = new Region(getRectangle(target.getX() + 3, target.getY() + target.getHeight() + 3,
                target.getWidth() - 6, 5 - vely));

        Region left = new Region(getRectangle(target.getX() - Math.abs(velx) * 1.3 - 3, target.getY() + 2,
                5 + Math.abs(velx) * 1.3, target.getHeight() - 4));

        Region right = new Region(getRectangle(target.getX() + target.getWidth() + 3, target.getY() + 2,
                5 + Math.abs(velx) * 1.3, target.getHeight() - 4));

        if (Math.abs(block.getVelX()) == 0 && Math.abs(block.getVelY()) == 0) {
            return; // No movement, no collision
        }

        Region upTest = new Region(up);
        Region downTest = new Region(down);
        Region leftTest = new Region(left);
        Region rightTest = new Region(right);
        upTest.op(block.getShapeArea(), Region.Op.INTERSECT);
        downTest.op(block.getShapeArea(), Region.Op.INTERSECT);
        leftTest.op(block.getShapeArea(), Region.Op.INTERSECT);
        rightTest.op(block.getShapeArea(), Region.Op.INTERSECT);

        Player player;
        if(target instanceof Player) {
            player = (Player) target;
        } else {
            player = null;
        }

        if(player != null) {
            Region upSuffocationTest = new Region(up);
            Region downSuffocationTest = new Region(down);
            Region leftSuffocationTest = new Region(left);
            Region rightSuffocationTest = new Region(right);
            upSuffocationTest.op(GameConstants.INSTANCE.getGlobalRigidArea(), Region.Op.INTERSECT);
            downSuffocationTest.op(GameConstants.INSTANCE.getGlobalRigidArea(), Region.Op.INTERSECT);
            leftSuffocationTest.op(GameConstants.INSTANCE.getGlobalRigidArea(), Region.Op.INTERSECT);
            rightSuffocationTest.op(GameConstants.INSTANCE.getGlobalRigidArea(), Region.Op.INTERSECT);

            if((!upSuffocationTest.isEmpty() && !downSuffocationTest.isEmpty() && Math.abs(block.getVelY()) > 0)
                    || (!leftSuffocationTest.isEmpty() && !rightSuffocationTest.isEmpty() && Math.abs(block.getVelX()) > 0))
                player.takeDamage(DeathCause.SUFFOCATION);
        }

        if (Math.abs(block.getVelY()) > 0) {
            if (block.getVelY() < 0 && !downTest.isEmpty()) {
                if(target.getPhysics().getGravity() <= 0) target.setCancelUP(true);
                target.setY(target.getY() + block.getVelY());

            } else if (block.getVelY() > 0 && !upTest.isEmpty()) {
                if(target.getPhysics().getGravity() >= 0) target.setCancelUP(true);
                target.setY(target.getY() + block.getVelY());
            }
            else if (block.getVelY() < 0 && !upTest.isEmpty()) {
                target.setCancelUP(false);
            }
            else if (block.getVelY() > 0 && !downTest.isEmpty()) {
                target.setCancelUP(false);
            }
        }

        if (Math.abs(block.getVelX()) > 0) {
            if (block.getVelX() < 0 && !rightTest.isEmpty()) {
                target.setCancelRIGHT(true);
                target.setX(target.getX() + block.getVelX());
            } else if (block.getVelX() > 0 && !leftTest.isEmpty()) {
                target.setCancelLEFT(true);
                target.setX(target.getX() + block.getVelX());
            }
        }
        if(player == null) return;

        if(controlReleaseTask!= null) {
            GameConstants.INSTANCE.getObjectHandler().removeTask(controlReleaseTask);
        }

        controlReleaseTask = new TickRunnable(() -> {
            player.setCancelDOWN(false);
            player.setCancelUP(false);
            player.setCancelLEFT(false);
            player.setCancelRIGHT(false);
        }, 5, 1, 1);

        GameConstants.INSTANCE.getObjectHandler().runRepeating(controlReleaseTask);
    }

    public void particleSubDividePhysics(Region breakPoint, int limit) {
        if(!(object instanceof Particle)) return;
        Particle particle = (Particle) object;
        if(particle.getSize() < limit) return;
        if(particle.getVelX() == 0 && particle.getVelY()==0) return;

        // Create a temporary region for intersection test to avoid modifying the original
        Region tempRegion = new Region(particle.getBounds());
        tempRegion.op(breakPoint, Region.Op.INTERSECT);

        if (!tempRegion.isEmpty()) {
            try {
                if(GameConstants.INSTANCE.getObjectHandler() != null) {
                    particleCreator.spawn(1, (float) particle.getX(), (float) particle.getY(), new String[]{particle.getColor()}, particle.getSize() / 2, 3, 10,
                            10, 2, particle.getTime() / 2, particle.getPhysics().getGravityX(), particle.getPhysics().getGravityY(),
                            -particle.getVelX() * 0.7, -particle.getVelY() * 0.7, particle.getBounceX(), particle.getBounceY(), particle.isRigid());

                    particleCreator.spawn(1, (float) particle.getX(), (float) particle.getY(), new String[]{particle.getColor()}, particle.getSize() / 2, 3, 10,
                            10, 2, particle.getTime() / 2, particle.getPhysics().getGravityX(), particle.getPhysics().getGravityY(),
                            particle.getVelX() * 0.7, particle.getVelY() * 0.7, particle.getBounceX(), particle.getBounceY(), particle.isRigid());

                    GameConstants.INSTANCE.getObjectHandler().removeObject(particle);
                }
            } catch (Exception ignored) {}
        }
    }

    public void particleMovementPhysics(Region obstruction) {
        if(!(object instanceof Particle)) return;
        Particle particle = (Particle) object;

        // Calculate movement based on current velocity (not random offset)
        velx = particle.getVelX();
        vely = particle.getVelY();


        Region blockCollisions = new Region(obstruction);

        if(blockCollisions.isEmpty()) {
            // No collisions - move freely
            particle.setX(particle.getX() + velx);
            particle.setY(particle.getY() + vely);
        }
        else {
            // Test movement with collision detection
            Rect currentBounds = particle.getBounds();

            // Test horizontal movement
            Rect testHorizontal = new Rect(currentBounds);
            testHorizontal.offset((int)velx, 0);
            Region horizTest = new Region(testHorizontal);
            horizTest.op(blockCollisions, Region.Op.INTERSECT);

            if (horizTest.isEmpty()) {
                particle.setX(particle.getX() + velx);
            } else {
                // Hit block horizontally - stop horizontal movement
                particle.setVelX(-particle.getVelX() * particle.getBounceX());
            }

            // Test vertical movement
            Rect testVertical = new Rect(currentBounds);
            testVertical.offset(0, (int)vely);
            Region vertTest = new Region(testVertical);
            vertTest.op(blockCollisions, Region.Op.INTERSECT);

            if (vertTest.isEmpty()) {
                particle.setY(particle.getY() + vely);
            } else {
                // Hit block vertically - stop vertical movement
                particle.setVelY(-particle.getVelY() * particle.getBounceY());
                particle.setVelX(particle.getVelX() * 0.5);
            }
        }

        // Apply gravity
        if(particle.getVelX() != gravityX) particle.setVelX(particle.getVelX() + gravityX);
        if(particle.getVelY() != gravityY)  particle.setVelY(particle.getVelY() + gravityY);
    }

    public void actorMovementPhysics(Region obstruction) {
        if(object.getSize() == 0) return;

        // Create collision regions at current position
        Region up = new Region(getRectangle(object.getX() +4, object.getY() - Math.abs(vely+(gravity > 0 ? 0 : gravity)),
                object.getWidth() -8, Math.abs(vely+(gravity > 0 ? 0 : gravity))));

        Region down = new Region(getRectangle(object.getX() +4, object.getY() + object.getHeight(),
                object.getWidth() -8, Math.abs(vely+(gravity > 0 ? gravity : 0))));

        Region left = new Region(getRectangle(object.getX() -Math.abs(velx)*2 -3, object.getY() +2,
                5+Math.abs(velx)*2, object.getHeight() -4));

        Region right = new Region(getRectangle(object.getX() +object.getWidth() +3, object.getY() +2,
                5+Math.abs(velx)*2, object.getHeight() -4));
        Region jump;
        
        if(gravity >= 0) {

            jump = new Region(getRectangle(object.getX() +3, object.getY() + object.getHeight(),
                    object.getWidth() -6, 15));

            assertGravity(obstruction, object.isPressUP(), object.isPressDOWN(), down, up, jump);
        }
        else {

            jump = new Region(getRectangle(object.getX() +3, object.getY() - 15,
                    object.getWidth() -6, 15));

            assertGravity(obstruction, object.isPressUP(), object.isPressDOWN(), up, down, jump);
        }


        if(speedX >= 0) {
            walk(obstruction, object.isPressUP(), object.isPressLEFT(), object.isPressRIGHT(), up, down, left, right, speedX);
        }
        else {
            walk(obstruction, object.isPressUP(), object.isPressRIGHT(), object.isPressLEFT(), up, down, left, right, -speedX);
        }
        
        // Calculate intended new position
        double newX = object.getX() + velx;
        double newY = object.getY() + vely / 2;

        // Prevent sprite from clipping into walls when the velocity is very high
        Region spriteCheckX;
        Region spriteCheckY;
        Region spriteX;
        Region spriteY;

        for(int i = 0; i < 3; i++) {
            spriteY = new Region(getRectangle(newX +object.getWidth()*0.5, newY, object.getWidth()*0.5, object.getHeight()));
            spriteCheckY = new Region(spriteY);
            spriteCheckY.op(obstruction, Region.Op.INTERSECT);
            if (!spriteCheckY.isEmpty()) {
                newY -= vely / 6;
            }

            spriteX = new Region(getRectangle(newX, newY +object.getHeight()*0.5, object.getWidth(), object.getHeight()*0.5));
            spriteCheckX = new Region(spriteX);
            spriteCheckX.op(obstruction, Region.Op.INTERSECT);
            if(!spriteCheckX.isEmpty()) {
                newX -= velx / 3;
            }
        }

        object.setX(newX);
        object.setY(newY);
        object.setVelX(velx);
        object.setVelY(vely);
        assertCollisions(obstruction);
    }

    /**
     * Helper method for handling walking and wall-jumping mechanics.
     * Manages movement on different surfaces and wall interactions.
     * EXACT 1-to-1 adaptation of original JavaFX walk method.
     *
     * @param block Collision shapeArea for solid blocks
     * @param w Up/jump input
     * @param a Left input
     * @param d Right input
     * @param up Upper collision shapeArea
     * @param down Lower collision shapeArea
     * @param left Left collision shapeArea
     * @param right Right collision shapeArea
     * @param speedX Movement speed in X direction
     */
    private void walk(Region block, boolean w, boolean a, boolean d, Region up, Region down, Region left, Region right, double speedX) {
        Region iceDown = new Region(down);
        Region iceLeft = new Region(left);
        iceLeft.op(GameConstants.INSTANCE.getGlobalIceArea(), Region.Op.INTERSECT);
        Region iceRight = new Region(right);
        iceRight.op(GameConstants.INSTANCE.getGlobalIceArea(), Region.Op.INTERSECT);
        Region rockDown = new Region(down);
        rockDown.op(block, Region.Op.INTERSECT);
        iceDown.op(GameConstants.INSTANCE.getGlobalIceArea(), Region.Op.INTERSECT);
        
        if (d) {
            Region rightTest = new Region(right);
            rightTest.op(block, Region.Op.INTERSECT);
            if (!rightTest.isEmpty()) {
                velx = 0;

                if(gravity > 0) {
                    // Wall jump logic for right wall
                    Region upTest = new Region(up);
                    upTest.op(block, Region.Op.INTERSECT);
                    Region downTest = new Region(down);
                    downTest.op(block, Region.Op.INTERSECT);
                    
                    if (w && upTest.isEmpty() && wjr && iceRight.isEmpty()) {
                        double newVelY;
                        if(downTest.isEmpty()) {
                            newVelY = -(speedY - 0.1 * speedY);
                            wjr = false;
                        }
                        else {
                            newVelY = -speedY;
                        }
                        vely = newVelY;
                    }
                }
                else if(gravity < 0) {
                    // Wall jump logic for right wall (inverted gravity)
                    Region upTest = new Region(up);
                    upTest.op(block, Region.Op.INTERSECT);
                    Region downTest = new Region(down);
                    downTest.op(block, Region.Op.INTERSECT);
                    if (w && downTest.isEmpty() && wjr && iceRight.isEmpty()) {
                        double newVelY;
                        if(upTest.isEmpty()) {
                            newVelY = (speedY - 0.1 * speedY);
                            wjr = false;
                        }
                        else {
                            newVelY = speedY;
                        }
                        vely = newVelY;
                    }
                }

            } else {
                velx = speedX;
            }
        } else if (a) {
            Region leftTest = new Region(left);
            leftTest.op(block, Region.Op.INTERSECT);
            if (!leftTest.isEmpty()) {
                velx = 0;

                if(gravity > 0) {
                    // Wall jump logic for left wall
                    Region upTest = new Region(up);
                    upTest.op(block, Region.Op.INTERSECT);
                    Region downTest = new Region(down);
                    downTest.op(block, Region.Op.INTERSECT);

                    if (w && upTest.isEmpty() && wjl && iceLeft.isEmpty()) {
                        double newVelY;
                        if(downTest.isEmpty()) {
                            newVelY = -(speedY - 0.1 * speedY);
                            wjl = false;
                        }
                        else {
                            newVelY = -speedY;
                        }
                        vely = newVelY;

                    }
                }
                else if(gravity < 0) {
                    // Wall jump logic for left wall (inverted gravity)
                    Region downTest = new Region(down);
                    downTest.op(block, Region.Op.INTERSECT);
                    Region upTest = new Region(up);
                    upTest.op(block, Region.Op.INTERSECT);
                    if (w && downTest.isEmpty() && wjl && iceLeft.isEmpty()) {
                        double newVelY;
                        if(upTest.isEmpty()) {
                            newVelY = (speedY - 0.1 * speedY);
                            wjl = false;
                        }
                        else {
                            newVelY = speedY;
                        }
                        vely = newVelY;
                    }
                }

            } else {
                velx = -speedX;
            }
        } else {
            // No directional input - apply friction/momentum
            Region leftTest = new Region(left);
            Region rightTest = new Region(right);
            leftTest.op(block, Region.Op.INTERSECT);
            rightTest.op(block, Region.Op.INTERSECT);
            
            // Apply friction if not blocked by walls
            if (Math.abs(velx) > 0.25 && leftTest.isEmpty() && rightTest.isEmpty()) {
                if (!iceDown.isEmpty()) {
                    velx += 0.0085 * velx;
                    jumpedFromIce = true;
                }
                else if(!rockDown.isEmpty()) jumpedFromIce = false;
                if(jumpedFromIce) {
                    velx -= 0.05 * velx;
                }
                else {
                    velx -= 0.2 * velx;
                }
            }
            else velx = 0;
        }
    }

    private void assertCollisions(Region obstruction) {

        Region up = new Region(getRectangle(object.getX() -object.getWidth()*0.1, object.getY() - object.getVelH() -object.getHeight()*0.1,
                object.getWidth() *1.1, 5+object.getVelH()));

        Region down = new Region(getRectangle(object.getX() -object.getWidth()*0.1, object.getY() +object.getHeight() +object.getHeight()*0.1,
                object.getWidth() *1.1, 5+object.getVelH()));

        Region left = new Region(getRectangle(object.getX() - object.getVelW() -object.getWidth()*0.1, object.getY()+object.getHeight()*0.1,
                5+object.getVelW(), object.getHeight() *0.8));

        Region right = new Region(getRectangle(object.getX() + object.getWidth() +object.getWidth()*0.1, object.getY()+object.getHeight()*0.1,
                5+object.getVelW(), object.getHeight() *0.8));

        Region upTest = new Region(up);
        upTest.op(obstruction, Region.Op.INTERSECT);
        Region downTest = new Region(down);
        downTest.op(obstruction, Region.Op.INTERSECT);
        Region leftTest = new Region(left);
        leftTest.op(obstruction, Region.Op.INTERSECT);
        Region rightTest = new Region(right);
        rightTest.op(obstruction, Region.Op.INTERSECT);

        if(Math.abs(object.getVelH())>0){
            if (!upTest.isEmpty() && downTest.isEmpty()) {
                object.setY(object.getY() + object.getVelH());
                object.setHeightBound(false);
            } else if (upTest.isEmpty() && !downTest.isEmpty()) {
                object.setY(object.getY() - object.getVelH());
                object.setHeightBound(false);
            } else if (!upTest.isEmpty() && !downTest.isEmpty()) {
                object.setHeight(object.getHeight() - object.getVelH());
                object.setHeightBound(true);
            }
        }

        if(Math.abs(object.getVelW())>0) {
            if (!leftTest.isEmpty() && rightTest.isEmpty()) {
                object.setX(object.getX() + object.getVelW());
                object.setWidthBound(false);
            } else if (leftTest.isEmpty() && !rightTest.isEmpty()) {
                object.setX(object.getX() - object.getVelW());
                object.setWidthBound(false);
            } else if (!leftTest.isEmpty() && !rightTest.isEmpty()) {
                object.setWidth(object.getWidth() - object.getVelW());
                object.setWidthBound(true);
            }
        }

        // Set the position, but physics will correct it if needed to prevent clipping
        if(!object.isWidthBound() && Math.abs(object.getVelW())>0) object.setX(object.getX() - object.getVelW() /2);
        if(!object.isHeightBound() && Math.abs(object.getVelH())>0) object.setY(object.getY() - object.getVelH() /2);
    }

    /**
     * Helper method for handling gravity and jumping mechanics.
     * Manages vertical movement and jump sound effects.
     * EXACT 1-to-1 adaptation of original JavaFX assertGravity method.
     *
     * @param block Collision shapeArea for solid blocks
     * @param w Up/jump input
     * @param a First collision shapeArea to check
     * @param b Second collision shapeArea to check
     */
    private void assertGravity(Region block, boolean w, boolean s, Region a, Region b, Region jump) {
        Region aTest = new Region(a);
        aTest.op(block, Region.Op.INTERSECT);
        Region jumpTest = new Region(jump);
        jumpTest.op(block, Region.Op.INTERSECT);

        if(gravity == 0) {
            wjl = false;
            wjr = false;
            Region bTest = new Region(b);
            bTest.op(block, Region.Op.INTERSECT);
            if(w) {
                if (bTest.isEmpty()) vely = -speedY;
                else vely = 0;
            }
            if (s) {
                if(aTest.isEmpty()) vely = speedY;
                else vely = 0;
            }
            if(Math.abs(vely) > 1 && !w && !s) vely *= 0.5;
            else if(Math.abs(vely) <= 1 && !w && !s) vely = 0;
            
            // In zero gravity, player is never on ground
            if (object instanceof Player) {
                ((Player) object).setOnGround(false);
            }
            return;
        }

        if (aTest.isEmpty()) {
            vely += gravity;
            // Player is in air
            if (object instanceof Player) {
                ((Player) object).setOnGround(false);
            }
        } else {
            wjl = true;
            wjr = true;
            if(gravity > 0 && vely > 0) vely = 0;
            else if (gravity < 0 && vely < 0) vely = 0;
            // Player is on ground
            if (object instanceof Player) {
                ((Player) object).setOnGround(true);
            }
        }
        if(!jumpTest.isEmpty()) {
            Region bTest = new Region(b);
            bTest.op(block, Region.Op.INTERSECT);
            if (w && bTest.isEmpty()) {
                if(gravity > 0) vely = -speedY;
                else if (gravity < 0) vely = speedY;
                // Play jump sound when actually jumping
                if (soundManager != null && object.isPlaySounds()) {
                    soundManager.playSound(SoundID.JUMP);
                }
            }
        }
    }

    /**
     * Creates a Rect with the specified dimensions.
     * Helper method for collision shapeArea creation.
     * EXACT 1-to-1 adaptation of original JavaFX getRectangle method.
     *
     * @param a X coordinate
     * @param b Y coordinate
     * @param width Width
     * @param height Height
     * @return Rect with the specified dimensions
     */
    public Rect getRectangle(double a, double b, double width, double height) {
        return new Rect((int) a, (int) b, (int) (a + width), (int) (b + height));
    }
    
    /**
     * Resets velocity to zero.
     */
    public void resetVelocity() {
        velx = 0;
        vely = 0;
    }

    public void setSpeedX(double speedX) {
        this.speedX = speedX;
    }

    public double getSpeedX() {
        return speedX;
    }

    public void setSpeedY(double speedY) {
        this.speedY = speedY;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public void setGravityX(double gravityX) {
        this.gravityX = gravityX;
    }

    public void setGravityY(double gravityY) {
        this.gravityY = gravityY;
    }

    public double getGravityX() {
        return gravityX;
    }

    public double getGravityY() {
        return gravityY;
    }

    public double getGravity() {
        return gravity;
    }

    public double getSpeedY() {
        return speedY;
    }
    
    
}