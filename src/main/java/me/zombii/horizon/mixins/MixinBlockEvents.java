package me.zombii.horizon.mixins;

import finalforeach.cosmicreach.blockevents.BlockEvents;
import finalforeach.cosmicreach.blockevents.actions.ActionId;
import finalforeach.cosmicreach.blockevents.actions.IBlockAction;
import me.zombii.horizon.Horizon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import static finalforeach.cosmicreach.blockevents.BlockEvents.ALL_ACTIONS;

@Mixin(BlockEvents.class)
public class MixinBlockEvents {

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static void registerBlockEventAction(Class<? extends IBlockAction> actionClass) {
        ActionId actionIdAnnotation = (ActionId)actionClass.getAnnotation(ActionId.class);
        if (actionIdAnnotation == null) {
            String var10002 = actionClass.getSimpleName();
            throw new RuntimeException("Class " + var10002 + " must have an @" + ActionId.class.getSimpleName() + " annotation");
        } else {
            String actionId = actionIdAnnotation.id();
            if (Horizon.actionReplacementMap.containsKey(actionClass)) {
                System.out.println(Horizon.actionReplacementMap.get(actionClass));
                ALL_ACTIONS.put(actionId, Horizon.actionReplacementMap.get(actionClass));
                return;
            }
            if (actionId == null) {
                throw new RuntimeException("Class " + actionClass.getSimpleName() + " cannot have a null action Id.");
            } else if (ALL_ACTIONS.get(actionId) != null) {
                System.err.println(actionClass);
            } else {
                ALL_ACTIONS.put(actionId, actionClass);
            }
        }
    }

}
