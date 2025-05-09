package me.zombii.horizon;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.github.puzzle.core.loader.meta.EnvType;
import com.github.puzzle.core.loader.provider.mod.entrypoint.impls.ModInitializer;
import com.github.puzzle.core.loader.provider.mod.entrypoint.impls.PostModInitializer;
import com.github.puzzle.core.localization.ILanguageFile;
import com.github.puzzle.core.localization.LanguageManager;
import com.github.puzzle.core.localization.files.LanguageFileVersion1;
import com.github.puzzle.game.PuzzleRegistries;
import com.github.puzzle.game.events.OnPreLoadAssetsEvent;
import com.github.puzzle.game.events.OnRegisterZoneGenerators;
import com.github.puzzle.game.resources.PuzzleGameAssetLoader;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityCreator;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.api.item.IItem;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
import me.zombii.horizon.commands.Commands;
import me.zombii.horizon.entity.BasicPhysicsEntity;
import me.zombii.horizon.entity.BasicShipEntity;
import me.zombii.horizon.entity.Cube;
import me.zombii.horizon.entity.WorldCube;
import me.zombii.horizon.items.GravityGun;
import me.zombii.horizon.items.MoonScepter;
import me.zombii.horizon.items.PortalGun;
import me.zombii.horizon.items.ToolGun;
import me.zombii.horizon.items.api.I3DItem;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.util.IItemRegistrar;
import me.zombii.horizon.util.NativeLibraryLoader;
import me.zombii.horizon.worldgen.NullGenerator;
import me.zombii.horizon.worldgen.VoidGenerator;
import me.zombii.horizon.worldgen.SuperFlat;
import meteordevelopment.orbit.EventHandler;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

public class Horizon implements ModInitializer {



    @Override
    public void onInit() {

//        if (com.github.puzzle.core.Constants.SIDE != EnvType.CLIENT)
        PhysicsThread.init();

        PuzzleRegistries.EVENT_BUS.subscribe(this);
        Commands.register();

        AbstractCosmicItem.register(new MoonScepter());
        AbstractCosmicItem.register(registerItem(new GravityGun()));
        AbstractCosmicItem.register(registerItem(new PortalGun()));
        AbstractCosmicItem.register(registerItem(new ToolGun()));

        CRBinSerializer.defaultClassSerializers.put(BoundingBox.class, (serial, name, bb) -> {
            if (bb == null) {
                serial.writeNullFloatArray(name);
            } else {
                serial.writeFloatArray(name, new float[]{
                        ((BoundingBox)bb).min.x,
                        ((BoundingBox)bb).min.y,
                        ((BoundingBox)bb).min.z,
                        ((BoundingBox)bb).max.x,
                        ((BoundingBox)bb).max.y,
                        ((BoundingBox)bb).max.z
                });

                if (bb instanceof ExtendedBoundingBox) {
                    if (((ExtendedBoundingBox) bb).hasInnerBounds()) {
                        serial.writeBoolean(name + "_isExtended", true);
                        serial.writeObj(OrientedBoundingBox.class, name + "_InnerBounds", ((ExtendedBoundingBox) bb).getInnerBounds());
                    }
                }
            }
        });

        CRBinSerializer.registerDefaultClassSerializer(OrientedBoundingBox.class, (serial, name, boundingBox) -> {
            if (boundingBox == null) {
                serial.writeBoolean(name + "_isNull", true);
                serial.writeNullFloatArray(name + "_bounds");
                serial.writeNullFloatArray(name + "_transform");
            } else {
                serial.writeBoolean(name + "_isNull", false);
                serial.writeObj(BoundingBox.class, name + "_bounds", boundingBox.getBounds());
                serial.writeObj(Matrix4.class, name + "_transform", boundingBox.getTransform());
            }
        });

        CRBinDeserializer.registerDefaultClassDeserializer(OrientedBoundingBox.class, (name, d) -> {
            boolean isNull = d.readBoolean(name + "_isNull", false);

            BoundingBox box = d.readObj(name+"_bounds", BoundingBox.class);
            Matrix4 transform = d.readObj(name+"_transform", Matrix4.class);

            if (isNull) {
                return null;
            } else {
                return new OrientedBoundingBox(box == null ? new BoundingBox() : box, transform == null ? new Matrix4().idt() : transform);
            }
        });

        CRBinSerializer.registerDefaultClassSerializer(Matrix4.class, (serial, s, mat) -> {
            if (mat == null) {
                serial.writeNullFloatArray("floats");
            } else {
                serial.writeFloatArray("floats", mat.val);
            }
        });

        CRBinDeserializer.registerDefaultClassDeserializer(Matrix4.class, (name, d) -> {
            float[] floats = d.readFloatArray(name);
            if (floats == null) {
                return null;
            } else {
                return new Matrix4(floats);
            }
        });

        CRBinDeserializer.defaultClassDeserializers.put(BoundingBox.class, (name, d) -> {
            float[] f = d.readFloatArray(name);
            if (f == null) {
                return null;
            } else if (f.length != 6) {
                throw new RuntimeException("Expected 6 floats for BoundingBox, but got " + f.length + " instead!");
            } else {
                BoundingBox bb = new BoundingBox();
                if (d.readBoolean(name + "_isExtended", false)) {
                    OrientedBoundingBox boundingBox = d.readObj(name + "_InnerBounds", OrientedBoundingBox.class);
                    ((ExtendedBoundingBox) bb).setInnerBounds(boundingBox);
                } else {
                    bb.min.set(f[0], f[1], f[2]);
                    bb.max.set(f[3], f[4], f[5]);
                }
                bb.update();
                return bb;
            }
        });
    }

    static <T extends I3DItem & IItem & Item> T registerItem(T item) {
        IItemRegistrar.registerItem(item);
        return item;
    }

    @EventHandler
    public void onEvent(OnRegisterZoneGenerators event) {
        event.registerGenerator(VoidGenerator::new);
        event.registerGenerator(SuperFlat::new);
        event.registerGenerator(NullGenerator::new);
    }

    @EventHandler
    public void onEvent(OnPreLoadAssetsEvent event) {
        ILanguageFile lang = null;
        try {
            lang = LanguageFileVersion1.loadLanguageFile(Objects.requireNonNull(PuzzleGameAssetLoader.locateAsset(Identifier.of(Constants.MOD_ID, "languages/en-US.json"))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LanguageManager.registerLanguageFile(lang);
    }

}
