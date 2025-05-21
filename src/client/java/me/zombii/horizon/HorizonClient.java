package me.zombii.horizon;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.github.puzzle.core.loader.launch.provider.mod.entrypoint.impls.ClientPostModInitializer;
import com.github.puzzle.core.loader.launch.provider.mod.entrypoint.impls.ClientPreModInitializer;
import finalforeach.cosmicreach.Threads;
import me.zombii.horizon.mesh.MeshInstancer;
import me.zombii.horizon.threading.LidarThread;
import me.zombii.horizon.threading.MeshingThread;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.util.DebugRenderUtil;
import me.zombii.horizon.util.InGameAccess;
import me.zombii.horizon.util.ItemRegistrar;
import me.zombii.horizon.world.physics.ChunkMeta;

public class HorizonClient implements ClientPreModInitializer, ClientPostModInitializer {

    @Override
    public void onPreInit() {
        MeshingThread.init();
        LidarThread.init();
        LidarThread.start();

        HorizonConstants.MESHER_INSTANCE = new MeshInstancer();
        HorizonConstants.ITEM_REGISTRAR_INSTANCE = new ItemRegistrar();
    }

    @Override
    public void onPostInit() {
        HorizonConstants.EXEC = (c) -> {
            if (c.modelInstance == null) return;

            ChunkMeta meta = PhysicsThread.chunkMap.get(c.zone.getChunkAtPosition(c.position));
            if (meta != null)
                DebugRenderUtil.renderRigidBody(InGameAccess.getAccess().getShapeRenderer(), meta.getBody());

            DebugRenderUtil.renderRigidBody(InGameAccess.getAccess().getShapeRenderer(), c.body);
        };
    }
}
