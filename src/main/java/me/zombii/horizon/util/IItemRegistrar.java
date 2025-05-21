package me.zombii.horizon.util;

import com.github.puzzle.core.Constants;
import com.github.puzzle.core.loader.meta.EnvType;
import finalforeach.cosmicreach.items.Item;
import io.github.puzzle.cosmic.api.item.IItem;
import me.zombii.horizon.HorizonConstants;
import me.zombii.horizon.items.api.I3DItem;

import static finalforeach.cosmicreach.items.Item.allItems;

public interface IItemRegistrar {

    static <T extends I3DItem & IItem & Item> T registerItem(T item) {
        if (Constants.SIDE == EnvType.CLIENT) {
            HorizonConstants.ITEM_REGISTRAR_INSTANCE.registerItemINST(item);
        } else allItems.put(item.getID(), item);

        return item;
    }

    <T extends I3DItem & IItem & Item> void registerItemINST(T item);

}
