package de.yonedash.smash;

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
            update();
            return;
        }

        // Set running to true
        this.running = true;

        // Runs as long as running is true, allows us to stop loop
        while (this.running) {
            update();
        }
    }

    private void update() {
        // Update the display -> loops game -> draws to display & updates game
        boolean success = this.instance.display.update();

        // If could not update display, do not count frames
        if (!success)
            return; //continue;

        // For some reason, the compiler is "optimizing" this while loop
        // away if there is no other code in here?!
        // So I am forced to implement the FPS/UPS counter in this class
        // instead of in the Display class
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
}
