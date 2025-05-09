package me.zombii.horizon.util;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.ImmutablePair;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.rendering.RenderOrder;
import finalforeach.cosmicreach.rendering.meshes.MeshData;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.util.ArrayUtils;
import me.zombii.horizon.world.VirtualChunk;
import me.zombii.horizon.world.VirtualWorld;

import java.util.Arrays;

public class CosmicMeshingUtil {

    static final Array<MeshData> emptyMeshDatas = new Array(false, 0, MeshData.class);

    public static Array<MeshData> getMeshData(VirtualWorld world, Vec3i pos, VirtualChunk chunk) {
        BlockState airBlockState = Block.AIR.getDefaultBlockState();
        Pair<Vec3i, VirtualChunk> structurePair = new ImmutablePair<>(pos, chunk);
        if (chunk.isEntirely(airBlockState)) {
            return emptyMeshDatas;
        } else {
            boolean isEntirelyOpaque = chunk.isEntirelyOpaque();
            if (isEntirelyOpaque && chunk.isCulledByAdjacentChunks(pos, world)) {
                return emptyMeshDatas;
            } else {
                Array<MeshData> meshDatas = new Array(3);
                boolean isEntirelyOneBlockSelfCulling = chunk.isEntirelyOneBlockSelfCulling();
                short[] blockLightLevels = new short[8];
                int[] skyLightLevels = new int[8];

                Arrays.fill(skyLightLevels, Short.MAX_VALUE);
//                Arrays.fill(blockLightLevels, (short) 4095);

                Pair<Vec3i, VirtualChunk> chunkNegX = world.getChunkAtChunkCoords(new Vec3i(pos.x() - 1, pos.y(), pos.z())) != null ? new ImmutablePair<>(new Vec3i(pos.x() - 1, pos.y(), pos.z()), world.getChunkAtChunkCoords(new Vec3i(pos.x() - 1, pos.y(), pos.z()))) : null;
                Pair<Vec3i, VirtualChunk> chunkPosX = world.getChunkAtChunkCoords(new Vec3i(pos.x() + 1, pos.y(), pos.z())) != null ? new ImmutablePair<>(new Vec3i(pos.x() + 1, pos.y(), pos.z()), world.getChunkAtChunkCoords(new Vec3i(pos.x() + 1, pos.y(), pos.z()))) : null;
                Pair<Vec3i, VirtualChunk> chunkNegY = world.getChunkAtChunkCoords(new Vec3i(pos.x(), pos.y() - 1, pos.z())) != null ? new ImmutablePair<>(new Vec3i(pos.x(), pos.y() - 1, pos.z()), world.getChunkAtChunkCoords(new Vec3i(pos.x(), pos.y() - 1, pos.z()))) : null;
                Pair<Vec3i, VirtualChunk> chunkPosY = world.getChunkAtChunkCoords(new Vec3i(pos.x(), pos.y() + 1, pos.z())) != null ? new ImmutablePair<>(new Vec3i(pos.x(), pos.y() + 1, pos.z()), world.getChunkAtChunkCoords(new Vec3i(pos.x(), pos.y() + 1, pos.z()))) : null;
                Pair<Vec3i, VirtualChunk> chunkNegZ = world.getChunkAtChunkCoords(new Vec3i(pos.x(), pos.y(), pos.z() - 1)) != null ? new ImmutablePair<>(new Vec3i(pos.x(), pos.y(), pos.z() - 1), world.getChunkAtChunkCoords(new Vec3i(pos.x(), pos.y(), pos.z() - 1))) : null;
                Pair<Vec3i, VirtualChunk> chunkPosZ = world.getChunkAtChunkCoords(new Vec3i(pos.x(), pos.y(), pos.z() + 1)) != null ? new ImmutablePair<>(new Vec3i(pos.x(), pos.y(), pos.z() + 1), world.getChunkAtChunkCoords(new Vec3i(pos.x(), pos.y(), pos.z() + 1))) : null;

                for(int idx = 0; idx < 4096; idx++) {
                    int localY = idx / 256;
                    int localX = (idx - localY * 256) / 16;
                    int localZ = (idx - localY * 256) % 16;
                    if ((isEntirelyOpaque || isEntirelyOneBlockSelfCulling) && localX != 0 && localX != 15 && localY != 0 && localY != 15 && localZ != 0 && localZ != 15) {
                        idx += 13;
                    } else {
                        BlockState b = chunk.getBlockState(localX, localY, localZ);
                        if (b != airBlockState) {
                            int globalX = (pos.x() * 16) + localX;
                            int globalY = (pos.y() * 16) + localY;
                            int globalZ = (pos.z() * 16) + localZ;
                            BlockState bnx = world.getBlockState(structurePair, chunkNegX, globalX - 1, globalY, globalZ);
                            BlockState bpx = world.getBlockState(structurePair, chunkPosX, globalX + 1, globalY, globalZ);
                            BlockState bny = world.getBlockState(structurePair, chunkNegY, globalX, globalY - 1, globalZ);
                            BlockState bpy = world.getBlockState(structurePair, chunkPosY, globalX, globalY + 1, globalZ);
                            BlockState bnz = world.getBlockState(structurePair, chunkNegZ, globalX, globalY, globalZ - 1);
                            BlockState bpz = world.getBlockState(structurePair, chunkPosZ, globalX, globalY, globalZ + 1);
                            int completeCullMask = 0;
                            completeCullMask |= bnx != null && !bnx.isPosXFaceOccluding ? 0 : 1;
                            completeCullMask |= bpx != null && !bpx.isNegXFaceOccluding ? 0 : 2;
                            completeCullMask |= bny != null && !bny.isPosYFaceOccluding ? 0 : 4;
                            completeCullMask |= bpy != null && !bpy.isNegYFaceOccluding ? 0 : 8;
                            completeCullMask |= bnz != null && !bnz.isPosZFaceOccluding ? 0 : 16;
                            completeCullMask |= bpz != null && !bpz.isNegZFaceOccluding ? 0 : 32;
                            if (completeCullMask != 63) {
                                int opaqueBitmask = 0;
                                Block block = b.getBlock();
                                boolean cullsSelf = b.cullsSelf();
                                opaqueBitmask |= bnx != null && !bnx.isPosXFaceOccluding && (!cullsSelf || block != bnx.getBlock() || !bnx.isSelfPosXFaceOccluding) ? 0 : 1;
                                opaqueBitmask |= bpx != null && !bpx.isNegXFaceOccluding && (!cullsSelf || block != bpx.getBlock() || !bpx.isSelfNegXFaceOccluding) ? 0 : 2;
                                opaqueBitmask |= bny != null && !bny.isPosYFaceOccluding && (!cullsSelf || block != bny.getBlock() || !bny.isSelfPosYFaceOccluding) ? 0 : 4;
                                opaqueBitmask |= bpy != null && !bpy.isNegYFaceOccluding && (!cullsSelf || block != bpy.getBlock() || !bpy.isSelfNegYFaceOccluding) ? 0 : 8;
                                opaqueBitmask |= bnz != null && !bnz.isPosZFaceOccluding && (!cullsSelf || block != bnz.getBlock() || !bnz.isSelfPosZFaceOccluding) ? 0 : 16;
                                opaqueBitmask |= bpz != null && !bpz.isNegZFaceOccluding && (!cullsSelf || block != bpz.getBlock() || !bpz.isSelfNegZFaceOccluding) ? 0 : 32;
                                BlockState bnxnynz = world.getBlockState(chunk, pos, globalX - 1, globalY - 1, globalZ - 1);
                                BlockState bnxny0z = world.getBlockState(chunk, pos, globalX - 1, globalY - 1, globalZ);
                                BlockState bnxnypz = world.getBlockState(chunk, pos, globalX - 1, globalY - 1, globalZ + 1);
                                BlockState bnx0ynz = world.getBlockState(chunk, pos, globalX - 1, globalY, globalZ - 1);
                                BlockState bnx0ypz = world.getBlockState(chunk, pos, globalX - 1, globalY, globalZ + 1);
                                BlockState bnxpynz = world.getBlockState(chunk, pos, globalX - 1, globalY + 1, globalZ - 1);
                                BlockState bnxpy0z = world.getBlockState(chunk, pos, globalX - 1, globalY + 1, globalZ);
                                BlockState bnxpypz = world.getBlockState(chunk, pos, globalX - 1, globalY + 1, globalZ + 1);
                                BlockState b0xnynz = world.getBlockState(chunk, pos, globalX, globalY - 1, globalZ - 1);
                                BlockState b0xnypz = world.getBlockState(chunk, pos, globalX, globalY - 1, globalZ + 1);
                                BlockState b0xpynz = world.getBlockState(chunk, pos, globalX, globalY + 1, globalZ - 1);
                                BlockState b0xpypz = world.getBlockState(chunk, pos, globalX, globalY + 1, globalZ + 1);
                                BlockState bpxnynz = world.getBlockState(chunk, pos, globalX + 1, globalY - 1, globalZ - 1);
                                BlockState bpxny0z = world.getBlockState(chunk, pos, globalX + 1, globalY - 1, globalZ);
                                BlockState bpxnypz = world.getBlockState(chunk, pos, globalX + 1, globalY - 1, globalZ + 1);
                                BlockState bpx0ynz = world.getBlockState(chunk, pos, globalX + 1, globalY, globalZ - 1);
                                BlockState bpx0ypz = world.getBlockState(chunk, pos, globalX + 1, globalY, globalZ + 1);
                                BlockState bpxpynz = world.getBlockState(chunk, pos, globalX + 1, globalY + 1, globalZ - 1);
                                BlockState bpxpy0z = world.getBlockState(chunk, pos, globalX + 1, globalY + 1, globalZ);
                                BlockState bpxpypz = world.getBlockState(chunk, pos, globalX + 1, globalY + 1, globalZ + 1);
                                opaqueBitmask |= bnxnynz != null && bnxnynz.isOpaque ? 64 : 0;
                                opaqueBitmask |= bnxny0z != null && bnxny0z.isOpaque ? 128 : 0;
                                opaqueBitmask |= bnxnypz != null && bnxnypz.isOpaque ? 256 : 0;
                                opaqueBitmask |= bnx0ynz != null && bnx0ynz.isOpaque ? 512 : 0;
                                opaqueBitmask |= bnx0ypz != null && bnx0ypz.isOpaque ? 1024 : 0;
                                opaqueBitmask |= bnxpynz != null && bnxpynz.isOpaque ? 2048 : 0;
                                opaqueBitmask |= bnxpy0z != null && bnxpy0z.isOpaque ? 4096 : 0;
                                opaqueBitmask |= bnxpypz != null && bnxpypz.isOpaque ? 8192 : 0;
                                opaqueBitmask |= b0xnynz != null && b0xnynz.isOpaque ? 16384 : 0;
                                opaqueBitmask |= b0xnypz != null && b0xnypz.isOpaque ? '耀' : 0;
                                opaqueBitmask |= b0xpynz != null && b0xpynz.isOpaque ? 65536 : 0;
                                opaqueBitmask |= b0xpypz != null && b0xpypz.isOpaque ? 131072 : 0;
                                opaqueBitmask |= bpxnynz != null && bpxnynz.isOpaque ? 262144 : 0;
                                opaqueBitmask |= bpxny0z != null && bpxny0z.isOpaque ? 524288 : 0;
                                opaqueBitmask |= bpxnypz != null && bpxnypz.isOpaque ? 1048576 : 0;
                                opaqueBitmask |= bpx0ynz != null && bpx0ynz.isOpaque ? 2097152 : 0;
                                opaqueBitmask |= bpx0ypz != null && bpx0ypz.isOpaque ? 4194304 : 0;
                                opaqueBitmask |= bpxpynz != null && bpxpynz.isOpaque ? 8388608 : 0;
                                opaqueBitmask |= bpxpy0z != null && bpxpy0z.isOpaque ? 16777216 : 0;
                                opaqueBitmask |= bpxpypz != null && bpxpypz.isOpaque ? 33554432 : 0;
                                MeshData md = null;
                                GameShader shader = GameShader.getShaderForBlockState(b);
                                RenderOrder renderOrder = RenderOrder.getRenderOrderForBlockState(b);

                                for(int i = 0; i < meshDatas.size; ++i) {
                                    MeshData candidate = meshDatas.get(i);
                                    if (candidate.getShader() == shader && candidate.getRenderOrder() == renderOrder) {
                                        md = candidate;
                                        break;
                                    }
                                }

                                if (md == null) {
                                    md = new MeshData(new FloatArray(1024), null, shader, renderOrder);
                                    meshDatas.add(md);
                                }

                                calculateBlockLightLevels(chunk, blockLightLevels, true, opaqueBitmask, localX, localY, localZ);
                                b.addVertices(md, globalX - (pos.x() * 16), globalY - (pos.y() * 16), globalZ - (pos.z() * 16), opaqueBitmask, blockLightLevels, skyLightLevels);
                            }
                        }
                    }
                }
                ArrayUtils.removeIf(meshDatas, (mdx) -> mdx.getVertices().isEmpty());
                return meshDatas;
            }
        }
    }

    private static short[] calculateBlockLightLevels(VirtualChunk chunk, short[] blockLightLevels, boolean hasNeighbouringBlockLightChunks, int opaqueBitmask, int localX, int localY, int localZ) {
        short lightLevel = 0;
//        short[] blockLightData = chunk.blockLightData;
        VirtualWorld zone = chunk.parent;
//        if (blockLightData == null) {
//            if (!hasNeighbouringBlockLightChunks || localX > 0 && localY > 0 && localZ > 0 && localX < 15 && localY < 15 && localZ < 15) {
//                return blockLightLevels;
//            }
//        } else {
        lightLevel = chunk.getBlockLight(localX, localY, localZ);
//        }

        Arrays.fill(blockLightLevels, lightLevel);
        int globalX = chunk.blockPos.x() + localX;
        int globalY = chunk.blockPos.y() + localY;
        int globalZ = chunk.blockPos.z() + localZ;
        int lightNxNyNz = zone.getBlockLight(chunk, globalX - 1, globalY - 1, globalZ - 1);
        int lightNxNy0z = zone.getBlockLight(chunk, globalX - 1, globalY - 1, globalZ);
        int lightNxNyPz = zone.getBlockLight(chunk, globalX - 1, globalY - 1, globalZ + 1);
        int lightNx0yNz = zone.getBlockLight(chunk, globalX - 1, globalY, globalZ - 1);
        int lightNx0y0z = zone.getBlockLight(chunk, globalX - 1, globalY, globalZ);
        int lightNx0yPz = zone.getBlockLight(chunk, globalX - 1, globalY, globalZ + 1);
        int lightNxPyNz = zone.getBlockLight(chunk, globalX - 1, globalY + 1, globalZ - 1);
        int lightNxPy0z = zone.getBlockLight(chunk, globalX - 1, globalY + 1, globalZ);
        int lightNxPyPz = zone.getBlockLight(chunk, globalX - 1, globalY + 1, globalZ + 1);
        int light0xNyNz = zone.getBlockLight(chunk, globalX, globalY - 1, globalZ - 1);
        int light0xNy0z = zone.getBlockLight(chunk, globalX, globalY - 1, globalZ);
        int light0xNyPz = zone.getBlockLight(chunk, globalX, globalY - 1, globalZ + 1);
        int light0x0yNz = zone.getBlockLight(chunk, globalX, globalY, globalZ - 1);
        int light0x0yPz = zone.getBlockLight(chunk, globalX, globalY, globalZ + 1);
        int light0xPyNz = zone.getBlockLight(chunk, globalX, globalY + 1, globalZ - 1);
        int light0xPy0z = zone.getBlockLight(chunk, globalX, globalY + 1, globalZ);
        int light0xPyPz = zone.getBlockLight(chunk, globalX, globalY + 1, globalZ + 1);
        int lightPxNyNz = zone.getBlockLight(chunk, globalX + 1, globalY - 1, globalZ - 1);
        int lightPxNy0z = zone.getBlockLight(chunk, globalX + 1, globalY - 1, globalZ);
        int lightPxNyPz = zone.getBlockLight(chunk, globalX + 1, globalY - 1, globalZ + 1);
        int lightPx0yNz = zone.getBlockLight(chunk, globalX + 1, globalY, globalZ - 1);
        int lightPx0y0z = zone.getBlockLight(chunk, globalX + 1, globalY, globalZ);
        int lightPx0yPz = zone.getBlockLight(chunk, globalX + 1, globalY, globalZ + 1);
        int lightPxPyNz = zone.getBlockLight(chunk, globalX + 1, globalY + 1, globalZ - 1);
        int lightPxPy0z = zone.getBlockLight(chunk, globalX + 1, globalY + 1, globalZ);
        int lightPxPyPz = zone.getBlockLight(chunk, globalX + 1, globalY + 1, globalZ + 1);
        short m = blockLightLevels[0];
        m = getMaxBlockLight(m, light0xNy0z);
        m = getMaxBlockLight(m, light0xNyNz);
        m = getMaxBlockLight(m, lightNxNy0z);
        m = getMaxBlockLight(m, lightNx0y0z);
        m = getMaxBlockLight(m, light0x0yNz);
        m = getMaxBlockLight(m, lightNx0yNz);
        m = getMaxBlockLight(m, lightNxNyNz);
        blockLightLevels[0] = m;
        m = blockLightLevels[1];
        m = getMaxBlockLight(m, light0xNy0z);
        m = getMaxBlockLight(m, light0xNyPz);
        m = getMaxBlockLight(m, lightNxNy0z);
        m = getMaxBlockLight(m, light0x0yPz);
        m = getMaxBlockLight(m, lightNx0y0z);
        m = getMaxBlockLight(m, lightNx0yPz);
        m = getMaxBlockLight(m, lightNxNyPz);
        blockLightLevels[1] = m;
        m = blockLightLevels[2];
        m = getMaxBlockLight(m, light0xNy0z);
        m = getMaxBlockLight(m, light0xNyNz);
        m = getMaxBlockLight(m, lightPxNy0z);
        m = getMaxBlockLight(m, light0x0yNz);
        m = getMaxBlockLight(m, lightPx0y0z);
        m = getMaxBlockLight(m, lightPx0yNz);
        m = getMaxBlockLight(m, lightPxNyNz);
        blockLightLevels[2] = m;
        m = blockLightLevels[3];
        m = getMaxBlockLight(m, light0xNy0z);
        m = getMaxBlockLight(m, light0xNyPz);
        m = getMaxBlockLight(m, lightPxNy0z);
        m = getMaxBlockLight(m, light0x0yPz);
        m = getMaxBlockLight(m, lightPx0y0z);
        m = getMaxBlockLight(m, lightPx0yPz);
        m = getMaxBlockLight(m, lightPxNyPz);
        blockLightLevels[3] = m;
        m = blockLightLevels[4];
        m = getMaxBlockLight(m, light0xPy0z);
        m = getMaxBlockLight(m, light0xPyNz);
        m = getMaxBlockLight(m, lightNxPy0z);
        m = getMaxBlockLight(m, light0x0yNz);
        m = getMaxBlockLight(m, lightNx0y0z);
        m = getMaxBlockLight(m, lightNx0yNz);
        m = getMaxBlockLight(m, lightNxPyNz);
        blockLightLevels[4] = m;
        m = blockLightLevels[5];
        m = getMaxBlockLight(m, light0xPy0z);
        m = getMaxBlockLight(m, light0xPyPz);
        m = getMaxBlockLight(m, lightNxPy0z);
        m = getMaxBlockLight(m, lightNx0y0z);
        m = getMaxBlockLight(m, light0x0yPz);
        m = getMaxBlockLight(m, lightNx0yPz);
        m = getMaxBlockLight(m, lightNxPyPz);
        blockLightLevels[5] = m;
        m = blockLightLevels[6];
        m = getMaxBlockLight(m, light0xPy0z);
        m = getMaxBlockLight(m, light0xPyNz);
        m = getMaxBlockLight(m, lightPxPy0z);
        m = getMaxBlockLight(m, light0x0yNz);
        m = getMaxBlockLight(m, lightPx0y0z);
        m = getMaxBlockLight(m, lightPx0yNz);
        m = getMaxBlockLight(m, lightPxPyNz);
        blockLightLevels[6] = m;
        m = blockLightLevels[7];
        m = getMaxBlockLight(m, light0xPy0z);
        m = getMaxBlockLight(m, light0xPyPz);
        m = getMaxBlockLight(m, lightPxPy0z);
        m = getMaxBlockLight(m, lightPx0y0z);
        m = getMaxBlockLight(m, light0x0yPz);
        m = getMaxBlockLight(m, lightPx0yPz);
        m = getMaxBlockLight(m, lightPxPyPz);
        blockLightLevels[7] = m;
        return blockLightLevels;
    }

    public static short getMaxBlockLight(int blockLightA, int blockLightB) {
        int r = Math.max(blockLightA & 3840, blockLightB & 3840) >> 8;
        int g = Math.max(blockLightA & 240, blockLightB & 240) >> 4;
        int b = Math.max(blockLightA & 15, blockLightB & 15);
        return (short)((r << 8) + (g << 4) + b);
    }

}
