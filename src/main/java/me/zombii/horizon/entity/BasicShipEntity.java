package me.zombii.horizon.entity;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.github.puzzle.game.util.IClientNetworkManager;
import com.jme3.bullet.collision.shapes.CollisionShape;
import finalforeach.cosmicreach.blocks.MissingBlockStateResult;
import finalforeach.cosmicreach.entities.EntityUtils;
import finalforeach.cosmicreach.entities.IDamageSource;
import finalforeach.cosmicreach.entities.components.GravityComponent;
import me.zombii.horizon.entity.api.IPhysicEntity;
import me.zombii.horizon.entity.api.IVirtualZoneEntity;
import me.zombii.horizon.rendering.mesh.IMeshInstancer;
import me.zombii.horizon.util.Vec3i;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.world.PhysicsChunk;
import me.zombii.horizon.world.PhysicsZone;
import org.checkerframework.checker.nullness.qual.NonNull;
import me.zombii.horizon.HorizonConstants;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.util.MatrixUtil;

import java.util.UUID;

public class BasicShipEntity extends Entity implements IPhysicEntity, IVirtualZoneEntity {

    private final Quaternion lastEularRotation;
    public PhysicsRigidBody body;

    public Quaternion rotation;
    UUID uuid;
    float mass = 0;

    public Matrix4 transform = new Matrix4();
    public PhysicsZone world;

    public BasicShipEntity() {
        super(HorizonConstants.MOD_ID + ":ship");
        removeUpdatingComponent(GravityComponent.INSTANCE);

        if (uuid == null)
            uuid = UUID.randomUUID();

        world = PhysicsZone.create(uuid);

        if (!IClientNetworkManager.isConnected()) {
            if (rotation == null) rotation = Quaternion.DIRECTION_Z;
            world = PhysicsZone.create(uuid);

            PhysicsChunk structure00 = new PhysicsChunk(new Vec3i(0, 0, 0));
            PhysicsChunk structure01 = new PhysicsChunk(new Vec3i(-1, 0, 0));
            PhysicsChunk structure10 = new PhysicsChunk(new Vec3i(0, 0, -1));
            PhysicsChunk structure11 = new PhysicsChunk(new Vec3i(-1, 0, -1));

            BlockState stone = BlockState.getInstance("base:stone_basalt[default]", MissingBlockStateResult.MISSING_OBJECT);
            BlockState light = BlockState.getInstance("base:torch[type=ground,level=4]", MissingBlockStateResult.MISSING_OBJECT);
            for (int x = 0; x < 8; x++) {
                for (int z = 0; z < 8; z++) {
                    structure00.setBlockState(stone, x, 0, z);
                    structure01.setBlockState(stone, x + 8, 0, z);
                    structure10.setBlockState(stone, x, 0, z + 8);
                    structure11.setBlockState(stone, x + 8, 0, z + 8);
                }
            }
            BlockState chair = BlockState.getInstance(HorizonConstants.MOD_ID + ":chair[default]", MissingBlockStateResult.MISSING_OBJECT);
            structure00.setBlockState(light, 0, 1, 0);
            structure01.setBlockState(light, 15, 1, 0);
            structure10.setBlockState(light, 0, 1, 15);
            structure11.setBlockState(light, 15, 1, 15);
            world.addChunk(structure00);
            world.addChunk(structure01);
            world.addChunk(structure10);
            world.addChunk(structure11);
            world.rebuildCollisionShape();

            body = new PhysicsRigidBody(world.CCS);
        }

        modelInstance = null;
        if (!IClientNetworkManager.isConnected()) {
            Threads.runOnMainThread(() -> modelInstance = IMeshInstancer.createZoneMesh(world));
        }

        transform.idt();
        lastEularRotation = Quaternion.DIRECTION_Z;
    }

    public OrientedBoundingBox oBoundingBox = new OrientedBoundingBox();

    @Override
    protected void onDeath() {
        PhysicsThread.removeEntity(this);
        super.onDeath();
    }

    @Override
    public void onAttackInteraction(Entity sourceEntity) {
        body.activate(true);
        body.setLinearVelocity(new Vector3f(sourceEntity.viewDirection.cpy().scl(12).x, sourceEntity.viewDirection.cpy().scl(12).y, sourceEntity.viewDirection.cpy().scl(12).z));
    }

    boolean initialized = false;

    @Override
    public void update(Zone zone, float deltaTime) {
        PhysicsThread.alertChunk(zone.getChunkAtPosition(position));

        MatrixUtil.rotateAroundOrigin3(oBoundingBox, transform, position, rotation);

        oBoundingBox.setBounds(world.AABB);
        oBoundingBox.setTransform(transform);

        if (!initialized) {
            PhysicsThread.alertChunk(zone.getChunkAtPosition(position));
            body.setPhysicsRotation(getEularRotation());
            body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
            body.setMass(5);
            initialized = true;

            PhysicsThread.addEntity(this);
            body.activate(true);
        } else {
            Vector3f vector3f = body.getPhysicsLocation(new Vector3f());
            position.set(vector3f.x, vector3f.y, vector3f.z);
            rotation = body.getPhysicsRotation(new Quaternion());
        }

        if (world.CCS_WAS_REBUILT) {
            System.out.println("rebuilding");
            PhysicsThread.INSTANCE.space.getRigidBodyList().forEach(e -> {
                if (e.nativeId() == body.nativeId()) {
                    System.out.println("E");
                    e.setCollisionShape(world.CCS);
                }
            });
            body.setCollisionShape(world.CCS);
            world.CCS_WAS_REBUILT = false;
            System.out.println("rebuilt");
        }
        body.setPhysicsRotation(getEularRotation());
        EntityUtils.updateEntityChunk(zone, this);
        updatePosition();

        if (!((ExtendedBoundingBox)localBoundingBox).hasInnerBounds()) {
            ((ExtendedBoundingBox)localBoundingBox).setInnerBounds(oBoundingBox);
        }

        getBoundingBox(globalBoundingBox);
    }

    @Override
    public void getBoundingBox(BoundingBox boundingBox) {
        ((ExtendedBoundingBox) boundingBox).setInnerBounds(oBoundingBox);
        boundingBox.update();
    }

    @Override
    public void read(CRBinDeserializer deserial) {
        super.read(deserial);

        IPhysicEntity.read(this, deserial);
        IVirtualZoneEntity.read(this, deserial);
        world.recalculateBounds();
        getBoundingBox(localBoundingBox);

        if (!IClientNetworkManager.isConnected()) {
            body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
            body.setPhysicsRotation(rotation);
        }

        Threads.runOnMainThread(() -> modelInstance = IMeshInstancer.createZoneMesh(world));
    }

    @Override
    public void write(CRBinSerializer serial) {
        super.write(serial);

        IPhysicEntity.write(this, serial);
        IVirtualZoneEntity.write(this, serial);
    }

    @Override
    public void render(Camera worldCamera) {
        if (modelInstance == null) return;
        MatrixUtil.rotateAroundOrigin3(oBoundingBox, transform, position, rotation);

        oBoundingBox.setBounds(world.AABB);
        oBoundingBox.setTransform(transform);

        tmpRenderPos.set(this.lastRenderPosition);
        TickRunner.INSTANCE.partTickLerp(tmpRenderPos, this.position);
        this.lastRenderPosition.set(tmpRenderPos);
        if (worldCamera.frustum.boundsInFrustum(this.globalBoundingBox)) {
            tmpModelMatrix.idt();
            MatrixUtil.rotateAroundOrigin3(oBoundingBox, tmpModelMatrix, tmpRenderPos, rotation);
            if (modelInstance != null) {
                modelInstance.render(this, worldCamera, tmpModelMatrix, true);
            }
        }
    }

    @Override
    @NonNull
    public PhysicsBody getBody() {
        return body;
    }

    @Override
    public Quaternion getEularRotation() {
        return rotation;
    }

    @Override
    public Quaternion getLastEularRotation() {
        return lastEularRotation;
    }

    @Override
    public void setLastEularRotation(Quaternion rot) {
        lastEularRotation.set(rot);
    }

    @Override
    @NonNull
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public float getMass() {
        return mass;
    }

    @Override
    public CollisionShape getCollisionShape() {
        return world.CCS;
    }

    @Override
    public void setEularRotation(Quaternion rot) {
        this.rotation = rot;
    }

    @Override
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void setMass(float mass) {

    }

    @Override
    public void setCollisionShape(CollisionShape shape) {

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

    @Override
    public PhysicsZone getWorld() {
        return world;
    }

}
