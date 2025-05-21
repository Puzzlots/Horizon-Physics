package me.zombii.horizon.rendering.mesh;

import com.badlogic.gdx.math.collision.BoundingBox;
import me.zombii.horizon.collision.AABB;

public interface IBlockBoundsMaker {

    AABB[] getBounds();
    AABB[] getBounds(int bx, int by, int bz);

}
