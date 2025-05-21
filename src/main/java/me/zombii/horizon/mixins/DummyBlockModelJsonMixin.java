package me.zombii.horizon.mixins;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import finalforeach.cosmicreach.rendering.blockmodels.DummyBlockModel;
import me.zombii.horizon.collision.AABB;
import me.zombii.horizon.rendering.mesh.IBlockBoundsMaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DummyBlockModel.class)
public class DummyBlockModelJsonMixin implements IBlockBoundsMaker {

    @Shadow private Array<BoundingBox> boundingBoxes;

    @Override
    public AABB[] getBounds() {
        if (this.boundingBoxes == null) return new AABB[0];
        AABB[] boundingBoxes = new AABB[this.boundingBoxes.items.length];

        for (int i = 0; i < this.boundingBoxes.items.length; i++) {
            AABB bb = new AABB();

            bb.setMin(this.boundingBoxes.items[i].min.x, this.boundingBoxes.items[i].min.y, this.boundingBoxes.items[i].min.z);
            bb.setMin(this.boundingBoxes.items[i].max.x, this.boundingBoxes.items[i].max.y, this.boundingBoxes.items[i].max.z);
            bb.update();
            boundingBoxes[i] = bb;
        }
        return boundingBoxes;
    }

    @Override
    public AABB[] getBounds(int bx, int by, int bz) {
        if (this.boundingBoxes == null) return new AABB[0];
        AABB[] boundingBoxes = new AABB[this.boundingBoxes.items.length];

        for (int i = 0; i < this.boundingBoxes.items.length; i++) {
            AABB bb = new AABB();

            bb.setMin(this.boundingBoxes.items[i].min.x + bx, this.boundingBoxes.items[i].min.y + by, this.boundingBoxes.items[i].min.z + bz);
            bb.setMax(this.boundingBoxes.items[i].max.x + bx, this.boundingBoxes.items[i].max.y + by, this.boundingBoxes.items[i].max.z + bz);
            bb.update();
            boundingBoxes[i] = bb;
        }
        return boundingBoxes;
    }

}
