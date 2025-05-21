package me.zombii.horizon.items;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.github.puzzle.game.resources.PuzzleGameAssetLoader;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.api.util.IIdentifier;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import me.zombii.horizon.HorizonConstants;
import me.zombii.horizon.items.api.I3DItem;

import java.util.concurrent.atomic.AtomicReference;

public class PortalGun extends AbstractCosmicItem implements I3DItem {

    static final Identifier modelLocation = Identifier.of(HorizonConstants.MOD_ID, "models/items/g3dj/Portal Gun.g3dj");

    public PortalGun() {
        super(Identifier.of(HorizonConstants.MOD_ID, "portal_gun"));
    }

    @Override
    public IIdentifier pGetIdentifier() {
        return (IIdentifier) Identifier.of(HorizonConstants.MOD_ID, "portal_gun");
    }

    @Override
    public String toString() {
        return getID();
    }

    @Override
    public String getName() {
        return "Portal Gun";
    }

    @Override
    public Identifier getModelLocation() {
        return modelLocation;
    }

    @Override
    public void loadModel(G3dModelLoader modelLoader, AtomicReference<ModelInstance> model) {
        final FileHandle modelHandle = PuzzleGameAssetLoader.locateAsset(getModelLocation());

        Threads.runOnMainThread(() -> {
            Model model1 = modelLoader.loadModel(modelHandle);

            model.set(new ModelInstance(model1));
        });
    }
}
