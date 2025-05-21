package me.zombii.horizon.mixins.client;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.jme3.bullet.objects.PhysicsRigidBody;
import finalforeach.cosmicreach.BlockSelection;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.networking.client.ClientNetworkManager;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.HorizonConstants;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
import me.zombii.horizon.entity.api.IPhysicEntity;
import me.zombii.horizon.items.LidarGun;
import me.zombii.horizon.rendering.IShapeRenderer;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.threading.ThreadHelper;
import me.zombii.horizon.util.DebugRenderUtil;
import me.zombii.horizon.util.InGameAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGame.class)
public class InGameMixin implements InGameAccess {

    @Shadow private static ShapeRenderer sr;

    @Shadow private static BoundingBox bb;

    @Shadow private static PerspectiveCamera rawWorldCamera;

    @Shadow private BlockSelection blockSelection;

    /**
     * @author Mr_Zombii
     * @reason Render Special Bounding Boxes
     */
    @Overwrite
    private static void drawEntityDebugBoundingBoxes(Zone playerZone) {
        if (sr == null) sr = new ShapeRenderer();
        if (bb == null) bb = new BoundingBox();
        if (sr.isDrawing()) return;
        sr.setProjectionMatrix(rawWorldCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Line);

        for(Entity e : playerZone.getAllEntities()) {
            e.getBoundingBox(bb);

            if (((ExtendedBoundingBox) bb).hasInnerBounds()) {
                DebugRenderUtil.renderBoundingBox(sr, ((ExtendedBoundingBox) bb).getInnerBounds());
            } else {
                DebugRenderUtil.renderBoundingBox(sr, bb);
            }
        }

        sr.end();
    }

    @Override
    public ShapeRenderer getShapeRenderer() {
        if (sr != null) return sr;
        sr = new ShapeRenderer();
        return sr;
    }

    @Override
    public PerspectiveCamera getRawWorldCamera() {
        return rawWorldCamera;
    }

    @Override
    public Vector3 getPlayerFacing() {
        return rawWorldCamera.direction.cpy();
    }

    @Override
    public BlockSelection getBlockSelection() {
        return blockSelection;
    }

    @Inject(method = "unloadWorld", at = @At("HEAD"))
    private void exitWorld(CallbackInfo ci) {
        if (!ClientNetworkManager.isConnected())
            if (PhysicsThread.INSTANCE != null)
                PhysicsThread.clear();
    }

    @Inject(method = "loadWorld(Lfinalforeach/cosmicreach/world/World;)V", at = @At("HEAD"))
    private void joinWorld(World world, CallbackInfo ci) {
        if (!ClientNetworkManager.isConnected())
            if (PhysicsThread.INSTANCE != null)
                PhysicsThread.start();
    }

    @Inject(method = "dispose", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/gamestates/InGame;unloadWorld()V", shift = At.Shift.AFTER))
    private void dispose(CallbackInfo ci) {
        ThreadHelper.killAll();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/items/ItemRenderer;renderHeldItem(Lcom/badlogic/gdx/math/Vector3;Lfinalforeach/cosmicreach/items/ItemStack;Lcom/badlogic/gdx/graphics/PerspectiveCamera;)V", shift = At.Shift.BEFORE))
    private void render(CallbackInfo ci) {
        if (sr == null) sr = new ShapeRenderer();

        sr.setProjectionMatrix(rawWorldCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Point);

        if (UI.hotbar.getSelectedItemStack() != null && UI.hotbar.getSelectedItemStack().getItem() instanceof LidarGun) {
            for (Vector3 point : IShapeRenderer.points) {
                if (IShapeRenderer.points.size() >= 90000) {
                    IShapeRenderer.points.remove(0);
                }
                if (point.dst(InGame.getLocalPlayer().getPosition()) <= 128) {
                    sr.point(point.x, point.y, point.z);
                }
            }
        }

        sr.end();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/gamestates/InGame;switchToGameState(Lfinalforeach/cosmicreach/gamestates/GameState;)V", ordinal = 0, shift = At.Shift.BEFORE))
    private void pause(CallbackInfo ci) {
        if (!ClientNetworkManager.isConnected())
            if (PhysicsThread.INSTANCE != null)
                PhysicsThread.pause();
    }

}
