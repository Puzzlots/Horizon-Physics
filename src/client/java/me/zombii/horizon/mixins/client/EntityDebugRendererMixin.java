package me.zombii.horizon.mixins.client;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import finalforeach.cosmicreach.BlockSelection;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.networking.client.ClientNetworkManager;
import finalforeach.cosmicreach.rendering.entities.EntityDebugRenderer;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
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

@Mixin(EntityDebugRenderer.class)
public class EntityDebugRendererMixin {

    @Shadow private static ShapeRenderer sr;

    @Shadow private static BoundingBox bb;

    /**
     * @author Mr_Zombii
     * @reason Render Special Bounding Boxes
     */
    @Overwrite
    public static void drawEntityDebugBoundingBoxes(Zone playerZone, Camera rawWorldCamera) {
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

}
