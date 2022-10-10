package de.yonedash.smash;

import kuusisto.tinysound.TinySound;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

// This class is responsible for the game update loop
public class GameLoop extends Thread {

    private final Instance instance;

    private boolean running;

    public GameLoop(Instance instance) {
        setName("Game Loop");
        this.instance = instance;
    }

    private long timeLastCounted;
    private int frames;
    private double framesPerSecond;

    @Override
    public void run() {
        // No need to start a loop since this method is scheduled if low power mode is enabled
        if (instance.gameConfig.lowPowerMode) {
            if (running) updateAndCheckForCrash();
            return;
        }

        // Set running to true
        this.running = true;

        // Runs as long as running is true, allows us to stop loop
        while (this.running) {
            updateAndCheckForCrash();
        }
    }

    private void updateAndCheckForCrash() {
        try {
            update();
        } catch (Exception e) {
            e.printStackTrace();

            this.running = false;

            crash(e);
        }
    }

    private void crash(Exception e) {
        Display display = this.instance.display;

        // shut down tinysound
        TinySound.shutdown();

        display.setVisible(true);
        display.showCursor();

        // turn off fullscreen
        display.setFullscreen(0, false);

        // remove display
        display.remove(display.canvas);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String date = sdf.format(new Date());

        JTextArea log = new JTextArea(
                "Oh no! The game has crashed!"
                        + "\n"
                        + "\nTimestamp: " + date
                        + "\nJava: " + System.getProperty("java.version") + " " + System.getProperty("java.vendor") + " " + System.getProperty("java.vendor.url")
                        + "\nOS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch")
                        + "\nGame Root: " + this.instance.gameRoot
                        + "\n"
                        + "\n/// STACKTRACE /// "
                        + "\n" + e.getClass().getName() + ": " + e.getMessage()
                        + "\n"
        );
        for (StackTraceElement trace : e.getStackTrace()) {
            log.setText(log.getText() + "\tat " + trace.toString() + "\n");
        }
        log.setEditable(false);

        display.add(new JScrollPane(log));

        display.setVisible(true);
        display.setSize(display.getWidth(), display.getHeight());

        Arrays.stream(display.getComponents()).forEach(Component::repaint);

        e.printStackTrace();

        System.gc();
    }

    private void update() {
        // Update the display -> loops game -> draws to display & updates game
        boolean success = this.instance.display.update();

        // If could not update display, do not count frames
        if (!success)
            return; //continue;

        this.frames++;

        // Update frames per second
        long timeNow = System.nanoTime();
        long timeDelta = timeNow - this.timeLastCounted;
        // ...but only every 1000ms
        if (timeDelta >= 1000L * 1000000) {
            this.timeLastCounted = timeNow;
            // Calculate frames per second
            this.framesPerSecond = this.frames * ((1000.0 * 1000000) / timeDelta);
            // Reset frames counter
            this.frames = 0;
        }

        // Unpause fog thread
        if (this.instance.fogThread.isAlive())
            synchronized (this.instance.fogThread) {
                this.instance.fogThread.notify();
            }

    }

    // Returns frames per second
    public double getFramesPerSecond() {
        return framesPerSecond;
    }

    public void end() {
        // Sets running to false, results in ending the loop
        this.running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
