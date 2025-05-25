package me.zombii.horizon.mixins;

import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityCreator;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import me.zombii.horizon.HorizonConstants;
import me.zombii.horizon.entity.BasicPhysicsEntity;
import me.zombii.horizon.entity.BasicShipEntity;
import me.zombii.horizon.entity.Cube;
import me.zombii.horizon.entity.Player;
import me.zombii.horizon.entity.WorldCube;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(EntityCreator.class)
public class FixupMixin {

    private static Entity readToEntity(Supplier<Entity> es, CRBinDeserializer deserial) {
        Entity e = es.get();
        if (deserial != null) {
            e.read(deserial);
        }

        return e;
    }

    private static void register(String id, Supplier<Entity> es) {
        EntityCreator.entityCreators.put(id, (d) -> readToEntity(es, d));
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void clinit(CallbackInfo ci) {
        EntityCreator.registerEntityCreator(HorizonConstants.MOD_ID + ":entity", () -> new WorldCube());
        EntityCreator.registerEntityCreator(HorizonConstants.MOD_ID + ":physics_entity", BasicPhysicsEntity::new);
        EntityCreator.registerEntityCreator(HorizonConstants.MOD_ID + ":ship", BasicShipEntity::new);

        EntityCreator.registerEntityCreator(HorizonConstants.MOD_ID + ":cube", Cube::new);
        EntityCreator.registerEntityCreator(HorizonConstants.MOD_ID + ":player", Player::new);
    }

}
