package me.zombii.horizon.world;

import finalforeach.cosmicreach.blockentities.BlockEntity;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.BlockLightLayeredData;
import finalforeach.cosmicreach.util.constants.Direction;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.rendering.mesh.IPhysicChunk;
import me.zombii.horizon.util.Vec3i;

import java.lang.reflect.Field;

public class PhysicsChunk extends Chunk implements IPhysicChunk {

    boolean needsRemeshing = true;

    public PhysicsChunk(int chunkX, int chunkY, int chunkZ) {
        super(chunkX, chunkY, chunkZ);
    }

    public PhysicsChunk(Vec3i vec3i) {
        super(vec3i.x(), vec3i.y(), vec3i.z());
    }

    @Override
    public boolean isCulledByAdjacentChunks(Zone zone) {
        for (Direction d : Direction.ALL_DIRECTIONS) {
            Chunk n = zone.getChunkAtChunkCoords(chunkX + d.getXOffset(), chunkY + d.getYOffset(), chunkZ + d.getZOffset());
            if (n == null || !n.isEntirelyOpaque()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void initChunkData() {
        super.initChunkData();
        blockLightData = new BlockLightLayeredData();
    }

    @Override
    public void setBlockState(BlockState blockState, int x, int y, int z) {
        if (blockData == null) initChunkData();

        Field field;
        try {
            field = Chunk.class.getDeclaredField("blockEntities");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        field.setAccessible(true);

        this.blockData = this.blockData.setBlockValue(blockState, x, y, z);
        Block block = blockState.getBlock();
        if (block.blockEntityId != null) {
            BlockEntity be = this.setBlockEntity(blockState, x, y, z);
            be.onSetBlockState(blockState);
        } else {
            try {
                if (field.get(this) != null) {
                    this.setBlockEntityDirect(blockState, (BlockEntity)null, x, y, z, true);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        this.isSaved = false;
        needsRemeshing = true;
    }

    @Override
    public Zone getZone() {
        return zone;
    }

    //    @Override
//    public BlockState getBlockState(int localX, int localY, int localZ) {
//        if (this.blockData == null) {
//            return Block.getBlockStateInstance("base:air[default]");
//        } else {
//            return localX >= 0 && localY >= 0 && localZ >= 0 && localX < 16 && localY < 16 && localZ < 16 ? this.blockData.getBlockValue(localX, localY, localZ) : null;
//        }
//    }

    @Override
    public boolean needsRemeshing() {
        return needsRemeshing;
    }

    @Override
    public void setNeedsRemeshing(boolean remeshing) {
        needsRemeshing = remeshing;
    }

    Zone zone;

    public void setZone(PhysicsZone physicsZone) {
        this.zone = physicsZone;
    }
}
