package me.zombii.horizon.items;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.math.Matrix3f;
import com.jme3.math.Matrix4f;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.PlayerEntity;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.api.block.IBlockPosition;
import io.github.puzzle.cosmic.api.entity.player.IPlayer;
import io.github.puzzle.cosmic.api.item.IItemSlot;
import io.github.puzzle.cosmic.api.world.IZone;
import io.github.puzzle.cosmic.impl.ray.Raycaster;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import io.github.puzzle.cosmic.util.APISide;
import me.zombii.horizon.HorizonConstants;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
import me.zombii.horizon.entity.api.IPhysicEntity;
import me.zombii.horizon.entity.api.IVirtualZoneEntity;
import me.zombii.horizon.rendering.IShapeRenderer;
import me.zombii.horizon.rendering.mesh.IBlockBoundsMaker;
import me.zombii.horizon.threading.LidarThread;
import me.zombii.horizon.util.ConversionUtil;
import me.zombii.horizon.world.PhysicsZone;

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
    public boolean pUse(APISide side, IItemSlot itemSlot, IPlayer player, IBlockPosition targetPlaceBlockPos, IBlockPosition targetBreakBlockPos, boolean isLeftClick) {
        if (isLeftClick) {
            IShapeRenderer.points.clear();
            return false;
        }

        LidarThread.queue(player.pGetZone().as(), player.pGetPosition().cpy().add(player.pGetViewOffset()), player.pGetEntity().pGetViewDirection().cpy());
        return false;
    }

    @Override
    public boolean pIsTool() {
        return true;
    }
}
