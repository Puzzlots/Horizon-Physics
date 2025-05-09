package me.zombii.horizon.mixins;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.entities.player.PlayerEntity;
import finalforeach.cosmicreach.util.Axis;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.util.ConversionUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin extends Entity {

    @Shadow public transient Player player;

    @Unique
    private transient PhysicsRigidBody body;

    public PlayerEntityMixin(String entityTypeId) {
        super(entityTypeId);
    }

    @Unique
    private void horizonPhysics$init() {
        body = new PhysicsRigidBody(new BoxCollisionShape(ConversionUtil.toJME(localBoundingBox).getExtent(new Vector3f())));
        body.setMass(0);
//        hasGravity = false;
    }

    @Inject(method = "<init>()V", at = @At("TAIL"))
    private void init0(CallbackInfo ci) {
        horizonPhysics$init();
    }

    boolean hasInit = false;

//    @Override
//    public void updateConstraints(Zone zone, Vector3 targetPosition) {
//
//    }

    private transient UUID uuid = UUID.randomUUID();

    private Vector3 acceleration = Vector3.Zero.cpy();
    private Vector3 posDiff = Vector3.Zero.cpy();
    private Vector3 targetPosition = Vector3.Zero.cpy();

//    @Override
//    public void update(Zone zone, double deltaTime) {
//        super.update(zone, deltaTime);
//        if (body != null) {
//            if (!hasInit) {
//                PhysicsThread.INSTANCE.space.addCollisionObject(body);
//                hasInit = true;
//            }
//            body.setPhysicsLocation(ConversionUtil.toJME(position));
//        }
//    }

    @Override
    public void render(Camera worldCamera) {
//        ChunkMeta meta = PhysicsThread.chunkMap.get(InGame.getLocalPlayer().getZone().getChunkAtPosition(position));
//        if (meta != null)
//            DebugRenderUtil.renderRigidBody(InGameAccess.getAccess().getShapeRenderer(), meta.getBody());
//        if (body != null) {
//            DebugRenderUtil.renderRigidBody(InGameAccess.getAccess().getShapeRenderer(), body);
//        }

        if (!GameSingletons.isClient || GameSingletons.client().getLocalPlayer() != this.player) {
            super.render(worldCamera);
        }
    }

    @Unique
    private float getLength(Vector3f a, Vector3f b) {
        return (float) Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2) + Math.pow(b.z - a.z, 2));
    }

//    @Override
//    public @NonNull PhysicsBody getBody() {
//        return body;
//    }
//
//    @Override
//    public Quaternion getEularRotation() {
//        return new Quaternion();
//    }
//
//    @Override
//    public @NonNull UUID getUUID() {
//        return uuid;
//    }
//
//    @Override
//    public float getMass() {
//        return 10;
//    }
//
//    @Override
//    public CollisionShape getCollisionShape() {
//        return shape;
//    }
//
//    @Override
//    public void setEularRotation(Quaternion rot) {
//
//    }
//
//    @Override
//    public void setUUID(UUID uuid) {
//
//    }
//
//    @Override
//    public void setMass(float mass) {
//
//    }
//
//    @Override
//    public void setCollisionShape(CollisionShape shape) {
//
//    }

//    @Override
//    public void updateConstraints(Zone zone, Vector3 targetPosition) {
//        this.tmpEntityBoundingBox.set(this.localBoundingBox);
//        this.tmpEntityBoundingBox.min.add(this.position);
//        this.tmpEntityBoundingBox.max.add(this.position);
//        this.tmpEntityBoundingBox.min.y = this.localBoundingBox.min.y + targetPosition.y;
//        this.tmpEntityBoundingBox.max.y = this.localBoundingBox.max.y + targetPosition.y;
//        this.tmpEntityBoundingBox.update();
//    }

}
