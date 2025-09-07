package me.zombii.horizon;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import dev.puzzleshq.puzzleloader.cosmic.core.modInitialises.ModInit;
import dev.puzzleshq.puzzleloader.cosmic.game.GameRegistries;
import dev.puzzleshq.puzzleloader.cosmic.game.events.zone.EventRegisterZoneGenerator;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import io.github.puzzle.cosmic.api.item.IItem;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
import me.zombii.horizon.items.*;
import me.zombii.horizon.items.api.I3DItem;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.util.IItemRegistrar;
import me.zombii.horizon.worldgen.NullGenerator;
import me.zombii.horizon.worldgen.VoidGenerator;
import me.zombii.horizon.worldgen.SuperFlat;
import net.neoforged.bus.api.SubscribeEvent;

public class Horizon implements ModInit {



    @Override
    public void onInit() {

//        if (com.github.puzzle.core.Constants.SIDE != EnvType.CLIENT)
        PhysicsThread.init();

        GameRegistries.COSMIC_EVENT_BUS.register(this);

        AbstractCosmicItem.register(new MoonScepter());
        Item.registerItem(registerItemModel(new GravityGun()));
        Item.registerItem(registerItemModel(new PortalGun()));
        Item.registerItem(registerItemModel(new ToolGun()));
        AbstractCosmicItem.register(new LidarGun());

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

    static <T extends I3DItem & IItem & Item> T registerItemModel(T item) {
        IItemRegistrar.registerItem(item);
        return item;
    }

    @SubscribeEvent
    public void onEvent(EventRegisterZoneGenerator event) {
        event.registerGenerator(VoidGenerator::new);
        event.registerGenerator(SuperFlat::new);
        event.registerGenerator(NullGenerator::new);
    }

}
