package de.yonedash.smash.launch;

public enum RenderPipeline {

    SOFTWARE("noddraw"), OPENGL("opengl"), METAL("metal"), DIRECT3D("d3d");

    private final String sunName;

    RenderPipeline(String sunName) {
        this.sunName = sunName;
    }

    public void setEnabled(boolean enabled) {
        System.setProperty("sun.java2d." + this.sunName, enabled ? "True" : "False");
    }

}
