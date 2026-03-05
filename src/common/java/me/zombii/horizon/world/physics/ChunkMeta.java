package me.zombii.horizon.world.physics;

import com.jme3.bullet.objects.PhysicsRigidBody;

public class ChunkMeta {

    private PhysicsRigidBody body;
    private ChunkValidity validity;

    public ChunkMeta() {
        this.body = null;
        this.validity = ChunkValidity.UNINITIALIZED;
    }

    public boolean hasNullBody() {
        return body == null;
    }

    public void setBody(PhysicsRigidBody body) {
        this.body = body;
    }

    public void setValidity(ChunkValidity valid) {
        validity = valid;
    }

    public PhysicsRigidBody getBody() {
        return body;
    }

    public ChunkValidity getValidity() {
        return validity;
    }

    public enum ChunkValidity {
        UNINITIALIZED,
        NEEDS_PHYSICS_REMESHING,
        HAS_RIGID_BODY

    }
}
