package me.zombii.horizon.mixins.client;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import finalforeach.cosmicreach.EntityRaycasts;
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
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRaycasts.class)
public class MixinBlockRaycasts {

    @Unique
    private Vector3 horizonPhysics$intersection = new Vector3();

    @Shadow private BoundingBox tmpBoundingBox;

    @Shadow private float maximumRaycastDist;

    /**
     * @author
     * @reason
     */
    @Overwrite
    boolean raycastForEntities(Zone zone, Ray ray, Player player) {
        boolean shouldInteract = Controls.usePlaceJustPressed();
        boolean shouldAttack = Controls.attackBreakJustPressed();
        if (!shouldAttack && !shouldInteract) {
            return false;
        } else {
            Entity playerEntity = player.getEntity();

            for (Entity e : zone.getAllEntities()) {
                if (e != playerEntity) {
                    e.getBoundingBox(this.tmpBoundingBox);
                    if (Intersector.intersectRayBounds(ray, this.tmpBoundingBox, this.horizonPhysics$intersection)) {
                        float distance = this.horizonPhysics$intersection.dst(ray.origin);
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
