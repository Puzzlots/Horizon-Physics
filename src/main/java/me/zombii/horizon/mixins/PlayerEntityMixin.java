package me.zombii.horizon.mixins;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.entities.player.PlayerEntity;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.entity.api.HEntity;
import me.zombii.horizon.entity.api.IPhysicEntity;
import me.zombii.horizon.rendering.mesh.IHorizonMesh;
import me.zombii.horizon.util.ConversionUtil;
import me.zombii.horizon.util.MatrixUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin extends Entity implements IPhysicEntity {

    @Shadow
    private transient Player player;

    @Unique
    private transient PhysicsRigidBody body;

    public PlayerEntityMixin(String entityTypeId) {
        super(entityTypeId);
    }

    @Unique
    private void horizonPhysics$init() {
        ((HEntity) this).hSetMass(10);
        body = new PhysicsRigidBody(new BoxCollisionShape(ConversionUtil.toJME(localBoundingBox).getExtent(new Vector3f())));
        body.setMass(0);

        if (GameSingletons.isHost){
            shape = ConversionUtil.toCollisionShape(rBoundingBox);
            body = new PhysicsRigidBody(shape);
            body.setFriction(1f);
            mass = 0f;
        }

        uuid = UUID.randomUUID();
        rotation = Quaternion.DIRECTION_Z;
        lastRotation = new Quaternion();
        transform = new Matrix4();
    }

    @Inject(method = "setPlayer", at = @At("TAIL"))
    private void init0(CallbackInfo ci) {
        horizonPhysics$init();
    }

    private transient Quaternion lastRotation;
    public transient Matrix4 transform;
    public transient Quaternion rotation;
    public transient UUID uuid;
    public transient Float mass = 1f;
    public transient CollisionShape shape;

    public transient BoundingBox rBoundingBox = new BoundingBox(new Vector3(-0.25f, 0f,-0.25f), new Vector3(0.25f, 1.9f, 0.25f));
    public transient OrientedBoundingBox oBoundingBox = new OrientedBoundingBox();

    @Inject(method = "render", at = @At("HEAD"))
    public void render(Camera worldCamera, CallbackInfo ci) {
        MatrixUtil.rotateAroundOrigin3(oBoundingBox, transform, position, rotation);

        oBoundingBox.setBounds(localBoundingBox);
        oBoundingBox.setTransform(transform);

        tmpRenderPos.set(this.lastRenderPosition);
        TickRunner.INSTANCE.partTickLerp(tmpRenderPos, this.position);
        this.lastRenderPosition.set(tmpRenderPos);
        if (worldCamera.frustum.boundsInFrustum(this.globalBoundingBox)) {
            tmpModelMatrix.idt();
            MatrixUtil.rotateAroundOrigin4(.5f, tmpModelMatrix, tmpRenderPos, rotation);
        }
    }

    @Override
    public void read(CRBinDeserializer deserial) {
        super.read(deserial);

        IPhysicEntity.read(this, deserial);
        try {
            ((IHorizonMesh) modelInstance).setShouldRefresh(true);
        } catch (Exception ignore) {}

        if (GameSingletons.isHost){
            body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
            body.setPhysicsRotation(rotation);
        }
        getBoundingBox(globalBoundingBox);
    }

    @Override
    public void write(CRBinSerializer serial) {
        super.write(serial);

        IPhysicEntity.write(this, serial);
    }

    @Override
    public PhysicsBody getBody() {
        return body;
    }

    @Override
    public Quaternion getEularRotation() {
        return rotation;
    }

    @Override
    public Quaternion getLastEularRotation() {
        return lastRotation;
    }

    @Override
    public void setLastEularRotation(Quaternion rot) {
        lastRotation.set(rot);
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public float getMass() {
        return mass;
    }

    @Override
    public CollisionShape getCollisionShape() {
        return shape;
    }

    @Override
    public void setEularRotation(Quaternion rot) {
        rotation.set(rot);
    }

    @Override
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void setMass(float mass) {
        this.mass = mass;
    }

    @Override
    public void setCollisionShape(CollisionShape shape) {
        this.shape = shape;
    }

    @Override
    public Vector3 getLastPosition() {
        return lastPosition;
    }

    @Override
    public void setLastPosition(Vector3 pos) {
        lastPosition.set(pos);
    }

    @Override
    public Zone getZone() {
        return zone;
    }

}
