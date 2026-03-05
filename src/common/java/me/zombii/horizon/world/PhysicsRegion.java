package me.zombii.horizon.world;

import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Region;
import finalforeach.cosmicreach.world.Zone;

public class PhysicsRegion extends Region {

    public PhysicsRegion(Zone zone, int regionX, int regionY, int regionZ) {
        super(zone, regionX, regionY, regionZ);
    }

//    @Override
//    public boolean isColumnGeneratedForChunk(Chunk chunk) {
//        return true;
//    }
//
//    @Override
//    public boolean isColumnGeneratedForChunkIndex(int index) {
//        return true;
//    }
}
