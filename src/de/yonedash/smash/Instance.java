package de.yonedash.smash;

import de.yonedash.smash.config.GameConfig;
import de.yonedash.smash.config.InputConfig;
import de.yonedash.smash.graphics.GraphicsThread;
import de.yonedash.smash.item.ItemRegistry;
import de.yonedash.smash.launch.LaunchConfig;
import de.yonedash.smash.resource.FontLexicon;
import de.yonedash.smash.graphics.TextureAtlas;
import de.yonedash.smash.scene.Scene;
import de.yonedash.smash.scene.SceneLoadMainMenu;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// "Main" class of game
public class Instance implements Runnable {

    public final Display display;
    public final TextureAtlas atlas;
    public final FontLexicon lexicon;

    public final ItemRegistry itemRegistry;
    public World world;

    public Scene scene;
    public final GameLoop gameLoop;

    public final Thread fogThread;


    public GameConfig gameConfig;
    public InputConfig inputConfig;
    public LaunchConfig launchData;

    public Instance(LaunchConfig launchData) {
        // Instance constructor, initialize variables before running

        // Set launch data
        this.launchData = launchData;

        // Initialize display
        this.display = new Display(this);

        // Initialize texture atlas
        this.atlas = new TextureAtlas(this);

        // Initialize font lexicon
        this.lexicon = new FontLexicon();

        // Initialize item registry
        this.itemRegistry = new ItemRegistry(this);

        // Initialize scene
        //this.scene = new SceneInGame(this);
        //this.scene = new SceneLoadWorld(this, "tutorial");
        this.scene = new SceneLoadMainMenu(this);

        // Initialize game loop thread
        this.gameLoop = new GameLoop(this);

        // Initialize fog loop thread
        this.fogThread = new GraphicsThread(this);

        // Initialize configs
        this.gameConfig = new GameConfig();
        this.inputConfig = new InputConfig();

    }



    @Override
    public void run() {
        // Starting game here

        // Load configurations
        this.gameConfig.load();
        this.inputConfig.load();

        // Show game window/"display"
        this.display.setVisible(true);

        // Start game loop thread
        if (Constants.LOW_POWER_MODE)
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(gameLoop, 0,
                    (long) (1_000_000 * (1000.0 / 10000.0)), TimeUnit.NANOSECONDS);
        else
            Executors.newSingleThreadScheduledExecutor().schedule(gameLoop, 0, TimeUnit.NANOSECONDS);
    }

    // Called in order to stop program
    public void stop(int status) {
        // End game loop
        this.gameLoop.end();

        // Dispose display
        this.display.dispose();

        // Exit program with status (exit-code)
        System.exit(status);
    }

}
