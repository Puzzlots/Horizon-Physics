package me.zombii.horizon.collision.vanilla;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import dev.puzzleshq.puzzleloader.loader.util.ReflectionUtil;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.util.Axis;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.entity.api.IPhysicEntity;
import me.zombii.horizon.rendering.mesh.IBlockBoundsMaker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Trash {

    public static void updateConstraints(Entity theEntity, Zone zone, Vector3 targetPosition) {
        if (theEntity instanceof IPhysicEntity) return;

        Entity[] entities = zone.getAllEntities().toArray(Entity.class);

        float floorFriction = 0.0F;
        theEntity.tmpEntityBoundingBox.set(theEntity.localBoundingBox);
        theEntity.tmpEntityBoundingBox.min.add(theEntity.position);
        theEntity.tmpEntityBoundingBox.max.add(theEntity.position);
        theEntity.tmpEntityBoundingBox.min.y = theEntity.localBoundingBox.min.y + targetPosition.y;
        theEntity.tmpEntityBoundingBox.max.y = theEntity.localBoundingBox.max.y + targetPosition.y;
        theEntity.tmpEntityBoundingBox.update();
        theEntity.collidedX = false;
        theEntity.collidedY = false;
        theEntity.collidedZ = false;
        int minBx = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.min.x);
        int minBy = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.min.y);
        int minBz = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.min.z);
        int maxBx = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.max.x);
        int maxBy = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.max.y);
        int maxBz = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.max.z);
        boolean isOnGround = false;
        float minPosY = targetPosition.y;
        float maxPosY = targetPosition.y;

        for(int bx = minBx; bx <= maxBx; ++bx) {
            for(int by = minBy; by <= maxBy; ++by) {
                for(int bz = minBz; bz <= maxBz; ++bz) {
                    BlockState blockAdj = zone.getBlockState(bx, by, bz);
                    if (blockAdj != null && !blockAdj.walkThrough) {
                        blockAdj.getBoundingBox(theEntity.tmpBlockBoundingBox, bx, by, bz);
                        if (theEntity.tmpBlockBoundingBox.intersects(theEntity.tmpEntityBoundingBox)) {
                            float oldY = theEntity.tmpEntityBoundingBox.min.y;

                            for (BoundingBox bb : ((IBlockBoundsMaker) blockAdj.getModel()).getBoundsGdx(bx, by, bz)) {
                                if (bb.intersects(theEntity.tmpEntityBoundingBox)) {
                                    theEntity.velocity.y = 0.0F;
                                    theEntity.onceVelocity.y = 0.0F;
                                    if (oldY <= bb.max.y && oldY >= bb.min.y) {
                                        minPosY = Math.max(minPosY, bb.max.y - theEntity.localBoundingBox.min.y);
                                        maxPosY = Math.max(maxPosY, minPosY);
                                        if (!theEntity.isOnGround) {
                                            theEntity.footstepTimer = 0.45F;
                                        }

                                        isOnGround = true;
                                        floorFriction = Math.max(floorFriction, blockAdj.friction);
                                        theEntity.blockBouncinessY = Math.max(theEntity.blockBouncinessY, blockAdj.bounciness);
                                    } else {
                                        maxPosY = Math.min(maxPosY, bb.min.y - theEntity.localBoundingBox.getHeight() - 0.01F);
                                        theEntity.blockBouncinessY = Math.min(theEntity.blockBouncinessY, -blockAdj.bounciness);
                                    }

                                    theEntity.collidedY = true;
                                    theEntity.onCollideWithBlock(Axis.Y, blockAdj, targetPosition, bx, by, bz);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isOnGround) {
            theEntity.floorFriction = floorFriction;
        } else if (!theEntity.isInFluid() && !theEntity.isNoClip()) {
            theEntity.floorFriction = 0.1F;
        } else {
            theEntity.floorFriction = 1.0F;
        }

        targetPosition.y = MathUtils.clamp(targetPosition.y, minPosY, maxPosY);
        theEntity.isOnGround = isOnGround;
        theEntity.tmpEntityBoundingBox.min.x = theEntity.localBoundingBox.min.x + targetPosition.x;
        theEntity.tmpEntityBoundingBox.max.x = theEntity.localBoundingBox.max.x + targetPosition.x;
        theEntity.tmpEntityBoundingBox.min.y = theEntity.localBoundingBox.min.y + targetPosition.y + 0.01F;
        theEntity.tmpEntityBoundingBox.max.y = theEntity.localBoundingBox.max.y + targetPosition.y;
        theEntity.tmpEntityBoundingBox.update();
        minBx = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.min.x);
        minBy = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.min.y);
        minBz = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.min.z);
        maxBx = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.max.x);
        maxBy = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.max.y);
        maxBz = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.max.z);
        boolean constrainBySneaking = shouldConstrainBySneak(theEntity, zone, theEntity.tmpBlockBoundingBox, theEntity.tmpEntityBoundingBox, minBx, minBy, minBz, maxBx, maxBz);
        if (constrainBySneaking) {
            theEntity.onceVelocity.x = 0.0F;
            theEntity.velocity.x = 0.0F;
            targetPosition.x = theEntity.position.x;
        }

        boolean steppedUpForAll = true;
        float desiredStepUp = targetPosition.y;
        if (!constrainBySneaking) {
            for(int bx = minBx; bx <= maxBx; ++bx) {
                for(int by = minBy; by <= maxBy; ++by) {
                    for(int bz = minBz; bz <= maxBz; ++bz) {
                        BlockState blockAdj = zone.getBlockState(bx, by, bz);
                        if (blockAdj != null && !blockAdj.walkThrough) {
                            blockAdj.getBoundingBox(theEntity.tmpBlockBoundingBox, bx, by, bz);
                            if (theEntity.tmpBlockBoundingBox.intersects(theEntity.tmpEntityBoundingBox)) {
                                boolean didStepUp = false;

                                for (BoundingBox bb : ((IBlockBoundsMaker)blockAdj.getModel()).getBoundsGdx(bx, by, bz)) {
                                    if (bb.intersects(theEntity.tmpEntityBoundingBox)) {
                                        if (!isOnGround || !(bb.max.y - theEntity.tmpEntityBoundingBox.min.y <= theEntity.maxStepHeight) || !(bb.max.y > theEntity.tmpEntityBoundingBox.min.y)) {
                                            didStepUp = false;
                                            steppedUpForAll = false;
                                            break;
                                        }

                                        float currentDesiredStepUp = Math.max(desiredStepUp, bb.max.y - theEntity.localBoundingBox.min.y);
                                        theEntity.tmpEntityBoundingBox2.set(theEntity.tmpEntityBoundingBox);
                                        theEntity.tmpEntityBoundingBox2.min.y = currentDesiredStepUp;
                                        theEntity.tmpEntityBoundingBox2.max.y = currentDesiredStepUp + theEntity.localBoundingBox.getHeight();
                                        theEntity.tmpEntityBoundingBox2.update();
                                        boolean canStepUp = true;

                                        label267:
                                        for (int bax = minBx; bax <= maxBx; ++bax) {
                                            for (int bay = by + 1; bay <= maxBy + 1; ++bay) {
                                                for (int baz = minBz; baz <= maxBz; ++baz) {
                                                    BlockState blockAbove = zone.getBlockState(bax, bay, baz);
                                                    if (blockAbove != null && !blockAbove.walkThrough) {
                                                        blockAbove.getBoundingBox(theEntity.tmpBlockBoundingBox2, bax, bay, baz);
                                                        canStepUp &= !theEntity.tmpBlockBoundingBox2.intersects(theEntity.tmpEntityBoundingBox2);
                                                        if (!canStepUp) {
                                                            break label267;
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (canStepUp) {
                                            desiredStepUp = currentDesiredStepUp;
                                            didStepUp = true;
                                        }
                                    }
                                }

                                if (!didStepUp) {

                                    for (BoundingBox bb : ((IBlockBoundsMaker)blockAdj.getModel()).getBoundsGdx(bx, by, bz)) {
                                        if (bb.intersects(theEntity.tmpEntityBoundingBox)) {
                                            float centX = theEntity.tmpBlockBoundingBox.getCenterX();
                                            if (centX > targetPosition.x) {
                                                targetPosition.x = bb.min.x - theEntity.tmpEntityBoundingBox.getWidth() / 2.0F - 0.01F;
                                            } else {
                                                targetPosition.x = bb.max.x + theEntity.tmpEntityBoundingBox.getWidth() / 2.0F + 0.01F;
                                            }

                                            theEntity.onCollideWithBlock(Axis.X, blockAdj, targetPosition, bx, by, bz);
                                            theEntity.collidedX = true;
                                            theEntity.onceVelocity.x = 0.0F;
                                            theEntity.velocity.x = 0.0F;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (steppedUpForAll) {
            targetPosition.y = desiredStepUp;
        }

        theEntity.tmpEntityBoundingBox.min.set(theEntity.localBoundingBox.min).add(targetPosition.x, targetPosition.y + 0.01F, targetPosition.z);
        theEntity.tmpEntityBoundingBox.max.set(theEntity.localBoundingBox.max).add(targetPosition);
        theEntity.tmpEntityBoundingBox.update();
        minBx = (int)Math.floor(theEntity.tmpEntityBoundingBox.min.x);
        minBy = (int)Math.floor(theEntity.tmpEntityBoundingBox.min.y);
        minBz = (int)Math.floor(theEntity.tmpEntityBoundingBox.min.z);
        maxBx = (int)Math.floor(theEntity.tmpEntityBoundingBox.max.x);
        maxBy = (int)Math.floor(theEntity.tmpEntityBoundingBox.max.y);
        maxBz = (int)Math.floor(theEntity.tmpEntityBoundingBox.max.z);
//        constrainBySneaking = shouldConstrainBySneak(theEntity, zone, theEntity.tmpBlockBoundingBox, theEntity.tmpEntityBoundingBox, minBx, minBy, minBz, maxBx, maxBz);
        constrainBySneaking = false;
        steppedUpForAll = true;
        desiredStepUp = targetPosition.y;
        if (constrainBySneaking) {
            theEntity.onceVelocity.z = 0.0F;
            theEntity.velocity.z = 0.0F;
            targetPosition.z = theEntity.position.z;
        } else {
            for(int bx = minBx; bx <= maxBx; ++bx) {
                for(int by = minBy; by <= maxBy; ++by) {
                    for(int bz = minBz; bz <= maxBz; ++bz) {
                        BlockState blockAdj = zone.getBlockState(bx, by, bz);
                        if (blockAdj != null && !blockAdj.walkThrough) {
                            blockAdj.getBoundingBox(theEntity.tmpBlockBoundingBox, bx, by, bz);
                            if (theEntity.tmpBlockBoundingBox.intersects(theEntity.tmpEntityBoundingBox)) {
                                boolean didStepUp = false;

                                for (BoundingBox bb : ((IBlockBoundsMaker)blockAdj.getModel()).getBoundsGdx(bx, by, bz)) {
                                    if (bb.intersects(theEntity.tmpEntityBoundingBox)) {
                                        if (!isOnGround || !(bb.max.y - theEntity.tmpEntityBoundingBox.min.y <= theEntity.maxStepHeight) || !(bb.max.y > theEntity.tmpEntityBoundingBox.min.y)) {
                                            didStepUp = false;
                                            steppedUpForAll = false;
                                            break;
                                        }

                                        float currentDesiredStepUp = Math.max(desiredStepUp, bb.max.y - theEntity.localBoundingBox.min.y);
                                        theEntity.tmpEntityBoundingBox2.set(theEntity.tmpEntityBoundingBox);
                                        theEntity.tmpEntityBoundingBox2.min.y = currentDesiredStepUp;
                                        theEntity.tmpEntityBoundingBox2.max.y = currentDesiredStepUp + theEntity.localBoundingBox.getHeight();
                                        theEntity.tmpEntityBoundingBox2.update();
                                        boolean canStepUp = true;

                                        label200:
                                        for (int bax = minBx; bax <= maxBx; ++bax) {
                                            for (int bay = by + 1; bay <= maxBy + 1; ++bay) {
                                                for (int baz = minBz; baz <= maxBz; ++baz) {
                                                    BlockState blockAbove = zone.getBlockState(bax, bay, baz);
                                                    if (blockAbove != null && !blockAbove.walkThrough) {
                                                        blockAbove.getBoundingBox(theEntity.tmpBlockBoundingBox2, bax, bay, baz);
                                                        canStepUp &= !theEntity.tmpBlockBoundingBox2.intersects(theEntity.tmpEntityBoundingBox2);
                                                        if (!canStepUp) {
                                                            break label200;
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (canStepUp) {
                                            desiredStepUp = currentDesiredStepUp;
                                            didStepUp = true;
                                        }
                                    }
                                }

                                if (!didStepUp) {

                                    for (BoundingBox bb : ((IBlockBoundsMaker)blockAdj.getModel()).getBoundsGdx(bx, by, bz)) {
                                        if (bb.intersects(theEntity.tmpEntityBoundingBox)) {
                                            float centZ = theEntity.tmpBlockBoundingBox.getCenterZ();
                                            if (centZ > targetPosition.z) {
                                                targetPosition.z = bb.min.z - theEntity.tmpEntityBoundingBox.getDepth() / 2.0F - 0.01F;
                                            } else {
                                                targetPosition.z = bb.max.z + theEntity.tmpEntityBoundingBox.getDepth() / 2.0F + 0.01F;
                                            }

                                            theEntity.onCollideWithBlock(Axis.Z, blockAdj, targetPosition, bx, by, bz);
                                            theEntity.collidedZ = true;
                                            theEntity.onceVelocity.z = 0.0F;
                                            theEntity.velocity.z = 0.0F;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (steppedUpForAll) {
            targetPosition.y = desiredStepUp;
        }

        theEntity.position.set(targetPosition);
    }

    private static boolean shouldConstrainBySneak(Entity theEntity, Zone zone, BoundingBox tmpBlockBoundingBox, BoundingBox tmpEntityBoundingBox, int minBx, int minBy, int minBz, int maxBx, int maxBz) {
        try {
            Method m = ReflectionUtil.getMethod(Entity.class, "shouldConstrainBySneak", Zone.class, BoundingBox.class, BoundingBox.class, int.class, int.class, int.class, int.class, int.class);
            return (boolean) m.invoke(theEntity, zone, tmpBlockBoundingBox, tmpEntityBoundingBox, minBx, minBy, minBz, maxBx, maxBz);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
