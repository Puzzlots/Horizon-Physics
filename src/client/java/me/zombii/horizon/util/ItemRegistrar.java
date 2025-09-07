package me.zombii.horizon.util;

import com.badlogic.gdx.utils.ObjectMap;
import dev.puzzleshq.puzzleloader.loader.util.ReflectionUtil;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.rendering.items.ItemModel;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import io.github.puzzle.cosmic.api.item.IItem;
import io.github.puzzle.cosmic.impl.client.item.CosmicItemModelWrapper;
import me.zombii.horizon.items.api.I3DItem;
import me.zombii.horizon.items.model.Item3DModel;

import java.lang.ref.WeakReference;
import java.util.function.Function;

public class ItemRegistrar implements IItemRegistrar{

    @Override
    public <T extends I3DItem & IItem & Item> void registerItemINST(T item) {
        ItemRenderer.referenceMap.put(item, new WeakReference<>(item));
        ObjectMap<Class<? extends Item>, Function<?, ItemModel>> modelCreators = null;
        try {
            modelCreators = (ObjectMap<Class<? extends Item>, Function<?, ItemModel>>) ReflectionUtil.getField(ItemRenderer.class, "modelCreators").get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        if (!modelCreators.containsKey((Class<? extends Item>) item.getClass())) {
            ItemRenderer.registerItemModelCreator((Class<? extends Item>) item.getClass(), (modItem) -> {
                return CosmicItemModelWrapper.wrap(new Item3DModel(item));
            });
        }
    }
}
