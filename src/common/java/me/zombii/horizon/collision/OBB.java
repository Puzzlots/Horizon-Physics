package me.zombii.horizon.collision;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.function.Function;

public class OBB implements Bounds {

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

    public OBB(
            AABB aabb
    ) {
        this(aabb.min, aabb.max);
    }

    public OBB(
            float x, float y, float z,
            float width, float height, float depth
    ) {
        vertices[0].set(x, y, z);
        vertices[1].set(x, y, z + depth);
        vertices[2].set(x, y + height, z);
        vertices[3].set(x, y + height, z + depth);
        vertices[4].set(x + width, y, z);
        vertices[5].set(x + width, y, z + depth);
        vertices[6].set(x + width, y + height, z);
        vertices[7].set(x + width, y + height, z + depth);
    }

    public OBB(
            Vector3 min,
            Vector3 max
    ) {
        vertices[0].set(min.x, min.y, min.z);
        vertices[1].set(min.x, min.y, max.z);
        vertices[2].set(min.x, max.y, min.z);
        vertices[3].set(min.x, max.y, max.z);
        vertices[4].set(max.x, min.y, min.z);
        vertices[5].set(max.x, min.y, max.z);
        vertices[6].set(max.x, max.y, min.z);
        vertices[7].set(max.x, max.y, max.z);
    }

    public OBB(
            float Xc000, float Yc000, float Zc000,
            float Xc001, float Yc001, float Zc001,
            float Xc010, float Yc010, float Zc010,
            float Xc011, float Yc011, float Zc011,
            float Xc100, float Yc100, float Zc100,
            float Xc101, float Yc101, float Zc101,
            float Xc110, float Yc110, float Zc110,
            float Xc111, float Yc111, float Zc111
    ) {
        vertices[0].set(Xc000, Yc000, Zc000);
        vertices[1].set(Xc001, Yc001, Zc001);
        vertices[2].set(Xc010, Yc010, Zc010);
        vertices[3].set(Xc011, Yc011, Zc011);
        vertices[4].set(Xc100, Yc100, Zc100);
        vertices[5].set(Xc101, Yc101, Zc101);
        vertices[6].set(Xc110, Yc110, Zc110);
        vertices[7].set(Xc111, Yc111, Zc111);
    }

    public OBB(
            Vector3 c000,
            Vector3 c001,
            Vector3 c010,
            Vector3 c011,
            Vector3 c100,
            Vector3 c101,
            Vector3 c110,
            Vector3 c111
    ) {
        vertices[0].set(c000);
        vertices[1].set(c001);
        vertices[2].set(c010);
        vertices[3].set(c011);
        vertices[4].set(c100);
        vertices[5].set(c101);
        vertices[6].set(c110);
        vertices[7].set(c111);
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

    public Vector3[] getVertices() {
        return vertices;
    }

    public void transform(Matrix3 mat) {
        for (Vector3 vertex : vertices)
            vertex.mul(mat);
    }

    public void transform(Matrix4 mat) {
        for (Vector3 vertex : vertices)
            vertex.mul(mat);
    }

    @Override
    public boolean intersects(Vector3 point) {
        Vector2 mmXA = getMinMax(getVertices(), (v) -> v.x);
        Vector2 mmYA = getMinMax(getVertices(), (v) -> v.y);
        Vector2 mmZA = getMinMax(getVertices(), (v) -> v.z);

        return check(mmXA, point.x) && check(mmYA, point.y) && check(mmZA, point.z);
    }

    @Override
    public boolean intersects(AABB aabb) {
        Vector2 mmXA = getMinMax(getVertices(), (v) -> v.x);
        Vector2 mmYA = getMinMax(getVertices(), (v) -> v.y);
        Vector2 mmZA = getMinMax(getVertices(), (v) -> v.z);

        Vector2 mmXB = getMinMax(aabb.getVertices(), (v) -> v.x);
        Vector2 mmYB = getMinMax(aabb.getVertices(), (v) -> v.y);
        Vector2 mmZB = getMinMax(aabb.getVertices(), (v) -> v.z);

        return check(mmXA, mmXB) && check(mmYA, mmYB) && check(mmZA, mmZB);
    }

    @Override
    public boolean intersects(Sphere sphere) {
        Vector2 mmX = getMinMax(getVertices(), (v) -> v.x);
        Vector2 mmY = getMinMax(getVertices(), (v) -> v.y);
        Vector2 mmZ = getMinMax(getVertices(), (v) -> v.z);

        double x = Math.max(mmX.y, Math.min(sphere.position.x, mmX.x));
        double y = Math.max(mmY.y, Math.min(sphere.position.y, mmY.x));
        double z = Math.max(mmZ.y, Math.min(sphere.position.z, mmZ.x));

        double dist = Math.sqrt(
                ((x - sphere.position.x) * (x - sphere.position.x)) +
                        ((y - sphere.position.y) * (y - sphere.position.y)) +
                        ((z - sphere.position.z) * (z - sphere.position.z))
        );

        return dist < sphere.radius;
    }

    public boolean intersects(OBB obb) {
        Vector2 mmXA = getMinMax(getVertices(), (v) -> v.x);
        Vector2 mmYA = getMinMax(getVertices(), (v) -> v.y);
        Vector2 mmZA = getMinMax(getVertices(), (v) -> v.z);

        Vector2 mmXB = getMinMax(obb.getVertices(), (v) -> v.x);
        Vector2 mmYB = getMinMax(obb.getVertices(), (v) -> v.y);
        Vector2 mmZB = getMinMax(obb.getVertices(), (v) -> v.z);

        return check(mmXA, mmXB) && check(mmYA, mmYB) && check(mmZA, mmZB);
    }

    public static boolean check(Vector2 a, Vector2 b) {
        return a.y <= b.x && a.y >= b.x;
    }

    public static boolean check(Vector2 a, float point) {
        return a.x >= point && a.y <= point;
    }

    public static Vector2 getMinMax(Vector3[] vertices, Function<Vector3, Float> c) {
        float hx = c.apply(vertices[0]);
        float lx = c.apply(vertices[0]);

        for (int i = 1; i < vertices.length; i++) {
            float tmp = c.apply(vertices[i]);

            if (tmp > hx) hx = tmp;
            if (tmp < lx) lx = tmp;
        }

        return new Vector2(lx, hx);
    }

}
