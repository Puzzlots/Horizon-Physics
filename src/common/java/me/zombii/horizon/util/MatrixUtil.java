package me.zombii.horizon.util;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.jme3.math.Quaternion;

public class MatrixUtil {

    public static void rotateAroundOrigin(Matrix4 transform, Vector3 pos, Vector3 angles) {
        Matrix4 pivot = new Matrix4();
        Vector3 c = new Vector3((pos.x) / 2, pos.y, (pos.z) / 2);
        pivot.setToTranslation(c);
        Matrix4 rot = new Matrix4();
        rot.setFromEulerAngles(angles.x, angles.y, angles.z);
        transform.idt();
        transform.mul(pivot);
        transform.mul(rot);
        transform.mul(pivot.inv());
        transform.setTranslation(pos);
    }

    public static void rotateAroundOrigin2(OrientedBoundingBox orientedBoundingBox, Matrix4 transform, Vector3 pos, Vector3 angles) {
        Matrix4 pivot = new Matrix4();
        Vector3 c = new Vector3((pos.x) / 2, pos.y + orientedBoundingBox.getBounds().max.y, (pos.z) / 2);
        pivot.setToTranslation(c);
        Matrix4 rot = new Matrix4();
        rot.setFromEulerAngles(angles.x, angles.y, angles.z);
        transform.idt();
        transform.mul(pivot);
        transform.mul(rot);
        transform.mul(pivot.inv());
        transform.setTranslation(pos);
    }

    public static void rotateAroundOrigin3(OrientedBoundingBox orientedBoundingBox, Matrix4 transform, Vector3 pos, Quaternion angles) {
        com.badlogic.gdx.math.Quaternion quaternion = new com.badlogic.gdx.math.Quaternion(angles.getX(), angles.getY(), angles.getZ(), angles.getW());
        Matrix4 pivot = new Matrix4();
        Vector3 c = new Vector3((pos.x) / 2, pos.y + orientedBoundingBox.getBounds().max.y, (pos.z) / 2);
        pivot.setToTranslation(c);
        Matrix4 rot = new Matrix4();
        rot.set(quaternion);
//        rot.setFromEulerAngles(angles.x, angles.y, angles.z);
//        transform.idt();
//        transform.mul(pivot);
        transform.mul(rot);
//        transform.mul(pivot.inv());
//        transform.setTranslation(pos);
        transform.mul(rot);
        transform.translate(pos);
    }

    public static void rotateAroundOrigin4(float s, Matrix4 transform, Vector3 pos, Quaternion angles) {
        com.badlogic.gdx.math.Quaternion quaternion = new com.badlogic.gdx.math.Quaternion(angles.getX(), angles.getY(), angles.getZ(), angles.getW());
        Matrix4 pivot = new Matrix4();
        Vector3 c = new Vector3((pos.x) / 2, (pos.y) / 2, (pos.z) / 2);
        pivot.setToTranslation(c);
        Matrix4 rot = new Matrix4();
        rot.set(quaternion);
//        transform.idt();
//        transform.mul(pivot);
//        transform.mul(rot);
//        transform.mul(pivot.inv());
//        transform.setTranslation(pos);
        transform.mul(rot);
        transform.translate(pos);
    }


}