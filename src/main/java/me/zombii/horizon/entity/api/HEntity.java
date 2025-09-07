package me.zombii.horizon.entity.api;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import finalforeach.cosmicreach.world.EntityChunk;
import io.github.puzzle.cosmic.api.entity.IEntity;

public interface HEntity extends IEntity {

    EntityChunk hGetCurrentChunk();

    Vector3 hGetVelocity();
    HEntity hSetVelocity(Vector3 velocity);
    HEntity hSetVelocity(float x, float y, float z);

    Vector3 hGetAcceleration();
    HEntity hSetAcceleration(Vector3 acceleration);
    HEntity hSetAcceleration(float x, float y, float z);

    float hGetMass();
    HEntity hSetMass(float mass);

    float hGetGravityModifier();

    BoundingBox hGetLocalBoundingBox();

    HEntity hSetPosition(Vector3 pos);
    Vector3 hGetPosition();

    HEntity hResetAcceleration();

//    boolean HGetCollidedX();
//    void hSetCollidedX(boolean collidedX);
//
//    boolean getCollidedY();
//    void setCollidedY(boolean collidedX);
//
//    boolean getCollidedZ();
//    void setCollidedZ(boolean collidedX);

}
