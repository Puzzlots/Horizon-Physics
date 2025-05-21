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
import finalforeach.cosmicreach.entities.components.GravityComponent;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Chunk;
import me.zombii.horizon.entity.api.IPhysicEntity;
import me.zombii.horizon.entity.api.IVirtualZoneEntity;
import me.zombii.horizon.rendering.mesh.IMeshInstancer;
import me.zombii.horizon.util.ConversionUtil;
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
import finalforeach.cosmicreach.worldgen.noise.SimplexNoise;
import me.zombii.horizon.world.PhysicsChunk;
import me.zombii.horizon.world.PhysicsZone;
import org.checkerframework.checker.nullness.qual.NonNull;
import me.zombii.horizon.HorizonConstants;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.util.MatrixUtil;

import java.util.UUID;
import java.util.function.Supplier;

// /summon funni-blocks:entity

public class WorldCube extends Entity implements IPhysicEntity, IVirtualZoneEntity {

    private Quaternion lastRotation;
    public PhysicsZone world;
    public PhysicsRigidBody body;

    static SimplexNoise noise = new SimplexNoise(345324532);

    public Vector3 rotation = new Vector3(0, 0, 0);
    public Matrix4 transform = new Matrix4();

    public UUID uuid;

    public void generateChunk(Chunk chunk, Vec3i vec3i) {
        BlockState air = BlockState.getInstance("base:air[default]", MissingBlockStateResult.MISSING_OBJECT);
        BlockState stoneBlock = BlockState.getInstance("base:stone_basalt[default]", MissingBlockStateResult.MISSING_OBJECT);
//        BlockState waterBlock = BlockState.getInstance("base:water[default]");
        BlockState waterBlock = BlockState.getInstance("base:light[power=on,lightRed=0,lightGreen=15,lightBlue=0]", MissingBlockStateResult.MISSING_OBJECT);

        for(int localX = 0; localX < 16; localX++) {
            int globalX = (vec3i.x() * 16) + localX;

            for(int localZ = 0; localZ < 16; localZ++) {
                int globalZ = (vec3i.z() * 16) + localZ;

                // Only have to sample height once for each Y column (not going to change on the Y axis ;p)
                double columnHeight = noise.noise2(globalX * 0.01f, globalZ * 0.01f) * 32f + 64f;

                for (int localY = 0; localY < 16; localY++) {
                    int globalY = (vec3i.y() * 16) + localY;

                    if(globalY <= columnHeight) {
                        chunk.setBlockState(stoneBlock, localX, localY, localZ);
                    } else {
                        if (globalY <= 64) {
                            if (chunk.getBlockState(localX, localY, localZ) == air) {
                                chunk.setBlockState(waterBlock, localX, localY, localZ);
                            }
                        }
                    }

                }
            }
        }
    }

    public OrientedBoundingBox oBoundingBox = new OrientedBoundingBox();

    public WorldCube(PhysicsZone world) {
        super(Identifier.of(HorizonConstants.MOD_ID, "entity").toString());
        this.world = world;
        uuid = UUID.randomUUID();
        world.rebuildCollisionShape();
        body = new PhysicsRigidBody(world.CCS);

        Threads.runOnMainThread(() -> modelInstance = IMeshInstancer.createZoneMesh(world));
        transform.idt();
        lastRotation = Quaternion.DIRECTION_Z;
        removeUpdatingComponent(GravityComponent.INSTANCE);
    }

    public WorldCube() {
        super(HorizonConstants.MOD_ID + ":entity");
        removeUpdatingComponent(GravityComponent.INSTANCE);

        if (uuid == null)
            uuid = UUID.randomUUID();

        world = PhysicsZone.create(uuid);
        if (!IClientNetworkManager.isConnected()) {
            for (int x = -4; x < 4; x++) {
                for (int y = 0; y < 7; y++) {
                    for (int z = -4; z < 4; z++) {
//            for (int x = 0; x < 1; x++) {
//                for (int y = 0; y < 2; y++) {
//                    for (int z = 0; z < 1; z++) {
                        Vec3i vec3i = new Vec3i(x, y, z);

                        Chunk chunk = new PhysicsChunk(vec3i);
                        chunk.initChunkData();
                        generateChunk(chunk, new Vec3i(x, y, z));
                        world.addChunk(chunk);
                    }
                }
            }
            world.recalculateBounds();
            world.rebuildCollisionShape();

            body = new PhysicsRigidBody(world.CCS);
        }
        modelInstance = null;

        if (!IClientNetworkManager.isConnected()) {
            Threads.runOnMainThread(() -> modelInstance = IMeshInstancer.createZoneMesh(world));
        }

        lastRotation = Quaternion.DIRECTION_Z;
    }

    @Override
    public void render(Camera worldCamera) {
        if (modelInstance == null) return;
        MatrixUtil.rotateAroundOrigin2(oBoundingBox, transform, position, rotation);

        oBoundingBox.setBounds(world.AABB);
        oBoundingBox.setTransform(transform);

        tmpRenderPos.set(this.lastRenderPosition);
        TickRunner.INSTANCE.partTickLerp(tmpRenderPos, this.position);
        this.lastRenderPosition.set(tmpRenderPos);
        if (worldCamera.frustum.boundsInFrustum(this.globalBoundingBox)) {
            tmpModelMatrix.idt();
            MatrixUtil.rotateAroundOrigin2(oBoundingBox, tmpModelMatrix, tmpRenderPos, rotation);
            if (modelInstance != null) {
                modelInstance.render(this, worldCamera, tmpModelMatrix, true);
            }
        }
    }

    boolean initialized = false;

    float rotX = 0;

    @Override
    public void update(Zone zone, float deltaTime) {
        PhysicsThread.alertChunk(zone.getChunkAtPosition(position));
        transform.rotate(1, 0, 1, rotX+=10);
        body.setPhysicsRotation(getEularRotation());

        if (!IClientNetworkManager.isConnected()){
            MatrixUtil.rotateAroundOrigin2(oBoundingBox, transform, position, rotation);

            oBoundingBox.setBounds(world.AABB);
            oBoundingBox.setTransform(transform);

            if (!initialized) {
                PhysicsThread.alertChunk(zone.getChunkAtPosition(position));
                body.setPhysicsRotation(getEularRotation());
                body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
                body.setMass(0);
                initialized = true;

                PhysicsThread.addEntity(this);
                body.activate(true);
            } else {
                Vector3f vector3f = body.getPhysicsLocation(null);
                position = ConversionUtil.fromJME(vector3f);
//                rotation = rigidBody.getPhysicsRotation(null)
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
        }
        EntityUtils.updateEntityChunk(zone, this);
        updatePosition();

        if (!((ExtendedBoundingBox)localBoundingBox).hasInnerBounds()) {
            ((ExtendedBoundingBox)localBoundingBox).setInnerBounds(oBoundingBox);
        }
        rotX++;

        getBoundingBox(globalBoundingBox);
    }

    @Override
    public @NonNull PhysicsBody getBody() {
        return body;
    }

    @Override
    public Quaternion getEularRotation() {
        com.badlogic.gdx.math.Quaternion quaternion = transform.getRotation(new com.badlogic.gdx.math.Quaternion());
        return new Quaternion(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
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
        return 0;
    }

    @Override
    public CollisionShape getCollisionShape() {
        return world.CCS;
    }

    @Override
    public void setEularRotation(Quaternion rot) {

    }

    @Override
    public void setUUID(UUID uuid) {

    }

    @Override
    public void setMass(float mass) {

    }

    @Override
    public void setCollisionShape(CollisionShape shape) {

    }

    <T> T readOrDefault(Supplier<T> read, T _default) {
        try {
            return read.get();
        } catch (Exception ignore) {
            return _default;
        }
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

    @Override
    public void read(CRBinDeserializer deserial) {
        super.read(deserial);

        IPhysicEntity.read(this, deserial);
        IVirtualZoneEntity.read(this, deserial);
        world.recalculateBounds();
        getBoundingBox(localBoundingBox);

        Threads.runOnMainThread(() -> modelInstance = IMeshInstancer.createZoneMesh(world));
    }

    @Override
    public void write(CRBinSerializer serial) {
        super.write(serial);

        IPhysicEntity.write(this, serial);
        IVirtualZoneEntity.write(this, serial);
    }

    @Override
    public void getBoundingBox(BoundingBox boundingBox) {
        ((ExtendedBoundingBox) boundingBox).setInnerBounds(oBoundingBox);
        boundingBox.update();
    }

}
