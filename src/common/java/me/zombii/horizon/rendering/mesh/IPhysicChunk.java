package me.zombii.horizon.rendering.mesh;

public interface IPhysicChunk {

    boolean needsRemeshing();
    void setNeedsRemeshing(boolean remeshing);

}
