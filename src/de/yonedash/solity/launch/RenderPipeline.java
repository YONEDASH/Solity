package de.yonedash.solity.launch;

import de.yonedash.solity.compat.OS;

import java.util.Arrays;

public enum RenderPipeline {

    SOFTWARE("noddraw", OS.values()), OPENGL("opengl", OS.values()), METAL("metal", OS.OSX), DIRECT3D("d3d", OS.WINDOWS), XRENDER("xrender", OS.LINUX);

    public static final RenderPipeline[] AVAILABLE = getAvailable();

    private final String sunName;
    private final OS[] os;

    RenderPipeline(String sunName, OS... os) {
        this.sunName = sunName;
        this.os = os;
    }

    public void setEnabled(boolean enabled) {
        // System.setProperty("sun.java2d.opengl.fbobject", "False");
        // System.setProperty("sun.java2d.trace", "count");
        System.setProperty("sun.java2d." + this.sunName, enabled ? "True" : "False");

        if (this == SOFTWARE) {
            Arrays.stream(RenderPipeline.AVAILABLE).filter(renderPipeline -> renderPipeline != this).forEach(renderPipeline -> renderPipeline.setEnabled(false));
        }
    }

    public OS[] getOperatingSystems() {
        return os;
    }

    static RenderPipeline[] getAvailable() {
        return Arrays.stream(values()).filter(renderPipeline -> Arrays.stream(renderPipeline.os).anyMatch(os -> os == OS.LOCAL_MACHINE)).toList().toArray(new RenderPipeline[0]);
    }
}
