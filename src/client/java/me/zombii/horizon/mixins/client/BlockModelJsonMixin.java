package me.zombii.horizon.mixins.client;

import com.badlogic.gdx.math.collision.BoundingBox;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJson;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJsonCuboid;
import me.zombii.horizon.collision.AABB;
import me.zombii.horizon.rendering.mesh.IBlockBoundsMaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockModelJson.class)
public class BlockModelJsonMixin implements IBlockBoundsMaker {

    @Shadow private BlockModelJsonCuboid[] cuboids;

    @Override
    public AABB[] getBounds() {
        if (this.cuboids == null) return new AABB[0];
        AABB[] boundingBoxes = new AABB[this.cuboids.length];

        for (int i = 0; i < boundingBoxes.length; i++) {
            AABB bb = new AABB();
            BlockModelJsonCuboid c = this.cuboids[i];

            float[] localBounds = c.getLocalBounds();
            float xA = localBounds[0] / 16.0F;
            float xB = localBounds[3] / 16.0F;
            float yA = localBounds[1] / 16.0F;
            float yB = localBounds[4] / 16.0F;
            float zA = localBounds[2] / 16.0F;
            float zB = localBounds[5] / 16.0F;
            bb.setMin(Math.min(xA, xB), Math.min(yA, yB), Math.min(zA, zB)).sub(c.inflate / 16.0F);
            bb.setMax(Math.max(xA, xB), Math.max(yA, yB), Math.max(zA, zB)).add(c.inflate / 16.0F);
            bb.update();
            boundingBoxes[i] = bb;
        }
        return boundingBoxes;
    }

    @Override
    public AABB[] getBounds(int bx, int by, int bz) {
        if (this.cuboids == null) return new AABB[0];
        AABB[] boundingBoxes = new AABB[this.cuboids.length];

        for (int i = 0; i < boundingBoxes.length; i++) {
            AABB bb = new AABB();
            BlockModelJsonCuboid c = this.cuboids[i];

            float[] localBounds = c.getLocalBounds();
            float xA = localBounds[0] / 16.0F;
            float xB = localBounds[3] / 16.0F;
            float yA = localBounds[1] / 16.0F;
            float yB = localBounds[4] / 16.0F;
            float zA = localBounds[2] / 16.0F;
            float zB = localBounds[5] / 16.0F;
            bb.setMin(bx + Math.min(xA, xB), by + Math.min(yA, yB), bz + Math.min(zA, zB)).sub(c.inflate / 16.0F);
            bb.setMax(bx + Math.max(xA, xB), by + Math.max(yA, yB), bz + Math.max(zA, zB)).add(c.inflate / 16.0F);
            bb.update();
            boundingBoxes[i] = bb;
        }
        return boundingBoxes;
    }

    @Override
    public BoundingBox[] getBoundsGdx() {
        if (this.cuboids == null) return new BoundingBox[0];
        BoundingBox[] boundingBoxes = new BoundingBox[this.cuboids.length];

        for (int i = 0; i < boundingBoxes.length; i++) {
            BoundingBox bb = new BoundingBox();
            BlockModelJsonCuboid c = this.cuboids[i];

            float[] localBounds = c.getLocalBounds();
            float xA = localBounds[0] / 16.0F;
            float xB = localBounds[3] / 16.0F;
            float yA = localBounds[1] / 16.0F;
            float yB = localBounds[4] / 16.0F;
            float zA = localBounds[2] / 16.0F;
            float zB = localBounds[5] / 16.0F;
            bb.min.set(Math.min(xA, xB), Math.min(yA, yB), Math.min(zA, zB)).sub(c.inflate / 16.0F);
            bb.max.set(Math.max(xA, xB), Math.max(yA, yB), Math.max(zA, zB)).add(c.inflate / 16.0F);
            bb.update();
            boundingBoxes[i] = bb;
        }
        return boundingBoxes;
    }

    @Override
    public BoundingBox[] getBoundsGdx(int bx, int by, int bz) {
        if (this.cuboids == null) return new BoundingBox[0];
        BoundingBox[] boundingBoxes = new BoundingBox[this.cuboids.length];

        for (int i = 0; i < boundingBoxes.length; i++) {
            BoundingBox bb = new BoundingBox();
            BlockModelJsonCuboid c = this.cuboids[i];

            float[] localBounds = c.getLocalBounds();
            float xA = localBounds[0] / 16.0F;
            float xB = localBounds[3] / 16.0F;
            float yA = localBounds[1] / 16.0F;
            float yB = localBounds[4] / 16.0F;
            float zA = localBounds[2] / 16.0F;
            float zB = localBounds[5] / 16.0F;
            bb.min.set(bx + Math.min(xA, xB), by + Math.min(yA, yB), bz + Math.min(zA, zB)).sub(c.inflate / 16.0F);
            bb.max.set(bx + Math.max(xA, xB), by + Math.max(yA, yB), bz + Math.max(zA, zB)).add(c.inflate / 16.0F);
            bb.update();
            boundingBoxes[i] = bb;
        }
        return boundingBoxes;
    }
}
