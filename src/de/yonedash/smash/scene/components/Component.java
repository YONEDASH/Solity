package de.yonedash.smash.scene.components;

import de.yonedash.smash.BoundingBox;
import de.yonedash.smash.Vec2D;
import de.yonedash.smash.config.KeyBind;
import de.yonedash.smash.scene.Scene;

import java.awt.*;

public abstract class Component {

    protected final Scene scene;

    protected boolean isVisible;
    protected BoundingBox bounds;

    public Component(Scene scene) {
        this.scene = scene;
        this.bounds = new BoundingBox(Vec2D.zero(), Vec2D.zero());
    }

    public abstract void update(Graphics2D g2d, double dt);

    public void setBounds(BoundingBox bounds) {
        this.bounds = bounds;
    }

    public void setBounds(double x, double y, double width, double height) {
        this.bounds.position.x = x;
        this.bounds.position.y = y;
        this.bounds.size.x = width;
        this.bounds.size.y = height;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    // Dummy methods down here, can be used in different screen to react to user input

    public void devicePressed(KeyBind.Device device, int code) {

    }

    public void deviceReleased(KeyBind.Device device, int code) {

    }

    public void mouseWheelMoved(int x, int y, double amount) {

    }

    public void mouseDragged(int x, int y, int button) {

    }

    public void mouseMoved(int x, int y) {

    }
}
