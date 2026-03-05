package me.zombii.horizon.mixins;

import finalforeach.cosmicreach.gameevents.ActionId;
import finalforeach.cosmicreach.gameevents.IGameEventAction;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEvents;
import finalforeach.cosmicreach.gameevents.blockevents.actions.IBlockAction;
import me.zombii.horizon.Horizon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import static finalforeach.cosmicreach.gameevents.blockevents.BlockEvents.ALL_ACTIONS;

@Mixin(BlockEvents.class)
public class MixinBlockEvents {

    /**
     * @author
     * @reason
     */
    @SuppressWarnings({"SuspiciousMethodCalls", "unchecked"})
    @Overwrite
    public static void registerBlockEventAction(Class<? extends IBlockAction> actionClass) {
        ActionId actionIdAnnotation = actionClass.getAnnotation(ActionId.class);
        if (actionIdAnnotation == null) {
            String var10002 = actionClass.getSimpleName();
            throw new RuntimeException("Class " + var10002 + " must have an @" + ActionId.class.getSimpleName() + " annotation");
        } else {
            String actionId = actionIdAnnotation.id();
            if (Horizon.actionReplacementMap.containsKey(actionClass)) {
                System.out.println(Horizon.actionReplacementMap.get(actionClass));
                ALL_ACTIONS.put(actionId, (Supplier)() -> {
                    try {
                        return (IGameEventAction)Horizon.actionReplacementMap.get(actionClass).getDeclaredConstructor().newInstance();
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                        throw new RuntimeException("Failed to create action " + actionId, e);
                    }
                });
                return;
            }
            if (actionId == null) {
                throw new RuntimeException("Class " + actionClass.getSimpleName() + " cannot have a null action Id.");
            } else if (ALL_ACTIONS.get(actionId) != null) {
                System.err.println(actionClass);
            } else {
                ALL_ACTIONS.put(actionId, (Supplier)() -> {
                    try {
                        return (IGameEventAction)actionClass.getDeclaredConstructor().newInstance();
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                        throw new RuntimeException("Failed to create action " + actionId, e);
                    }
                });
            }
        }
    }

}
