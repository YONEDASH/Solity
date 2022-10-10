package de.yonedash.smash.progression.story;

import de.yonedash.smash.*;
import de.yonedash.smash.config.InputConfig;
import de.yonedash.smash.config.KeyBind;
import de.yonedash.smash.entity.*;
import de.yonedash.smash.progression.saves.SaveGame;
import de.yonedash.smash.scene.SceneInWorld;

import java.awt.*;
import java.io.IOException;

public class MainStory extends Story {

    public MainStory() {
        super("level");
    }

    final String langPrefix = "story.main.";

    EntityNPC stranger, wizard;

    @Override
    protected void initStep(double dt, SceneInWorld scene, int step) {
        Instance instance = scene.instance;
        World world = instance.world;
        InputConfig inputConfig = instance.inputConfig;
        EntityPlayer player = scene.player;
        SaveGame saveGame = instance.world.saveGame;
        stranger = new EntityNPC(Vec2D.zero());
        System.out.println("Story step " + step + " initialized isCheckpoint=" + checkpointLoaded);

        switch (step) {
            case 0 -> {
                stranger = new EntityNPC(Vec2D.zero());
                expectEntityHere(instance, player, new Vec2D(-279, 1467));
                stranger.getBoundingBox().position = new Vec2D(5384, 3848);
                world.entitiesLoaded.add(stranger);
            }
            case 1 -> {
                setMovementLockState(instance, true);
                world.prompt(new TextPrompt(scene.localize(langPrefix + "stranger"), scene.localize(langPrefix + step), this::nextStep, 3));
            }
            case 2 -> {
                setMovementLockState(instance, false);
                stranger.removeWhenHidden();
                world.prompt(new TextPrompt(scene.localize(langPrefix + "stranger"), scene.localize(langPrefix + step), this::nextStep, 3));
            }
            case 3 -> {
                setMovementLockState(instance, false);
                markStepAsCheckpoint(saveGame);
                saveProgress(saveGame);
                if (checkpointLoaded) expectEntityHere(instance, player, new Vec2D(5384, 3848));

                scatter(EntityAnt.class, world, new Vec2D(7163, 6002), 3, Tile.TILE_SIZE * 1.5);
            }
            case 5 -> {
                markStepAsCheckpoint(saveGame);
                saveProgress(saveGame);

                if (checkpointLoaded) expectEntityHere(instance, player, new Vec2D(7053, 5828));

                wizard = new EntityNPC(Vec2D.zero());
                wizard.getBoundingBox().position = new Vec2D(7053, 5828 - Tile.TILE_SIZE * 2);

                for (int i = 0; i < 20; i++) {
                    BoundingBox bb = new BoundingBox(wizard.getBoundingBox().center().clone(), wizard.getBoundingBox().size.multiply(0.1));
                    EntityParticle particle = new EntityParticle(bb, instance.atlas.animSlash, world.random(scene, dt) * 180.0, Math.abs(world.random(scene, dt)) * 5, 150, wizard.getZ() + 1, true);
                    world.entitiesLoaded.add(particle);
                }

                world.entitiesLoaded.add(wizard);

                world.prompt(new TextPrompt(scene.localize(langPrefix + "wizard"), scene.localize(langPrefix + step), this::nextStep, 3));
            }
            case 6 -> {
                saveGame.getSkills().skillDash.setLevel(2);
                try {
                    saveGame.save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                player.setDashesLeft(saveGame.getSkills().skillDash.getMaximumDashCount());

                world.prompt(new TextPrompt(scene.localize(langPrefix + "wizard"), scene.localize(langPrefix + step), this::nextStep, 3));
            }
            case 7, 8 -> {
                world.prompt(new TextPrompt(scene.localize(langPrefix + "wizard"), scene.localize(langPrefix + step), this::nextStep, 3));
            }
            case 9 -> {
                markStepAsCheckpoint(saveGame);
                saveProgress(saveGame);

                if (checkpointLoaded) expectEntityHere(instance, player, new Vec2D(7053, 5828));

                if (wizard != null)
                    wizard.removeWhenHidden();
            }
            case 11 -> {
                world.prompt(new TextPrompt(scene.localize(langPrefix + "wizard"), scene.localize(langPrefix + step), this::nextStep, 5));
            }
            case 12 -> {
                markStepAsCheckpoint(saveGame);
                saveProgress(saveGame);
            }
        }
    }

    private void setMovementLockState(Instance instance, boolean state) {
        InputConfig inputConfig = instance.inputConfig;
        if (!state) {
            inputConfig.getBind("moveUp").unlock();
            inputConfig.getBind("moveLeft").unlock();
            inputConfig.getBind("moveRight").unlock();
            inputConfig.getBind("moveDown").unlock();
            return;
        }
        inputConfig.getBind("moveUp").lock();
        inputConfig.getBind("moveLeft").lock();
        inputConfig.getBind("moveRight").lock();
        inputConfig.getBind("moveDown").lock();
    }

    @Override
    protected void updateStep(Graphics2D g2d, double dt, SceneInWorld scene, int step) {
        Instance instance = scene.instance;
        World world = instance.world;
        InputConfig inputConfig = instance.inputConfig;
        EntityPlayer player = scene.player;

        switch (step) {
            case 0 -> {
                drawObjectiveText(scene.localize(langPrefix + "pathobjFind"), g2d, scene);

                if (stranger.getBoundingBox().center().distanceSqrt(player.getBoundingBox().center()) < Tile.TILE_SIZE * 3) {
                    nextStep();
                }
            }
            case 1, 2 -> {
                drawObjectiveText(scene.localize(langPrefix + "pathobj"), g2d, scene);
            }
            case 3 -> {
                drawObjectiveText(scene.localize(langPrefix + "pathobj"), g2d, scene);

                for (Entity e : world.entitiesLoaded.stream().filter(e -> e instanceof EntityProjectile).toList()) {
                    if (e instanceof EntityProjectile ep && ep.getShooter() instanceof EntityAnt) {
                        nextStep();
                        break;
                    }
                }
            }
            case 4 -> {
                long antCount = countEntities(EntityAnt.class, world);
                if (antCount == 0) {
                    nextStep();
                }

                drawObjectiveText(scene.localize("story.main.antsremaining", antCount), g2d, scene);
            }
        }
    }

}
