package de.yonedash.smash.progression.story;

import de.yonedash.smash.*;
import de.yonedash.smash.config.InputConfig;
import de.yonedash.smash.config.KeyBind;
import de.yonedash.smash.entity.*;
import de.yonedash.smash.localization.BindLocalizer;
import de.yonedash.smash.scene.SceneInWorld;

import java.awt.*;

public class TutorialStory extends Story {

    private EntityAnt starterAnt;

    public TutorialStory() {
        super("tutorial");
    }

    @Override
    protected void initStep(double dt, SceneInWorld scene, int step) {
        Instance instance = scene.instance;
        World world = instance.world;
        InputConfig inputConfig = instance.inputConfig;
        String langPrefix = "story.tutorial.";

        Vec2D superPosition = new Vec2D(929.3092032249493, -1064.0024031388386);

        switch (step) {
            case 0 -> {
                // Lock keys
                inputConfig.getBind("moveUp").lock();
                inputConfig.getBind("moveLeft").lock();
                inputConfig.getBind("moveRight").lock();
                inputConfig.getBind("moveDown").lock();
                inputConfig.getBind("dash").lock();
                inputConfig.getBind("shoot").lock();

                // Set max health
                scene.player.setMaxHealth(6.0);

                // Teleport player to start position
                scene.player.getBoundingBox().position = new Vec2D(-914.6496488888881, 2259.841097095963);
                scene.resetCameraPosition();

                world.prompt(new TextPrompt(scene.localize(langPrefix + "title"), scene.localize(langPrefix + step), this::nextStep, 3));
            }
            case 1 -> world.prompt(new TextPrompt(scene.localize(langPrefix + "title"), scene.localize(langPrefix + step, BindLocalizer.getActualBindName(inputConfig.getBind("skipDialog"))), this::nextStep, TextPrompt.MANUAL_SKIP));
            case 2 -> {
                // Reset distance moved
                this.distanceMoved = 0.0;

                // Spawn 1 ant
                starterAnt = new EntityAnt(superPosition.clone());
                world.entitiesLoaded.add(starterAnt);

                world.prompt(new TextPrompt(scene.localize(langPrefix + "title"), scene.localize(langPrefix + step, BindLocalizer.getActualBindName(inputConfig.getBind("moveUp")), BindLocalizer.getActualBindName(inputConfig.getBind("moveLeft")), BindLocalizer.getActualBindName(inputConfig.getBind("moveDown")), BindLocalizer.getActualBindName(inputConfig.getBind("moveRight"))), this::nextStep, TextPrompt.UNSKIPPABLE));
            }
            case 3 -> {
                // Reset distance moved
                this.distanceMoved = 0.0;

                // Set player dashes
                scene.player.setDashesLeft(2f);

                world.prompt(new TextPrompt(scene.localize(langPrefix + "title"), scene.localize(langPrefix + step, BindLocalizer.getActualBindName(inputConfig.getBind("dash"))), this::nextStep, 3));
            }
            case 4 -> {

            }
            case 5 -> {
                // Unlock shoot key
                inputConfig.getBind("shoot").unlock();

                world.prompt(new TextPrompt(scene.localize(langPrefix + "title"), scene.localize(langPrefix + step, BindLocalizer.getDeviceName(scene, KeyBind.Device.MOUSE), BindLocalizer.getActualBindName(inputConfig.getBind("shoot"))), this::nextStep, TextPrompt.UNSKIPPABLE));
            }
            case 6 -> {
                markStepAsCheckpoint();

                // Clear world of entities
                cleanupWorld(world, checkpointLoaded);

                if (checkpointLoaded) {
                    scene.player.getBoundingBox().position = superPosition.clone();
                    scene.resetCameraPosition();
                }

                // Set player stats
                scene.player.setMaxHealth(6.0);
                scene.player.setHealth(scene.player.getMaxHealth());
                scene.player.setDashesLeft(2f);

                // Reset waypoint
                world.waypoint = null;

                // Spawn multiple ants
                this.scatter(EntityAnt.class, world, new Vec2D(2942.8999863129607, 768.3406287984217),3, Tile.TILE_SIZE * 6);
                this.scatter(EntityAnt.class, world, new Vec2D(-187.15708563148226, 2428.290422384279),3, Tile.TILE_SIZE * 6);

                // Reset player health and dashes
                EntityPlayer player = scene.player;
                player.setHealth(player.getMaxHealth());
                player.setDashesLeft(2.0f);

                world.prompt(new TextPrompt(scene.localize(langPrefix + "title"), scene.localize(langPrefix + step), this::nextStep, 3));
            }
            case 7 -> {
                // Do nothing here
            }
            case 8 -> {
                // Spawn shard


                // Set waypoint
                world.waypoint = new Vec2D(-771.8151771893367, -129.58057274493697);

                world.prompt(new TextPrompt(scene.localize(langPrefix + "title"), scene.localize(langPrefix + step, BindLocalizer.getActualBindName(inputConfig.getBind("use"))), this::nextStep, TextPrompt.UNSKIPPABLE));
            }
            case 9 -> {
                // End tutorial


                world.prompt(new TextPrompt(scene.localize(langPrefix + "title"), scene.localize(langPrefix + step), this::nextStep, 5));
            }

        }
    }

    private double distanceMoved;

    @Override
    protected void updateStep(Graphics2D g2d, double dt, SceneInWorld scene, int step) {
        EntityPlayer player = scene.player;
        World world = scene.instance.world;

        // Don't allow movement below certain step
        if (this.step < 2) {
            player.getMotion().multiply(0);
        }

        // Set waypoint to starter ant
        if (this.step >= 2 && this.step <= 5) {
            world.waypoint = starterAnt.getBoundingBox().center();
        }

        if (this.step == 2 && scene.hasPromptRevealFinished()) {
            InputConfig inputConfig = scene.instance.inputConfig;

            // Unlock a few movement keys
            inputConfig.getBind("moveUp").unlock();
            inputConfig.getBind("moveLeft").unlock();
            inputConfig.getBind("moveRight").unlock();
            inputConfig.getBind("moveDown").unlock();
        }

        if (this.step == 3 && scene.hasPromptRevealFinished()) {
            InputConfig inputConfig = scene.instance.inputConfig;

            // Unlock dash movement key
            inputConfig.getBind("dash").unlock();
        }

        if ((this.step == 2 || this.step == 3) && scene.hasPromptRevealFinished()) {
            this.distanceMoved += player.getMotion().length();

            if (distanceMoved > Tile.TILE_SIZE * 8) {
                nextStep();
            }
        }

        if (this.step == 4 && player.getBoundingBox().center().distanceSqrt(world.waypoint) < Tile.TILE_SIZE * 6) {
            nextStep();
        }

        if (this.step < 4 || (this.step == 4 && !scene.hasPromptRevealFinished())) {
            world.entitiesLoaded.forEach(entity -> {
                if (entity instanceof EntityDamageable entityDamageable)
                    entityDamageable.setHealth(entityDamageable.getMaxHealth());
            });
        }

        if (this.step < 3) {
            player.setDashesLeft(0);
        }

        if (this.step <= 5) {
            player.setHealth(player.getMaxHealth());
        }

        if (this.step == 5 || this.step == 6 || this.step == 7) {
            long antCount = countEntities(EntityAnt.class, world);
            if (antCount == 0) {
                nextStep();
            }

            if (this.step == 6 || this.step == 7) {
                drawObjectiveText(scene.localize("story.tutorial.antsremaining", antCount), g2d, scene);
            }
        }

        // Freeze every entity if prompt is shown
        if (this.step < 3 || (this.step == 5 && !scene.hasPromptRevealFinished())) {
            // Freeze target
            for (Entity entity : world.entitiesLoaded) {
                if (entity == player)
                    continue;
                entity.getMotion().multiply(0.0); // No movement
                if (entity instanceof EntityEnemy enemy) enemy.shootIdleTime = 0.0; // No shooting
            }
        }
    }

}
