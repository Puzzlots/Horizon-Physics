package me.zombii.horizon.mixins;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.components.GravityComponent;
import finalforeach.cosmicreach.entities.components.IUpdateEntityComponent;
import finalforeach.cosmicreach.world.EntityChunk;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.collision.vanilla.EntityCollision;
import me.zombii.horizon.entity.api.HEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class EntityMixin implements HEntity {

    @Shadow public BoundingBox localBoundingBox;

    @Shadow public float gravityModifier;

    @Shadow private Vector3 acceleration;

    @Shadow public transient EntityChunk currentChunk;

    @Shadow public Vector3 velocity;

    @Shadow public Vector3 position;

    @Shadow public abstract void removeUpdatingComponent(IUpdateEntityComponent c);

    /**
     * @author Mr_Zombii
     * @reason Start Collision Re-write
     */
//    @Overwrite
//    public void updateConstraints(Zone zone, Vector3 targetPosition) {
//        removeUpdatingComponent(GravityComponent.INSTANCE);
//
//        EntityCollision.updateConstraints((Entity) (Object) this, zone, targetPosition);
//    }

    /**
     * @author Mr_Zombii
     * @reason Start Collision Re-write
     */
//    @Overwrite
//    public void updatePositions(Zone zone, float deltaTime) {
//        EntityCollision.updatePositions((Entity & HEntity) (Object) this, zone, deltaTime);
//    }

    @Override
    public EntityChunk hGetCurrentChunk() {
        return currentChunk;
    }

    @Override
    public Vector3 hGetVelocity() {
        return velocity;
    }

    @Override
    public HEntity hSetVelocity(Vector3 velocity) {
        this.velocity.set(velocity);
        return this;
    }

    @Override
    public HEntity hSetVelocity(float x, float y, float z) {
        this.velocity.set(x, y, z);
        return this;
    }

    @Override
    public Vector3 hGetAcceleration() {
        return acceleration;
    }

    @Override
    public HEntity hSetAcceleration(Vector3 acceleration) {
        this.acceleration.set(acceleration);
        return this;
    }

    @Override
    public HEntity hSetAcceleration(float x, float y, float z) {
        this.acceleration.set(x, y, z);
        return this;
    }

    @Override
    public float hGetGravityModifier() {
        return gravityModifier;
    }

    @Override
    public BoundingBox hGetLocalBoundingBox() {
        return localBoundingBox;
    }

    @Override
    public Vector3 hGetPosition() {
        return position;
    }

    @Override
    public HEntity hSetPosition(Vector3 pos) {
        this.position.set(pos);
        return this;
    }

    transient float mass = 1;

    @Override
    public HEntity hSetMass(float mass) {
        this.mass = mass;
        return this;
    }

    @Override
    public float hGetMass() {
        return mass;
    }

    @Override
    public HEntity hResetAcceleration() {
        acceleration.setZero();
        return this;
    }
}
