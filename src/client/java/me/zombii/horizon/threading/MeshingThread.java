package me.zombii.horizon.threading;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.PauseableThread;
import com.badlogic.gdx.utils.Queue;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.rendering.SharedQuadIndexData;
import finalforeach.cosmicreach.rendering.meshes.GameMesh;
import finalforeach.cosmicreach.rendering.meshes.MeshData;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Chunk;
import me.zombii.horizon.rendering.mesh.IPhysicChunk;
import me.zombii.horizon.util.VCosmicMeshingUtil;
import me.zombii.horizon.world.PhysicsZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class MeshingThread implements Runnable {

    public static Logger LOGGER = LoggerFactory.getLogger("Horizon Meshing Thread");
    public static MeshingThread INSTANCE;
    public static PauseableThread parent;

    static final Queue<Runnable> queuedRunnables = new Queue<>();

    public static AtomicReference<VirtualChunkMeshMeta> post(AtomicReference<VirtualChunkMeshMeta> ref, PhysicsZone zone, Chunk chunk) {
        if (chunk.blockData.isEntirely(Block.AIR.getDefaultBlockState()))
            return ref;

        queuedRunnables.addLast(() -> {
            synchronized (ref) {
                Threads.runOnMainThread(() -> {
                    Array<MeshData> array = VCosmicMeshingUtil.getMeshData(zone, chunk);

                    VirtualChunkMeshMeta data = ref.get() != null ? ref.get() : new VirtualChunkMeshMeta();

                    if (array != null && !array.isEmpty()) {
                        VirtualChunkMeshMeta meshData = MeshingThread.INSTANCE.buildChunkMeshMeta(data, array);
                        ref.set(meshData);
                    }
                    ((IPhysicChunk) chunk).setNeedsRemeshing(false);
                });
            }
        });

        if (!started) start();
        else parent.onResume();

        return ref;
    }

    public static AtomicReference<VirtualChunkMeshMeta> post(PhysicsZone zone, Chunk chunk) {
        return post(new AtomicReference<>(), zone, chunk);
    }

    public static class VirtualChunkMeshMeta {
        public GameShader defaultLayerShader;
        public GameMesh defaultLayerMesh;
        public GameShader semiTransparentLayerShader;
        public GameMesh semiTransparentLayerMesh;
        public GameShader transparentLayerShader;
        public GameMesh transparentLayerMesh;
    }

    public MeshingThread() {
        INSTANCE = this;
        parent = (PauseableThread) ThreadHelper.getThread("meshing");
    }

    @Override
    public void run() {
        synchronized (queuedRunnables) {
            while (!queuedRunnables.isEmpty()) {
                Runnable runnable = queuedRunnables.removeFirst();
                if (runnable != null)
                    runnable.run();
                else
                    LOGGER.warn("Uh oh, A null runnable was found on the `MeshingThread`");
            }
        }
        parent.onPause();
    }

    public static boolean started = false;

    private VirtualChunkMeshMeta buildChunkMeshMeta(VirtualChunkMeshMeta meta, Array<MeshData> data) {
        for (int i = 0; i < data.size; i++) {
            MeshData meshData = data.get(i);
            switch (data.get(i).getRenderOrder()) {
                case FULLY_TRANSPARENT -> {
                    meta.transparentLayerMesh = buildMesh(meshData);
                    meta.transparentLayerShader = meshData.getShader();
                }
                case PARTLY_TRANSPARENT -> {
                    meta.semiTransparentLayerMesh = buildMesh(meshData);
                    meta.semiTransparentLayerShader = meshData.getShader();
                }
                case DEFAULT -> {
                    meta.defaultLayerMesh = buildMesh(meshData);
                    meta.defaultLayerShader = meshData.getShader();
                }
            }
        }
        return meta;
    }

    private GameMesh buildMesh(MeshData data) {
//        if (BlockModelJson.useIndices) {
//            return data.toIntIndexedMesh(true);
//        } else {
            GameMesh mesh = data.toSharedIndexMesh(true);
            if (mesh != null) {
                int numIndices = (mesh.getNumVertices() * 6) / 4;
                SharedQuadIndexData.allowForNumIndices(numIndices, false);
            }
            return mesh;
//        }
    }

    public static void clear() {
        synchronized (queuedRunnables) {
            queuedRunnables.clear();
        }

        parent.onPause();
    }

    public static void pause() {
        parent.onPause();
    }

    public static void resume() {
        parent.onResume();
    }

    public static void init() {
        parent = ThreadHelper.createPauseable("meshing", new MeshingThread());
    }

    public static PauseableThread start() {
        started = true;
        if (parent == null) {
            throw new RuntimeException("Call `init()` on the `MeshingThread` first.");
        }
//        try {
        parent.start();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        return parent;
    }
}
