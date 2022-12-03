package de.yonedash.solity.scene.components;

import de.yonedash.solity.*;
import de.yonedash.solity.config.KeyBind;
import de.yonedash.solity.scene.Scene;

import java.awt.*;

public class Button extends LocalizedComponent {

    protected Color textColor;
    protected boolean backgroundVisible, enabled;
    protected int[] textAlign;
    protected double fontScale;

    public int selected; // 0 = not selected, 1 = selected via diverse inputs, 2 = selected via mouse

    public Button(Scene scene, String key) {
        super(scene, key);
        this.textColor = Color.WHITE;
        this.textAlign = new int[] { Align.CENTER, Align.CENTER };
        this.enabled = true;
        this.fontScale = 1.0;
    }

    @Override
    public void devicePressed(KeyBind.Device device, int code) {
        if ((selected >= 2 && device == KeyBind.Device.KEYBOARD && code == '\n') // if enter is pressed
                || (selected >= 1 && device == KeyBind.Device.MOUSE && code == 1) // left mouse button is pressed
            && enabled) {
            this.scene.fireComponent(this);
            scene.instance.audioProcessor.play(scene.instance.library.clickSound);
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        int selectedBefore = this.selected;
        this.selected = enabled ? (this.backgroundVisible || this.textBounds == null ? this.bounds.contains(new Vec2D(x, y)) : this.textBounds.contains(new Vec2D(x, y))) ? 2 : 0 : 0;
        if (selectedBefore != this.selected && this.selected > 0) {
            scene.instance.audioProcessor.play(scene.instance.library.selectSound);
        }
    }

    protected Color currentTextColor;

    protected BoundingBox textBounds;

    @Override
    public void update(Graphics2D g2d, double dt) {
        Instance instance = this.scene.instance;
        Vec2D center = this.bounds.center();
        FontRenderer fontRenderer = this.scene.fontRenderer;

        Rectangle rect = new Rectangle((int) this.bounds.position.x, (int) this.bounds.position.y, (int) this.bounds.size.x, (int) this.bounds.size.y);

        g2d.setClip(rect);

        double inset = 0.0;
        if (this.backgroundVisible) {
            g2d.setColor(new Color(0, 0, 0, 70));
            g2d.fill(rect);
            g2d.setStroke(new BasicStroke(scene.scaleToDisplay(6.0)));
            g2d.setColor(enabled ? Color.WHITE : Color.GRAY);
            inset = 8.0;
            rect.x += scene.scaleToDisplay(inset);
            rect.y += scene.scaleToDisplay(inset);
            rect.width -= scene.scaleToDisplay(inset) * 2;
            rect.height -= scene.scaleToDisplay(inset) * 2;
            g2d.draw(rect);
        }

        updateColor(dt);
        drawText(g2d, dt, instance, fontRenderer, center, inset);

        g2d.setClip(null);
    }

    protected void drawText(Graphics2D g2d, double dt, Instance instance, FontRenderer fontRenderer, Vec2D center, double inset) {
        g2d.setColor(enabled ? this.currentTextColor : this.currentTextColor.darker());

        float fontSize = 60.0f;
        String text = this.scene.localize(this.key, this.localizationObjects);

        g2d.setFont(instance.lexicon.equipmentPro.deriveFont((float) this.scene.scaleToDisplay(fontScale * fontSize)));
        Vec2D textBounds;
        while ((textBounds = fontRenderer.bounds(g2d, text)).x > bounds.size.x - inset * 2 || textBounds.y > bounds.size.y - inset * 2) {
            fontSize *= 0.9;
            g2d.setFont(instance.lexicon.equipmentPro.deriveFont((float) this.scene.scaleToDisplay(fontScale * fontSize)));
        }

        this.textBounds = fontRenderer.drawStringAccurately(g2d, text, (int) (this.textAlign[0] == Align.CENTER ? center.x : this.textAlign[0] == Align.RIGHT ? this.bounds.position.x + this.bounds.size.x - this.scene.scaleToDisplay(10.0 + inset) : this.bounds.position.x + this.scene.scaleToDisplay(10.0 + inset)), (int) (this.textAlign[1] == Align.CENTER ? center.y : this.textAlign[1] == Align.BOTTOM ? this.bounds.position.y + this.bounds.size.y : this.bounds.position.y), this.textAlign[0], this.textAlign[1], true);
    }

    protected void updateColor(double dt) {
        if (this.currentTextColor == null)
            this.currentTextColor = this.textColor;
        Color targetColor = this.selected == 0 || !enabled ? this.textColor : this.textColor.brighter();
        double d = this.scene.time(0.02, dt);
        this.currentTextColor = new Color(
                (int) Math.min(Math.max(this.currentTextColor.getRed() + (targetColor.getRed() - this.currentTextColor.getRed()) * d, 0), 255),
                (int) Math.min(Math.max(this.currentTextColor.getGreen() + (targetColor.getGreen() - this.currentTextColor.getGreen()) * d, 0), 255),
                (int) Math.min(Math.max(this.currentTextColor.getBlue() + (targetColor.getBlue() - this.currentTextColor.getBlue()) * d, 0), 255)
        );

    }

    public void setBackgroundVisible(boolean backgroundVisible) {
        this.backgroundVisible = backgroundVisible;
    }

    public boolean isBackgroundVisible() {
        return backgroundVisible;
    }

    public void setTextAlign(int horizontal, int vertical) {
        this.textAlign[0] = horizontal;
        this.textAlign[1] = vertical;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setFontScale(double fontScale) {
        this.fontScale = fontScale;
    }

    public double getFontScale() {
        return fontScale;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Color getTextColor() {
        return textColor;
    }
}
