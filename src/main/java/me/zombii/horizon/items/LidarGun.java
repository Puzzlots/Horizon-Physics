package me.zombii.horizon.items;

import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.api.entity.player.IPlayer;
import io.github.puzzle.cosmic.api.world.IZone;
import io.github.puzzle.cosmic.impl.ray.Raycaster;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import io.github.puzzle.cosmic.util.APISide;
import me.zombii.horizon.HorizonConstants;
import me.zombii.horizon.rendering.IShapeRenderer;
import me.zombii.horizon.threading.LidarThread;

public class LidarGun extends AbstractCosmicItem {

    static final Identifier id = Identifier.of(HorizonConstants.MOD_ID, "lidar-gun");

    public LidarGun() {
        super(id);
        this.addTexture(ItemModelType.ITEM_MODEL_3D, Identifier.of("cosmic-api", "null_stick.png"));
    }

    Raycaster.RaycastContext context;

    IZone zone;
    IPlayer player;
    Entity[] entities;

    @Override
    public boolean use(APISide side, ItemSlot itemSlot, Player player, BlockPosition targetPlaceBlockPos, BlockPosition targetBreakBlockPos, boolean isLeftClick) {
        if (isLeftClick) {
            IShapeRenderer.points.clear();
            return false;
        }

        LidarThread.queue(player.getZone(), player.getPosition().cpy().add(getViewOffset(player)), player.getEntity().getViewDirection().cpy());
        return false;
    }

    private static Vector3 getViewOffset(Player player) {
        if (player.isSneaking()) return player.sneakingViewPositionOffset;
        if (player.isProne) return player.proneViewPositionOffset;
        return player.standingViewPositionOffset;
    }

    @Override
    public boolean isTool() {
        return true;
    }
}
