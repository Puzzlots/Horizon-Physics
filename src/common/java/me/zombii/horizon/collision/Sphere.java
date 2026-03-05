package me.zombii.horizon.collision;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import static me.zombii.horizon.collision.OBB.getMinMax;

public class Sphere implements Bounds {

    Vector3 position;
    float radius;

    public Sphere(Vector3 position, float radius) {
        this.position = position;
        this.radius = radius;
    }

    public Vector3 getPosition() {
        return position;
    }

    public float getRadius() {
        return radius;
    }

    @Override
    public boolean intersects(Vector3 point) {
        return point.dst(position) < radius;
    }

    @Override
    public boolean intersects(AABB aabb) {
        double x = Math.max(aabb.min.x, Math.min(position.x, aabb.max.x));
        double y = Math.max(aabb.min.y, Math.min(position.y, aabb.max.y));
        double z = Math.max(aabb.min.z, Math.min(position.z, aabb.max.z));

        double dist = Math.sqrt(
                ((x - position.x) * (x - position.x)) +
                        ((y - position.y) * (y - position.y)) +
                        ((z - position.z) * (z - position.z))
        );

        return dist < radius;
    }

    @Override
    public boolean intersects(Sphere sphere) {
        double dist = sphere.position.dst(position);
        return dist < sphere.radius + radius;
    }

    @Override
    public boolean intersects(OBB obb) {
        Vector2 mmX = getMinMax(obb.getVertices(), (v) -> v.x);
        Vector2 mmY = getMinMax(obb.getVertices(), (v) -> v.y);
        Vector2 mmZ = getMinMax(obb.getVertices(), (v) -> v.z);

        double x = Math.max(mmX.y, Math.min(position.x, mmX.x));
        double y = Math.max(mmY.y, Math.min(position.y, mmY.x));
        double z = Math.max(mmZ.y, Math.min(position.z, mmZ.x));

        double dist = Math.sqrt(
                ((x - position.x) * (x - position.x)) +
                        ((y - position.y) * (y - position.y)) +
                        ((z - position.z) * (z - position.z))
        );

        return dist < radius;
    }
}
