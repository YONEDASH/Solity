package de.yonedash.smash.launch;

import de.yonedash.smash.config.XMLConfig;

import java.io.File;

public class LaunchConfig extends XMLConfig {

    private RenderPipeline renderPipeline;

    public LaunchConfig(File file) {
        super(file);
    }

    @Override
    protected void init() {
        add("renderPipeline", RenderPipeline.SOFTWARE.name());
        add("saveGameLastPlayed", null);
    }

    @Override
    public void load() {
        super.load();

        this.renderPipeline = RenderPipeline.valueOf(getString("renderPipeline"));
    }

    public RenderPipeline getRenderPipeline() {
        return renderPipeline;
    }
}
