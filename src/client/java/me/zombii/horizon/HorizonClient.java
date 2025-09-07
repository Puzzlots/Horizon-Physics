package me.zombii.horizon;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import dev.puzzleshq.puzzleloader.cosmic.core.modInitialises.ClientPostModInit;
import dev.puzzleshq.puzzleloader.cosmic.core.modInitialises.ClientPreModInit;
import finalforeach.cosmicreach.Threads;
import me.zombii.horizon.mesh.MeshInstancer;
import me.zombii.horizon.threading.LidarThread;
import me.zombii.horizon.threading.MeshingThread;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.util.DebugRenderUtil;
import me.zombii.horizon.util.InGameAccess;
import me.zombii.horizon.util.ItemRegistrar;
import me.zombii.horizon.world.physics.ChunkMeta;

public class HorizonClient implements ClientPreModInit, ClientPostModInit {

    @Override
    public void onClientPreInit() {
        MeshingThread.init();
        LidarThread.init();
        LidarThread.start();

        HorizonConstants.MESHER_INSTANCE = new MeshInstancer();
        HorizonConstants.ITEM_REGISTRAR_INSTANCE = new ItemRegistrar();
    }

    @Override
    public void onClientPostInit() {
        HorizonConstants.EXEC = (c) -> {
            if (c.modelInstance == null) return;

            ChunkMeta meta = PhysicsThread.chunkMap.get(c.zone.getChunkAtPosition(c.position));
            if (meta != null)
                DebugRenderUtil.renderRigidBody(InGameAccess.getAccess().getShapeRenderer(), meta.getBody());

            DebugRenderUtil.renderRigidBody(InGameAccess.getAccess().getShapeRenderer(), c.body);
        };
    }
}
