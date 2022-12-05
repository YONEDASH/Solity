package de.yonedash.solity;

import de.yonedash.solity.audio.AudioLibrary;
import de.yonedash.solity.audio.AudioProcessor;
import de.yonedash.solity.compat.adapter.Adapter;
import de.yonedash.solity.config.GameConfig;
import de.yonedash.solity.config.GraphicsConfig;
import de.yonedash.solity.config.InputConfig;
import de.yonedash.solity.graphics.GraphicsThread;
import de.yonedash.solity.item.ItemRegistry;
import de.yonedash.solity.launch.LaunchConfig;
import de.yonedash.solity.resource.FontLexicon;
import de.yonedash.solity.graphics.TextureAtlas;
import de.yonedash.solity.scene.Scene;
import de.yonedash.solity.scene.SceneLoadMainMenu;
import kuusisto.tinysound.TinySound;

import javax.swing.*;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// "Main" class of game
public class Instance implements Runnable {

    public final Properties buildProperties;

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

    public Instance(Properties buildProperties, LaunchConfig launchData, Adapter adapter, String gameRoot) {
        // Instance constructor, initialize variables before running

        // Set build properties
        this.buildProperties = buildProperties;

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
        if (this.gameConfig.lowPowerMode) {
            gameLoop.setRunning(true);
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(gameLoop, 0,
                    (long) (1_000_000 * (1000.0 / 10000.0)), TimeUnit.NANOSECONDS);
        } else
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
