//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.zombii.horizon;

import com.badlogic.gdx.math.Vector2;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.savelib.blockdata.SingleBlockData;
import finalforeach.cosmicreach.savelib.blocks.IBlockDataFactory;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.worldgen.ChunkColumn;
import finalforeach.cosmicreach.worldgen.ZoneGenerator;
import finalforeach.cosmicreach.worldgen.noise.SimplexNoise;
import finalforeach.cosmicreach.worldgen.noise.WhiteNoise;
import finalforeach.cosmicreach.worldgen.trees.CoconutTree;
import me.zombii.horizon.world.PhysicsChunk;
import me.zombii.horizon.world.PhysicsRegion;
import me.zombii.horizon.world.PhysicsZone;

public class NostalgicIslandZoneGenerator extends ZoneGenerator {
    public int groundLevel = 32;
    BlockState airblock;
    BlockState waterblock;
    BlockState stoneBasaltBlock;
    BlockState grassBlock;
    BlockState sandBlock;
    BlockState dirtBlock;
    private SimplexNoise simplexNoise;
    private SimplexNoise simplexNoiseTrees;
    private WhiteNoise whiteNoiseTreesX;
    private WhiteNoise whiteNoiseTreesZ;
    IBlockDataFactory<BlockState> chunkDataFactory = new IBlockDataFactory<BlockState>() {
        public IBlockData<BlockState> createChunkData() {
            SingleBlockData<BlockState> chunkData = new SingleBlockData(NostalgicIslandZoneGenerator.this.airblock);
            return chunkData;
        }
    };

    public void create() {
        this.airblock = this.getBlockStateInstance("base:air[default]");
        this.waterblock = this.getBlockStateInstance("base:water[default]");
        this.stoneBasaltBlock = this.getBlockStateInstance("base:stone_basalt[default]");
        this.grassBlock = this.getBlockStateInstance("base:grass[default]");
        this.sandBlock = this.getBlockStateInstance("base:sand[default]");
        this.dirtBlock = this.getBlockStateInstance("base:dirt[default]");
        this.simplexNoise = new SimplexNoise(this.seed);
        this.simplexNoiseTrees = new SimplexNoise(this.seed + 100L);
        this.whiteNoiseTreesX = new WhiteNoise(this.seed);
        this.whiteNoiseTreesZ = new WhiteNoise(this.seed + 100L);
    }

    public String getSaveKey() {
        return "base:nostalgic_island";
    }

    public void generateForChunkColumn(Zone zone, ChunkColumn col) {
        PhysicsZone zone1 = (PhysicsZone) zone;
        if (col.chunkY >= 0) {
            if (col.chunkY <= Math.floorDiv(this.groundLevel + 64, 16)) {
                int maxCy = col.chunkY + 15;

                for(int cy = col.chunkY; cy <= maxCy; ++cy) {
                    Chunk chunk = zone1.getChunkAtChunkCoords(col.chunkX, cy, col.chunkZ);
                    if (chunk == null) {
                        chunk = new PhysicsChunk(col.chunkX, cy, col.chunkZ);
                        chunk.initChunkData(this.chunkDataFactory);
                        zone1.addChunk(chunk);
                        col.addChunk(chunk);
                    }

                    this.generateForChunk(zone, chunk);
                }

                for(int cy = col.chunkY; cy <= maxCy; ++cy) {
                    Chunk chunk = zone1.getChunkAtChunkCoords(col.chunkX, cy, col.chunkZ);
                    if (chunk != null) {
                        float freq = 0.01F;
                        if (!(this.simplexNoiseTrees.noise2((float)chunk.blockX * freq, (float)chunk.blockZ * freq) > 0.0F) && (chunk.blockData.hasValueInPalette(this.grassBlock) || chunk.blockData.hasValueInPalette(this.sandBlock))) {
                            int xOff = (int)(16.0F * this.whiteNoiseTreesX.noise2DNormalized((float)chunk.blockX, (float)chunk.blockZ));
                            int zOff = (int)(16.0F * this.whiteNoiseTreesZ.noise2DNormalized((float)chunk.blockX, (float)chunk.blockZ));
                            this.generateTree(zone1, chunk.blockX + xOff, chunk.blockY + 16, chunk.blockZ + zOff);
                        }
                    }
                }

            }
        }
    }

    private void generateForChunk(Zone z, Chunk chunk) {
        if (chunk.region == null)
            chunk.region = new PhysicsRegion(z, 0, 0, 0);

        PhysicsZone zone = (PhysicsZone) chunk.region.zone;

        for(int localY = 0; localY < 16; ++localY) {
            int globalY = chunk.blockY + localY;
            if (globalY == 0) {
                chunk.fillLayer(this.stoneBasaltBlock, localY);
            } else if (globalY < this.groundLevel) {
                chunk.fillLayer(this.waterblock, localY);
            }

            if (globalY > 0) {
                for(int localX = 0; localX < 16; ++localX) {
                    int globalX = chunk.blockX + localX;

                    for(int localZ = 0; localZ < 16; ++localZ) {
                        int globalZ = chunk.blockZ + localZ;
                        float freq = 0.005F;
                        float freq2 = 0.01F;
                        float freq3 = 0.02F;
                        float freq4 = 0.05F;
                        int currentGround = (int)((float)this.groundLevel + 8.0F * this.simplexNoise.noise2((float)globalX * freq, (float)globalZ * freq) + 14.0F * this.simplexNoise.noise2((float)globalX * freq2, (float)globalZ * freq2) + 2.0F * this.simplexNoise.noise2((float)globalX * freq3, (float)globalZ * freq3) + 1.0F * this.simplexNoise.noise2((float)globalX * freq4, (float)globalZ * freq4));
                        float distFromCent = Vector2.len((float)globalX, (float)globalZ);
                        float d = distFromCent / 3.0F;
                        if (globalY < currentGround && (float)currentGround - d > (float)(globalY - currentGround)) {
                            if (globalY <= this.groundLevel + 3) {
                                chunk.setBlockState(this.sandBlock, localX, localY, localZ);
                                if (zone.getBlockState(globalX, globalY - 2, globalZ) == this.sandBlock && globalY > 0) {
                                    zone.setBlockState(this.stoneBasaltBlock, globalX, globalY - 2, globalZ);
                                }
                            } else {
                                chunk.setBlockState(this.dirtBlock, localX, localY, localZ);
                                if (zone.getBlockState(globalX, globalY - 2, globalZ) == this.dirtBlock && globalY > 0) {
                                    zone.setBlockState(this.stoneBasaltBlock, globalX, globalY - 2, globalZ);
                                }
                            }
                        } else if (chunk.getBlockState(localX, localY, localZ) == this.airblock && globalY > 0 && zone.getBlockState(globalX, globalY - 1, globalZ) == this.dirtBlock && globalY > 0) {
                            zone.setBlockState(this.grassBlock, globalX, globalY - 1, globalZ);
                        }
                    }
                }
            }
        }

    }

    private void generateTree(Zone zone, int globalX, int maxGlobalY, int globalZ) {
        int treeY = maxGlobalY + 16 + 1;

        for(int y = treeY; y > maxGlobalY - 16; --y) {
            BlockState blockState = zone.getBlockState(globalX, y, globalZ);
            if (blockState != null && blockState != this.airblock) {
                if (blockState != this.grassBlock && blockState != this.sandBlock) {
                    return;
                }

                treeY = y + 1;
                break;
            }
        }

        if (treeY <= maxGlobalY) {
            CoconutTree.DEFAULT.generateTree(this.seed, zone, globalX, treeY, globalZ);
        }
    }

    public int getDefaultRespawnYLevel() {
        return -16;
    }

    public Vector2 getSpawnPoint(Vector2 spawnpoint, int attempt) {
        this.spawnRandom.setSeed(this.seed + (long)attempt);
        float maxDist = 200.0F;
        float dist = this.spawnRandom.nextFloat(maxDist);
        spawnpoint.set(this.spawnRandom.nextFloat(), this.spawnRandom.nextFloat()).nor().scl(dist);
        return spawnpoint;
    }
}
