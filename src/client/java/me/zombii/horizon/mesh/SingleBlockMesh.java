package me.zombii.horizon.mesh;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.rendering.RenderOrder;
import finalforeach.cosmicreach.rendering.SharedQuadIndexData;
import finalforeach.cosmicreach.rendering.entities.IEntityAnimation;
import finalforeach.cosmicreach.rendering.entities.IEntityModel;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.rendering.entities.instances.EntityModelInstance;
import finalforeach.cosmicreach.rendering.meshes.GameMesh;
import finalforeach.cosmicreach.rendering.meshes.MeshData;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Sky;
import me.zombii.horizon.rendering.mesh.IHorizonMesh;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class SingleBlockMesh implements IEntityModelInstance, IHorizonMesh {

    GameShader shader;
    AtomicReference<BlockState> state;
    public boolean needsRemeshing = true;

    GameMesh mesh;

    public SingleBlockMesh(AtomicReference<BlockState> state) {
        this.state = state;

        shader = ChunkShader.DEFAULT_BLOCK_SHADER;
    }

    @Override
    public IEntityModel getModel() {
        return new IEntityModel() {
            @Override
            public IEntityModelInstance getNewModelInstance(Supplier<? extends IEntityModelInstance> supplier) {
                EntityModelInstance instance = (EntityModelInstance) supplier.get();
                instance.setEntityModel(this);
                return instance;
            }

            @Override
            public <T extends IEntityModelInstance> T getNewModelInstance(Class<T> aClass) {
                EntityModelInstance instance = new EntityModelInstance();
                instance.setEntityModel(this);
                return (T) instance;
            }

            @Override
            public IEntityModelInstance getNewModelInstance() {
                EntityModelInstance instance = new EntityModelInstance();
                instance.setEntityModel(this);
                return instance;
            }
        };
    }

    @Override
    public void setTint(float v, float v1, float v2, float v3) {

    }

    Vector3 sunDirection = new Vector3();

    Matrix4 rotTmp = new Matrix4();

    @Override
    public void render(Entity _entity, Camera camera, Matrix4 tmp, boolean shouldRender) {
        rotTmp.idt();
        rotTmp.set(tmp.getRotation(new Quaternion()));
        Sky.currentSky.getSunDirection(sunDirection);
        sunDirection.rot(rotTmp);

        if (needsRemeshing) {
            MeshData data = new MeshData(shader, RenderOrder.DEFAULT);
            state.get().addVertices(data, 0, 0, 0);

//            if (BlockModelJson.useIndices) {
//                mesh = data.toIntIndexedMesh(true);
//            } else {
                mesh = data.toSharedIndexMesh(true);
                if (mesh != null) {
                    int numIndices = (mesh.getNumVertices() * 6) / 4;
                    SharedQuadIndexData.allowForNumIndices(numIndices, false);
                }
//            }
            needsRemeshing = false;
        }

        renderBlock(camera, tmp);

    }

    public void renderBlock(Camera camera, Matrix4 tmp) {
        if (mesh != null) {
//            if (!BlockModelJson.useIndices) {
                SharedQuadIndexData.bind();
//            }

            Vector3 batchPos = new Vector3(-.5f, -.5f, -.5f);
            try {
                this.shader.bind(camera);
                this.shader.bindOptionalMatrix4("u_projViewTrans", camera.combined);
//                this.shader.bindOptionalUniform4f("tintColor", Sky.currentSky.currentAmbientColor.cpy());
                this.shader.bindOptionalBool("u_isItem", false);
                this.shader.bindOptionalMatrix4("u_modelMat", tmp);
                this.shader.bindOptionalUniform3f("u_batchPosition", batchPos);
                this.shader.bindOptionalUniform3f("u_sunDirection", sunDirection);
                this.shader.bindOptionalUniform3f("cameraPosition", camera.position);
                this.shader.bindOptionalInt("u_renderDistanceInChunks", 18);
                this.shader.bindOptionalFloat("u_fogDensity", 0.0F);

                mesh.bind(this.shader.shader);
                mesh.render(this.shader.shader, GL20.GL_TRIANGLES);
                mesh.unbind(this.shader.shader);

                this.shader.unbind();
            } catch (Exception ignore) {}

//            if (!BlockModelJson.useIndices) {
                SharedQuadIndexData.unbind();
//            }

        }
    }

    @Override
    public Color getCurrentAmbientColor() {
        return Color.WHITE.cpy();
    }

    @Override
    public void addAnimation(String s) {

    }

    @Override
    public void removeAnimation(String s) {

    }

    @Override
    public void removeAnimation(IEntityAnimation iEntityAnimation) {

    }

    @Override
    public void setEntityModel(IEntityModel iEntityModel) {

    }

    @Override
    public Array<? extends IEntityAnimation> getAnimations() {
        return new Array<>();
    }

    @Override
    public void shadowAnimations(Array<? extends IEntityAnimation> array) {

    }

    @Override
    public void setShouldRefresh(boolean shouldRefresh) {
        needsRemeshing = shouldRefresh;
    }
}
