package de.yonedash.smash.graphics;

import de.yonedash.smash.OpenSimplexNoise;
import de.yonedash.smash.resource.Texture;
import de.yonedash.smash.graphics.TextureAtlas;
import de.yonedash.smash.graphics.TextureStatic;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class GraphicsUtils {

    public static void rotate(Graphics2D g2d, double rotationDeg, int x, int y) {
        AffineTransform tx = g2d.getTransform();
        double rad = Math.toRadians(rotationDeg);
        tx.rotate(rad, x, y);
        g2d.setTransform(tx);
    }

    public static void setAlpha(Graphics2D g2d, float alpha) {
        AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    Math.max(Math.min(alpha, 1.0f), 0.0f));
        g2d.setComposite(alphaComposite);
    }

}
