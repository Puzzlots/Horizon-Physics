package me.zombii.horizon.items.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonReader;
import com.github.puzzle.game.resources.PuzzleGameAssetLoader;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.rendering.RenderOrder;
import finalforeach.cosmicreach.rendering.meshes.MeshData;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import io.github.puzzle.cosmic.api.client.model.ICosmicItemModel;
import io.github.puzzle.cosmic.api.item.IItem;
import io.github.puzzle.cosmic.api.item.IItemStack;
import io.github.puzzle.cosmic.impl.client.item.ItemShader;
import me.zombii.horizon.items.api.I3DItem;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

public class Item3DModel implements ICosmicItemModel {

    static G3dModelLoader loader;
    private final GameShader program;
    AtomicReference<ModelInstance> modelInstance = new AtomicReference<>();

    ModelBatch batch;
    Environment environment;
    final Vector3 scalar;

    public Item3DModel(I3DItem i3DItem) {
        this.program = (new MeshData(ItemShader.DEFAULT_ITEM_SHADER, RenderOrder.FULLY_TRANSPARENT)).getShader();

        scalar = i3DItem.getScalar();

        if (loader == null) {
            Threads.runOnMainThread(() -> {
                loader = new G3dModelLoader(new JsonReader());

                i3DItem.loadModel(loader, modelInstance);
            });
        } else {
            Threads.runOnMainThread(() -> {
                synchronized (loader) {
                    i3DItem.loadModel(loader, modelInstance);
                }
            });
        }

        Threads.runOnMainThread(() -> {
            environment = new Environment();
            Texture screenBG = PuzzleGameAssetLoader.LOADER.loadSync("horizon:models/items/g3dj/screen_bg.001.png", Texture.class);
            Texture toolgun = PuzzleGameAssetLoader.LOADER.loadSync("horizon:models/items/g3dj/toolgun.png", Texture.class);
            Texture toolgun2 = PuzzleGameAssetLoader.LOADER.loadSync("horizon:models/items/g3dj/toolgun2.png", Texture.class);
            Texture toolgun3 = PuzzleGameAssetLoader.LOADER.loadSync("horizon:models/items/g3dj/toolgun3.png", Texture.class);

            environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
            environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

            batch = new ModelBatch();
        });
    }

    public void renderGeneric(Vector3 pos, IItemStack stack, Camera cam, Matrix4 tmpMat, boolean isInSlot) {
        if (modelInstance.get() != null) {
            tmpMat = tmpMat.cpy();
            tmpMat.mul(new Matrix4().scl(scalar).inv());
            tmpMat.scl(scalar);
            modelInstance.get().transform.set(tmpMat);
            batch.begin(cam);
            batch.render(modelInstance.get(), environment);
            batch.end();
        }
    }

    @Override
    public void renderInSlot(Vector3 vector3, IItemStack itemStack, Camera camera, Matrix4 matrix4, boolean useAmbientLighting) {
        Matrix4 matrix5 = new Matrix4();
        matrix5.setToTranslation(new Vector3(0, 0, 0));
//        matrix5.translate(0.5F, 0.2F, 0.5F);
//        matrix5.scale(0.4f, 0.4f, 0.4f);
        Gdx.gl.glDisable(GL30.GL_CULL_FACE);
        Gdx.gl.glDepthFunc(GL30.GL_LESS);
        renderGeneric(new Vector3(0, 0, 0), itemStack, itemCam2, noRotMtrx, true);
        Gdx.gl.glDepthFunc(GL30.GL_ALWAYS);
        Gdx.gl.glEnable(GL30.GL_CULL_FACE);
    }


    @Override
    public void renderAsHeldItem(Vector3 vector3, IItemStack itemStack, Camera camera, float popUpTimer, float maxPopUpTimer, float swingTimer, float maxSwingTimer) {
        Matrix4 tmpHeldMat4 = new Matrix4();
        heldItemCamera.fieldOfView = 50;
        heldItemCamera.viewportHeight = camera.viewportHeight;
        heldItemCamera.viewportWidth = camera.viewportWidth;
        heldItemCamera.near = camera.near;
        heldItemCamera.far = camera.far;
        heldItemCamera.update();
        tmpHeldMat4.idt();
//        tmpHeldMat4.setToTranslation(0, 0, 0);
//        tmpHeldMat4.scale(0.4f, 0.4f, 0.4f);
        tmpHeldMat4.translate(new Vector3(0, 0, 0));
        float swing;
        if (popUpTimer > 0) {
            swing = (float)Math.pow(popUpTimer / maxPopUpTimer, 2.0) / 2;
            tmpHeldMat4.translate(0, -1 * swing, 0);
        }

        tmpHeldMat4.translate(1.65F, -1.5F, -6.3F);
        tmpHeldMat4.rotate(Vector3.Y, -94);
        tmpHeldMat4.translate(-0.25F, -0.25F, -0.25F);
        tmpHeldMat4.rotate(Vector3.Z, 18);
        if (swingTimer > 0) {
            swing = swingTimer / maxSwingTimer;
            swing = 1 - (float)Math.pow(swing - 0.5F, 2.0) / 0.25F;
            tmpHeldMat4.rotate(Vector3.Z, 90 * swing);
            float st = -swing;
            tmpHeldMat4.translate(st * 2, st, 0);
        }

//        tmpHeldMat4.translate(0.6F, 0, 0);
//        tmpHeldMat4.translate(0, -0.2F, 0);
        tmpHeldMat4.rotate(new Vector3(0, 0, 1), -20);
//        tmpHeldMat4.rotate(new Vector3(1, 0, 0), 15);
        tmpHeldMat4.translate(0, -4, 0);

        GL20.glClear(GL20.GL_STENCIL_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        renderGeneric(vector3, itemStack, heldItemCamera, tmpHeldMat4, false);
    }

    @Override
    public void renderAsEntity(Vector3 vector3, IItemStack itemStack, Camera camera, Matrix4 matrix4) {
        Gdx.gl.glDisable(GL30.GL_CULL_FACE);
        matrix4.translate(0.5F, 0.2F, 0.5F);
        matrix4.scale(0.4f, 0.4f, 0.4f);
        renderGeneric(vector3, itemStack, camera, matrix4, false);
        Gdx.gl.glEnable(GL30.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL30.GL_CULL_FACE);
    }

    @Override
    public void dispose(WeakReference<Item> weakReference) {
        if (modelInstance.get() != null) {
            modelInstance.get().model.dispose();
        }
    }

    @Override
    public Camera getItemSlotCamera() {
        return itemCam2;
    }

    static final PerspectiveCamera heldItemCamera;
    static final Matrix4 noRotMtrx = new Matrix4();
    static final PerspectiveCamera itemCam2 = new PerspectiveCamera(67, 100, 100);

    static {
        noRotMtrx.setTranslation(0, -2, 0);
        itemCam2.position.set(1, 7, 3);
        itemCam2.lookAt(0, 0, 0);
        itemCam2.update();
//        tintColor = new Color();
//        tmpBlockPos = new BlockPosition((Chunk)null, 0, 0, 0);
        heldItemCamera = new PerspectiveCamera();
    }
}
