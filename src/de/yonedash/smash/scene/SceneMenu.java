package de.yonedash.smash.scene;

import de.yonedash.smash.*;
import de.yonedash.smash.entity.EntityPlayer;
import de.yonedash.smash.entity.LevelObject;
import de.yonedash.smash.graphics.EntityFog;
import de.yonedash.smash.graphics.TextureAtlas;
import de.yonedash.smash.resource.Texture;

import java.awt.*;
import java.util.Random;

public abstract class SceneMenu extends Scene {

    private static OpenSimplexNoise openSimplexNoise1, openSimplexNoise2;
    private static double simplexOffset;
    private static boolean menuInitialized, playerInitialized;
    private static double playerPosX, playerPosY;
    private static EntityFog entityFog;
    private static double fogOffset;

    public SceneMenu(Instance instance) {
        super(instance);

        if (!menuInitialized) {
            menuInitialized = true;
            long seed = new Random().nextLong();
            openSimplexNoise1 = new OpenSimplexNoise(seed);
            openSimplexNoise2 = new OpenSimplexNoise(seed / 2);
            entityFog = new EntityFog(new Chunk(0, 0, new LevelObject[0]));
        }
    }

    public void drawBackground(Graphics2D g2d, double dt) {
        Display display = this.instance.display;
        int width = display.getWidth(), height = display.getHeight();

        g2d.setColor(Constants.MAP_BACKGROUND_COLOR.darker());
        g2d.fillRect(0, 0, width, height);
    }

    public void drawPlayer(Graphics2D g2d, double dt, double minX) {
        Display display = this.instance.display;
        int width = display.getWidth(), height = display.getHeight();

        // Draw player
        double speed = 0.001;
        double random1 = openSimplexNoise1.eval(simplexOffset, simplexOffset += speed);
        double random2 = openSimplexNoise2.eval(simplexOffset, simplexOffset += speed);

        TextureAtlas atlas = this.instance.atlas;

        BoundingBox desiredView = new BoundingBox(new Vec2D(minX, 0), new Vec2D(width - minX, height)).scale(0.8);

        double playerSize = 400.0;int pullPart1 = random1 < -0.5 ? 0x001 : random1 < 0.0 ? 0x002 : random1 < 0.5 ? 0x003 : 0x004;

        int pullPart2 = random2 < 0 | !desiredView.contains(
                        new Vec2D(playerPosX + super.scaleToDisplay(playerSize / 2),
                                playerPosY + super.scaleToDisplay(playerSize / 2)))
                ? atlas.walkSubId : atlas.idleSubId;

        Texture texture = atlas.getBundle(EntityPlayer.class).pull(pullPart1 + pullPart2);
        if (texture == null) texture = atlas.invalid;

        if (!playerInitialized) {
            playerInitialized = true;
            playerPosX = width - width / 3 - super.scaleToDisplay(playerSize / 2);
            playerPosY = height / 2 - super.scaleToDisplay(playerSize / 2);
        }

        if (pullPart2 == atlas.walkSubId) {
            double walkSpeed = super.time(super.scaleToDisplay(2) * 0.2, dt);
            if (pullPart1 == 0x001)
                playerPosY -= walkSpeed;
            else if (pullPart1 == 0x002)
                playerPosX += walkSpeed;
            else if (pullPart1 == 0x003)
                playerPosY += walkSpeed;
            else if (pullPart1 == 0x004)
                playerPosX -= walkSpeed;

            while (playerPosY + playerSize < 0)
                playerPosY += height + playerSize;

            while (playerPosY > height)
                playerPosY -= height + playerSize;

            while (playerPosX + playerSize < minX)
                playerPosX += width + playerSize - minX;

            while (playerPosX > width)
                playerPosX -= width + playerSize - minX;
        }

        g2d.drawImage(texture.getBufferedImage(),
                (int) playerPosX, (int) playerPosY,
                super.scaleToDisplay(playerSize), super.scaleToDisplay(playerSize),
                null);
    }

    public void drawFog(Graphics2D g2d, double dt) {
        Display display = this.instance.display;
        int width = display.getWidth(), height = display.getHeight();

        entityFog.updateFog(fogOffset, 0.0, openSimplexNoise1);
        g2d.drawImage(entityFog.getImage(), 0, 0, width, height, null);

        fogOffset -= super.time(0.1, dt);
    }

}
