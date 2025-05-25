package me.zombii.horizon.items;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.github.puzzle.game.resources.PuzzleGameAssetLoader;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.api.block.IBlockPosition;
import io.github.puzzle.cosmic.api.entity.player.IPlayer;
import io.github.puzzle.cosmic.api.item.IItemSlot;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import io.github.puzzle.cosmic.util.APISide;
import me.zombii.horizon.HorizonConstants;
import me.zombii.horizon.entity.api.IPhysicEntity;
import me.zombii.horizon.items.api.I3DItem;
import me.zombii.horizon.util.PhysicsUtil;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

import java.util.concurrent.atomic.AtomicReference;

public class GravityGun extends AbstractCosmicItem implements I3DItem {

    Identifier modelLocation = Identifier.of(HorizonConstants.MOD_ID, "models/items/g3dj/PhysicsGun.glb");

    public GravityGun() {
        super(Identifier.of(HorizonConstants.MOD_ID, "gravity_gun"));
    }

    @Override
    public String toString() {
        return getID();
    }

    public static IPhysicEntity heldEntity;

    static Vector3 intersectionPoint = new Vector3();
    static Ray ray = new Ray();

    public static void move(PhysicsRigidBody body, Vector3 position) {
        Vector3 playerPos = GameSingletons.world.players.get(0).getPosition().cpy().add(0, 2, 0);
        Vector3 direction = GameSingletons.world.players.get(0).getEntity().viewDirection;
        playerPos.add(direction.cpy().scl(2f));
        Vector3f playerPosF = new Vector3f(playerPos.x, playerPos.y, playerPos.z);

        Vector3f myPos = new Vector3f(position.x, position.y, position.z);
        Vector3f dir = new Vector3f(playerPosF);
        dir = dir.subtract(myPos).mult(3);

        body.setLinearVelocity(dir);
        body.activate(true);
    }

    @Override
    public boolean pUse(APISide side, IItemSlot itemSlot, IPlayer player, IBlockPosition targetPlaceBlockPos, IBlockPosition targetBreakBlockPos, boolean isLeftClick) {
        if (side == APISide.SERVER || side == APISide.SINGLE_PLAYER_CLIENT && isLeftClick) {
            if (heldEntity != null) {
                heldEntity.setPickedUp(!heldEntity.isPickedUp());
                heldEntity = null;
                return true;
            }

            Vector3 rayStart = ((Player)player).getPosition().cpy().add(0, 1, 0);
            ray.set(rayStart, ((Player)player).getEntity().viewDirection);
            Vector3 rayEnd = rayStart.cpy().add(((Player)player).getEntity().viewDirection.cpy().scl(5));

            intersectionPoint.setZero();
            PhysicsUtil.raycast(intersectionPoint, ray, rayEnd, this::interact);
        }

        return super.pUse(side, itemSlot, player, targetPlaceBlockPos, targetBreakBlockPos, isLeftClick);
    }

    public void interact(float dist, Entity e, PhysicsRayTestResult result) {
        System.out.println("E " + ((IPhysicEntity) e).canBePickedUp());
        if (((IPhysicEntity) e).canBePickedUp()) {
            IPhysicEntity entity = (IPhysicEntity) e;
            entity.setPickedUp(!entity.isPickedUp());
            if (entity.isPickedUp()) heldEntity = entity;
        }
    }

    @Override
    public String getName() {
        return "Gravity Gun";
    }

    @Override
    public Identifier getModelLocation() {
        return modelLocation;
    }

    @Override
    public void loadModel(G3dModelLoader modelLoader, AtomicReference<ModelInstance> model) {
        final FileHandle modelHandle = PuzzleGameAssetLoader.locateAsset(getModelLocation());

        Threads.runOnMainThread(() -> {
            GLBLoader loader = new GLBLoader();
            SceneAsset model1 = loader.load(modelHandle, true);

            ModelInstance instance = new ModelInstance(model1.scene.model);

            model.set(instance);
        });
    }
}
