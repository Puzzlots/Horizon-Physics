package me.zombii.horizon.world;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.github.puzzle.core.loader.util.Reflection;
import com.github.puzzle.game.util.IClientNetworkManager;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.world.*;
import finalforeach.cosmicreach.worldgen.ZoneGenerator;
import me.zombii.horizon.HorizonConstants;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.util.APoint3dMap;

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

        Reflection.setFieldContents(this, "chunks", new APoint3dMap<>());

        if (PhysicsThread.INSTANCE != null && !IClientNetworkManager.isConnected())
            CCS = new CompoundCollisionShape();
    }

    public static PhysicsZone create(UUID uuid) {
        return new PhysicsZone(GameSingletons.world, uuid.toString(), ZoneGenerator.getZoneGenerator(HorizonConstants.MOD_ID + ":null"));
    }

    @Override
    public void addChunk(Chunk chunk) {
//        chunk.region = getRegionAtRegionCoords(mainCoords.x(), mainCoords.y(), mainCoords.z());
//        synchronized(this.getRegionLock()) {
//            Region region = chunk.region;
//            if (region == null) {
//                region = new Region(this, 0, 0, 0);
//                this.addRegion(region);
//            }
//
//            region.putChunk(chunk);
//        }
        ((PhysicsChunk)chunk).setZone(this);
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
        if (chunk == null) return emptyChunk;
        return chunk;
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
