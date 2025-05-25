package me.zombii.horizon.collision.vanilla;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.github.puzzle.core.loader.util.Reflection;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.PlayerEntity;
import finalforeach.cosmicreach.util.Axis;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
import me.zombii.horizon.collision.AABB;
import me.zombii.horizon.entity.api.IPhysicEntity;
import me.zombii.horizon.entity.api.IVirtualZoneEntity;
import me.zombii.horizon.rendering.mesh.IBlockBoundsMaker;
import me.zombii.horizon.util.ConversionUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EntityCollision {
//
//    /**
//     *  Vanilla facing method: {@link Entity#updatePositions(Zone, float)}
//     */
//    public static <T extends Entity & IPhysicEntity> void updatePositions(T entity, Zone zone, float deltaTime) {
//        if (entity.currentChunk != null) {
//        }
//    }
//

    static final AABB TMP_AABB = new AABB();
    static final BoundingBox TMP_BOUNDS = new BoundingBox();
    static final Vector3 TMP_VEC = new Vector3();

    static float minPosY = 0;
    static float maxPosY = 0;

    static final Vector3 TARGET_POS = new Vector3();
    private static float floorFriction = 0;

    /**
     *  Vanilla facing method: {@link Entity#updateConstraints(Zone, Vector3)}
     */
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
        minBx = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.min.x);
        minBy = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.min.y);
        minBz = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.min.z);
        maxBx = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.max.x);
        maxBy = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.max.y);
        maxBz = (int)Math.floor((double)theEntity.tmpEntityBoundingBox.max.z);
        constrainBySneaking = shouldConstrainBySneak(theEntity, zone, theEntity.tmpBlockBoundingBox, theEntity.tmpEntityBoundingBox, minBx, minBy, minBz, maxBx, maxBz);
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
        Method m = Reflection.getMethod(Entity.class, "shouldConstrainBySneak", Zone.class, BoundingBox.class, BoundingBox.class, int.class, int.class, int.class, int.class, int.class);
        try {
            return (boolean) m.invoke(theEntity, zone, tmpBlockBoundingBox, tmpEntityBoundingBox, minBx, minBy, minBz, maxBx, maxBz);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setupTmpBounds(Entity theEntity, Vector3 targetPosition) {
        TMP_AABB.setMax(TMP_VEC.set(theEntity.localBoundingBox.max).add(
                theEntity.position.x,
                theEntity.position.y + targetPosition.y,
                theEntity.position.z
        ));
        TMP_AABB.setMin(TMP_VEC.set(theEntity.localBoundingBox.min).add(
                theEntity.position.x,
                theEntity.position.y + targetPosition.y,
                theEntity.position.z
        ));
        TMP_AABB.update();

        theEntity.tmpEntityBoundingBox.max.set(theEntity.localBoundingBox.max).add(
                theEntity.position.x,
                theEntity.position.y + targetPosition.y,
                theEntity.position.z
        );
        theEntity.tmpEntityBoundingBox.min.set(theEntity.localBoundingBox.min).add(
                theEntity.position.x,
                theEntity.position.y + targetPosition.y,
                theEntity.position.z
        );
        theEntity.tmpEntityBoundingBox.update();
    }

    static final Matrix4 matrix4f = new Matrix4();

    private static Boolean testHit(Entity theEntity, Entity[] entities, Zone zone, Vector3 blockPosition) {
        for (Entity entity : entities) {
            if (entity instanceof PlayerEntity pe) continue;

            BoundingBox eb = entity.globalBoundingBox;

            if (((ExtendedBoundingBox) eb).hasInnerBounds()) {
                if (((ExtendedBoundingBox) eb).getInnerBounds().contains(theEntity.tmpEntityBoundingBox)) {
                    if (entity instanceof IPhysicEntity) {
                        matrix4f.idt();
                        matrix4f.set(ConversionUtil.fromJME(((IPhysicEntity) entity).getEularRotation()));
                        Vector3 v = blockPosition.cpy().sub(entity.position).unrotate(matrix4f);

                        if (entity instanceof IVirtualZoneEntity vz) {
                            BlockPosition pos = BlockPosition.ofGlobal(vz.getWorld(), (int) v.x, (int) v.y, (int) v.z);
                            if (pos.chunk == null) return false;
                            BlockState blockState = pos.getBlockState();
                            if (checkState(theEntity, zone, blockState, pos, v)) return true;
                        }
                    } else return true;
                }
            } else if (eb.contains(theEntity.tmpEntityBoundingBox)) {
                return true;
            }
        }


        BlockPosition pos = BlockPosition.ofGlobal(zone, (int) blockPosition.x, (int) blockPosition.y, (int) blockPosition.z);
        if (pos.chunk == null) return false;
        BlockState blockState = pos.getBlockState();

        if (checkState(theEntity, zone, blockState, pos, blockPosition)) return true;

        return false;
    }

    private static boolean checkState(Entity theEntity, Zone zone, BlockState blockState, BlockPosition pos, Vector3 point) {
        if (blockState != null && !blockState.walkThrough) {
            float oldY = theEntity.tmpEntityBoundingBox.min.y;

            for (AABB bb : ((IBlockBoundsMaker) blockState.getModel()).getBounds(pos.getGlobalX(), pos.getGlobalY(), pos.getGlobalZ())) {
                if (bb.intersects(TMP_AABB)) {
                    theEntity.velocity.y = 0;
                    theEntity.onceVelocity.y = 0;
                    if (oldY <= bb.getMax().y && oldY >= bb.getMin().y) {
                        minPosY = Math.max(minPosY, bb.getMax().y - theEntity.localBoundingBox.min.y);
                        maxPosY = Math.max(maxPosY, minPosY);
                        if (!theEntity.isOnGround) {
                            theEntity.footstepTimer = 0.45F;
                        }

                        theEntity.isOnGround = true;
                        floorFriction = Math.max(floorFriction, blockState.friction);
                        theEntity.blockBouncinessY = Math.max(theEntity.blockBouncinessY, blockState.bounciness);
                    } else {
                        maxPosY = Math.min(maxPosY, bb.getMin().y - theEntity.localBoundingBox.getHeight() - 0.01F);
                        theEntity.blockBouncinessY = Math.min(theEntity.blockBouncinessY, -blockState.bounciness);
                    }

                    theEntity.collidedY = true;
                    theEntity.onCollideWithBlock(Axis.Y, blockState, TARGET_POS, (int) point.x, (int) point.y, (int) point.z);
                    return true;
                }
            }
        }
        return false;
    }
}
