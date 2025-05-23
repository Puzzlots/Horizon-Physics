package me.zombii.horizon.mixins.client;

import finalforeach.cosmicreach.gamestates.MainMenu;
import me.zombii.horizon.HorizonConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MainMenu.class)
public class MainMenuMixin {

    @Inject(method = "create", at = @At("HEAD"))
    private void create0(CallbackInfo ci) {
        HorizonConstants.LOGGER.info("THE START OF THE MAIN MENU's create()");
    }

    @Inject(method = "create", at = @At("TAIL"))
    private void create1(CallbackInfo ci) {
        HorizonConstants.LOGGER.info("THE END OF THE MAIN MENU's create()");
    }

}
