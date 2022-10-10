package de.yonedash.smash.scene;

import de.yonedash.smash.Display;
import de.yonedash.smash.Instance;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Arrays;

public abstract class SceneLoading extends Scene {

    // This value represents progress of the loading bar
    protected double progress;

    //
    private boolean hasFinished, done;
    private static final int PROGRESS_BAR_FADE_TIME = 750, PROGRESS_BAR_STAY_TIME = 500;
    private double visibilityDelayTimer;

    public SceneLoading(Instance instance) {
        super(instance);
    }

    @Override
    public void update(Graphics2D g2d, double dt) {
        // Get display dimensions
        Display display = this.instance.display;
        int width = display.getWidth(), height = display.getHeight();

        // Define bar dimensions
        double barWidth = 600.0, barHeight = barWidth * 0.2, barInset = 14.0;

        // Draw loading bar in center of screen
        if (!hasFinished) {
            visibilityDelayTimer += dt;
        } else {
            visibilityDelayTimer -= dt;

            if (visibilityDelayTimer <= 0 && !done) {
                done = true;
                switchScene();
            }
        }

        int alpha = (int) (255 * Math.max(Math.min(visibilityDelayTimer / (double) PROGRESS_BAR_FADE_TIME, 1), 0));

        g2d.setColor(new Color(255, 255, 255, alpha));
        g2d.setStroke(new BasicStroke(super.scaleToDisplay(8.0)));
        g2d.drawRect(width / 2 - super.scaleToDisplay(barWidth) / 2, height / 2 - super.scaleToDisplay(barHeight) / 2,
                super.scaleToDisplay(barWidth), super.scaleToDisplay(barHeight));
        g2d.fillRect(width / 2 - super.scaleToDisplay(barWidth) / 2 + super.scaleToDisplay(barInset), height / 2 - super.scaleToDisplay(barHeight) / 2 + super.scaleToDisplay(barInset),
                super.scaleToDisplay((barWidth - barInset * 2) * (hasFinished ? 1 : progress)), super.scaleToDisplay(barHeight - barInset * 2));
    }

    public void finish() {
        if (visibilityDelayTimer >= PROGRESS_BAR_FADE_TIME + PROGRESS_BAR_STAY_TIME) {
            hasFinished = true;
            visibilityDelayTimer = PROGRESS_BAR_FADE_TIME;
        }
    }

    public abstract void switchScene();


    protected double getFieldsInitializedRatio(Object object) {
        Class<?> clazz = object.getClass();
        Field[] fields = clazz.getFields();
        long initialized = Arrays.stream(fields).filter(field -> {
            try {
                return field.get(object) != null;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).count();
        return initialized / (double) fields.length;
    }
}
