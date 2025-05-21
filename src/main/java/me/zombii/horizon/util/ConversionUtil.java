package me.zombii.horizon.util;

import com.badlogic.gdx.math.Vector3;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class ConversionUtil {

    public static Quaternion toJME(com.badlogic.gdx.math.Quaternion quaternion) {
        return new Quaternion(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
    }

    public static com.badlogic.gdx.math.Quaternion fromJME(Quaternion quaternion) {
        return new com.badlogic.gdx.math.Quaternion(quaternion.getX(), quaternion.getY(), quaternion.getZ(), quaternion.getW());
    }

    public static Vector3f toJME(Vector3 vec) {
        return new Vector3f(vec.x, vec.y, vec.z);
    }

    public static Vector3 fromJME(Vector3f vec) {
        return new Vector3(vec.x, vec.y, vec.z);
    }

    public static BoundingBox toJME(com.badlogic.gdx.math.collision.BoundingBox vec) {
        return new BoundingBox(toJME(vec.min), toJME(vec.max));
    }

    public static com.badlogic.gdx.math.collision.BoundingBox fromJME(BoundingBox vec) {
        return new com.badlogic.gdx.math.collision.BoundingBox(fromJME(vec.getMin(null)), fromJME(vec.getMax(null)));
    }

    public static BoxCollisionShape toCollisionShape(com.badlogic.gdx.math.collision.BoundingBox globalBoundingBox) {
        BoundingBox box = toJME(globalBoundingBox);

        return new BoxCollisionShape(box.getExtent(null));
    }
}
