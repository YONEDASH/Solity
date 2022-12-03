package de.yonedash.solity.scene;

import de.yonedash.solity.*;
import de.yonedash.solity.config.KeyBind;
import de.yonedash.solity.scene.components.Component;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Scene {

    // Instance access in screen children
    public final Instance instance;

    // Access to font renderer in screen children
    public final FontRenderer fontRenderer;

    // Scales scale
    protected double scaleFactor = 1.0;

    // Components
    protected final CopyOnWriteArrayList<Component> components;

    public Scene(Instance instance) {
        this.instance = instance;
        this.fontRenderer = new FontRenderer(this);
        this.components = new CopyOnWriteArrayList<>();
    }

    // Easy access to display
    public Display getDisplay() {
        return this.instance.display;
    }

    // Abstract function, forces children of this class to write their own implementation
    // Graphics2D for drawing to the screen, dt as delta-time for game logic
    public abstract void update(Graphics2D g2d, double dt);

    protected void updateComponents(Graphics2D g2d, double dt) {
        this.components.forEach(component -> component.update(g2d, dt));
    }

    public double calculateDisplayScaleFactor() {
        // Get display width & height
        Display display = this.getDisplay();
        int displayWidth = display.getWidth(), displayHeight = display.getHeight();
        // Calculate scale factor
        double factor = ((displayWidth / (double) Constants.SCALE_WIDTH)
                + (displayHeight / (double) Constants.SCALE_HEIGHT)) / 2.0;

        // Calculate scale factor and drop accuracy to a 10th by rounding and dividing
        return Math.round(factor * this.scaleFactor * Constants.DISPLAY_SCALE * 0.5 * 10.0) / 10.0;
    }

    // Scale value to display dimension-scale
    public int scaleToDisplay(double d) {
        // Scale value
        return (int) Math.round(d * calculateDisplayScaleFactor());
    }

    // Scales Vec2D to display dimension-scale
    public Vec2D createScaledToDisplay(Vec2D vec2D) {
        double d = calculateDisplayScaleFactor();
        return new Vec2D(vec2D.x * d, vec2D.y * d);
    }

    // Scales BoundingBox to display dimension-scale
    public BoundingBox createScaledToDisplay(BoundingBox boundingBox) {
        return new BoundingBox(createScaledToDisplay(boundingBox.position), createScaledToDisplay(boundingBox.size));
    }

    // (Transfers delta time to milliseconds and) multiplies them with value
    // For example this results in constant movement speeds independent of the frame rate
    public double time(double val, double dt) {
        return val * Math.min(100.0, dt);//(* 0.0000001);
    }

    public float time(float val, double dt) {
        return val * (float) Math.min(100f, dt);//* 0.0000001f;
    }

    // Localize text
    public String localize(String key, Object... args) {
        return this.instance.gameConfig.language.getProvider().localize(key, args);
    }

    // Dummy methods down here, can be used in different screen to react to user input

    public void devicePressed(KeyBind.Device device, int code) {
        this.components.forEach(component -> component.devicePressed(device, code));
    }

    public void deviceReleased(KeyBind.Device device, int code) {
        this.components.forEach(component -> component.deviceReleased(device, code));
    }

    public void keyPressed(int key) {

    }

    public void keyReleased(int key) {

    }

    public void mousePressed(int x, int y, int button) {

    }

    public void mouseReleased(int x, int y, int button) {

    }

    public void mouseWheelMoved(int x, int y, double amount) {
        this.components.forEach(component -> component.mouseWheelMoved(x, y, amount));
    }

    public void mouseDragged(int x, int y, int button) {
        this.components.forEach(component -> component.mouseDragged(x, y, button));
    }

    public void mouseMoved(int x, int y) {
        this.components.forEach(component -> component.mouseMoved(x, y));
    }

    public void fireComponent(Component component) {

    }
}
