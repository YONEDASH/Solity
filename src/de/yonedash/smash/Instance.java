package de.yonedash.smash;

import de.yonedash.smash.audio.AudioLibrary;
import de.yonedash.smash.audio.AudioProcessor;
import de.yonedash.smash.compat.OS;
import de.yonedash.smash.compat.adapter.Adapter;
import de.yonedash.smash.config.GameConfig;
import de.yonedash.smash.config.GraphicsConfig;
import de.yonedash.smash.config.InputConfig;
import de.yonedash.smash.graphics.GraphicsThread;
import de.yonedash.smash.item.ItemRegistry;
import de.yonedash.smash.launch.LaunchConfig;
import de.yonedash.smash.resource.FontLexicon;
import de.yonedash.smash.graphics.TextureAtlas;
import de.yonedash.smash.scene.Scene;
import de.yonedash.smash.scene.SceneLoadMainMenu;
import kuusisto.tinysound.TinySound;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// "Main" class of game
public class Instance implements Runnable {

    public final Display display;
    public final TextureAtlas atlas;
    public final FontLexicon lexicon;
    public final AudioLibrary library;

    public final ItemRegistry itemRegistry;
    public World world;

    public Scene scene;
    public final GameLoop gameLoop;

    public final Thread fogThread;

    public final AudioProcessor audioProcessor;

    public final Adapter adapter;
    public final String gameRoot;

    public GameConfig gameConfig;
    public GraphicsConfig graphicsConfig;
    public InputConfig inputConfig;
    public LaunchConfig launchData;

    public Instance(LaunchConfig launchData, Adapter adapter, String gameRoot) {
        // Instance constructor, initialize variables before running

        // Set launch data
        this.launchData = launchData;

        // Set OS adapter
        this.adapter = adapter;

        // Set game root path
        this.gameRoot = gameRoot;

        // Initialize display
        this.display = new Display(this);

        // Initialize texture atlas
        this.atlas = new TextureAtlas(this);

        // Initialize font lexicon
        this.lexicon = new FontLexicon();

        // Initialize audio library
        this.library = new AudioLibrary();

        // Initialize audio processor
        this.audioProcessor = new AudioProcessor(this);

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
        this.gameConfig = new GameConfig(this);
        this.graphicsConfig = new GraphicsConfig(this);
        this.inputConfig = new InputConfig(this);
    }


    @Override
    public void run() {
        // Set platform Java look and feel
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            e.printStackTrace();
            stop(2);
        }

        // Starting game here

        // Load configurations
        this.gameConfig.load();
        this.graphicsConfig.load();
        this.inputConfig.load();

        // Load audio volumes
        this.audioProcessor.loadVolumesFromConfig();

        // Show game window/"display"
        this.display.setVisible(true);

        // Check if fullscreen is turned on, if so, set display fullscreen
        if (this.gameConfig.getBoolean("fullscreen"))
            this.display.setFullscreen(0, true);

        // Initialize TinySound
        TinySound.init();

        // Start game loop thread
        if (this.gameConfig.lowPowerMode)
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
