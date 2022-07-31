package de.yonedash.smash.launch;

import de.yonedash.smash.Constants;
import de.yonedash.smash.Instance;

import java.io.File;

public class Launch {

    // Program starts here
    public static void main(String[] args) {
        // Load launch configuration
        LaunchConfig launchConfig = new LaunchConfig(new File(Constants.GAME_ROOT, "launch.xml"));
        launchConfig.load();

        // Enable render pipeline
        RenderPipeline renderPipeline = launchConfig.getRenderPipeline();
        renderPipeline.setEnabled(true);
        System.out.println("RenderPipeline=" + renderPipeline.name());

        // Create new instance and run
        (new Instance(launchConfig)).run();
    }

}
