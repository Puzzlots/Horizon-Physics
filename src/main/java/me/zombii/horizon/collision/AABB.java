package me.zombii.horizon.collision;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import static me.zombii.horizon.collision.OBB.check;
import static me.zombii.horizon.collision.OBB.getMinMax;

public class AABB implements Bounds {

    Vector3[] vertices = new Vector3[]{
        new Vector3(),
        new Vector3(),
        new Vector3(),
        new Vector3(),
        new Vector3(),
        new Vector3(),
        new Vector3(),
        new Vector3(),
    };

    Vector3 min = new Vector3();
    Vector3 max = new Vector3();
    Vector3 center = new Vector3();

    public AABB() {
        update();
    }

    public AABB(
            float x, float y, float z,
            float width, float height, float depth
    ) {
        this.min.set(x, y, z);
        this.max.set(x + width, y + height, z + depth);

        update();
    }

    public AABB(Vector3 min, Vector3 max) {
        this.min.set(min);
        this.max.set(max);

        update();
    }

    public void update() {
        vertices[0].set(min.x, min.y, min.z);
        vertices[1].set(min.x, min.y, max.z);
        vertices[2].set(min.x, max.y, min.z);
        vertices[3].set(min.x, max.y, max.z);
        vertices[4].set(max.x, min.y, min.z);
        vertices[5].set(max.x, min.y, max.z);
        vertices[6].set(max.x, max.y, min.z);
        vertices[7].set(max.x, max.y, max.z);
        center.set(min).add(max).scl(0.5F);
    }

    public Vector3 getCorner000() {
        return vertices[0];
    }

    public Vector3 getCorner001() {
        return vertices[1];
    }

    public Vector3 getCorner010() {
        return vertices[2];
    }

    public Vector3 getCorner011() {
        return vertices[3];
    }

    public Vector3 getCorner100() {
        return vertices[4];
    }

    public Vector3 getCorner101() {
        return vertices[5];
    }

    public Vector3 getCorner110() {
        return vertices[6];
    }

    public Vector3 getCorner111() {
        return vertices[7];
    }

    public Vector3 getMin() {
        return min;
    }

    public Vector3 getMax() {
        return max;
    }

    public Vector3[] getVertices() {
        return vertices;
    }

    public boolean intersects(Vector3 point) {
        return point.x >= min.x && point.y >= min.y && point.z >= min.z &&
                point.x <= max.x && point.y <= max.y && point.z <= max.z;
    }

    public boolean intersects(AABB aabb) {
        return min.x <= aabb.max.x &&
                max.x >= aabb.min.x &&
                min.y <= aabb.max.y &&
                max.y >= aabb.min.y &&
                min.z <= aabb.max.z &&
                max.z >= aabb.min.z;
    }

    @Override
    public boolean intersects(Sphere sphere) {
        double x = Math.max(min.x, Math.min(sphere.position.x, max.x));
        double y = Math.max(min.y, Math.min(sphere.position.y, max.y));
        double z = Math.max(min.z, Math.min(sphere.position.z, max.z));

        double dist = Math.sqrt(
                ((x - sphere.position.x) * (x - sphere.position.x)) +
                        ((y - sphere.position.y) * (y - sphere.position.y)) +
                        ((z - sphere.position.z) * (z - sphere.position.z))
        );

        return dist < sphere.radius;
    }

    @Override
    public boolean intersects(OBB obb) {
        Vector2 mmXA = getMinMax(getVertices(), (v) -> v.x);
        Vector2 mmYA = getMinMax(getVertices(), (v) -> v.y);
        Vector2 mmZA = getMinMax(getVertices(), (v) -> v.z);

        Vector2 mmXB = getMinMax(obb.getVertices(), (v) -> v.x);
        Vector2 mmYB = getMinMax(obb.getVertices(), (v) -> v.y);
        Vector2 mmZB = getMinMax(obb.getVertices(), (v) -> v.z);

        return check(mmXA, mmXB) && check(mmYA, mmYB) && check(mmZA, mmZB);
    }

    public Vector3 setMax(float x, float y, float z) {
        return this.max.set(x, y, z);
    }

    public Vector3 setMin(float x, float y, float z) {
        return this.min.set(x, y, z);
    }

    public Vector3 getCenter() {
        return center;
    }


}
