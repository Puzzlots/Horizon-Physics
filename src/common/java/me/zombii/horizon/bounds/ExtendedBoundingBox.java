package me.zombii.horizon.bounds;

import com.badlogic.gdx.math.collision.OrientedBoundingBox;

public interface ExtendedBoundingBox {

    boolean hasInnerBounds();
    OrientedBoundingBox getInnerBounds();
    void setInnerBounds(OrientedBoundingBox boundingBox);

}
