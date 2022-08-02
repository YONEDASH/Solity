package de.yonedash.smash.progression.story;

import de.yonedash.smash.*;
import de.yonedash.smash.entity.*;
import de.yonedash.smash.graphics.VisualEffect;
import de.yonedash.smash.progression.saves.SaveGame;
import de.yonedash.smash.progression.saves.StateCapture;
import de.yonedash.smash.resource.FontLexicon;
import de.yonedash.smash.scene.Scene;
import de.yonedash.smash.scene.SceneInWorld;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Story {

    protected StateCapture capture;

    private final String mapName;
    protected int checkpoint;
    protected int step;
    protected boolean stepInitialized;

    protected boolean checkpointLoaded;

    protected Story(String mapName) {
        this.mapName = mapName;
        this.step = -1;
        this.checkpoint = 0;
        this.stepInitialized = false;
    }

    protected void drawObjectiveText(String text, Graphics2D g2d, Scene scene) {
        g2d.setColor(Color.WHITE);

        double textSize = 50.0;

        String title = scene.localize("story.objective");

        double centerX = scene.instance.display.getWidth() * 0.5;
        double centerY = scene.instance.display.getHeight() * 0.1;

        FontLexicon lexicon = scene.instance.lexicon;
        FontRenderer fontRenderer = scene.fontRenderer;
        double objectiveWidth = scene.instance.display.getWidth() * 0.6;
        double textSpace = 0.8;

        g2d.setFont(lexicon.equipmentPro.deriveFont((float) scene.scaleToDisplay(textSize)));
        ArrayList<String> lines = new ArrayList<>();
        String[] words = text.contains(" ") ? text.split(" ") : new String[] { text };
        String currentLine = "";
        double textHeight = 0.0;
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            currentLine += currentLine.isEmpty() ? word : " " + word;
            Vec2D bounds = fontRenderer.bounds(g2d, currentLine);
            double lineWidth = bounds.x;
            if (lineWidth * 0.5 > objectiveWidth * textSpace * 0.5 || i == words.length - 1) {
                textHeight += bounds.y;
                lines.add(currentLine);
                currentLine = "";
            }
        }

        double spaceBetweenLines = scene.scaleToDisplay(4.0);

        // Draw author
        g2d.setFont(lexicon.futilePro.deriveFont((float) scene.scaleToDisplay(textSize * 1.0)));
        Vec2D titleBounds = fontRenderer.bounds(g2d, title);
        textHeight += titleBounds.y;
        fontRenderer.drawString(g2d, title, (int) centerX, (int) (centerY - textHeight * 0.5 - spaceBetweenLines - scene.scaleToDisplay(12.0)), FontRenderer.CENTER, FontRenderer.CENTER, false);

        // Draw lines
        g2d.setFont(lexicon.equipmentPro.deriveFont((float) scene.scaleToDisplay(textSize)));
        double lineOffsetY = titleBounds.y + 0.0 - (textHeight * 0.5) - (lines.size() * spaceBetweenLines * 0.5) + spaceBetweenLines * 0.5;
        for (String line : lines) {
            Vec2D bounds = fontRenderer.bounds(g2d, line.isEmpty() ? "X" : line);
            fontRenderer.drawString(g2d, line, (int) (centerX - bounds.x * 0.5), (int) (centerY + lineOffsetY), FontRenderer.LEFT, FontRenderer.CENTER, false);
            lineOffsetY += bounds.y + spaceBetweenLines;
        }

    }

    protected long countEntities(Class<? extends Entity> entityClazz, World world) {
        AtomicLong unloadedCount = new AtomicLong();
        world.chunks.forEach(chunk -> unloadedCount.addAndGet(chunk.getEntities().stream().filter(entity -> entity.getClass() == entityClazz).count()));
        return unloadedCount.get() + world.entitiesLoaded.stream().filter(entity -> entity.getClass() == entityClazz).count();
    }

    // Removes all entities
    protected void cleanupWorld(World world, boolean force) {
        world.chunks.forEach(chunk -> {
            ArrayList<Entity> toRemove = new ArrayList<>();
            for (Entity entity : chunk.getEntities()) {
                if (!(entity instanceof EntityPlayer) && !(entity instanceof VisualEffect) && !(entity instanceof EntityCoin))
                    if (force)
                        toRemove.add(entity);
                    else
                        entity.removeWhenHidden();
            }
            chunk.getEntities().removeAll(toRemove);
        });
        for (Entity entity : world.entitiesLoaded) {
            if (!(entity instanceof EntityPlayer) && !(entity instanceof VisualEffect) && !(entity instanceof EntityCoin))
                if (force)
                    world.entitiesLoaded.remove(entity);
                else
                    entity.removeWhenHidden();
        }
    }

    protected void scatter(Class<? extends Entity> entityClazz, World world, Vec2D position, int amount, double distance) {
        double deg = 360.0 / amount;
        for (double rotation = 0.0; rotation < 360.0; rotation += deg) {
            double dis = distance;
            Vec2D origin = position.clone();

            // Create new entity object using Java reflection
            Entity entity;
            try {
                entity = entityClazz.getConstructor(Vec2D.class).newInstance(origin);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            // This variable stores the raycasted object
            LevelObject obj;

            // This variable counts iterations to prevent infinite loops
            int i = 0;

            // Try to find obstructions using raycasting
            while ((obj = MathUtils.rayCast(world, new BoundingBox(origin.clone(), entity.getBoundingBox().size), rotation, 5.0, dis, false)) != null && i++ < 1 + (distance / Tile.TILE_SIZE)) {
                double delta = dis - obj.getBoundingBox().center().distanceSqrt(origin);
                dis -= delta + Tile.TILE_SIZE;
            }

            entity.getBoundingBox().position = origin.add(rotation, dis);

            // Add to loaded entities
            world.entitiesLoaded.add(entity);
        }
    }

    protected void markStepAsCheckpoint(SaveGame saveGame) {
        this.checkpoint = this.step;
        if (!this.checkpointLoaded)
            this.capture = saveGame.capture();
    }

    protected void saveProgress(SaveGame saveGame) {
        if (this.capture != null)
            this.capture.apply(saveGame);
        try {
            saveGame.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadCheckpoint() {
        this.checkpointLoaded = true;
        initStep(this.checkpoint);
    }

    public void update(Graphics2D g2d, double dt, SceneInWorld scene) {
        if (!stepInitialized) {
            stepInitialized = true;
            initStep(dt, scene, step);
            checkpointLoaded = false;
        }

        updateStep(g2d, dt, scene, step);

        // Count up playtime
        SaveGame saveGame = scene.instance.world.saveGame;
        long playTime = saveGame.getLong("playTime");
        saveGame.set("playTime", (playTime + (int) Math.ceil(dt)));
    }

    protected abstract void initStep(double dt, SceneInWorld scene, int step);
    protected abstract void updateStep(Graphics2D g2d, double dt, SceneInWorld scene, int step);

    public void initStep(int step) {
        this.step = step;
        this.stepInitialized = false;
    }

    public void nextStep() {
        initStep(++this.step);
    }

    public String getMapName() {
        return mapName;
    }
}
