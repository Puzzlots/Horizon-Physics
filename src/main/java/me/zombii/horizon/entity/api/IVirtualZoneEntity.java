package me.zombii.horizon.entity.api;

import com.badlogic.gdx.utils.ByteArray;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.io.ByteArrayUtils;
import finalforeach.cosmicreach.io.ChunkLoader;
import finalforeach.cosmicreach.io.ChunkSaver;
import finalforeach.cosmicreach.savelib.IChunkByteReader;
import finalforeach.cosmicreach.savelib.IChunkByteWriter;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.world.Chunk;
import me.zombii.horizon.world.PhysicsChunk;
import me.zombii.horizon.world.PhysicsRegion;
import me.zombii.horizon.world.PhysicsZone;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface IVirtualZoneEntity {

    PhysicsZone getWorld();

    static <T extends Entity & IPhysicEntity & IVirtualZoneEntity> void write(T entity, CRBinSerializer serial) {
        PhysicsZone zone = entity.getWorld();
        ByteArray array = new ByteArray();
        zone.chunks.forEach(c -> {
            c.region = new PhysicsRegion(zone, 0, 0, 0);
            ChunkSaver.saveChunk(c, array, new IChunkByteWriter() {
                @Override
                public void writeInt(int i) {
                    ByteArrayUtils.writeInt(array, i);
                }

                @Override
                public void writeByte(int i) {
                    ByteArrayUtils.writeByte(array, i);
                }

                @Override
                public void writeShort(short i) {
                    ByteArrayUtils.writeShort(array, i);
                }

                @Override
                public void writeString(String s) {
                    ByteArrayUtils.writeString(array, s);
                }
            });
        });

        serial.writeInt("chunkCount", zone.getNumberOfChunks());
        serial.writeByteArray("chunks", array.items);
    }

    static <T extends Entity & IPhysicEntity & IVirtualZoneEntity> void read(T entity, CRBinDeserializer deserial) {
        int chunkCount = deserial.readInt("chunkCount", 0);
        byte[] chunks = deserial.readByteArray("chunks");
        ByteBuffer array = ByteBuffer.wrap(chunks);

        for (int i = 0; i < chunkCount; i++) {
            int x = ByteArrayUtils.readInt(array);
            int y = ByteArrayUtils.readInt(array);
            int z = ByteArrayUtils.readInt(array);

            int chunkVer = 4;

            Chunk chunk = new PhysicsChunk(x, y, z);

            try {
                ChunkLoader.readChunk(chunkVer, new IChunkByteReader() {
                    @Override
                    public int readInt() throws IOException {
                        return ByteArrayUtils.readInt(array);
                    }

                    @Override
                    public byte readByte() throws IOException {
                        return ByteArrayUtils.readByte(array);
                    }

                    @Override
                    public String readString() throws IOException {
                        return ByteArrayUtils.readString(array);
                    }

                    @Override
                    public short readShort() throws IOException {
                        return ByteArrayUtils.readShort(array);
                    }
                }, entity.getWorld(), chunk);
                entity.getWorld().addChunk(chunk);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
