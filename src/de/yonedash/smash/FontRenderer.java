package de.yonedash.smash;

import de.yonedash.smash.scene.Scene;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

public class FontRenderer implements Align {
    private final Scene scene;
    
    public FontRenderer(Scene scene) {
        this.scene = scene;
    }

    public BoundingBox drawString(Graphics2D g2d, String text, int x, int y, int alignHorizontal, int alignVertical, boolean shadowed) {
        BoundingBox bounds = bounds(g2d, text, x, y, alignHorizontal, alignVertical);

        // if shadows enabled draw shadow
        if (shadowed) {
            Font font = g2d.getFont();
            Color color = g2d.getColor();
            g2d.setColor(new Color(0, 0, 0, g2d.getColor().getAlpha()));

            int posX = (int) bounds.position.x;
            int posY = (int) bounds.position.y;
            String line = "";
            for (String c : text.split("")) {
                line += c;
                g2d.setFont(font);
                Vec2D lineSize = bounds(g2d, line);
                Vec2D charSize = bounds(g2d, c);
                g2d.setFont(font.deriveFont(Font.BOLD).deriveFont(font.getSize2D() * 1.1f));
                Vec2D boldSize = bounds(g2d, "!Xy");
                drawString0(g2d, c, posX + (int) lineSize.x - (int) charSize.x + (int) (charSize.x / 2), posY + (int) (boldSize.y / 2), CENTER, CENTER);
            }

            g2d.setFont(font);
            g2d.setColor(color);
        }


        // draw string
        return drawString0(g2d, text, x, y, alignHorizontal, alignVertical);
    }

    private BoundingBox drawString0(Graphics2D g2d, String text, int x, int y, int alignHorizontal, int alignVertical) {
        Vec2D vb = bounds(g2d, "!Xy");
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
        return new BoundingBox(new Vec2D(x + horizontalModifier, y + verticalModifier - vb.y), new Vec2D(hb.x, vb.y));
    }


    public BoundingBox drawStringAccurately(Graphics2D g2d, String text, int x, int y, int alignHorizontal, int alignVertical, boolean shadowed) {
        BoundingBox bounds = boundsAccurately(g2d, text, x, y, alignHorizontal, alignVertical);

        // if shadows enabled draw shadow
        if (shadowed) {
            Font font = g2d.getFont();
            Color color = g2d.getColor();
            g2d.setColor(new Color(0, 0, 0, g2d.getColor().getAlpha()));

            int posX = (int) bounds.position.x;
            int posY = (int) bounds.position.y;
            String line = "";
            for (String c : text.split("")) {
                line += c;
                g2d.setFont(font);
                Vec2D lineSize = bounds(g2d, line);
                Vec2D charSize = bounds(g2d, c);
                g2d.setFont(font.deriveFont(Font.BOLD).deriveFont(font.getSize2D() * 1.1f));
                Vec2D boldSize = bounds(g2d, text);
                drawString0Accurately(g2d, c, posX + (int) lineSize.x - (int) charSize.x + (int) (charSize.x / 2), y + (int) (boldSize.y / 2), CENTER, alignVertical == Align.CENTER ? Align.BOTTOM : alignVertical == Align.TOP ? Align.CENTER : alignVertical == Align.BOTTOM ? Align.TOP : 0);
            }

            g2d.setFont(font);
            g2d.setColor(color);
        }


        // draw string
        return drawString0Accurately(g2d, text, x, y, alignHorizontal, alignVertical);
    }

    private BoundingBox drawString0Accurately(Graphics2D g2d, String text, int x, int y, int alignHorizontal, int alignVertical) {
        Vec2D vb = bounds(g2d, text);
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
        return new BoundingBox(new Vec2D(x + horizontalModifier, y + verticalModifier - vb.y), new Vec2D(hb.x, vb.y));
    }

    private BoundingBox bounds(Graphics2D g2d, String text, int x, int y, int alignHorizontal, int alignVertical) {
        Vec2D vb = bounds(g2d, "!Xy");
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
        return new BoundingBox(new Vec2D(x + horizontalModifier, y + verticalModifier - vb.y), new Vec2D(hb.x, vb.y));
    }


    private BoundingBox boundsAccurately(Graphics2D g2d, String text, int x, int y, int alignHorizontal, int alignVertical) {
        Vec2D vb = bounds(g2d, text);
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
        return new BoundingBox(new Vec2D(x + horizontalModifier, y + verticalModifier - vb.y), new Vec2D(hb.x, vb.y));
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
