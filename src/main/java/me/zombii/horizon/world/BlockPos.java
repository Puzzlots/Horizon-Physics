package me.zombii.horizon.world;

import com.badlogic.gdx.utils.Pool;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.util.constants.Direction;

import static me.zombii.horizon.world.VirtualChunk.to1DCoords;

public class BlockPos{

    public VirtualChunk chunk;
    public int x;
    public int y;
    public int z;

    public BlockPos(
            VirtualChunk chunk,
            int x,
            int y,
            int z
    ) {
        this.chunk = chunk;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public short getBlockLight() {
//        System.out.println(x + " " + y + " " + z + " " + to1DCoords(x, y, z));
        return chunk.blockLights[to1DCoords(x, y, z)];
    }

    public void setBlockLight(short light) {
        chunk.blockLights[to1DCoords(x, y, z)] = light;
    }

    public void flagTouchingChunksForRemeshing(VirtualWorld zone, boolean updateImmediately) {
        this.chunk.flagTouchingChunksForRemeshing(zone, this.x, this.y, this.z, updateImmediately);
    }

    public void setBlockLight(int r, int g, int b) {
        chunk.setBlockLight(r, g, b, x, y, z);
    }

    public BlockState getBlockState() {
        return chunk.getBlockState(x, y, z);
    }

    public BlockPos set(VirtualChunk chunk, int localX, int localY, int localZ) {
        this.chunk = chunk;
        this.x = localX;
        this.y = localY;
        this.z = localZ;
        return this;
    }

    public BlockPos set(BlockPos pos) {
        this.set(pos.chunk, pos.x, pos.y, pos.z);
        return this;
    }

    public BlockPos getOffsetBlockPos(Pool<BlockPos> pool, VirtualWorld zone, int offsetX, int offsetY, int offsetZ) {
        return this.getOffsetBlockPos(pool.obtain(), zone, offsetX, offsetY, offsetZ);
    }

    public BlockPos getOffsetBlockPos(VirtualWorld zone, int offsetX, int offsetY, int offsetZ) {
        return this.getOffsetBlockPos(new BlockPos(null, 0, 0, 0), zone, offsetX, offsetY, offsetZ);
    }

    public BlockPos getOffsetBlockPos(BlockPos destBlockPos, VirtualWorld zone, int offsetX, int offsetY, int offsetZ) {
        if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
            destBlockPos.set(this);
        } else {
            int nLocalX = this.x + offsetX;
            int nLocalY = this.y + offsetY;
            int nLocalZ = this.z + offsetZ;
            VirtualChunk c = this.chunk;
            if (nLocalX < 0 || nLocalX >= 16 || nLocalY < 0 || nLocalY >= 16 || nLocalZ < 0 || nLocalZ >= 16) {
                int nGlobalX = this.chunk.blockPos.x() + nLocalX;
                int nGlobalY = this.chunk.blockPos.y() + nLocalY;
                int nGlobalZ = this.chunk.blockPos.z() + nLocalZ;
                c = zone.getChunkAtBlock(nGlobalX, nGlobalY, nGlobalZ);
                if (c == null || c == VirtualWorld.emptyStructure) {
                    return null;
                }

                nLocalX = nGlobalX - c.blockPos.x();
                nLocalY = nGlobalY - c.blockPos.y();
                nLocalZ = nGlobalZ - c.blockPos.z();
            }

            destBlockPos.set(c, nLocalX, nLocalY, nLocalZ);
        }
        return destBlockPos;
    }

    public BlockPos getOffsetBlockPos(VirtualWorld zone, Direction d) {
        return this.getOffsetBlockPos(zone, d.getXOffset(), d.getYOffset(), d.getZOffset());
    }

    public BlockPos getOffsetBlockPos(BlockPos destBlockPos, VirtualWorld zone, Direction d) {
        return this.getOffsetBlockPos(destBlockPos, zone, d.getXOffset(), d.getYOffset(), d.getZOffset());
    }

    public void free() {
    }

}
