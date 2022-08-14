package de.yonedash.smash.scene;

import de.yonedash.smash.*;
import de.yonedash.smash.entity.LevelObject;
import de.yonedash.smash.graphics.EntityFog;
import de.yonedash.smash.resource.Texture;

import java.awt.*;
import java.util.Random;

public abstract class SceneMenu extends Scene {
    private static OpenSimplexNoise openSimplexNoise;
    private static boolean menuInitialized;
    protected static EntityFog entityFog;
    private static double fogOffset, backgroundOffset;

    public SceneMenu(Instance instance) {
        super(instance);

        if (!menuInitialized) {
            menuInitialized = true;
            long seed = new Random().nextLong();
            openSimplexNoise = new OpenSimplexNoise(seed);
            entityFog = new EntityFog(new Chunk(0, 0, new LevelObject[0]));
            entityFog.createFog(instance.graphicsConfig);
        }

        // Show cursor
        this.instance.display.showCursor();
    }

    public void drawBackground(Graphics2D g2d, double dt) {
        Display display = this.instance.display;
        int width = display.getWidth(), height = display.getHeight();

        // Draw background/grass color in case of background not covering the entire screen
        g2d.setColor(Constants.MAP_BACKGROUND_COLOR);
        g2d.fillRect(0, 0, width, height);

        double bgMoveSpeed = 0.00004, bgMoveFactor = 400.0;

        // Calculates circle movement translation
        double displayScale = calculateDisplayScaleFactor();
        double tx = Math.cos(backgroundOffset * 2) * bgMoveFactor * displayScale, ty = Math.sin(backgroundOffset * 0.5) * bgMoveFactor * displayScale;

        // Translate
        g2d.translate(tx, ty);

        // Draw scaled background in center of screen
        double backgroundScale = super.scaleToDisplay(6.0);
        Texture background = this.instance.atlas.uiBackground;
        int backgroundWidth = (int) (background.getWidth() * backgroundScale);
        int backgroundHeight = (int) (background.getHeight() * backgroundScale);
        g2d.drawImage(background.getBufferedImage(),
                width / 2 - backgroundWidth / 2, height / 2 - backgroundHeight / 2,
                backgroundWidth, backgroundHeight, null);

        // Revert translation
        g2d.translate(-tx, -ty);

        // Draw fog on top of background
        drawFog(g2d, dt);

        // Move background by adding speed to offset
        backgroundOffset += super.time(bgMoveSpeed, dt);
    }

    public void drawFog(Graphics2D g2d, double dt) {
        Display display = this.instance.display;
        int width = display.getWidth(), height = display.getHeight();

        entityFog.updateFog(fogOffset, 0.0, openSimplexNoise);
        g2d.drawImage(entityFog.getImage(), 0, 0, width, height, null);

        fogOffset -= super.time(0.1, dt);
    }

}
