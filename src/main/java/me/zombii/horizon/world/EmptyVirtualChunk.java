package me.zombii.horizon.world;

import finalforeach.cosmicreach.blocks.MissingBlockStateResult;
import finalforeach.cosmicreach.util.constants.Direction;
import me.zombii.horizon.util.Vec3i;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class EmptyVirtualChunk extends VirtualChunk {

    // int blocks
    // blocksIndex, chunk x y z, bp x y z
    short[] positions;

    public EmptyVirtualChunk(
            short version,
            Vec3i chunkPos
    ) {
        super(version, chunkPos);
    }

    public void setParentWorld(VirtualWorld world) {
        this.parent = world;
    }

    public void flagTouchingChunksForRemeshing(VirtualWorld zone, int localX, int localY, int localZ, boolean updateImmediately) {
    }

    private void flagForRemeshing(boolean updateImmediately) {
    }

    public static int to1DCoords(int x, int y, int z) {
        return (z * 16 * 16) + (y * 16) + x;
    }

    public static int to1DCoords(Vec3i vec3i) {
        return (vec3i.z() * 16 * 16) + (vec3i.y() * 16) + vec3i.x();
    }

    public static Vec3i to3DCoords(int index) {
        int tmpIndex = index;

        int z = tmpIndex / (16 * 16);
        tmpIndex -= (z * 16 * 16);
        int y = tmpIndex / 16;
        int x = tmpIndex % 16;
        return new Vec3i(x, y, z);
    }

    public void setBlockState(BlockState state, int x, int y, int z) {
    }

    public void propagateLights() {
    }

    public void setBlockLight(int r, int g, int b, int x, int y, int z) {
    }

    static Map<String, BlockState> blockStateCache = new HashMap<>();

    public BlockState getBlockState(int x, int y, int z) {
        return Block.AIR.getDefaultBlockState();
    }

    public void prunePalette() {
    }

    public boolean isEntirely(BlockState state) {
        prunePalette();
        return (palette.size() == 1) && (Objects.equals(palette.get(0), state.getSaveKey()));
    }

    public boolean isEntirely(Predicate<BlockState> statePredicate) {
        for (int idx : positions) {
            BlockState state = getInstance(palette.get(idx));

            if (!statePredicate.test(state)) {
                return false;
            }
        }
        return true;
    }

    public boolean isEntirelyOpaque() {
        return isEntirely((b) -> b != null && b.isOpaque);
    }

    public boolean isEntirelyOneBlockSelfCulling() {
        prunePalette();
        return (palette.size() == 1) && isEntirely((b) -> b != null && b.cullsSelf());
    }

    public boolean isCulledByAdjacentChunks(Vec3i pos, VirtualWorld zone) {
        for (Direction d : Direction.ALL_DIRECTIONS) {
            VirtualChunk n = zone.getChunkAtChunkCoords(new Vec3i(pos.x() + d.getXOffset(), pos.y() + d.getYOffset(), pos.z() + d.getZOffset()));
            if (n == null || !n.isEntirelyOpaque()) {
                return false;
            }
        }

        return true;
    }

    public int getMaxNonEmptyBlockIdxYXZ() {
        return 4096;
    }

    public static BlockState getInstance(String str) {
        if (blockStateCache.containsKey(str)) return blockStateCache.get(str);
        blockStateCache.put(str, BlockState.getInstance(str, MissingBlockStateResult.MISSING_OBJECT));
        return blockStateCache.get(str);
    };

    public void foreach(BiConsumer<Vec3i, BlockState> blockConsumer) {
        for (int i = 0; i < positions.length; i++) {
            int idx = positions[i];
            blockConsumer.accept(
                    to3DCoords(i),
                    getInstance(palette.get(idx))
            );
        }
    }

    public short getBlockLight(int localX, int localY, int localZ) {
        return 0;
    }
}
