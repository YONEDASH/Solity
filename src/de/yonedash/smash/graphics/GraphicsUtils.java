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

    public static Texture createNoiseTexture(OpenSimplexNoise noise, double size, TextureAtlas atlas, int w, int h) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < w; y++)
        {
            for (int x = 0; x < h; x++)
            {
                double value = noise.eval(x / size, y / size);
                int rgb = 0x010101 * (int)((value + 1) * 127.5);
                image.setRGB(x, y, rgb);
            }
        }

        TextureStatic textureStatic = new TextureStatic(atlas, image);
        return textureStatic;
    }

}
