package me.zombii.horizon.mixins;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoundingBox.class)
public abstract class BoundingBoxMixin implements ExtendedBoundingBox {

    @Shadow @Final public Vector3 min;
    @Shadow @Final public Vector3 max;

    @Shadow public abstract BoundingBox set(Vector3 minimum, Vector3 maximum);

    @Unique
    OrientedBoundingBox exampleMod$innerBoundingBox;

    @Inject(method = "set(Lcom/badlogic/gdx/math/collision/BoundingBox;)Lcom/badlogic/gdx/math/collision/BoundingBox;", at = @At("HEAD"), cancellable = true)
    public void set0(BoundingBox bounds, CallbackInfoReturnable<BoundingBox> cir) {
        if (((ExtendedBoundingBox)bounds).hasInnerBounds()) {
            exampleMod$innerBoundingBox = ((ExtendedBoundingBox)bounds).getInnerBounds();
        } else exampleMod$innerBoundingBox = null;
        cir.setReturnValue(this.set(bounds.min, bounds.max));
    }

    @Override
    public boolean hasInnerBounds() {
        return exampleMod$innerBoundingBox != null;
    }

    @Override
    public OrientedBoundingBox getInnerBounds() {
        return exampleMod$innerBoundingBox;
    }

    @Override
    public void setInnerBounds(OrientedBoundingBox boundingBox) {
        exampleMod$innerBoundingBox = boundingBox;
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public Vector3 getCorner000(Vector3 out) {
        if (hasInnerBounds()) {
            return getInnerBounds().getCorner000(out);
        } else {
            return out.set(min.x, min.y, min.z);
        }
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public Vector3 getCorner001(Vector3 out) {
        if (hasInnerBounds()) {
            return getInnerBounds().getCorner001(out);
        } else {
            return out.set(this.min.x, this.min.y, this.max.z);
        }
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public Vector3 getCorner010(Vector3 out) {
        if (hasInnerBounds()) {
            return getInnerBounds().getCorner010(out);
        } else {
            return out.set(this.min.x, this.max.y, this.min.z);
        }
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public Vector3 getCorner011(Vector3 out) {
        if (hasInnerBounds()) {
            return getInnerBounds().getCorner011(out);
        } else {
            return out.set(this.min.x, this.max.y, this.max.z);
        }
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public Vector3 getCorner100(Vector3 out) {
        if (hasInnerBounds()) {
            return getInnerBounds().getCorner100(out);
        } else {
            return out.set(this.max.x, this.min.y, this.min.z);
        }
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public Vector3 getCorner101(Vector3 out) {
        if (hasInnerBounds()) {
            return getInnerBounds().getCorner101(out);
        } else {
            return out.set(this.max.x, this.min.y, this.max.z);
        }
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public Vector3 getCorner110(Vector3 out) {
        if (hasInnerBounds()) {
            return getInnerBounds().getCorner110(out);
        } else {
            return out.set(this.max.x, this.max.y, this.min.z);
        }
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public Vector3 getCorner111(Vector3 out) {
        if (hasInnerBounds()) {
            return getInnerBounds().getCorner111(out);
        } else {
            return out.set(this.max.x, this.max.y, this.max.z);
        }
    }


    @Shadow public abstract boolean isValid();
    @Final
    @Shadow
    private static Vector3 tmpVector;
    @Final
    @Shadow
    private Vector3 cnt;
    @Final
    @Shadow
    private Vector3 dim;

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public boolean contains(BoundingBox b) {
        if (((ExtendedBoundingBox)b).hasInnerBounds() && hasInnerBounds())
            return getInnerBounds().contains(((ExtendedBoundingBox)b).getInnerBounds());
        if (hasInnerBounds()) return getInnerBounds().contains(b);

        return !this.isValid() || this.min.x <= b.min.x && this.min.y <= b.min.y && this.min.z <= b.min.z && this.max.x >= b.max.x && this.max.y >= b.max.y && this.max.z >= b.max.z;
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public boolean contains(OrientedBoundingBox obb) {
        if (hasInnerBounds()) return getInnerBounds().contains(obb);

        return this.contains(obb.getCorner000(tmpVector)) && this.contains(obb.getCorner001(tmpVector)) && this.contains(obb.getCorner010(tmpVector)) && this.contains(obb.getCorner011(tmpVector)) && this.contains(obb.getCorner100(tmpVector)) && this.contains(obb.getCorner101(tmpVector)) && this.contains(obb.getCorner110(tmpVector)) && this.contains(obb.getCorner111(tmpVector));
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public boolean intersects(BoundingBox b) {
        if (((ExtendedBoundingBox)b).hasInnerBounds() && hasInnerBounds())
            return getInnerBounds().intersects(((ExtendedBoundingBox)b).getInnerBounds());
        if (hasInnerBounds()) return getInnerBounds().intersects(b);

        if (!this.isValid()) {
            return false;
        } else {
            float lx = Math.abs(this.cnt.x - b.cnt.x);
            float sumx = this.dim.x / 2.0F + b.dim.x / 2.0F;
            float ly = Math.abs(this.cnt.y - b.cnt.y);
            float sumy = this.dim.y / 2.0F + b.dim.y / 2.0F;
            float lz = Math.abs(this.cnt.z - b.cnt.z);
            float sumz = this.dim.z / 2.0F + b.dim.z / 2.0F;
            return lx <= sumx && ly <= sumy && lz <= sumz;
        }
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public boolean contains(Vector3 v) {
        if (hasInnerBounds()) return getInnerBounds().contains(v);

        return this.min.x <= v.x && this.max.x >= v.x && this.min.y <= v.y && this.max.y >= v.y && this.min.z <= v.z && this.max.z >= v.z;
    }

}
