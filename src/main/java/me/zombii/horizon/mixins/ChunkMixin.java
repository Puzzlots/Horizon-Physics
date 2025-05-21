package me.zombii.horizon.mixins;

import me.zombii.horizon.rendering.mesh.IPhysicChunk;

public class ChunkMixin implements IPhysicChunk {

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
