package me.zombii.horizon.mixins;

import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Chunk;
import me.zombii.horizon.threading.PhysicsThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
public class CollisionMeshInvalidator {

    @Inject(method= "setBlockState*", at=@At("TAIL"))
    private void setBlockState(BlockState blockState, int x, int y, int z, CallbackInfo ci) {
        PhysicsThread.invalidateChunk((Chunk)(Object)this);
    }

}
