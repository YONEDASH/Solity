package de.yonedash.smash.entity;

import de.yonedash.smash.BoundingBox;
import de.yonedash.smash.Scene;
import de.yonedash.smash.graphics.LightSource;

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
