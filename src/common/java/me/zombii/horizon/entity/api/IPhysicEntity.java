package me.zombii.horizon.entity.api;

import com.badlogic.gdx.math.Vector3;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.math.Quaternion;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.networking.packets.entities.EntityPositionPacket;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.world.Zone;

import java.util.UUID;
import java.util.function.Supplier;

public interface IPhysicEntity {

    PhysicsBody getBody();

    Quaternion getEularRotation();
    Quaternion getLastEularRotation();
    void setLastEularRotation(Quaternion rot);

    UUID getUUID();
    float getMass();
    CollisionShape getCollisionShape();
    default boolean isPickedUp() {
        return false;
    }
    default boolean canBePickedUp() {
        return false;
    }

    void setEularRotation(Quaternion rot);
    void setUUID(UUID uuid);
    void setMass(float mass);
    void setCollisionShape(CollisionShape shape);
    default void setPickedUp(boolean pickedUp) {}

    default void onInteract() {}

    static <T> T readOrDefault(Supplier<T> read, T _default) {
        try {
            return read.get();
        } catch (Exception ignore) {
            return _default;
        }
    }

    static <T extends Entity & IPhysicEntity> void read(T entity, CRBinDeserializer deserial) {
        entity.setUUID(readOrDefault(() -> {
            String uid = deserial.readString("uuid");
            return UUID.fromString(uid);
        }, UUID.randomUUID()));

        entity.setEularRotation(readOrDefault(() -> {
            float rot_x = deserial.readFloat("rot_x", 0);
            float rot_y = deserial.readFloat("rot_y", 0);
            float rot_z = deserial.readFloat("rot_z", 0);
            float rot_w = deserial.readFloat("rot_w", 0);

            return new Quaternion(rot_x, rot_y, rot_z, rot_w);
        }, new Quaternion(0, 0, 0, 0)));

        entity.setMass(readOrDefault(() -> {
            return deserial.readFloat("mass", 0.25f);
        }, 0.25f));
    }

    static <T extends Entity & IPhysicEntity> void write(T entity, CRBinSerializer serial) {
        serial.writeString("uuid", entity.getUUID() == null ? UUID.randomUUID().toString() : entity.getUUID().toString());

        serial.writeFloat("rot_x", entity.getEularRotation().getX());
        serial.writeFloat("rot_y", entity.getEularRotation().getY());
        serial.writeFloat("rot_z", entity.getEularRotation().getZ());
        serial.writeFloat("rot_w", entity.getEularRotation().getW());

        serial.writeFloat("mass", entity.getMass());
    }

    Vector3 getPosition();
    Vector3 getLastPosition();
    void setLastPosition(Vector3 pos);

    Zone getZone();

    EntityPositionPacket positionPacket = new EntityPositionPacket();

    default void updatePosition() {
        if (ServerSingletons.SERVER != null) {
            boolean shouldSendPacket = false;
            shouldSendPacket |= !getPosition().epsilonEquals(getLastPosition());
            shouldSendPacket |= !getEularRotation().equals(getLastEularRotation());
            if (shouldSendPacket) {
                positionPacket.setEntity((Entity) this);
                ServerSingletons.SERVER.broadcast(getZone(), positionPacket);
            }
        }

        setLastPosition(getPosition());
        setLastEularRotation(getEularRotation());
    }

}
