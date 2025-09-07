package me.zombii.horizon.mixins.client;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import finalforeach.cosmicreach.BlockRaycasts;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.networking.client.ClientNetworkManager;
import finalforeach.cosmicreach.networking.packets.entities.AttackEntityPacket;
import finalforeach.cosmicreach.networking.packets.entities.InteractEntityPacket;
import finalforeach.cosmicreach.settings.Controls;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockRaycasts.class)
public class MixinBlockRaycasts {

    @Shadow public Vector3 intersection;

    @Shadow private BoundingBox tmpBoundingBox;

    @Shadow private Vector3 intersectionPoint;

    @Shadow private float maximumRaycastDist;

    /**
     * @author
     * @reason
     */
    @Overwrite
    private boolean raycastForEntities(Zone zone, Ray ray, Player player) {
        boolean shouldInteract = Controls.usePlaceJustPressed();
        boolean shouldAttack = Controls.attackBreakJustPressed();
        if (!shouldAttack && !shouldInteract) {
            return false;
        } else {
            Entity playerEntity = player.getEntity();

            for (Entity e : zone.getAllEntities()) {
                if (e != playerEntity) {
                    e.getBoundingBox(this.tmpBoundingBox);
                    if (Intersector.intersectRayBounds(ray, this.tmpBoundingBox, this.intersectionPoint)) {
                        float distance = this.intersectionPoint.dst(ray.origin);
                        if (!(distance > this.maximumRaycastDist)) {
                            if (shouldAttack) {
                                if (GameSingletons.isHost) {
                                    e.onAttackInteraction(player, UI.hotbar.getSelectedSlotNum());
                                }

                                if (ClientNetworkManager.isConnected()) {
                                    ClientNetworkManager.sendAsClient(new AttackEntityPacket(e, UI.hotbar.getSelectedSlotNum()));
                                }
                            }

                            if (shouldInteract) {
                                ItemStack heldItemStack = UI.hotbar.getSelectedItemStack();
                                if (GameSingletons.isHost) {
                                    e.onUseInteraction(InGame.getLocalPlayer(), heldItemStack);
                                }

                                if (ClientNetworkManager.isConnected()) {
                                    ClientNetworkManager.sendAsClient(new InteractEntityPacket(e, UI.hotbar.getSelectedSlotNum()));
                                }

                                if (heldItemStack != null && heldItemStack.amount <= 0) {
                                    UI.hotbar.getSelectedSlot().setItemStack((ItemStack) null);
                                }
                            }

                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }

}
