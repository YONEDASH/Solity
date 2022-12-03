package de.yonedash.solity.entity;

import de.yonedash.solity.BoundingBox;
import de.yonedash.solity.scene.Scene;
import de.yonedash.solity.graphics.LightSource;

import java.awt.*;

public interface DisplayEntity {

    BoundingBox getBoundingBox();
    void draw(Scene scene, Graphics2D g2d, double dt);
    int getY();
    int getZ();
    LightSource getLightSource();

    default boolean doesEmitLight() {
        return getLightSource() != null;
    }

}
