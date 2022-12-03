package de.yonedash.solity.scene;

import de.yonedash.solity.Instance;
import de.yonedash.solity.LevelData;
import de.yonedash.solity.World;
import de.yonedash.solity.config.KeyBind;
import de.yonedash.solity.progression.saves.SaveGame;
import de.yonedash.solity.progression.story.Story;
import de.yonedash.solity.resource.TiledMap;

import java.awt.*;

public class SceneLoadWorld extends SceneLoading {

    private Thread thread;
    private boolean threadRunning;
    private TiledMap tiledMap;
    private LevelData levelData;

    public SceneLoadWorld(Instance instance, Story story, SaveGame saveGame) {
        super(instance);

        this.tiledMap = new TiledMap("/assets/map/" + story.getMapName() + ".tmx");

        this.thread = new Thread(() -> {
            instance.inputConfig.getBinds().values().forEach(KeyBind::unlock);

            instance.world = new World(saveGame);
            instance.world.story = story;

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

        // Stop music
        this.instance.audioProcessor.stopMusic();
    }

    private void startLoadThread() {
        this.threadRunning = true;
        this.thread.start();
    }

    @Override
    public void update(Graphics2D g2d, double dt) {
        this.progress = (this.tiledMap.reportProgress() + (this.instance.world == null ? 0 : this.instance.world.reportProgress())) / 2.0;
        super.update(g2d, dt);

        if (this.levelData == null && !this.threadRunning) {
            startLoadThread();
        } else if (!this.threadRunning) {
            finish();
        }
    }

    @Override
    public void switchScene() {
        this.instance.scene = new SceneInWorld(this.instance);
    }
}
