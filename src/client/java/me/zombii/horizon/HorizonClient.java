package me.zombii.horizon;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.client.ClientPostModInit;
import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.client.ClientPreModInit;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.items.ISlotContainerParent;
import finalforeach.cosmicreach.savelib.utils.TriConsumer;
import finalforeach.cosmicreach.ui.screens.custom.CustomItemScreen;
import me.zombii.horizon.mesh.MeshInstancer;
import me.zombii.horizon.threading.LidarThread;
import me.zombii.horizon.threading.MeshingThread;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.util.DebugRenderUtil;
import me.zombii.horizon.util.InGameAccess;
import me.zombii.horizon.util.ItemRegistrar;
import me.zombii.horizon.world.physics.ChunkMeta;

import static finalforeach.cosmicreach.ui.screens.custom.CustomScreenButtonEvents.buttonEvents;

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

//        buttonEvents.put("runTrigger", (button, screen, data) -> {
//            String triggerId = data.getString("triggerId", "");
//            button.addListener((event) -> {
//                if (event instanceof InputEvent ie) {
//                    if (ie.getType() != InputEvent.Type.touchDown) {
//                        return false;
//                    }
//
//                    if (screen instanceof CustomItemScreen itemScreen) {
//                        ISlotContainerParent patt0$temp = itemScreen.itemComponent.parent;
//                        if (patt0$temp instanceof CustomUIBlockEntity be) {
//                            BlockPosition bp = BlockPosition.ofGlobal(be.getZone(), be.getGlobalX(), be.getGlobalY(), be.getGlobalZ());
//                            Object netId = button.getUserObject();
//                            if (netId != null) {
//                                netId = netId.toString();
//                            }
//
//                            screen.runTrigger(data, (String)netId, triggerId, bp);
//                        }
//                    }
//                }
//
//                return false;
//            });
//        });
    }
}
