package me.zombii.horizon.util;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.rendering.RenderOrder;
import finalforeach.cosmicreach.rendering.meshes.MeshData;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.IBlockLightData;
import finalforeach.cosmicreach.savelib.lightdata.skylight.ISkylightData;
import finalforeach.cosmicreach.util.ArrayUtils;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Chunk;
import me.zombii.horizon.world.PhysicsZone;

import java.util.Arrays;

public class VCosmicMeshingUtil {

    static final Array<MeshData> emptyMeshDatas = new Array(false, 0, MeshData.class);

    public static Array<MeshData> getMeshData(PhysicsZone zone, Chunk chunk) {
        IBlockData<BlockState> blockData = (IBlockData<BlockState>) chunk.getBlockData();
        BlockState airBlockState = Block.AIR.getDefaultBlockState();
        if (blockData.isEntirely(airBlockState)) {
            return emptyMeshDatas;
        } else {
            boolean isEntirelyOpaque = chunk.isEntirelyOpaque();
            if (isEntirelyOpaque && chunk.isCulledByAdjacentChunks(zone)) {
                return emptyMeshDatas;
            } else {
                Array<MeshData> meshDatas = new Array(3);
                boolean isEntirelyOneBlockSelfCulling = chunk.isEntirelyOneBlockSelfCulling();
                short[] blockLightLevels = new short[8];
                int[] skyLightLevels = new int[8];

                Arrays.fill(skyLightLevels, Short.MAX_VALUE);
                Arrays.fill(blockLightLevels, (short) 4095);

                boolean hasNeighbouringBlockLightChunks = chunk.blockLightData != null || chunk.hasAdjacentBlockLightChunks(zone);
                int maxIdx = chunk.getMaxNonEmptyBlockIdxYXZ();
                Chunk chunkNegX = zone.getChunkAtChunkCoords(chunk.chunkX - 1, chunk.chunkY, chunk.chunkZ);
                Chunk chunkPosX = zone.getChunkAtChunkCoords(chunk.chunkX + 1, chunk.chunkY, chunk.chunkZ);
                Chunk chunkNegY = zone.getChunkAtChunkCoords(chunk.chunkX, chunk.chunkY - 1, chunk.chunkZ);
                Chunk chunkPosY = zone.getChunkAtChunkCoords(chunk.chunkX, chunk.chunkY + 1, chunk.chunkZ);
                Chunk chunkNegZ = zone.getChunkAtChunkCoords(chunk.chunkX, chunk.chunkY, chunk.chunkZ - 1);
                Chunk chunkPosZ = zone.getChunkAtChunkCoords(chunk.chunkX, chunk.chunkY, chunk.chunkZ + 1);

                for(int idx = 0; idx < maxIdx; ++idx) {
                    int localY = idx / 256;
                    int localX = (idx - localY * 256) / 16;
                    int localZ = (idx - localY * 256) % 16;
                    if ((isEntirelyOpaque || isEntirelyOneBlockSelfCulling) && localX != 0 && localX != 15 && localY != 0 && localY != 15 && localZ != 0 && localZ != 15) {
                        idx += 13;
                    } else {
                        BlockState b = blockData.getBlockValue(localX, localY, localZ);
                        if (b != airBlockState) {
                            int globalX = chunk.getBlockX() + localX;
                            int globalY = chunk.getBlockY() + localY;
                            int globalZ = chunk.getBlockZ() + localZ;
                            BlockState bnx = zone.getBlockState(chunk, chunkNegX, globalX - 1, globalY, globalZ);
                            BlockState bpx = zone.getBlockState(chunk, chunkPosX, globalX + 1, globalY, globalZ);
                            BlockState bny = zone.getBlockState(chunk, chunkNegY, globalX, globalY - 1, globalZ);
                            BlockState bpy = zone.getBlockState(chunk, chunkPosY, globalX, globalY + 1, globalZ);
                            BlockState bnz = zone.getBlockState(chunk, chunkNegZ, globalX, globalY, globalZ - 1);
                            BlockState bpz = zone.getBlockState(chunk, chunkPosZ, globalX, globalY, globalZ + 1);
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
                                BlockState bnxnynz = zone.getBlockState(chunk, globalX - 1, globalY - 1, globalZ - 1);
                                BlockState bnxny0z = zone.getBlockState(chunk, globalX - 1, globalY - 1, globalZ);
                                BlockState bnxnypz = zone.getBlockState(chunk, globalX - 1, globalY - 1, globalZ + 1);
                                BlockState bnx0ynz = zone.getBlockState(chunk, globalX - 1, globalY, globalZ - 1);
                                BlockState bnx0ypz = zone.getBlockState(chunk, globalX - 1, globalY, globalZ + 1);
                                BlockState bnxpynz = zone.getBlockState(chunk, globalX - 1, globalY + 1, globalZ - 1);
                                BlockState bnxpy0z = zone.getBlockState(chunk, globalX - 1, globalY + 1, globalZ);
                                BlockState bnxpypz = zone.getBlockState(chunk, globalX - 1, globalY + 1, globalZ + 1);
                                BlockState b0xnynz = zone.getBlockState(chunk, globalX, globalY - 1, globalZ - 1);
                                BlockState b0xnypz = zone.getBlockState(chunk, globalX, globalY - 1, globalZ + 1);
                                BlockState b0xpynz = zone.getBlockState(chunk, globalX, globalY + 1, globalZ - 1);
                                BlockState b0xpypz = zone.getBlockState(chunk, globalX, globalY + 1, globalZ + 1);
                                BlockState bpxnynz = zone.getBlockState(chunk, globalX + 1, globalY - 1, globalZ - 1);
                                BlockState bpxny0z = zone.getBlockState(chunk, globalX + 1, globalY - 1, globalZ);
                                BlockState bpxnypz = zone.getBlockState(chunk, globalX + 1, globalY - 1, globalZ + 1);
                                BlockState bpx0ynz = zone.getBlockState(chunk, globalX + 1, globalY, globalZ - 1);
                                BlockState bpx0ypz = zone.getBlockState(chunk, globalX + 1, globalY, globalZ + 1);
                                BlockState bpxpynz = zone.getBlockState(chunk, globalX + 1, globalY + 1, globalZ - 1);
                                BlockState bpxpy0z = zone.getBlockState(chunk, globalX + 1, globalY + 1, globalZ);
                                BlockState bpxpypz = zone.getBlockState(chunk, globalX + 1, globalY + 1, globalZ + 1);
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
                                    if (candidate.shader == shader && candidate.renderOrder == renderOrder) {
                                        md = candidate;
                                        break;
                                    }
                                }

                                if (md == null) {
                                    md = new MeshData(new FloatArray(1024), null, shader, renderOrder);
                                    meshDatas.add(md);
                                }

                                calculateBlockLightLevels(zone, chunk, blockLightLevels, hasNeighbouringBlockLightChunks, opaqueBitmask, localX, localY, localZ);
//                                calculateSkyLightLevels(zone, chunk, skyLightLevels, localX, localY, localZ);

                                b.addVertices(md, globalX - (chunk.chunkX * 16), globalY - (chunk.chunkY * 16), globalZ - (chunk.chunkZ * 16), opaqueBitmask, blockLightLevels, skyLightLevels);

                            }
                        }
                    }
                }

                ArrayUtils.removeIf(meshDatas, (mdx) -> mdx.vertices.isEmpty());
                return meshDatas;
            }
        }
    }

    private static int[] calculateSkyLightLevels(PhysicsZone zone, Chunk chunk, int[] skyLightLevels, int localX, int localY, int localZ) {
        int skyLightLevel = 0;
        ISkylightData skyLightData = chunk.skyLightData;
        if (skyLightData == null) {
            if (localX > 0 && localY > 0 && localZ > 0 && localX < 15 && localY < 15 && localZ < 15) {
                Arrays.fill(skyLightLevels, skyLightLevel);
                return skyLightLevels;
            }
        } else {
            skyLightLevel = skyLightData.getSkyLight(localX, localY, localZ);
        }

        Arrays.fill(skyLightLevels, skyLightLevel);
        int globalX = chunk.blockX + localX;
        int globalY = chunk.blockY + localY;
        int globalZ = chunk.blockZ + localZ;
        int lightNxNyNz = zone.getSkyLight(chunk, globalX - 1, globalY - 1, globalZ - 1);
        int lightNxNy0z = zone.getSkyLight(chunk, globalX - 1, globalY - 1, globalZ);
        int lightNxNyPz = zone.getSkyLight(chunk, globalX - 1, globalY - 1, globalZ + 1);
        int lightNx0yNz = zone.getSkyLight(chunk, globalX - 1, globalY, globalZ - 1);
        int lightNx0y0z = zone.getSkyLight(chunk, globalX - 1, globalY, globalZ);
        int lightNx0yPz = zone.getSkyLight(chunk, globalX - 1, globalY, globalZ + 1);
        int lightNxPyNz = zone.getSkyLight(chunk, globalX - 1, globalY + 1, globalZ - 1);
        int lightNxPy0z = zone.getSkyLight(chunk, globalX - 1, globalY + 1, globalZ);
        int lightNxPyPz = zone.getSkyLight(chunk, globalX - 1, globalY + 1, globalZ + 1);
        int light0xNyNz = zone.getSkyLight(chunk, globalX, globalY - 1, globalZ - 1);
        int light0xNy0z = zone.getSkyLight(chunk, globalX, globalY - 1, globalZ);
        int light0xNyPz = zone.getSkyLight(chunk, globalX, globalY - 1, globalZ + 1);
        int light0x0yNz = zone.getSkyLight(chunk, globalX, globalY, globalZ - 1);
        int light0x0yPz = zone.getSkyLight(chunk, globalX, globalY, globalZ + 1);
        int light0xPyNz = zone.getSkyLight(chunk, globalX, globalY + 1, globalZ - 1);
        int light0xPy0z = zone.getSkyLight(chunk, globalX, globalY + 1, globalZ);
        int light0xPyPz = zone.getSkyLight(chunk, globalX, globalY + 1, globalZ + 1);
        int lightPxNyNz = zone.getSkyLight(chunk, globalX + 1, globalY - 1, globalZ - 1);
        int lightPxNy0z = zone.getSkyLight(chunk, globalX + 1, globalY - 1, globalZ);
        int lightPxNyPz = zone.getSkyLight(chunk, globalX + 1, globalY - 1, globalZ + 1);
        int lightPx0yNz = zone.getSkyLight(chunk, globalX + 1, globalY, globalZ - 1);
        int lightPx0y0z = zone.getSkyLight(chunk, globalX + 1, globalY, globalZ);
        int lightPx0yPz = zone.getSkyLight(chunk, globalX + 1, globalY, globalZ + 1);
        int lightPxPyNz = zone.getSkyLight(chunk, globalX + 1, globalY + 1, globalZ - 1);
        int lightPxPy0z = zone.getSkyLight(chunk, globalX + 1, globalY + 1, globalZ);
        int lightPxPyPz = zone.getSkyLight(chunk, globalX + 1, globalY + 1, globalZ + 1);
        int m = skyLightLevels[0];
        m = Math.max(m, light0xNy0z);
        m = Math.max(m, light0xNyNz);
        m = Math.max(m, lightNxNy0z);
        m = Math.max(m, lightNx0y0z);
        m = Math.max(m, light0x0yNz);
        m = Math.max(m, lightNx0yNz);
        m = Math.max(m, lightNxNyNz);
        skyLightLevels[0] = m;
        m = skyLightLevels[1];
        m = Math.max(m, light0xNy0z);
        m = Math.max(m, light0xNyPz);
        m = Math.max(m, lightNxNy0z);
        m = Math.max(m, light0x0yPz);
        m = Math.max(m, lightNx0y0z);
        m = Math.max(m, lightNx0yPz);
        m = Math.max(m, lightNxNyPz);
        skyLightLevels[1] = m;
        m = skyLightLevels[2];
        m = Math.max(m, light0xNy0z);
        m = Math.max(m, light0xNyNz);
        m = Math.max(m, lightPxNy0z);
        m = Math.max(m, light0x0yNz);
        m = Math.max(m, lightPx0y0z);
        m = Math.max(m, lightPx0yNz);
        m = Math.max(m, lightPxNyNz);
        skyLightLevels[2] = m;
        m = skyLightLevels[3];
        m = Math.max(m, light0xNy0z);
        m = Math.max(m, light0xNyPz);
        m = Math.max(m, lightPxNy0z);
        m = Math.max(m, light0x0yPz);
        m = Math.max(m, lightPx0y0z);
        m = Math.max(m, lightPx0yPz);
        m = Math.max(m, lightPxNyPz);
        skyLightLevels[3] = m;
        m = skyLightLevels[4];
        m = Math.max(m, light0xPy0z);
        m = Math.max(m, light0xPyNz);
        m = Math.max(m, lightNxPy0z);
        m = Math.max(m, light0x0yNz);
        m = Math.max(m, lightNx0y0z);
        m = Math.max(m, lightNx0yNz);
        m = Math.max(m, lightNxPyNz);
        skyLightLevels[4] = m;
        m = skyLightLevels[5];
        m = Math.max(m, light0xPy0z);
        m = Math.max(m, light0xPyPz);
        m = Math.max(m, lightNxPy0z);
        m = Math.max(m, lightNx0y0z);
        m = Math.max(m, light0x0yPz);
        m = Math.max(m, lightNx0yPz);
        m = Math.max(m, lightNxPyPz);
        skyLightLevels[5] = m;
        m = skyLightLevels[6];
        m = Math.max(m, light0xPy0z);
        m = Math.max(m, light0xPyNz);
        m = Math.max(m, lightPxPy0z);
        m = Math.max(m, light0x0yNz);
        m = Math.max(m, lightPx0y0z);
        m = Math.max(m, lightPx0yNz);
        m = Math.max(m, lightPxPyNz);
        skyLightLevels[6] = m;
        m = skyLightLevels[7];
        m = Math.max(m, light0xPy0z);
        m = Math.max(m, light0xPyPz);
        m = Math.max(m, lightPxPy0z);
        m = Math.max(m, lightPx0y0z);
        m = Math.max(m, light0x0yPz);
        m = Math.max(m, lightPx0yPz);
        m = Math.max(m, lightPxPyPz);
        skyLightLevels[7] = m;
        return skyLightLevels;
    }

    public static short getMaxBlockLight(int blockLightA, int blockLightB) {
        int r = Math.max(blockLightA & 3840, blockLightB & 3840) >> 8;
        int g = Math.max(blockLightA & 240, blockLightB & 240) >> 4;
        int b = Math.max(blockLightA & 15, blockLightB & 15);
        return (short)((r << 8) + (g << 4) + b);
    }

    private static short[] calculateBlockLightLevels(PhysicsZone zone, Chunk chunk, short[] blockLightLevels, boolean hasNeighbouringBlockLightChunks, int opaqueBitmask, int localX, int localY, int localZ) {
        short lightLevel = 0;
        IBlockLightData blockLightData = chunk.blockLightData;
//        if (blockLightData == null) {
//            if (!hasNeighbouringBlockLightChunks || localX > 0 && localY > 0 && localZ > 0 && localX < 15 && localY < 15 && localZ < 15) {
//                return blockLightLevels;
//            }
//        } else {
            lightLevel = blockLightData.getBlockLight(localX, localY, localZ);
//        }

        Arrays.fill(blockLightLevels, lightLevel);
        int globalX = chunk.blockX + localX;
        int globalY = chunk.blockY + localY;
        int globalZ = chunk.blockZ + localZ;
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

}
