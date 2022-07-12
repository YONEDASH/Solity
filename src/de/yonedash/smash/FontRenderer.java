package de.yonedash.smash;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

public class FontRenderer {

    public static final int CENTER = 0, LEFT = 1, TOP = 2, RIGHT = 3, BOTTOM = 4;
    private final Scene scene;
    
    public FontRenderer(Scene scene) {
        this.scene = scene;
    }

    public BoundingBox drawString(Graphics2D g2d, String text, int x, int y, int alignHorizontal, int alignVertical, boolean shadowed) {
        // if shadows enabled draw shadow
        if (shadowed) {
            final Color currentColor = g2d.getColor();
            g2d.setColor(new Color(0, 0, 0, currentColor.getAlpha()));
            int offset = this.scene.scaleToDisplay(Math.max(2, g2d.getFontMetrics().getHeight() * 0.025) / this.scene.scaleFactor);
            drawString0(g2d, text, x + offset, y + offset, alignHorizontal, alignVertical);
            g2d.setColor(currentColor);
        }

        // draw string
        return drawString0(g2d, text, x, y, alignHorizontal, alignVertical);
    }

    private BoundingBox drawString0(Graphics2D g2d, String text, int x, int y, int alignHorizontal, int alignVertical) {
        Vec2D vb = bounds(g2d, "!X,");
        double verticalModifier = switch(alignVertical) {
            case CENTER -> vb.y * 0.5;
            case BOTTOM -> 0;
            case TOP -> +vb.y;
            default -> throw new IllegalArgumentException("Unexpected vertical align");
        };
        Vec2D hb = bounds(g2d, text);
        double horizontalModifier = switch(alignHorizontal) {
            case CENTER -> -hb.x / 2;
            case LEFT -> 0;
            case RIGHT -> -hb.x;
            default -> throw new IllegalArgumentException("Unexpected horizontal align");
        };
        g2d.drawString(text, x + (int) horizontalModifier, y + (int) verticalModifier);
        return new BoundingBox(new Vec2D(x, y), hb);
    }

    public Vec2D bounds(Graphics2D g2d, String text) {
        FontRenderContext context = g2d.getFontRenderContext();
        GlyphVector vector = g2d.getFont().createGlyphVector(context, text);
        Rectangle2D r2d = vector.getVisualBounds();
        BoundingBox boundingBox = new BoundingBox(
                new Vec2D(r2d.getX(), r2d.getY()), new Vec2D(r2d.getWidth(), r2d.getHeight())
        );
        Vec2D bounds = boundingBox.abs();
        bounds.y *= 0.5;
        return bounds;
    }

}
