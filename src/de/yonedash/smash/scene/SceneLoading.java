package de.yonedash.smash.scene;

import de.yonedash.smash.Display;
import de.yonedash.smash.Instance;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Arrays;

public class SceneLoading extends Scene {

    // This value represents progress of the loading bar
    protected double progress;

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
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(super.scaleToDisplay(8.0)));
        g2d.drawRect(width / 2 - super.scaleToDisplay(barWidth) / 2, height / 2 - super.scaleToDisplay(barHeight) / 2,
                super.scaleToDisplay(barWidth), super.scaleToDisplay(barHeight));
        g2d.fillRect(width / 2 - super.scaleToDisplay(barWidth) / 2 + super.scaleToDisplay(barInset), height / 2 - super.scaleToDisplay(barHeight) / 2 + super.scaleToDisplay(barInset),
                super.scaleToDisplay((barWidth - barInset * 2) * progress), super.scaleToDisplay(barHeight - barInset * 2));
    }

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
