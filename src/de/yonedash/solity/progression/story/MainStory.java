package de.yonedash.solity.progression.story;

import de.yonedash.solity.*;
import de.yonedash.solity.config.InputConfig;
import de.yonedash.solity.entity.*;
import de.yonedash.solity.progression.saves.SaveGame;
import de.yonedash.solity.scene.SceneCreditRoll;
import de.yonedash.solity.scene.SceneInWorld;
import de.yonedash.solity.scene.SceneLoadMainMenu;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class MainStory extends Story {

    public MainStory() {
        super("level");
    }

    final String langPrefix = "story.main.";

    EntityNPC stranger, wizard;
    Entity bottle;

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
                expectEntityHere(instance, player, new Vec2D(-279, 1467));
            }
            case 1 -> {
                setMovementLockState(instance, true);
                world.prompt(new TextPrompt(scene.localize(langPrefix + "stranger"), scene.localize(langPrefix + step), this::nextStep, 3));
            }
            case 2 -> {
                setMovementLockState(instance, false);
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
            case 7, 8, 10, 11, 12 -> {
                world.prompt(new TextPrompt(scene.localize(langPrefix + "wizard"), scene.localize(langPrefix + step), this::nextStep, 3));
            }
            case 9 -> {
                markStepAsCheckpoint(saveGame);
                saveProgress(saveGame);

                if (checkpointLoaded) expectEntityHere(instance, player, new Vec2D(7053, 5828));

                if (wizard != null)
                    wizard.removeWhenHidden();

                scatter(EntityAnt.class, world, new Vec2D(9165.707137424239, 4504.6541754040345), 3, Tile.TILE_SIZE * 1.5);
                scatter(EntityAnt.class, world, new Vec2D(6135.363959188942, 2088.5790126959337), 2, Tile.TILE_SIZE * 1.5);
                scatter(EntityAnt.class, world, new Vec2D(5977.202715649658, -1726.297476318725), 1, Tile.TILE_SIZE * 1.5);
                scatter(EntityAnt.class, world, new Vec2D(-1078.7423070816499, -2048.5581678113676), 4, Tile.TILE_SIZE * 1.5);
                scatter(EntityAnt.class, world, new Vec2D(794.5532196672708, 6210.037704022375), 2, Tile.TILE_SIZE * 1.5);
                scatter(EntityAnt.class, world, new Vec2D(2134.9140400825786, 6252.2183296146995), 6, Tile.TILE_SIZE * 1.5);

            }
            case 13 -> {
                this.bottle = new EntityBottle(new Vec2D(900, 9937), EntityBottle.Color.RED);
                world.entitiesLoaded.add(this.bottle);

                // Set waypoint
                world.waypoint = this.bottle.getBoundingBox().center();
            }
            case 14 -> {
                // save checkpoint, so you can play the empty world
                world.saveGame.set("checkpoint/step", 15);
                saveProgress(world.saveGame);

                // roll credits
                instance.scene = new SceneCreditRoll(instance, new SceneLoadMainMenu(instance));
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

                if (new Vec2D(5384, 3848).distanceSqrt(player.getBoundingBox().center()) < Tile.TILE_SIZE * 3) {
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
            case 9 -> {
                long antCount = countEntities(EntityAnt.class, world);

                drawObjectiveText(scene.localize("story.main.antsremaining", antCount), g2d, scene);

                if (antCount == 0) {
                    nextStep();
                    world.waypoint = null;
                } else {
                    ArrayList<Entity> entities = new ArrayList<>(world.entitiesLoaded);
                    world.chunks.forEach(chunk -> entities.addAll(chunk.getEntities()));
                    Optional<Entity> o = entities.stream().filter(entity -> entity instanceof EntityAnt).findFirst();
                    if (o.isPresent()) {
                        Vec2D targetPos = o.get().getBoundingBox().center();
                        double dis = player.getBoundingBox().center().distanceSqrt(targetPos);
                        if (dis >= Tile.TILE_SIZE * 4) {
                            world.waypoint = targetPos;
                        }
                    }
                }
            }
            case 13 -> {
                drawObjectiveText(scene.localize("story.main.potion"), g2d, scene);

                if (world.waypoint != null && scene.player.getBoundingBox().center().distanceSqrt(world.waypoint) < Tile.TILE_SIZE && !world.entitiesLoaded.contains(bottle)) {
                    nextStep();
                }
            }
        }
    }

}
