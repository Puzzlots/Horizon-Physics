package me.zombii.horizon.world;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import dev.puzzleshq.puzzleloader.loader.util.ReflectionUtil;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.rendering.IChunkMeshGroup;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.world.*;
import finalforeach.cosmicreach.worldgen.ZoneGenerator;
import me.zombii.horizon.HorizonConstants;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.util.APoint3dMap;
import me.zombii.horizon.worldgen.NullGenerator;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class PhysicsZone extends Zone {
    public BoundingBox AABB = new BoundingBox();
    public CollisionShape CCS;
    public boolean CCS_WAS_REBUILT = false;

    RegionCoords mainCoords = new RegionCoords(0, 0, 0);
    public static Chunk emptyChunk = new PhysicsChunk(0, 0, 0);
    static {
        emptyChunk.initChunkData();
    }

    public PhysicsZone(World world, String zoneId, ZoneGenerator worldGen) {
        super(world, zoneId, worldGen);

        try {
            ReflectionUtil.getField(this, "chunks").set(this, new APoint3dMap<>());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        if (PhysicsThread.INSTANCE != null && GameSingletons.isHost)
            CCS = new CompoundCollisionShape();
    }

    public static PhysicsZone create(UUID uuid) {
//        ZoneGenerator.getZoneGenerator(HorizonConstants.MOD_ID + ":null")
        return new PhysicsZone(GameSingletons.world, uuid.toString(), new NullGenerator());
    }

    public static PhysicsZone create(UUID uuid, ZoneGenerator worldGen) {
//        ZoneGenerator.getZoneGenerator(HorizonConstants.MOD_ID + ":null")
        return new PhysicsZone(GameSingletons.world, uuid.toString(), worldGen);
    }

    IChunkMeshGroup meshGroup = new IChunkMeshGroup() {

        @Override
        public boolean hasMesh() {
            return false;
        }

        @Override
        public Object getAllMeshData() {
            return null;
        }

        @Override
        public void flagForRemeshing(boolean b) {
            flagged = b;
        }

        boolean flagged;
        @Override
        public boolean isFlaggedForRemeshing() {
            return flagged;
        }

        @Override
        public boolean hasExpectedMeshGenCount() {
            return false;
        }

        @Override
        public void flushRemeshRequests() {

        }

        @Override
        public void setMeshVertices(Chunk chunk, Object o) {

        }

        @Override
        public boolean isFlaggedForImmediateRemesh() {
            return flagged;
        }

        @Override
        public void setToRemeshImmediately(boolean b) {
            flagForRemeshing(b);
        }

        @Override
        public void setExpectedMeshGenCount() {

        }

        @Override
        public void dispose() {

        }

        @Override
        public boolean isAllMeshDataEmpty() {
            return false;
        }

        @Override
        public void setUninitializedMeshGenCount() {

        }
    };

    @Override
    public void addChunk(Chunk chunk) {
        chunk.region = getRegionAtRegionCoords(mainCoords.x(), mainCoords.y(), mainCoords.z());
        chunk.setMeshGroup(meshGroup);
        synchronized(this.getRegionLock()) {
            PhysicsRegion region = (PhysicsRegion) chunk.region;
            if (region == null) {
                region = new PhysicsRegion(this, 0, 0, 0);
                this.addRegion(region);
            }

            region.putChunk(chunk);
        }
//        chunk.setZone(this);
        this.chunks.put(chunk, chunk.chunkX, chunk.chunkY, chunk.chunkZ);
//        super.addChunk(chunk);
        recalculateBounds();
    }

    @Override
    public int getSkyLight(Chunk candidateChunk, int x, int y, int z) {
        return 15;
    }

    public void recalculateBounds() {
        AtomicInteger max_x = new AtomicInteger();
        AtomicInteger max_y = new AtomicInteger();
        AtomicInteger max_z = new AtomicInteger();
        AtomicInteger min_x = new AtomicInteger();
        AtomicInteger min_y = new AtomicInteger();
        AtomicInteger min_z = new AtomicInteger();

        this.chunks.forEach(c -> {
            max_x.set(Math.max((16 * (c.chunkX + 1)), max_x.get()));
            max_y.set(Math.max((16 * (c.chunkY + 1)), max_y.get()));
            max_z.set(Math.max((16 * (c.chunkZ + 1)), max_z.get()));

            min_x.set(Math.min((16 * c.chunkX), min_x.get()));
            min_y.set(Math.min((16 * c.chunkY), min_y.get()));
            min_z.set(Math.min((16 * c.chunkZ), min_z.get()));
        });

        AABB.min.set(new Vector3(min_x.get(), min_y.get(), min_z.get()));
        AABB.max.set(new Vector3(max_x.get(), max_y.get(), max_z.get()));
    }

    @Override
    public Chunk getChunkAtChunkCoords(int cx, int cy, int cz) {
        Chunk chunk = this.chunks.get(cx, cy, cz);
//        if (chunk == null) return emptyChunk;
        return chunk;
    }

    public void setBlockState(BlockState block, int x, int y, int z) {
        int cx = Math.floorDiv(x, 16);
        int cy = Math.floorDiv(y, 16);
        int cz = Math.floorDiv(z, 16);
        Chunk c = this.getChunkAtChunkCoords(cx, cy, cz);
        if (c == null) {
            c = new PhysicsChunk(cx, cy, cz);
            c.initChunkData();
            this.addChunk(c);
        }

        x -= 16 * cx;
        y -= 16 * cy;
        z -= 16 * cz;
        c.setBlockState(block, x, y, z);
    }

    public void rebuildCollisionShape() {
        PhysicsThread.post(this);
    }

    public void propagateLight() {
        chunks.forEach((c) -> {
            lightPropagator.calculateLightingForChunk(this, c, false);
        });
    }
}
