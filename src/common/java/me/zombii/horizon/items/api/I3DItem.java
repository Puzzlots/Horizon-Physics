package me.zombii.horizon.items.api;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.math.Vector3;
import dev.puzzleshq.puzzleloader.cosmic.game.util.IndependentAssetLoader;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.util.Identifier;

import java.util.concurrent.atomic.AtomicReference;

public interface I3DItem {

    Identifier getModelLocation();
    default void loadModel(G3dModelLoader modelLoader, AtomicReference<ModelInstance> model) {
        final FileHandle modelHandle = IndependentAssetLoader.loadAsset2(getModelLocation());

        Threads.runOnMainThread(() -> {
            Model m = modelLoader.loadModel(modelHandle);
            model.set(new ModelInstance(m));
        });
    }

    default Vector3 getScalar() {
        return new Vector3(1, 1, 1);
    }

}
