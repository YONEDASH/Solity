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

            instance.lexicon.load();

            instance.atlas.load();

            instance.library.load();

            this.threadRunning = false;
            this.done = true;

            try {
                this.thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        });
        this.thread.setName("Scene Main Menu Load Thread");
    }

    private void startLoadThread() {
        this.threadRunning = true;
        this.thread.start();
    }

    @Override
    public void update(Graphics2D g2d, double dt) {
        this.progress = (getFieldsInitializedRatio(this.instance.atlas) + getFieldsInitializedRatio(this.instance.lexicon) + getFieldsInitializedRatio(this.instance.library)) / 3.0;
        super.update(g2d, dt);

        if (!done && !this.threadRunning) {
            startLoadThread();
        } else if (done && !this.threadRunning) {
            this.instance.scene = new SceneMainMenu(this.instance);
        }
    }
}
