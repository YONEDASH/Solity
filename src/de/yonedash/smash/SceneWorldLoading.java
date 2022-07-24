package de.yonedash.smash;

import de.yonedash.smash.resource.TiledMap;

import java.awt.*;

public class SceneWorldLoading extends SceneLoading {

    private Thread thread;
    private boolean threadRunning;
    private TiledMap tiledMap;
    private LevelData levelData;

    public SceneWorldLoading(Instance instance, String mapName) {
        super(instance);

        this.tiledMap = new TiledMap("/assets/map/" + mapName + ".tmx");

        this.thread = new Thread(() -> {
            this.levelData = this.tiledMap.load(this.instance.atlas);

            if (this.levelData == null) {
                System.err.println("Map could not be loaded");
                this.instance.stop(1);
            }

            this.instance.world.load(instance, this.levelData);

            if (!this.instance.fogThread.isAlive())
                this.instance.fogThread.start();

            this.threadRunning = false;

            try {
                this.thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        this.thread.setName("Scene World Load Thread");
    }

    private void startLoadThread() {
        this.threadRunning = true;
        this.thread.start();
    }

    @Override
    public void update(Graphics2D g2d, double dt) {
        this.progress = (this.tiledMap.reportProgress() + this.instance.world.reportProgress()) / 2.0;
        super.update(g2d, dt);

        if (this.levelData == null && !this.threadRunning) {
            startLoadThread();
        } else if (!this.threadRunning) {
            this.instance.scene = new SceneInGame(this.instance);
        }
    }
}
