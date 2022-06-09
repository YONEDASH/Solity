package de.yonedash.smash;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class GraphicsUtils {

    public static void rotate(Graphics2D g2d, double rotationDeg, int x, int y) {
        AffineTransform tx = g2d.getTransform();
        double rad = Math.toRadians(rotationDeg);
        tx.rotate(rad, x, y);
        g2d.setTransform(tx);
    }

}
