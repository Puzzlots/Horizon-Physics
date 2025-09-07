package me.zombii.horizon.mixins;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import finalforeach.cosmicreach.entities.CommonEntityTags;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityUtils;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EntityUtils.class)
public class MixinEntityUtils {

    /**
     * @author Mr-Zombii
     * @reason Allow non AABB bounding box nudging
     */
    @Overwrite
    public static <T extends BoundingBox & ExtendedBoundingBox> void nudgeEntityFromOthers(Entity sourceEntity) {
        sourceEntity.forEachEntityInNearbyChunks((e) -> {
            if (e != sourceEntity) {
                if (e.hasTag(CommonEntityTags.NO_ENTITY_PUSH)) {
                    return;
                }

                T boundBoxA = (T) sourceEntity.globalBoundingBox;
                T boundBoxB = (T) e.globalBoundingBox;

                boolean intersects = false;
                if (boundBoxA.hasInnerBounds() && boundBoxB.hasInnerBounds()) intersects = boundBoxA.getInnerBounds().intersects(boundBoxB.getInnerBounds());
                if (boundBoxA.hasInnerBounds() && !boundBoxB.hasInnerBounds()) intersects = boundBoxA.getInnerBounds().intersects(boundBoxB);
                if (!boundBoxA.hasInnerBounds() && boundBoxB.hasInnerBounds()) intersects = boundBoxB.getInnerBounds().intersects(boundBoxA);
                if (!boundBoxA.hasInnerBounds() && !boundBoxB.hasInnerBounds()) intersects = boundBoxB.intersects(boundBoxA);

                if (intersects) {
                    Vector3 position = sourceEntity.position;
                    float dst = position.dst(e.position);
                    if (dst == 0.0F) {
                        position.add(MathUtils.random(0.001F), MathUtils.random(0.001F), MathUtils.random(0.001F));
                    }

                    float xDiff = Math.signum(position.x - e.position.x);
                    float yDiff = Math.signum(position.y - e.position.y);
                    float zDiff = Math.signum(position.z - e.position.z);
                    float force = Math.min(2.0F / dst, 100.0F);
                    sourceEntity.onceVelocity.add(xDiff * force, yDiff * force, zDiff * force);
                }
            }

        });
    }

}
