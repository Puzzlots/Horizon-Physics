package me.zombii.horizon.collision;

import com.badlogic.gdx.math.Vector3;

public interface Bounds {

    boolean intersects(Vector3 point);
    boolean intersects(AABB aabb);
    boolean intersects(Sphere sphere);
    boolean intersects(OBB obb);

    Vector3 tmp = new Vector3();
    default boolean intersects(float x, float y, float z) {
        return intersects(tmp.set(x, y, z));
    }
}
