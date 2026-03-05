package me.zombii.horizon.mixins;

import finalforeach.cosmicreach.networking.netty.NettyServer;
import me.zombii.horizon.threading.PhysicsThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NettyServer.class)
public class ServerLauncherPhysicsLoader {

    @Inject(method = "run", at = @At("HEAD"))
    private static void run(CallbackInfo ci) {
        PhysicsThread.start();
    }

}
