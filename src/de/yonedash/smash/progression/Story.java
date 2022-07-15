package de.yonedash.smash.progression;

import de.yonedash.smash.SceneInGame;

import java.awt.*;

public abstract class Story {

    protected int step;
    protected boolean stepInitialized;

    protected Story() {
        this.step = -1;
        this.stepInitialized = false;
    }

    public void update(Graphics2D g2d, double dt, SceneInGame scene) {
        if (!stepInitialized) {
            stepInitialized = true;
            initStep(dt, scene, step);
        }

        updateStep(g2d, dt, scene, step);
    }

    protected abstract void initStep(double dt, SceneInGame scene, int step);
    protected abstract void updateStep(Graphics2D g2d, double dt, SceneInGame scene, int step);

    public void initStep(int step) {
        this.step = step;
        this.stepInitialized = false;
    }

    public void nextStep() {
        initStep(++this.step);
    }

    public void start() {
        initStep(0);
    }

}
