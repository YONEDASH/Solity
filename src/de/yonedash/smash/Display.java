package de.yonedash.smash;

import de.yonedash.smash.graphics.GraphicsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

// This class represents the game window
public class Display extends JFrame {

    private final Instance instance;

    // We want protected access to this class in order to access it in the canvas class
    protected final Canvas canvas;

    private final Input input;

    public Display(Instance instance) {
        // Save variable for game instance
        this.instance = instance;

        // Set display dimensions
        super.setSize(1280, 720);

        // Reset relative position
        this.setLocationRelativeTo(null);

        // Set title
        super.setTitle("Solity renderPipeline=" + instance.launchData.getRenderPipeline().name());

        // Set close operation, we want program to exit when display window is closed
        super.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Stop game instance with status code zero (= stop on purpose)
                instance.stop(0);
            }
        });

        // Create canvas instance (which we will draw on)
        this.canvas = new Canvas();
        // Set canvas color to black
        this.canvas.setBackground(Color.BLACK);
        // Add canvas to window
        super.add(this.canvas);

        // In order for canvas to work properly, we need to manage the buffer
        // strategy based on the visibility of this window
        super.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Ignore
            }

            @Override
            public void componentShown(ComponentEvent e) {
                // Create buffer strategy after window was shown
                Display.this.canvas.createBufferStrategy(2);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                // Dispose buffer strategy after window was hidden
                Display.this.canvas.getBufferStrategy().dispose();
            }
        });

        // Create input instance, used for user input
        this.input = new Input(instance, this);
        // We only want to listen to user input if the window is focused
        // in order to prevent any confusion & bugs
        this.canvas.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // Add input listeners if window is in focus
                Display.this.input.listen();
            }

            @Override
            public void focusLost(FocusEvent e) {
                // Remove input listeners if window is not in focus
                Display.this.input.ignore();
            }
        });
    }

    // Returns Input object
    public Input getInput() {
        return input;
    }

    // Represents the device this window is currently on fullscreen
    private GraphicsDevice fullscreenDevice;

    // Sets this window on fullscreen on graphics device/monitor/"screen" n or turns fullscreen off
    // Returns if set fullscreen attempt was successful
    public boolean setFullscreen(int n, boolean state) {
        // Get local graphics environment to access device array
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();

        // Check whether fullscreen should be turned on
        if (state) {
            // Get screen device array
            GraphicsDevice[] screens = environment.getScreenDevices();

            // Cancel operation if screen n is out of bounds
            if (n < 0 || n >= screens.length)
                return false;

            // Get target screen from array
            GraphicsDevice target = screens[n];

            if (this.fullscreenDevice != null) {
                // If we already on fullscreen
                if (this.fullscreenDevice == target) {
                    // If this window is already on fullscreen, goal is already achieved
                    return true;
                }

                // If window is not on fullscreen on target screen, turn off fullscreen
                this.fullscreenDevice.setFullScreenWindow(null);
            }

            // Set fullscreen device to target
            this.fullscreenDevice = target;

            // Turn on fullscreen
            this.fullscreenDevice.setFullScreenWindow(this);

        } else {
            if (this.fullscreenDevice == null) {
                // If fullscreen device does not exist, cancel, goal already achieved
                return false;
            }

            // Turn off fullscreen on device
            this.fullscreenDevice.setFullScreenWindow(null);

            // Set fullscreen device to null
            this.fullscreenDevice = null;

        }
        return true;
    }

    // Returns whether window is on fullscreen
    public boolean isFullscreen() {
        // Checks if fullscreen device exists and this window currently is the fullscreen window
        return this.fullscreenDevice != null && this.fullscreenDevice.getFullScreenWindow() == this;
    }

    // Stores time display was last updated
    private long timeLastUpdated;

    // Hides cursor
    public void hideCursor() {
        // Create a new cursor with transparent image
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");

        // Set cursor to display
        this.setCursor(blankCursor);
    }

    // Shows cursor
    public void showCursor() {
        this.setCursor(Cursor.getDefaultCursor());
    }

    // Updates display
    public boolean update() {
        // Current time
        long timeNow = System.nanoTime();
        // Calculate time delta
        long dt = timeNow - timeLastUpdated;

        // Cap framerate to fps limit (exception: cap=0 -> unlimited frames)
        if (Constants.FPS_LIMIT > 0
                && dt <= 1_000_000 * (1000.0 / Constants.FPS_LIMIT))
            return false;

        // Cap framerate if game on fullscreen to prevent tearing
        if (isFullscreen()
                && dt <= 1_000_000 * (1000.0 / this.fullscreenDevice.getDisplayMode().getRefreshRate()))
            return false;

        // Update update-time
        this.timeLastUpdated = System.nanoTime();

        // Skip render if display is not visible
        if (!isVisible())
            return false;

        // Get buffer strategy
        BufferStrategy buffer = this.canvas.getBufferStrategy();

        // Skip render if buffer does not exist
        if (buffer == null)
            return false;

        // Get graphics object
        Graphics2D g2d = (Graphics2D) buffer.getDrawGraphics();

        // Clear screen
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Update texture atlas
        this.instance.atlas.update(dt / 1_000_000.0);

        // Update scene
        try {
            this.instance.scene.update(g2d, dt / 1_000_000.0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            return false;
        }


        // Show buffer to display
        buffer.show();

        return true;
    }

}
