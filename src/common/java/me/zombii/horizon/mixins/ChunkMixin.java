package me.zombii.horizon.mixins;

import finalforeach.cosmicreach.world.Chunk;
import me.zombii.horizon.rendering.mesh.IPhysicChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Chunk.class)
public class ChunkMixin implements IPhysicChunk {

    @Unique
    boolean needsRemeshing = true;

    @Override
    public boolean needsRemeshing() {
        return needsRemeshing;
    }

    @Override
    public void setNeedsRemeshing(boolean remeshing) {
        needsRemeshing = remeshing;
    }
}
