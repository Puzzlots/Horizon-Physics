package me.zombii.horizon.worldgen;

import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.savelib.blockdata.LayeredBlockData;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.worldgen.ChunkColumn;
import finalforeach.cosmicreach.worldgen.ZoneGenerator;
import me.zombii.horizon.HorizonConstants;

public class VoidGenerator extends ZoneGenerator {
    private final int softMaxY = 255;
    private final int softMinY = 0;

    // Fetches on class instantiation, so will not be null
    BlockState airBlock = this.getBlockStateInstance("base:air[default]");

    @Override
    public String getSaveKey() {
        return HorizonConstants.MOD_ID + ":void";
    }

    @Override
    protected String getName() {
        // Not fetched from the lang file
        return "void";
    }

    // Called on world load/create, after this.seed is set
    @Override
    public void create() {
        // Create noise generators
    }

    // Generate a chunk-column of the world at once (easier for the lighting engine this way)
    @Override
    public void generateForChunkColumn(Zone zone, ChunkColumn col) {
        int maxChunkY = Math.floorDiv(this.softMaxY, 16);
        int minChunkY = Math.floorDiv(this.softMinY, 16);

        // Only generate the regions containing minY < y < maxY
        if(col.chunkY < minChunkY || col.chunkY > maxChunkY) return;


        for(int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++) {
            var chunk = zone.getChunkAtChunkCoords(col.chunkX, chunkY, col.chunkZ);

            if (chunk == null) {
                // Create a new chunk if it doesn't exist
                chunk = new Chunk(col.chunkX, chunkY, col.chunkZ);

                // Create the chunk data
                chunk.initChunkData(() -> new LayeredBlockData<>(airBlock));

                // Register the chunks in the world and column
                zone.addChunk(chunk);
                col.addChunk(chunk);
            }

        }
    }

    @Override
    public int getDefaultRespawnYLevel() {
        return -16;
    }
}
