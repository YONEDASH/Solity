package de.yonedash.solity;

import de.yonedash.solity.config.KeyBind;

import java.awt.event.*;
import java.util.ArrayList;

public class Input {

    private final Instance instance;
    private final Display display;

    private final ArrayList<Integer> keysDown;
    private final ArrayList<Integer> buttonsDown;
    private final KeyAdapter keyboardAdapter;
    private final MouseAdapter mouseAdapter;

    protected Input(Instance instance, Display display) {
        // Store instance & display since we will need it
        this.instance = instance;
        this.display = display;

        // Create pressed/down lists
        this.keysDown = new ArrayList<>();
        this.buttonsDown = new ArrayList<>();

        // Initialize input adapters
        this.keyboardAdapter = new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                // If key is not pressed add it to the keysDown list
                if (!keysDown.contains(e.getKeyCode())) {
                    keysDown.add(e.getKeyCode());
                }

                // todo Fullscreen key binds
                if (e.isAltDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Input.this.display.setFullscreen(0, !Input.this.display.isFullscreen());
                }

                // Call corresponding scene function
                Input.this.instance.scene.keyPressed(e.getKeyCode());
                Input.this.instance.scene.devicePressed(KeyBind.Device.KEYBOARD, e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // If key is pressed remove it from the keysDown list
                keysDown.removeIf(k -> k == e.getKeyCode());

                // Call corresponding scene function
                Input.this.instance.scene.keyReleased(e.getKeyCode());
                Input.this.instance.scene.deviceReleased(KeyBind.Device.KEYBOARD, e.getKeyCode());
            }
        };

        this.mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // If button is not pressed add it to the buttonsDown list
                if (!buttonsDown.contains(e.getButton())) {
                    buttonsDown.add(e.getButton());
                }

                // Call corresponding scene function
                Input.this.instance.scene.mousePressed(e.getX(), e.getY(), e.getButton());
                Input.this.instance.scene.devicePressed(KeyBind.Device.MOUSE, e.getButton());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // If button is pressed remove it from the buttonsDown list
                buttonsDown.removeIf(b -> b == e.getButton());

                // Call corresponding scene function
                Input.this.instance.scene.mouseReleased(e.getX(), e.getY(), e.getButton());
                Input.this.instance.scene.deviceReleased(KeyBind.Device.MOUSE, e.getButton());
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Call corresponding scene function
                Input.this.instance.scene.mouseWheelMoved(e.getX(), e.getY(), e.getPreciseWheelRotation());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // Call corresponding scene function
                Input.this.instance.scene.mouseDragged(e.getX(), e.getY(), e.getButton());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // Call corresponding scene function
                Input.this.instance.scene.mouseMoved(e.getX(), e.getY());
            }
        };
    }

    // Returns whether a devices button is pressed
    public boolean isDeviceButtonPressed(KeyBind.Device device, int code) {
        if (device == KeyBind.Device.KEYBOARD)
            return keysDown.contains(code);
        else if (device == KeyBind.Device.MOUSE)
            return buttonsDown.contains(code);
        return false;
    }

    public boolean isBindPressed(KeyBind bind) {
        return isDeviceButtonPressed(bind.getDevice(), bind.getCode());
    }

    private boolean listening;

    protected void listen() {
        // Do not run code if already listening
        if (listening)
            return;

        // Add listeners to canvas in order for input events to fire

        // Keyboard
        this.display.canvas.addKeyListener(keyboardAdapter);

        // Mouse
        this.display.canvas.addMouseListener(mouseAdapter);
        this.display.canvas.addMouseMotionListener(mouseAdapter);
        this.display.canvas.addMouseWheelListener(mouseAdapter);

        // Update state
        listening = true;
    }

    protected void ignore() {
        // Do not run code if already ignoring
        if (!listening)
            return;


        // Remove listeners

        // Keyboard
        this.display.canvas.removeKeyListener(keyboardAdapter);

        // Mouse
        this.display.canvas.removeMouseListener(mouseAdapter);
        this.display.canvas.removeMouseMotionListener(mouseAdapter);
        this.display.canvas.removeMouseWheelListener(mouseAdapter);

        // Update state
        listening = false;
    }

}
