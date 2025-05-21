package me.zombii.horizon.mixins;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.jme3.math.Quaternion;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.networking.packets.entities.EntityPositionPacket;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.world.Zone;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import me.zombii.horizon.entity.api.IPhysicEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EntityPositionPacket.class)
public class EntityPositionPacketMixin extends GamePacket {

    EntityUniqueId entityId = new EntityUniqueId();
    public Vector3 position = new Vector3();
    public Quaternion rotation = Quaternion.DIRECTION_Z;
    public Vector3 viewDir = new Vector3();
    public Vector3 viewDirOff = new Vector3();
    public BoundingBox box = new BoundingBox();

    /**
     * @author Mr_Zombii
     * @reason Add Rotation
     */
    @Overwrite
    public void setEntity(Entity entity) {
        this.entityId.set(entity.uniqueId);
        this.position.set(entity.getPosition());
        if (entity instanceof IPhysicEntity physicEntity)
            rotation.set(physicEntity.getEularRotation());
        this.viewDir.set(entity.viewDirection);
        this.viewDirOff.set(entity.viewPositionOffset);
    }

    @Override
    public void receive(ByteBuf in) {
        this.readEntityUniqueId(in, this.entityId);
        this.readVector3(in, this.position);
        this.readVector3(in, this.viewDir);
        this.readVector3(in, this.viewDirOff);

        rotation.set(in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
    }

    @Override
    public void write() {
        this.writeEntityUniqueId(this.entityId);
        this.writeVector3(this.position);
        this.writeVector3(this.viewDir);
        this.writeVector3(this.viewDirOff);

        writeFloat(rotation.getX());
        writeFloat(rotation.getY());
        writeFloat(rotation.getZ());
        writeFloat(rotation.getW());
    }

    @Override
    public void handle(NetworkIdentity identity, ChannelHandlerContext ctx) {
        if (!identity.isServer()) {
            Zone zone = identity.getZone();
            if (zone != null) {
                Entity e = zone.getEntity(this.entityId);
                if (e != null && e != GameSingletons.client().getLocalPlayer().getEntity()) {
                    e.position.set(this.position);
                    if (e instanceof IPhysicEntity)
                        ((IPhysicEntity) e).setEularRotation(rotation);
                    e.viewDirection.set(this.viewDir);
                    e.viewPositionOffset.set(this.viewDirOff);
                    e.getBoundingBox(e.globalBoundingBox);
                }

            }
        }
    }

}
