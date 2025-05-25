package me.zombii.horizon.mixins;

import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.collision.vanilla.EntityCollision;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Entity.class)
public class EntityMixin {

    /**
     * @author Mr_Zombii
     * @reason Start Collision Re-write
     */
    @Overwrite
    public void updateConstraints(Zone zone, Vector3 targetPosition) {
        EntityCollision.updateConstraints((Entity) (Object) this, zone, targetPosition);
    }

}
