package de.yonedash.smash;

import java.awt.*;

public abstract class Scene {

    // Instance access in screen children
    public final Instance instance;

    // Access to font renderer in screen children
    protected final FontRenderer fontRenderer;

    // Scales scale
    protected double scaleFactor = 1.0;

    public Scene(Instance instance) {
        this.instance = instance;
        this.fontRenderer = new FontRenderer(this);
    }

    // Easy access to display
    public Display getDisplay() {
        return this.instance.display;
    }

    // Abstract function, forces children of this class to write their own implementation
    // Graphics2D for drawing to the screen, dt as delta-time for game logic
    public abstract void update(Graphics2D g2d, double dt);

    public double calculateDisplayScaleFactor() {
        // Get display width & height
        Display display = this.getDisplay();
        int displayWidth = display.getWidth(), displayHeight = display.getHeight();
        // Calculate scale factor
        double factor = ((displayWidth / (double) Constants.SCALE_WIDTH)
                + (displayHeight / (double) Constants.SCALE_HEIGHT)) / 2.0;

        // Drop accuracy to a 10th
        return Math.round(factor * this.scaleFactor * Constants.DISPLAY_SCALE * 0.5 * 10.0) / 10.0;
    }

    // Scale value to display dimension-scale
    public int scaleToDisplay(double d) {
        // Scale value
        return (int) Math.floor(d * calculateDisplayScaleFactor());
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

    // Transfers delta time to milliseconds and multiplies them with value
    // For example this results in constant movement speeds independent of the frame rate
    public double time(double val, double dt) {
        return val * Math.min(100.0, dt);//* 0.0000001;
    }

    public float time(float val, double dt) {
        return val * (float) Math.min(100f, dt);//* 0.0000001f;
    }

    // Dummy methods down here, can be used in different screen to react to user input

    public void keyPressed(int key) {

    }

    public void keyReleased(int key) {

    }

    public void mousePressed(int x, int y, int button) {

    }

    public void mouseReleased(int x, int y, int button) {

    }

    public void mouseWheelMoved(int x, int y, double amount) {

    }

    public void mouseDragged(int x, int y, int button) {

    }

    public void mouseMoved(int x, int y) {

    }

}
