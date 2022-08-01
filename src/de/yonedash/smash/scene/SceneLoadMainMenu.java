package de.yonedash.smash.scene;

import de.yonedash.smash.Instance;

import java.awt.*;

public class SceneLoadMainMenu extends SceneLoading {

    private Thread thread;
    private boolean threadRunning, done;

    public SceneLoadMainMenu(Instance instance) {
        super(instance);

        this.thread = new Thread(() -> {
            instance.atlas.flush();
            System.out.println("flush");

            instance.atlas.load();
            System.out.println("Aload");

            instance.lexicon.load();
            System.out.println("Lload");

            this.threadRunning = false;
            this.done = true;
            System.out.println("done");

            try {
                this.thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println("join");
        });
        this.thread.setName("Scene Main Menu Load Thread");
    }

    private void startLoadThread() {
        this.threadRunning = true;
        this.thread.start();
    }

    @Override
    public void update(Graphics2D g2d, double dt) {
        this.progress = (getFieldsInitializedRatio(this.instance.atlas) + getFieldsInitializedRatio(this.instance.lexicon)) / 2.0;
        super.update(g2d, dt);

        System.out.println(progress + " " + done + " " + threadRunning + " " + thread);

        if (!done && !this.threadRunning) {
            startLoadThread();
            System.out.println("started");
        } else if (done && !this.threadRunning) {
            this.instance.scene = new SceneMainMenu(this.instance);
            System.out.println("nexted");
        }
    }
}
