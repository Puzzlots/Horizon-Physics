package me.zombii.horizon.threading;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.PauseableThread;
import com.badlogic.gdx.utils.Queue;
import com.github.puzzle.core.Constants;
import com.github.puzzle.core.loader.meta.EnvType;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.world.Chunk;
import me.zombii.horizon.entity.api.IPhysicEntity;
import me.zombii.horizon.mesh.IBlockBoundsMaker;
import me.zombii.horizon.util.ConversionUtil;
import me.zombii.horizon.util.NativeLibraryLoader;
import me.zombii.horizon.world.PhysicsZone;
import me.zombii.horizon.world.VirtualChunk;
import me.zombii.horizon.world.VirtualWorld;
import me.zombii.horizon.world.physics.ChunkMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PhysicsThread implements TickingRunnable {

    public static Logger LOGGER = LoggerFactory.getLogger("Horizon Physics Thread");
    public static PhysicsThread INSTANCE;
    public static final Queue<PhysicsBody> queuedObjects = new Queue<>();
    public static final Map<Chunk, ChunkMeta> chunkMap = new HashMap<>();
    public static final Map<UUID, IPhysicEntity> allEntities = new HashMap<>();
    public static final Map<PhysicsBody, IPhysicEntity> allEntitiesByBody = new HashMap<>();
    public static final Queue<PhysicsBody> bodiesToRemove = new Queue<>();
    public static final Queue<Runnable> queuedRunnables = new Queue<>();

    public PhysicsSpace space;
    public static PauseableThread parent;

    public PhysicsThread() {
        INSTANCE = this;
        parent = (PauseableThread) ThreadHelper.getThread("physics");
    }

    boolean isInitialized = false;
    public boolean shouldRun = false;

    public static void post(PhysicsZone world) {
        queuedRunnables.addLast(() -> {
            synchronized (world) {
                boolean isNew = false;
                CompoundCollisionShape collisionShape = new CompoundCollisionShape();
//                if (collisionShape == null) {
//                    isNew = true;
//                    collisionShape = new CompoundCollisionShape();
//                }
//
//                if (!isNew) {
//                    for (ChildCollisionShape shape : collisionShape.listChildren()) {
//                        System.out.println("pebus");
//                        collisionShape.removeChildShape(shape.getShape());
//                    }
//                }



                try {
                    world.chunks.forEach(c -> {
                        INSTANCE.createPhysicsMesh2(collisionShape, c);
                    });
                } catch (Exception ignore) {}

                world.CCS = collisionShape;
                world.CCS_WAS_REBUILT = true;
                System.gc();
            }
        });
    }

    void physicsInit() {
        if (isInitialized) return;
        isInitialized = true;

        space = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
        space.setGravity(new Vector3f(0, -9.81f, 0));

        synchronized (queuedObjects) {
            while (!queuedObjects.isEmpty()) {
                space.addCollisionObject(queuedObjects.removeFirst());
            }
        }
    }

    public static void alertChunk(final Chunk chunk) {
        if (chunk == null) return;
        INSTANCE.meshChunk(chunk);
        Array<Chunk> adjacentChunks = new Array<>();
        chunk.getAdjacentChunks(chunk.region.zone, adjacentChunks);
        while (!adjacentChunks.isEmpty()) {
            Chunk c = adjacentChunks.pop();
            INSTANCE.meshChunk(c);
        }
    }

    void meshChunk(final Chunk chunk) {
        synchronized (chunk.region.zone) {
            ChunkMeta meta = chunkMap.get(chunk);
            boolean chunkExist = meta != null;

            if (chunkExist && meta.getValidity() == ChunkMeta.ChunkValidity.HAS_RIGID_BODY && !meta.hasNullBody()) return;
            if (!chunkExist) meta = new ChunkMeta();

            IBlockData<BlockState> blocks = chunk.blockData;
            if (blocks == null || blocks.isEntirely((b) -> !b.walkThrough) || blocks.isEntirely(Block.AIR.getDefaultBlockState())) return;

            CompoundCollisionShape shape = INSTANCE.createPhysicsMesh(chunk);
            meta.setValidity(ChunkMeta.ChunkValidity.HAS_RIGID_BODY);
            if (!chunkExist) {
                PhysicsRigidBody body = new PhysicsRigidBody(shape, 0);
                body.setPhysicsLocation(new Vector3f(chunk.blockX, chunk.blockY, chunk.blockZ));
                meta.setBody(body);
                addPhysicsBody(body);
                chunkMap.put(chunk, meta);
                return;
            }
            meta.getBody().setCollisionShape(shape);
        }
    }

    public static IPhysicEntity addEntity(IPhysicEntity entity) {
        System.out.println(entity.getClass().getName());

        allEntities.put(entity.getUUID(), entity);
        allEntitiesByBody.put(entity.getBody(), entity);
        addPhysicsBody(entity.getBody());
        return entity;
    }

    public static <T extends Entity & IPhysicEntity> T getByBody(PhysicsBody body) {
        return (T) allEntitiesByBody.get(body);
    }

    public static <T extends Entity & IPhysicEntity> T removeEntity(T entity) {
        allEntities.remove(entity.getUUID());
        bodiesToRemove.addLast(entity.getBody());
        return entity;
    }

    public static void post(Runnable runnable) {
        queuedRunnables.addLast(runnable);
    }

    public static void post(VirtualWorld world) {
        queuedRunnables.addLast(() -> {
            synchronized (world) {
                boolean isNew = false;
                CompoundCollisionShape collisionShape = new CompoundCollisionShape();
//                if (collisionShape == null) {
//                    isNew = true;
//                    collisionShape = new CompoundCollisionShape();
//                }
//
//                if (!isNew) {
//                    for (ChildCollisionShape shape : collisionShape.listChildren()) {
//                        System.out.println("pebus");
//                        collisionShape.removeChildShape(shape.getShape());
//                    }
//                }



                try {
                    for (VirtualChunk chunk : world.structureMap.values()) {
                        INSTANCE.createPhysicsMesh(collisionShape, chunk);
                    }
                } catch (Exception ignore) {}

                world.CCS = collisionShape;
                world.CCS_WAS_REBUILT = true;
                System.gc();
            }
        });
    }

    public static void invalidateChunk(Chunk chunk) {
        if (chunk == null || !chunkMap.containsKey(chunk)) return;

        chunkMap.get(chunk).setValidity(ChunkMeta.ChunkValidity.NEEDS_PHYSICS_REMESHING);
        synchronized (allEntities) {
            for (UUID uuid : allEntities.keySet()) {
                IPhysicEntity entity = allEntities.get(uuid);
                try {
                    entity.getBody().activate(true);
                } catch (Exception e) {
                    LOGGER.warn("Null body found for entity \"{}\" with uuid \"{}\"", ((Entity) entity).entityTypeId, uuid);
                }
            }
        }
    }

    private static <T extends PhysicsBody> T addPhysicsBody(T body) {
        queuedObjects.addLast(body);
        return body;
    }

    @Override
    public void run(float delta) {
        if (!isInitialized) physicsInit();
        if (shouldRun) {
            synchronized (queuedRunnables) {
                while (!queuedRunnables.isEmpty()) {
                    queuedRunnables.removeFirst().run();
                }
                space.activateAll(true);
            }

            synchronized (bodiesToRemove) {
                while (!bodiesToRemove.isEmpty()) {
                    space.removeCollisionObject(bodiesToRemove.removeFirst());
                }
            }

            synchronized (queuedObjects) {
                while (!queuedObjects.isEmpty()) {
                    space.addCollisionObject(queuedObjects.removeFirst());
                }
            }

            synchronized (allEntities) {
                space.update(delta);
            }
        }
    }

    public static void clear() {
        synchronized (queuedObjects) {
            queuedObjects.clear();
            chunkMap.clear();
            allEntities.clear();
            queuedRunnables.clear();
        }

        INSTANCE.shouldRun = false;
        parent.onPause();
    }

    public static void pause() {
        INSTANCE.shouldRun = false;
        parent.onPause();
    }

    public static boolean started = false;

    public static void resume() {
        INSTANCE.shouldRun = true;
        parent.onResume();
    }

    public static void init() {
        parent = ThreadHelper.createTicking("physics", new PhysicsThread());

        boolean success = NativeLibraryLoader.loadLibbulletjme(com.github.puzzle.core.Constants.SIDE == EnvType.CLIENT, "Release", "Sp");
        if (!success) {
            throw new RuntimeException("Failed to load native library. Please contact nab138, he may need to add support for your platform.");
        }
    }

    public static PauseableThread start() {
        if (started) {
            INSTANCE.shouldRun = true;
            resume();
            return null;
        }
        if (parent == null) {
            throw new RuntimeException("Call `init()` on the `PhysicsThread` first.");
        }

        parent.start();
        INSTANCE.shouldRun = true;

        started = true;
        return parent;
    }

    // Collision Mesh Util
    public CompoundCollisionShape createPhysicsMesh(CompoundCollisionShape mesh, VirtualChunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    int globalX = (chunk.chunkPos.x() * 16) + x;
                    int globalY = (chunk.chunkPos.y() * 16) + y;
                    int globalZ = (chunk.chunkPos.z() * 16) + z;

                    BlockState state = chunk.getBlockState(x, y, z);
                    if (!isCollideableState(state)) { continue; }
                    shapeFromBlockState(mesh, new Vector3f(globalX, globalY, globalZ).add(0.5f, 0.5f, 0.5f), state);
                }
            }
        }

        return mesh;
    }

    public CompoundCollisionShape createPhysicsMesh2(CompoundCollisionShape mesh, Chunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    int globalX = (chunk.chunkX * 16) + x;
                    int globalY = (chunk.chunkY * 16) + y;
                    int globalZ = (chunk.chunkZ * 16) + z;

                    BlockState state = chunk.getBlockState(x, y, z);
                    if (!isCollideableState(state)) { continue; }
                    shapeFromBlockState(mesh, new Vector3f(globalX, globalY, globalZ).add(0.5f, 0.5f, 0.5f), state);
                }
            }
        }

        return mesh;
    }

    public CompoundCollisionShape createPhysicsMesh(CompoundCollisionShape mesh, Chunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockState state = chunk.getBlockState(x, y, z);
                    if (!isCollideableState(state)) continue;
                    shapeFromBlockState(mesh, new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f), state);
                }
            }
        }

        return mesh;
    }

    public CompoundCollisionShape createPhysicsMesh(VirtualChunk chunk) {
        return createPhysicsMesh(new CompoundCollisionShape(), chunk);
    }

    public CompoundCollisionShape createPhysicsMesh(Chunk chunk) {
        return createPhysicsMesh(new CompoundCollisionShape(), chunk);
    }

    private static final Map<String, BoundingBox[]> blockStateArrayMap = new ConcurrentHashMap<>();

    public CompoundCollisionShape shapeFromBlockState(CompoundCollisionShape mesh, Vector3f vector3f, BlockState state){
        BoundingBox[] boundingBoxes;
        if (blockStateArrayMap.containsKey(state.getSaveKey())) {
            boundingBoxes = blockStateArrayMap.get(state.getSaveKey());
        } else {
            boundingBoxes = ((IBlockBoundsMaker)state.getModel()).getBounds();
            blockStateArrayMap.put(state.getSaveKey(), boundingBoxes);
        }

        for (BoundingBox b : boundingBoxes) {
            Vector3f halfExtents = new Vector3f((b.max.x - b.min.x) / 2, (b.max.y - b.min.y) / 2, (b.max.z - b.min.z) / 2);
            Vector3f center = new Vector3f(b.getCenterX() - 0.5f, b.getCenterY() - 0.5f, b.getCenterZ() - 0.5f);
            BoxCollisionShape boxShape = new BoxCollisionShape(halfExtents);
            mesh.addChildShape(boxShape, vector3f.add(center));
        }

        return mesh;
    }

    boolean isCollideableState(BlockState state) {
        return !(state == null || state.walkThrough);
    }

}
