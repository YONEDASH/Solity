package de.yonedash.solity.launch;

import de.yonedash.solity.Instance;
import de.yonedash.solity.compat.OS;
import de.yonedash.solity.compat.adapter.Adapter;

import java.io.File;

public class Launch {

    // Program starts here
    public static void main(String[] args) {
        // Create OS adapter obj
        Adapter adapter = OS.LOCAL_MACHINE.newAdapter();
        // Create var with game folder path
        String gameRoot = adapter.getApplicationDataPath() + "/" + "Solity";
        System.out.println("Game Root is " + gameRoot);

        // Load launch configuration
        LaunchConfig launchConfig = new LaunchConfig(new File(gameRoot, "launch.ini"));
        launchConfig.load();

        // Enable render pipeline
        RenderPipeline renderPipeline = launchConfig.getRenderPipeline();
        renderPipeline.setEnabled(true);
        System.out.println("Rendering with " + renderPipeline.name() + " on " + OS.LOCAL_MACHINE + "/" + adapter.getClass().getSimpleName());

        // Create new instance and run
        (new Instance(launchConfig, adapter, gameRoot)).run();
    }

}
