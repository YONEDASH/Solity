package de.yonedash.smash.progression;

import de.yonedash.smash.*;
import de.yonedash.smash.config.InputConfig;
import de.yonedash.smash.config.KeyBind;
import de.yonedash.smash.entity.*;

import java.awt.*;

public class TutorialStory extends Story {

    private EntityAnt tutorialTarget;

    private int timesTargetHit;

    @Override
    protected void initStep(double dt, SceneInGame scene, int step) {
        Instance instance = scene.instance;
        World world = instance.world;
        InputConfig inputConfig = instance.inputConfig;

        switch (step) {
            case 0 -> {
                // Lock movement keys
                inputConfig.getBind("moveUp").lock();
                inputConfig.getBind("moveLeft").lock();
                inputConfig.getBind("moveRight").lock();
                inputConfig.getBind("moveDown").lock();

                world.prompt(new TextPrompt("Tutorial", "Welcome to the tutorial. Lets start with the basics.", this::nextStep, 3));
            }
            case 1 -> world.prompt(new TextPrompt("Tutorial", "In order to quickly skip waiting time on dialogs like these press " + BindLocalizer.getActualBindName(inputConfig.getBind("skipDialog")) + ".", this::nextStep, TextPrompt.MANUAL_SKIP));
            case 2 -> world.prompt(new TextPrompt("Tutorial", "Using " + BindLocalizer.getActualBindName(inputConfig.getBind("skipDialog")) + " you can also skip the typing process of dialogs like these. Try it now! Noooooow imaaaaagiiiiinnneeeee sooooommeeeee looooooonggg teeeeexxxxttt.", this::nextStep, TextPrompt.MANUAL_SKIP));
            case 3 -> world.prompt(new TextPrompt("Tutorial", "You can move your character with " + BindLocalizer.getActualBindName(inputConfig.getBind("moveUp")) + ", " + BindLocalizer.getActualBindName(inputConfig.getBind("moveLeft")) + ", " + BindLocalizer.getActualBindName(inputConfig.getBind("moveDown")) + " and " + BindLocalizer.getActualBindName(inputConfig.getBind("moveRight")) + ".", this::nextStep, 3));
            case 4 -> {
                // Reset distance
                this.distanceMoved = 0.0;

                // Unlock movement keys
                inputConfig.getBind("moveUp").unlock();
                inputConfig.getBind("moveLeft").unlock();
                inputConfig.getBind("moveRight").unlock();
                inputConfig.getBind("moveDown").unlock();

                world.prompt(new TextPrompt("Tutorial", "Try it!", this::nextStep, TextPrompt.UNSKIPPABLE));
            }
            case 5 -> world.prompt(new TextPrompt("Tutorial", "Well done.", this::nextStep, 3));
            case 6 -> world.prompt(new TextPrompt("Tutorial", "You have probably noticed the crosshair on your screen. Move it with your " + KeyBind.Device.MOUSE.name() + "!", this::nextStep, TextPrompt.UNSKIPPABLE));
            case 7 -> {
                // Set waypoint
                world.waypoint = new Vec2D(Tile.TILE_SIZE * 25, Tile.TILE_SIZE * 28);

                // Spawn ant
                this.timesTargetHit = 0;
                this.tutorialTarget = new EntityAnt(world.waypoint) {
                    @Override
                    public boolean collide(Scene s, Entity entity) {
                        if (entity instanceof EntityProjectile proj && proj.getShooter() == scene.player) {
                            TutorialStory.this.timesTargetHit++;

                            if (TutorialStory.this.step == 8) {
                                nextStep();
                            }
                        }

                        return super.collide(scene, entity);
                    }
                };
                world.entitiesLoaded.add(this.tutorialTarget);


                world.prompt(new TextPrompt("Tutorial", "Now it's time to let the action begin. Head to the waypoint.", this::nextStep, TextPrompt.UNSKIPPABLE));
            }
            case 8 -> world.prompt(new TextPrompt("Tutorial", "Shoot the ant while aiming on it and pressing " + BindLocalizer.getActualBindName(inputConfig.getBind("shoot")) + ".", this::nextStep, TextPrompt.UNSKIPPABLE));
            case 9 -> {
                world.prompt(new TextPrompt("Tutorial", "Great work!", this::nextStep, TextPrompt.UNSKIPPABLE));
                this.tutorialTarget.removeWhenHidden();
            }
        }
    }

    private double distanceMoved;

    private Vec2D mousePosBefore;

    @Override
    protected void updateStep(Graphics2D g2d, double dt, SceneInGame scene, int step) {
        EntityPlayer player = scene.player;
        World world = scene.instance.world;

        // Don't allow movement below step 4
        if (this.step < 4) {
            player.getMotion().multiply(0);
        }

        if (this.step == 4) {
            this.distanceMoved += player.getMotion().length();

            if (this.distanceMoved >= Tile.TILE_SIZE * 3) {
                nextStep();
            }
        }

        if (this.step == 6) {
            Vec2D currentPos = scene.mousePosition;
            if (this.mousePosBefore != null && scene.hasPromptRevealFinished()) {
                Vec2D delta = currentPos.clone().subtract(this.mousePosBefore);
                this.distanceMoved += delta.length();
                if (this.distanceMoved >= scene.scaleToDisplay(Tile.TILE_SIZE * 80)) {
                    nextStep();
                }
            }
            this.mousePosBefore = currentPos.clone();
        }

        if (this.step == 7 && world.waypoint != null) {
            double distance = player.getBoundingBox().center().distanceSqrt(world.waypoint);

            if (distance <= Tile.TILE_SIZE * 4) {
                nextStep();
            }
        }

        if (this.step >= 7 && world.entitiesLoaded.contains(tutorialTarget)) {
            // Freeze target
            tutorialTarget.getMotion().multiply(0.0); // No movement
            tutorialTarget.shootIdleTime = 0.0; // No shooting
        }
    }

}
