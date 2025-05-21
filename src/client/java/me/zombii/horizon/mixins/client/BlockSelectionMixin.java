package me.zombii.horizon.mixins.client;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import finalforeach.cosmicreach.BlockSelection;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.networking.client.ClientNetworkManager;
import finalforeach.cosmicreach.networking.packets.entities.AttackEntityPacket;
import finalforeach.cosmicreach.networking.packets.entities.InteractEntityPacket;
import finalforeach.cosmicreach.settings.Controls;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.entity.api.IPhysicEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockSelection.class)
public class BlockSelectionMixin {

    @Shadow private Ray ray;

//    /**
//     * @author
//     * @reason
//     */
//    @Overwrite
//    private boolean raycastForEntities(Zone zone, Camera worldCamera) {
//        this.ray.set(worldCamera.position, worldCamera.direction);
//        boolean shouldInteract = Controls.usePlaceJustPressed();
//        boolean shouldAttack = Controls.attackBreakJustPressed();
//        if (!shouldAttack && !shouldInteract) {
//            return false;
//        } else {
//            Entity playerEntity = InGame.getLocalPlayer().getEntity();
//
//            for (Entity e : zone.getAllEntities()) {
//                    if (e != playerEntity) {
//                        e.getBoundingBox(this.tmpBoundingBox);
//                        if (Intersector.intersectRayBounds(this.ray, this.tmpBoundingBox, this.intersectionPoint)) {
//                            float distance = this.intersectionPoint.dst(worldCamera.position);
//                            if (!(distance > this.maximumRaycastDist)) {
//                                if (shouldAttack) {
//                                    if (GameSingletons.isHost) {
//                                        e.onAttackInteraction(playerEntity);
//                                    }
//
//                                    if (ClientNetworkManager.isConnected()) {
//                                        ClientNetworkManager.sendAsClient(new AttackEntityPacket(e));
//                                    }
//                                }
//
//                                if (shouldInteract) {
//                                    ItemStack heldItemStack = UI.hotbar.getSelectedItemStack();
//                                    if (GameSingletons.isHost) {
//                                        e.onUseInteraction(InGame.getLocalPlayer(), heldItemStack);
//                                    }
//
//                                    if (ClientNetworkManager.isConnected()) {
//                                        ClientNetworkManager.sendAsClient(new InteractEntityPacket(e, UI.hotbar.getSelectedSlotNum()));
//                                    }
//
//                                    if (heldItemStack != null && heldItemStack.amount <= 0) {
//                                        UI.hotbar.getSelectedSlot().itemStack = null;
//                                    }
//                                }
//
//                                return true;
//                            }
//                        }
//                    }
//            }
//
//            return false;
//        }
//    }

}
