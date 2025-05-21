package me.zombii.horizon.rendering;

import com.badlogic.gdx.math.Vector3;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public interface IShapeRenderer {

    List<Vector3> points = new CopyOnWriteArrayList<>();

    static void queuePoint(Vector3 v) {
        points.add(v);
    }

}
