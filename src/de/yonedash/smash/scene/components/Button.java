package de.yonedash.smash.scene.components;

import de.yonedash.smash.*;
import de.yonedash.smash.config.KeyBind;
import de.yonedash.smash.scene.Scene;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Button extends LocalizedComponent {

    protected Color textColor;
    protected boolean backgroundVisible;
    protected int[] textAlign;

    protected int selected; // 0 = not selected, 1 = selected via diverse inputs, 2 = selected via mouse

    public Button(Scene scene, String key) {
        super(scene, key);
        this.textColor = Color.WHITE;
        this.textAlign = new int[] { Align.CENTER, Align.CENTER };
    }

    @Override
    public void devicePressed(KeyBind.Device device, int code) {
        if ((selected >= 2 && device == KeyBind.Device.KEYBOARD && code == '\n') // if enter is pressed
                || (selected >= 1 && device == KeyBind.Device.MOUSE && code == 1)) { // left mouse button is pressed
            this.scene.fireComponent(this);
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        this.selected = (this.backgroundVisible || this.textBounds == null ? this.bounds.contains(new Vec2D(x, y)) : this.textBounds.contains(new Vec2D(x, y))) ? 2 : 0;
    }

    private Color currentTextColor;

    protected BoundingBox textBounds;

    @Override
    public void update(Graphics2D g2d, double dt) {
        Instance instance = this.scene.instance;
        Vec2D center = this.bounds.center();
        FontRenderer fontRenderer = this.scene.fontRenderer;

        Rectangle rect = new Rectangle((int) this.bounds.position.x, (int) this.bounds.position.y, (int) this.bounds.size.x, (int) this.bounds.size.y);

        g2d.setClip(rect);

        if (this.backgroundVisible) {
            g2d.setColor(Color.MAGENTA);
            g2d.draw(rect);
        }

        if (this.currentTextColor == null)
            this.currentTextColor = this.textColor;
        Color targetColor = this.selected == 0 ? this.textColor : this.textColor.brighter();
        double d = this.scene.time(0.02, dt);
        this.currentTextColor = new Color(
                (int) Math.min(Math.max(this.currentTextColor.getRed() + (targetColor.getRed() - this.currentTextColor.getRed()) * d, 0), 255),
                (int) Math.min(Math.max(this.currentTextColor.getGreen() + (targetColor.getGreen() - this.currentTextColor.getGreen()) * d, 0), 255),
                (int) Math.min(Math.max(this.currentTextColor.getBlue() + (targetColor.getBlue() - this.currentTextColor.getBlue()) * d, 0), 255)
        );

        g2d.setColor(this.currentTextColor);
        g2d.setFont(instance.lexicon.equipmentPro.deriveFont((float) this.scene.scaleToDisplay(60.0)));
        this.textBounds = fontRenderer.drawStringAccurately(g2d, this.scene.localize(this.key, this.localizationObjects), (int) (this.textAlign[0] == Align.CENTER ? center.x : this.textAlign[0] == Align.RIGHT ? this.bounds.position.x + this.bounds.size.x - this.scene.scaleToDisplay(10.0) : this.bounds.position.x + this.scene.scaleToDisplay(10.0)), (int) (this.textAlign[1] == Align.CENTER ? center.y : this.textAlign[1] == Align.BOTTOM ? this.bounds.position.y + this.bounds.size.y : this.bounds.position.y), this.textAlign[0], this.textAlign[1], true);

        g2d.setClip(null);
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
}
