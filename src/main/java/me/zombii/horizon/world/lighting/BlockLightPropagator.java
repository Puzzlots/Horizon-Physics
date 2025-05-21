package me.zombii.horizon.world.lighting;

import com.badlogic.gdx.utils.Queue;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.util.constants.Direction;
import me.zombii.horizon.world.BlockPos;
import me.zombii.horizon.world.VirtualWorld;

public class BlockLightPropagator {
    public BlockLightPropagator() {
    }

    public static void propagateBlockDarkness(VirtualWorld zone, Queue<BlockPos> darkQueue) {
        Queue<BlockPos> lightQueue = new Queue<>();

        int packedLight;
        int b;
        int GREENparentLightLimit;
        while(darkQueue.notEmpty()) {
            BlockPos position = darkQueue.removeFirst();
            int lpacked = position.getBlockLight();
            int r = (lpacked & 3840) >> 8;
            packedLight = (lpacked & 240) >> 4;
            b = lpacked & 15;
            GREENparentLightLimit = lpacked;

            if (r != packedLight || packedLight != b || b != 0) {
                position.setBlockLight(0, 0, 0);
                position.flagTouchingChunksForRemeshing(zone, false);
            }

            BlockState curBlock = position.getBlockState();
            if (curBlock != null && curBlock.isLightEmitter()) {
                lightQueue.addLast(position);
            }

            Direction[] var10 = Direction.ALL_DIRECTIONS;

            for (Direction d : var10) {
                PooledLightLimitPos neighbourPosObj = new PooledLightLimitPos(null, null, 0, 0, 0);
                BlockPos neighbourPos = position.getOffsetBlockPos(neighbourPosObj, zone, d);
                if (neighbourPos != null) {
                    neighbourPosObj.lightLimit = GREENparentLightLimit;
                    if (position instanceof PooledLightLimitPos limitPos) {
                        GREENparentLightLimit = limitPos.getCombinedLimit(GREENparentLightLimit);
                    }
                    int lnpacked = neighbourPos.getBlockLight();
                    int nr = (lnpacked & 3840) >> 8;
                    int ng = (lnpacked & 240) >> 4;
                    int nb = lnpacked & 15;
                    boolean addToDarkQueue = nr != 0 && nr < r && nr < neighbourPosObj.getRedLimit();
                    addToDarkQueue |= ng != 0 && ng < packedLight && ng < neighbourPosObj.getGreenLimit();
                    addToDarkQueue |= nb != 0 && nb < b && nb < neighbourPosObj.getBlueLimit();
                    if (addToDarkQueue) {
                        darkQueue.addLast(neighbourPos);
                    }

                    if (nr != 0 && nr >= r || ng != 0 && ng >= packedLight || nb != 0 && nb >= b) {
                        BlockState nBlock = position.getBlockState();
                        if (nBlock == null || !nBlock.isLightEmitter()) {
                            PooledLightLimitPos lightPosObj = new PooledLightLimitPos(null, null, 0, 0, 0);
                            lightPosObj.set(neighbourPosObj);
                            lightQueue.addLast(lightPosObj);
                        }
                    }
                }
            }
        }

        for (BlockPos lightPos : lightQueue) {
            BlockState nblock = lightPos.getBlockState();
            if (nblock != null) {
                packedLight = lightPos.getBlockLight();

                int red = (packedLight & 3840) >> 8;
                GREENparentLightLimit = (packedLight & 240) >> 4;
                int blue = packedLight & 15;
                red = Math.max(red, nblock.lightLevelRed);
                GREENparentLightLimit = Math.max(GREENparentLightLimit, nblock.lightLevelGreen);
                blue = Math.max(blue, nblock.lightLevelBlue);
                lightPos.setBlockLight(red, GREENparentLightLimit, blue);
            }
        }

        if (lightQueue.notEmpty()) {
            propagateBlockLights(zone, lightQueue);
        }

    }

    public static void propagateBlockLights(VirtualWorld zone, Queue<BlockPos> lightQueue) {
        while(lightQueue.notEmpty()) {
            BlockPos position = lightQueue.removeFirst();
            int lpacked = position.getBlockLight();
            int r = (lpacked & 3840) >> 8;
            int g = (lpacked & 240) >> 4;
            int b = lpacked & 15;
            BlockState blockState = position.getBlockState();
            if (blockState != null) {
                r = Math.max(r, blockState.lightLevelRed);
                g = Math.max(g, blockState.lightLevelGreen);
                b = Math.max(b, blockState.lightLevelBlue);
            }

            Direction[] var8 = Direction.ALL_DIRECTIONS;

            for (Direction d : var8) {
                BlockPos neighbourPos = position.getOffsetBlockPos(zone, d);
                if (neighbourPos != null) {
                    int lnpacked = neighbourPos.getBlockLight();
                    int nr = (lnpacked & 3840) >> 8;
                    int ng = (lnpacked & 240) >> 4;
                    int nb = lnpacked & 15;
                    BlockState block = neighbourPos.getBlockState();
                    if (block == null || block.lightAttenuation != 15) {
                        int atten = Math.max(block.lightAttenuation, 1);
                        if (nr < r - atten || ng < g - atten || nb < b - atten) {
                            int ir = Math.max(nr, r - atten);
                            int ig = Math.max(ng, g - atten);
                            int ib = Math.max(nb, b - atten);
                            neighbourPos.setBlockLight(ir, ig, ib);
                            neighbourPos.chunk.flagTouchingChunksForRemeshing(zone, neighbourPos.x, neighbourPos.y, neighbourPos.z, false);
                            if (ir > 1 || ig > 1 || ib > 1) {
                                lightQueue.addLast(neighbourPos);
                            }
                        }
                    }
                }
            }

            position.free();
        }

    }
}
