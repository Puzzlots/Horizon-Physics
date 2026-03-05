package me.zombii.horizon.collision.vanilla;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.utils.Array;
import dev.puzzleshq.puzzleloader.loader.util.ReflectionUtil;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityUtils;
import finalforeach.cosmicreach.entities.player.PlayerEntity;
import finalforeach.cosmicreach.util.Axis;
import finalforeach.cosmicreach.world.Zone;
import io.github.puzzle.cosmic.api.entity.IEntity;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
import me.zombii.horizon.collision.Collision3D;
import me.zombii.horizon.entity.api.HEntity;
import me.zombii.horizon.entity.api.IPhysicEntity;
import me.zombii.horizon.entity.api.IVirtualZoneEntity;
import me.zombii.horizon.util.ConversionUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EntityCollision {
    
    public static final Vector3 gravity = new Vector3(0, -1f, 0);

    /**
     *  Vanilla facing method: {@link Entity#updatePositions(Zone, float)}
     */
    public static <T extends Entity & IEntity & HEntity> void updatePositions(T entity, Zone zone, float deltaTime) {
        if (entity.currentChunk != null) {
            entity.blockBouncinessY = 0.0F;
            boolean wasOnGround = entity.isOnGround;
            entity.lastPosition.set(entity.position);
            float ax = entity.acceleration.x * deltaTime;
            float ay = entity.acceleration.y * deltaTime;
            float az = entity.acceleration.z * deltaTime;
            entity.velocity.add(ax, ay, az);
            float oldVelocityY = entity.velocity.y;
            if (entity.isNoClip()) {
                entity.floorFriction = 1.0F;
                EntityUtils.applyFriction(1.0F, entity.velocity);
            } else {
                EntityUtils.applyFriction(entity.floorFriction, entity.velocity);
            }

            entity.velocity.add(entity.onceVelocity);
            float vx = entity.velocity.x * deltaTime;
            float vy = entity.velocity.y * deltaTime;
            float vz = entity.velocity.z * deltaTime;
            entity.posDiff.set(vx, vy, vz);
            entity.targetPosition.set(entity.position).add(entity.posDiff);
            if (entity.isNoClip()) {
                entity.position.add(entity.posDiff);
                entity.velocity.sub(entity.onceVelocity);
            } else {
                float d = entity.targetPosition.dst(entity.position);
                if (d < 1.0F) {
                    updateConstraints(entity, zone, entity.targetPosition);
                } else {
                    entity.posDiff.set(entity.targetPosition).sub(entity.position).scl(1.0F / d);
                    entity.targetPosition.set(entity.position);
                    float floor = (float)Math.floor(d);

                    for(float l = 0.0F; l < floor; ++l) {
                        entity.targetPosition.add(entity.posDiff);
                        updateConstraints(entity, zone, entity.targetPosition);
                    }

                    if (d - floor > 0.0F) {
                        entity.posDiff.scl(d - floor);
                        entity.targetPosition.add(entity.posDiff);
                        updateConstraints(entity, zone, entity.targetPosition);
                    }
                }

                if (entity.isOnGround && !wasOnGround) {
                    float displacement = entity.position.y - entity.lastPosition.y;
                    double initialSquared = Math.pow(oldVelocityY, 2.0F);
                    float finalVelocity = (float)Math.sqrt(initialSquared + (double)(2.0F * entity.acceleration.y * displacement));
                    if (Float.isNaN(finalVelocity)) {
                        finalVelocity = 0.0F;
                    }

                    float entityBounciness = entity.getBounciness();
                    float bounceSign = Math.signum(entity.blockBouncinessY);
                    if (bounceSign == 0.0F) {
                        bounceSign = 1.0F;
                    }

                    float bounceFactor = Math.max(Math.abs(entity.blockBouncinessY), entityBounciness) * bounceSign;
                    entity.velocity.y = finalVelocity * bounceFactor;
                    entity.fallDamage.onLand(entity, (finalVelocity + ay / 2.0F) * (1.0F - bounceFactor));
                } else {
                    entity.velocity.sub(entity.onceVelocity);
                }
            }

            entity.getBoundingBox(entity.globalBoundingBox);
            entity.acceleration.setZero();
            entity.onceVelocity.setZero();
            if (entity.isOnGround) {
                if (wasOnGround) {
                    entity.velocity.y = 0.0F;
                }

                if (entity.footstepTimer >= 0.45F) {
                    entity.playFootstepSound();
                }

                float dist = Vector2.dst2(entity.lastPosition.x, entity.lastPosition.z, entity.position.x, entity.position.z) / deltaTime;
                if (entity.position.x - entity.lastPosition.x != 0.0F || entity.position.z - entity.lastPosition.z != 0.0F) {
                    float factor = 1.0F;
                    if ((double)dist > 0.3) {
                        factor = 2.0F;
                    }

                    if ((double)dist < 0.1) {
                        factor = 0.5F;
                    }

                    if ((double)dist < 0.02) {
                        factor = 0.0F;
                    }

                    entity.footstepTimer += deltaTime * factor;
                }
            }
        }

        EntityUtils.updateEntityChunk(zone, entity);
        entity.sendPositionPacket();
    }

    public static <T extends Entity & IEntity & HEntity> void updateConstraints(T entity, Zone zone, Vector3 targetPosition) {
        entity.forEachEntityInNearbyChunks((e) -> {
            if (entity == e) return;
            if (((ExtendedBoundingBox)entity.globalBoundingBox).hasInnerBounds()) return;
            if (!((ExtendedBoundingBox)e.globalBoundingBox).hasInnerBounds()) return;
            OrientedBoundingBox boundingBox = ((ExtendedBoundingBox)e.globalBoundingBox).getInnerBounds();

            Vector3 response = Collision3D.getCollisionResponse(entity.globalBoundingBox, boundingBox);
            targetPosition.sub(response);
        });
        updateConstraintsA(entity, zone, targetPosition);
    }

    /**
     *  Vanilla facing method: {@link Entity#updateConstraints(Zone, Vector3)}
     */
    public static <T extends Entity & IEntity & HEntity> void updateConstraintsA(T entity, Zone zone, Vector3 targetPosition) {
        float floorFriction = 0.0F;
        entity.tmpEntityBoundingBox.set(entity.localBoundingBox);
        entity.tmpEntityBoundingBox.min.add(entity.position);
        entity.tmpEntityBoundingBox.max.add(entity.position);
        entity.tmpEntityBoundingBox.min.y = entity.localBoundingBox.min.y + targetPosition.y;
        entity.tmpEntityBoundingBox.max.y = entity.localBoundingBox.max.y + targetPosition.y;
        entity.tmpEntityBoundingBox.update();
        entity.collidedX = false;
        entity.collidedY = false;
        entity.collidedZ = false;
        int minBx = (int)Math.floor(entity.tmpEntityBoundingBox.min.x);
        int minBy = (int)Math.floor(entity.tmpEntityBoundingBox.min.y);
        int minBz = (int)Math.floor(entity.tmpEntityBoundingBox.min.z);
        int maxBx = (int)Math.floor(entity.tmpEntityBoundingBox.max.x);
        int maxBy = (int)Math.floor(entity.tmpEntityBoundingBox.max.y);
        int maxBz = (int)Math.floor(entity.tmpEntityBoundingBox.max.z);
        boolean isOnGround = false;
        float minPosY = targetPosition.y;
        float maxPosY = targetPosition.y;

        for(int bx = minBx; bx <= maxBx; ++bx) {
            for(int by = minBy; by <= maxBy; ++by) {
                for(int bz = minBz; bz <= maxBz; ++bz) {
                    BlockState blockAdj = zone.getBlockState(bx, by, bz);
                    if (blockAdj != null && !blockAdj.walkThrough) {
                        blockAdj.getBoundingBox(entity.tmpBlockBoundingBox, bx, by, bz);
                        if (entity.tmpBlockBoundingBox.intersects(entity.tmpEntityBoundingBox)) {
                            blockAdj.getAllBoundingBoxes(entity.tmpBlockBoundingBoxes, bx, by, bz);
                            float oldY = entity.tmpEntityBoundingBox.min.y;

                            for (BoundingBox bb : entity.tmpBlockBoundingBoxes) {
                                if (bb.intersects(entity.tmpEntityBoundingBox)) {
                                    entity.velocity.y = 0.0F;
                                    entity.onceVelocity.y = 0.0F;
                                    if (oldY <= bb.max.y && oldY >= bb.min.y) {
                                        minPosY = Math.max(minPosY, bb.max.y - entity.localBoundingBox.min.y);
                                        maxPosY = Math.max(maxPosY, minPosY);
                                        if (!entity.isOnGround) {
                                            entity.footstepTimer = 0.45F;
                                        }

                                        isOnGround = true;
                                        floorFriction = Math.max(floorFriction, blockAdj.friction);
                                        entity.blockBouncinessY = Math.max(entity.blockBouncinessY, blockAdj.bounciness);
                                    } else {
                                        maxPosY = Math.min(maxPosY, bb.min.y - entity.localBoundingBox.getHeight() - 0.01F);
                                        entity.blockBouncinessY = Math.min(entity.blockBouncinessY, -blockAdj.bounciness);
                                    }

                                    entity.collidedY = true;
                                    entity.onCollideWithBlock(Axis.Y, blockAdj, targetPosition, bx, by, bz);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isOnGround) {
            entity.floorFriction = floorFriction;
        } else if (!entity.isInFluid() && !entity.isNoClip()) {
            entity.floorFriction = 0.1F;
        } else {
            entity.floorFriction = 1.0F;
        }

        targetPosition.y = MathUtils.clamp(targetPosition.y, minPosY, maxPosY);
        entity.isOnGround = isOnGround;
        entity.tmpEntityBoundingBox.min.x = entity.localBoundingBox.min.x + targetPosition.x;
        entity.tmpEntityBoundingBox.max.x = entity.localBoundingBox.max.x + targetPosition.x;
        entity.tmpEntityBoundingBox.min.y = entity.localBoundingBox.min.y + targetPosition.y + 0.01F;
        entity.tmpEntityBoundingBox.max.y = entity.localBoundingBox.max.y + targetPosition.y;
        entity.tmpEntityBoundingBox.update();
        minBx = (int)Math.floor(entity.tmpEntityBoundingBox.min.x);
        minBy = (int)Math.floor(entity.tmpEntityBoundingBox.min.y);
        minBz = (int)Math.floor(entity.tmpEntityBoundingBox.min.z);
        maxBx = (int)Math.floor(entity.tmpEntityBoundingBox.max.x);
        maxBy = (int)Math.floor(entity.tmpEntityBoundingBox.max.y);
        maxBz = (int)Math.floor(entity.tmpEntityBoundingBox.max.z);
        boolean constrainBySneaking = entity.shouldConstrainBySneak(zone, entity.tmpBlockBoundingBox, entity.tmpEntityBoundingBox, minBx, minBy, minBz, maxBx, maxBz);
        if (constrainBySneaking) {
            entity.onceVelocity.x = 0.0F;
            entity.velocity.x = 0.0F;
            targetPosition.x = entity.position.x;
        }

        boolean steppedUpForAll = true;
        float desiredStepUp = targetPosition.y;
        if (!constrainBySneaking) {
            for(int bx = minBx; bx <= maxBx; ++bx) {
                for(int by = minBy; by <= maxBy; ++by) {
                    for(int bz = minBz; bz <= maxBz; ++bz) {
                        BlockState blockAdj = zone.getBlockState(bx, by, bz);
                        if (blockAdj != null && !blockAdj.walkThrough) {
                            blockAdj.getBoundingBox(entity.tmpBlockBoundingBox, bx, by, bz);
                            if (entity.tmpBlockBoundingBox.intersects(entity.tmpEntityBoundingBox)) {
                                boolean didStepUp = false;

                                for (BoundingBox bb : blockAdj.getAllBoundingBoxes(entity.tmpBlockBoundingBoxes, bx, by, bz)) {
                                    if (bb.intersects(entity.tmpEntityBoundingBox)) {
                                        if (!isOnGround || !(bb.max.y - entity.tmpEntityBoundingBox.min.y <= entity.maxStepHeight) || !(bb.max.y > entity.tmpEntityBoundingBox.min.y)) {
                                            didStepUp = false;
                                            steppedUpForAll = false;
                                            break;
                                        }

                                        float currentDesiredStepUp = Math.max(desiredStepUp, bb.max.y - entity.localBoundingBox.min.y);
                                        entity.tmpEntityBoundingBox2.set(entity.tmpEntityBoundingBox);
                                        entity.tmpEntityBoundingBox2.min.y = currentDesiredStepUp;
                                        entity.tmpEntityBoundingBox2.max.y = currentDesiredStepUp + entity.localBoundingBox.getHeight();
                                        entity.tmpEntityBoundingBox2.update();
                                        boolean canStepUp = true;

                                        label267:
                                        for (int bax = minBx; bax <= maxBx; ++bax) {
                                            for (int bay = by + 1; bay <= maxBy + 1; ++bay) {
                                                for (int baz = minBz; baz <= maxBz; ++baz) {
                                                    BlockState blockAbove = zone.getBlockState(bax, bay, baz);
                                                    if (blockAbove != null && !blockAbove.walkThrough) {
                                                        blockAbove.getBoundingBox(entity.tmpBlockBoundingBox2, bax, bay, baz);
                                                        canStepUp &= !entity.tmpBlockBoundingBox2.intersects(entity.tmpEntityBoundingBox2);
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

                                    for (BoundingBox bb : blockAdj.getAllBoundingBoxes(entity.tmpBlockBoundingBoxes, bx, by, bz)) {
                                        if (bb.intersects(entity.tmpEntityBoundingBox)) {
                                            float centX = entity.tmpBlockBoundingBox.getCenterX();
                                            if (centX > targetPosition.x) {
                                                targetPosition.x = bb.min.x - entity.tmpEntityBoundingBox.getWidth() / 2.0F - 0.01F;
                                            } else {
                                                targetPosition.x = bb.max.x + entity.tmpEntityBoundingBox.getWidth() / 2.0F + 0.01F;
                                            }

                                            entity.onCollideWithBlock(Axis.X, blockAdj, targetPosition, bx, by, bz);
                                            entity.collidedX = true;
                                            entity.onceVelocity.x = 0.0F;
                                            entity.velocity.x = 0.0F;
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

        entity.tmpEntityBoundingBox.min.set(entity.localBoundingBox.min).add(targetPosition.x, targetPosition.y + 0.01F, targetPosition.z);
        entity.tmpEntityBoundingBox.max.set(entity.localBoundingBox.max).add(targetPosition);
        entity.tmpEntityBoundingBox.update();
        minBx = (int)Math.floor((double)entity.tmpEntityBoundingBox.min.x);
        minBy = (int)Math.floor((double)entity.tmpEntityBoundingBox.min.y);
        minBz = (int)Math.floor((double)entity.tmpEntityBoundingBox.min.z);
        maxBx = (int)Math.floor((double)entity.tmpEntityBoundingBox.max.x);
        maxBy = (int)Math.floor((double)entity.tmpEntityBoundingBox.max.y);
        maxBz = (int)Math.floor((double)entity.tmpEntityBoundingBox.max.z);
        constrainBySneaking = entity.shouldConstrainBySneak(zone, entity.tmpBlockBoundingBox, entity.tmpEntityBoundingBox, minBx, minBy, minBz, maxBx, maxBz);
        steppedUpForAll = true;
        desiredStepUp = targetPosition.y;
        if (constrainBySneaking) {
            entity.onceVelocity.z = 0.0F;
            entity.velocity.z = 0.0F;
            targetPosition.z = entity.position.z;
        } else {
            for(int bx = minBx; bx <= maxBx; ++bx) {
                for(int by = minBy; by <= maxBy; ++by) {
                    for(int bz = minBz; bz <= maxBz; ++bz) {
                        BlockState blockAdj = zone.getBlockState(bx, by, bz);
                        if (blockAdj != null && !blockAdj.walkThrough) {
                            blockAdj.getBoundingBox(entity.tmpBlockBoundingBox, bx, by, bz);
                            if (entity.tmpBlockBoundingBox.intersects(entity.tmpEntityBoundingBox)) {
                                boolean didStepUp = false;

                                for (BoundingBox bb : blockAdj.getAllBoundingBoxes(entity.tmpBlockBoundingBoxes, bx, by, bz)) {
                                    if (bb.intersects(entity.tmpEntityBoundingBox)) {
                                        if (!isOnGround || !(bb.max.y - entity.tmpEntityBoundingBox.min.y <= entity.maxStepHeight) || !(bb.max.y > entity.tmpEntityBoundingBox.min.y)) {
                                            didStepUp = false;
                                            steppedUpForAll = false;
                                            break;
                                        }

                                        float currentDesiredStepUp = Math.max(desiredStepUp, bb.max.y - entity.localBoundingBox.min.y);
                                        entity.tmpEntityBoundingBox2.set(entity.tmpEntityBoundingBox);
                                        entity.tmpEntityBoundingBox2.min.y = currentDesiredStepUp;
                                        entity.tmpEntityBoundingBox2.max.y = currentDesiredStepUp + entity.localBoundingBox.getHeight();
                                        entity.tmpEntityBoundingBox2.update();
                                        boolean canStepUp = true;

                                        label200:
                                        for (int bax = minBx; bax <= maxBx; ++bax) {
                                            for (int bay = by + 1; bay <= maxBy + 1; ++bay) {
                                                for (int baz = minBz; baz <= maxBz; ++baz) {
                                                    BlockState blockAbove = zone.getBlockState(bax, bay, baz);
                                                    if (blockAbove != null && !blockAbove.walkThrough) {
                                                        blockAbove.getBoundingBox(entity.tmpBlockBoundingBox2, bax, bay, baz);
                                                        canStepUp &= !entity.tmpBlockBoundingBox2.intersects(entity.tmpEntityBoundingBox2);
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

                                    for (BoundingBox bb : blockAdj.getAllBoundingBoxes(entity.tmpBlockBoundingBoxes, bx, by, bz)) {
                                        if (bb.intersects(entity.tmpEntityBoundingBox)) {
                                            float centZ = entity.tmpBlockBoundingBox.getCenterZ();
                                            if (centZ > targetPosition.z) {
                                                targetPosition.z = bb.min.z - entity.tmpEntityBoundingBox.getDepth() / 2.0F - 0.01F;
                                            } else {
                                                targetPosition.z = bb.max.z + entity.tmpEntityBoundingBox.getDepth() / 2.0F + 0.01F;
                                            }

                                            entity.onCollideWithBlock(Axis.Z, blockAdj, targetPosition, bx, by, bz);
                                            entity.collidedZ = true;
                                            entity.onceVelocity.z = 0.0F;
                                            entity.velocity.z = 0.0F;
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

        entity.position.set(targetPosition);
    }
//    public static <T extends Entity & IEntity & HEntity> void updatePositions(T entity, Zone zone, float deltaTime) {
//        if (entity instanceof IPhysicEntity && !(entity instanceof PlayerEntity)) return;
//        if (entity instanceof PlayerEntity) return;

//        if (entity.hGetCurrentChunk() != null) {
//            entity.isOnGround = false;
//
//
//
//            entity.hResetAcceleration();
//        }
//
//        EntityUtils.updateEntityChunk(zone, entity);
//        entity.sendPositionPacket();
//    }


//    static final AABB TMP_AABB = new AABB();
    static final BoundingBox TMP_BOUNDS = new BoundingBox();
    static final Vector3 TMP_VEC = new Vector3();

    static float minPosY = 0;
    static float maxPosY = 0;

    static final Vector3 TARGET_POS = new Vector3();
    private static float floorFriction = 0;

    /**
     *  Vanilla facing method: {@link Entity#updateConstraints(Zone, Vector3)}
     */
//    public static void updateConstraints(Entity theEntity, Zone zone, Vector3 targetPosition) {
//        if (theEntity instanceof IPhysicEntity) return;
//
//        Entity[] entities = zone.getAllEntities().toArray(Entity.class);
//
//
//        theEntity.position.set(targetPosition);
//    }

    private static boolean shouldConstrainBySneak(Entity theEntity, Zone zone, BoundingBox tmpBlockBoundingBox, BoundingBox tmpEntityBoundingBox, int minBx, int minBy, int minBz, int maxBx, int maxBz) {
        Method m = null;
        try {
            m = ReflectionUtil.getMethod(Entity.class, "shouldConstrainBySneak", Zone.class, BoundingBox.class, BoundingBox.class, int.class, int.class, int.class, int.class, int.class);
            return (boolean) m.invoke(theEntity, zone, tmpBlockBoundingBox, tmpEntityBoundingBox, minBx, minBy, minBz, maxBx, maxBz);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setupTmpBounds(Entity theEntity, Vector3 targetPosition) {
//        TMP_AABB.setMax(TMP_VEC.set(theEntity.localBoundingBox.max).add(
//                theEntity.position.x,
//                theEntity.position.y + targetPosition.y,
//                theEntity.position.z
//        ));
//        TMP_AABB.setMin(TMP_VEC.set(theEntity.localBoundingBox.min).add(
//                theEntity.position.x,
//                theEntity.position.y + targetPosition.y,
//                theEntity.position.z
//        ));
//        TMP_AABB.update();

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

//            for (AABB bb : ((IBlockBoundsMaker) blockState.getModel()).getBounds(pos.getGlobalX(), pos.getGlobalY(), pos.getGlobalZ())) {
//                if (bb.intersects(TMP_AABB)) {
//                    theEntity.velocity.y = 0;
//                    theEntity.onceVelocity.y = 0;
//                    if (oldY <= bb.getMax().y && oldY >= bb.getMin().y) {
//                        minPosY = Math.max(minPosY, bb.getMax().y - theEntity.localBoundingBox.min.y);
//                        maxPosY = Math.max(maxPosY, minPosY);
//                        if (!theEntity.isOnGround) {
//                            theEntity.footstepTimer = 0.45F;
//                        }
//
//                        theEntity.isOnGround = true;
//                        floorFriction = Math.max(floorFriction, blockState.friction);
//                        theEntity.blockBouncinessY = Math.max(theEntity.blockBouncinessY, blockState.bounciness);
//                    } else {
//                        maxPosY = Math.min(maxPosY, bb.getMin().y - theEntity.localBoundingBox.getHeight() - 0.01F);
//                        theEntity.blockBouncinessY = Math.min(theEntity.blockBouncinessY, -blockState.bounciness);
//                    }
//
//                    theEntity.collidedY = true;
//                    theEntity.onCollideWithBlock(Axis.Y, blockState, TARGET_POS, (int) point.x, (int) point.y, (int) point.z);
//                    return true;
//                }
//            }
        }
        return false;
    }
}
