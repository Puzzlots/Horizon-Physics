package me.zombii.horizon.entity;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.github.puzzle.game.util.IClientNetworkManager;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityUtils;
import finalforeach.cosmicreach.entities.IDamageSource;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.HorizonConstants;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
import me.zombii.horizon.entity.api.IPhysicEntity;
import me.zombii.horizon.entity.api.ISingleEntityBlock;
import me.zombii.horizon.items.GravityGun;
import me.zombii.horizon.rendering.mesh.IHorizonMesh;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.util.ConversionUtil;
import me.zombii.horizon.rendering.mesh.IMeshInstancer;
import me.zombii.horizon.util.MatrixUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Cube extends Entity implements IPhysicEntity, ISingleEntityBlock {

    private Quaternion lastRotation;
    public PhysicsRigidBody body;
    public Matrix4 transform;
    public Quaternion rotation;
    public UUID uuid;
    public Float mass;
    public CollisionShape shape;
    public AtomicReference<BlockState> state = new AtomicReference<>();
    boolean isPickedUp;

    public BoundingBox rBoundingBox = new BoundingBox(new Vector3(-0.5f, -0.5f,-0.5f), new Vector3(0.5f, 0.5f, 0.5f));
    public OrientedBoundingBox oBoundingBox = new OrientedBoundingBox();

    BlockState[] states = Block.allBlockStates.values().toArray(new BlockState[0]);

    public Cube() {
        super(HorizonConstants.MOD_ID + ":cube");

        if (!IClientNetworkManager.isConnected()){
//            state.set(BlockState.getInstance("base:furnace[lit=off]"));
            state.set(states[new Random().nextInt(0, states.length)]);
            shape = PhysicsThread.INSTANCE.shapeFromBlockState(new CompoundCollisionShape(), new Vector3f(), state.get());

            body = new PhysicsRigidBody(shape);
            body.setFriction(1f);
            mass = 2.5f;
        }

        uuid = UUID.randomUUID();
        rotation = Quaternion.DIRECTION_Z;
        lastRotation = new Quaternion();
        transform = new Matrix4();

        Threads.runOnMainThread(() -> modelInstance = IMeshInstancer.createSingleBlockMesh(state));
    }

    public void setPickedUp(boolean pickedUp) {
        isPickedUp = pickedUp;
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
    public boolean isPickedUp() {
        return isPickedUp;
    }

    @Override
    public boolean canBePickedUp() {
        return true;
    }

    @Override
    public void hit(IDamageSource damageSource, float amount) {
    }

    @Override
    public void onAttackInteraction(Entity sourceEntity) {
        super.onAttackInteraction(sourceEntity);
        setPickedUp(false);
        if (equals(GravityGun.heldEntity)) {
            GravityGun.heldEntity = null;
        }

        body.activate(true);
        body.setLinearVelocity(new Vector3f(sourceEntity.viewDirection.cpy().scl(12).x, sourceEntity.viewDirection.cpy().scl(12).y, sourceEntity.viewDirection.cpy().scl(12).z));
    }

    @Override
    public void getBoundingBox(BoundingBox boundingBox) {
        ((ExtendedBoundingBox) boundingBox).setInnerBounds(oBoundingBox);
        boundingBox.update();
    }

    boolean initialized = false;

    @Override
    public void update(Zone zone, float deltaTime) {
        PhysicsThread.alertChunk(zone.getChunkAtPosition(position));

        if (!IClientNetworkManager.isConnected()) {
            MatrixUtil.rotateAroundOrigin3(oBoundingBox, transform, position, rotation);

            oBoundingBox.setBounds(rBoundingBox);
            oBoundingBox.setTransform(transform);

            if (!initialized) {
                PhysicsThread.alertChunk(zone.getChunkAtPosition(position));
                body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
                body.setPhysicsRotation(rotation);
                body.setMass(mass);
                initialized = true;

                PhysicsThread.addEntity(this);
                body.activate(true);
            } else {
                Vector3f vector3f = body.getPhysicsLocation(null);
                position = ConversionUtil.fromJME(vector3f);
                rotation = body.getPhysicsRotation(null);
//            body.setPhysicsRotation(rotation = Quaternion.DIRECTION_Z);
            }
        }
        EntityUtils.updateEntityChunk(zone, this);
        updatePosition();

        if (canBePickedUp() && isPickedUp()) {
            GravityGun.move(body,position);
        }

        if (!((ExtendedBoundingBox)localBoundingBox).hasInnerBounds()) {
            ((ExtendedBoundingBox)localBoundingBox).setInnerBounds(oBoundingBox);
        }

        getBoundingBox(globalBoundingBox);
    }

    @Override
    public void render(Camera worldCamera) {
        MatrixUtil.rotateAroundOrigin3(oBoundingBox, transform, position, rotation);

        oBoundingBox.setBounds(rBoundingBox);
        oBoundingBox.setTransform(transform);

//        HorizonConstants.EXEC.accept(this);

        tmpRenderPos.set(this.lastRenderPosition);
        TickRunner.INSTANCE.partTickLerp(tmpRenderPos, this.position);
        this.lastRenderPosition.set(tmpRenderPos);
        if (worldCamera.frustum.boundsInFrustum(this.globalBoundingBox)) {
            tmpModelMatrix.idt();
            MatrixUtil.rotateAroundOrigin4(.5f, tmpModelMatrix, tmpRenderPos, rotation);
            if (modelInstance != null) {
                modelInstance.render(this, worldCamera, tmpModelMatrix, true);
            }
        }
    }

    @Override
    public void read(CRBinDeserializer deserial) {
        super.read(deserial);

        IPhysicEntity.read(this, deserial);
        ISingleEntityBlock.read(this, deserial);
        try {
            ((IHorizonMesh) modelInstance).setShouldRefresh(true);
        } catch (Exception ignore) {}

        if (!IClientNetworkManager.isConnected()){
            body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
            body.setPhysicsRotation(rotation);
            body.setCollisionShape(PhysicsThread.INSTANCE.shapeFromBlockState(new CompoundCollisionShape(), new Vector3f(), state.get()));
        }
        getBoundingBox(globalBoundingBox);
    }

    @Override
    public void write(CRBinSerializer serial) {
        super.write(serial);

        IPhysicEntity.write(this, serial);
        ISingleEntityBlock.write(this, serial);
    }

    @Override
    public @NonNull PhysicsBody getBody() {
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
    public @NonNull UUID getUUID() {
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
    public BlockState getState() {
        return state.get();
    }

    @Override
    public void setState(BlockState state) {
        this.state.set(state);
    }
}
