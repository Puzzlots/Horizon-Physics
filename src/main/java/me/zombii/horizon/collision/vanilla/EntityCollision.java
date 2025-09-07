package me.zombii.horizon.collision.vanilla;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import dev.puzzleshq.puzzleloader.loader.util.ReflectionUtil;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityUtils;
import finalforeach.cosmicreach.entities.player.PlayerEntity;
import finalforeach.cosmicreach.world.Zone;
import io.github.puzzle.cosmic.api.entity.IEntity;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
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
                    entity.updateConstraints(zone, entity.targetPosition);
                } else {
                    entity.posDiff.set(entity.targetPosition).sub(entity.position).scl(1.0F / d);
                    entity.targetPosition.set(entity.position);
                    float floor = (float)Math.floor((double)d);

                    for(float l = 0.0F; l < floor; ++l) {
                        entity.targetPosition.add(entity.posDiff);
                        entity.updateConstraints(zone, entity.targetPosition);
                    }

                    if (d - floor > 0.0F) {
                        entity.posDiff.scl(d - floor);
                        entity.targetPosition.add(entity.posDiff);
                        entity.updateConstraints(zone, entity.targetPosition);
                    }
                }

                if (entity.isOnGround && !wasOnGround) {
                    float displacement = entity.position.y - entity.lastPosition.y;
                    double initialSquared = Math.pow((double)oldVelocityY, (double)2.0F);
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
    public static void updateConstraints(Entity theEntity, Zone zone, Vector3 targetPosition) {
        if (theEntity instanceof IPhysicEntity) return;

        Entity[] entities = zone.getAllEntities().toArray(Entity.class);


        theEntity.position.set(targetPosition);
    }

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
