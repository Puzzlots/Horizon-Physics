package me.zombii.horizon.collision;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;

public class Collision3D {

    public static Vector3 getCollisionResponse(BoundingBox aabb, OrientedBoundingBox obb) {
        Vector3 mtv = new Vector3();
        float minOverlap = Float.MAX_VALUE;

        // AABB axes
        Vector3[] aabbAxes = {
                new Vector3(1, 0, 0),
                new Vector3(0, 1, 0),
                new Vector3(0, 0, 1)
        };

        // OBB axes from transform
        Vector3[] obbAxes = getOBBAxes(obb);

        // Candidate axes
        Vector3[] axes = new Vector3[15];
        int index = 0;

        // AABB axes
        for (int i = 0; i < 3; i++) axes[index++] = aabbAxes[i];

        // OBB axes
        for (int i = 0; i < 3; i++) axes[index++] = obbAxes[i];

        // Cross products
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Vector3 cross = aabbAxes[i].cpy().crs(obbAxes[j]);
                if (cross.len2() > 1e-6f) {
                    axes[index++] = cross.nor();
                }
            }
        }

        // Get corners
        Vector3[] aabbCorners = getAABBCorners(aabb);
        Vector3[] obbCorners = getOBBCorners(obb);

        // SAT
        for (int i = 0; i < index; i++) {
            Vector3 axis = axes[i];
            float[] projAABB = project(aabbCorners, axis);
            float[] projOBB = project(obbCorners, axis);

            float overlap = getOverlap(projAABB[0], projAABB[1], projOBB[0], projOBB[1]);
            if (overlap <= 0) {
                return Vector3.Zero; // no collision
            } else {
                mtv = chooseMTV(axis, overlap, mtv, minOverlap, new Vector3(0, 1, 0));
                if (overlap < minOverlap) {
                    minOverlap = overlap;
                }
            }
        }

        // Orient MTV from AABB → OBB
        Vector3 aabbCenter = aabb.getCenter(new Vector3());
        Vector3 obbCenter = new Vector3().set(obb.transform.getTranslation(new Vector3()));
        Vector3 diff = obbCenter.sub(aabbCenter);
        if (diff.dot(mtv) < 0) {
            mtv.scl(-1);
        }

        return mtv;
    }

    private static Vector3 chooseMTV(Vector3 candidateAxis, float overlap,
                                     Vector3 currentMTV, float minOverlap,
                                     Vector3 upAxis) {
        if (overlap < minOverlap) {
            // Prefer up-axis if overlap is "close enough" to the best
            if (currentMTV != null && upAxis.dot(candidateAxis) > 0.9f) {
                if (Math.abs(overlap - minOverlap) < 0.05f * minOverlap) {
                    return upAxis.cpy().scl(overlap);
                }
            }
            return candidateAxis.cpy().scl(overlap);
        }
        return currentMTV;
    }

    private static Vector3[] getAABBCorners(BoundingBox box) {
        Vector3 min = box.min;
        Vector3 max = box.max;
        return new Vector3[]{
                new Vector3(min.x, min.y, min.z),
                new Vector3(max.x, min.y, min.z),
                new Vector3(min.x, max.y, min.z),
                new Vector3(max.x, max.y, min.z),
                new Vector3(min.x, min.y, max.z),
                new Vector3(max.x, min.y, max.z),
                new Vector3(min.x, max.y, max.z),
                new Vector3(max.x, max.y, max.z),
        };
    }

    private static Vector3[] getOBBCorners(OrientedBoundingBox obb) {
        // Local half extents
        Vector3 halfExtents = obb.getBounds().getDimensions(new Vector3()).scl(0.5f);

        // Local center
        Vector3 localCenter = obb.getBounds().getCenter(new Vector3());

        // Axes in world space
        Vector3[] axes = getOBBAxes(obb);
        Vector3 ex = axes[0].cpy().scl(halfExtents.x);
        Vector3 ey = axes[1].cpy().scl(halfExtents.y);
        Vector3 ez = axes[2].cpy().scl(halfExtents.z);

        // World center = transform(local center)
        Vector3 c = localCenter.prj(obb.transform);

        return new Vector3[]{
                c.cpy().add(ex).add(ey).add(ez),
                c.cpy().add(ex).add(ey).sub(ez),
                c.cpy().add(ex).sub(ey).add(ez),
                c.cpy().add(ex).sub(ey).sub(ez),
                c.cpy().sub(ex).add(ey).add(ez),
                c.cpy().sub(ex).add(ey).sub(ez),
                c.cpy().sub(ex).sub(ey).add(ez),
                c.cpy().sub(ex).sub(ey).sub(ez),
        };
    }

    private static Vector3[] getOBBAxes(OrientedBoundingBox obb) {
        Matrix4 m = obb.transform;
        Vector3[] axes = new Vector3[3];

        // Extract orientation basis (column vectors of rotation)
        axes[0] = new Vector3(m.val[Matrix4.M00], m.val[Matrix4.M01], m.val[Matrix4.M02]).nor(); // X
        axes[1] = new Vector3(m.val[Matrix4.M10], m.val[Matrix4.M11], m.val[Matrix4.M12]).nor(); // Y
        axes[2] = new Vector3(m.val[Matrix4.M20], m.val[Matrix4.M21], m.val[Matrix4.M22]).nor(); // Z

        return axes;
    }

    private static float[] project(Vector3[] points, Vector3 axis) {
        float min = points[0].dot(axis);
        float max = min;
        for (int i = 1; i < points.length; i++) {
            float p = points[i].dot(axis);
            if (p < min) min = p;
            if (p > max) max = p;
        }
        return new float[]{min, max};
    }

    private static float getOverlap(float minA, float maxA, float minB, float maxB) {
        return Math.min(maxA, maxB) - Math.max(minA, minB);
    }
}
