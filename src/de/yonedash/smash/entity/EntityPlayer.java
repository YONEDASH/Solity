package de.yonedash.smash.entity;

import de.yonedash.smash.*;
import de.yonedash.smash.config.InputConfig;
import de.yonedash.smash.config.KeyBind;
import de.yonedash.smash.graphics.LightSource;
import de.yonedash.smash.progression.skills.SkillDash;
import de.yonedash.smash.scene.Scene;
import de.yonedash.smash.scene.SceneInWorld;

import java.awt.*;

public class EntityPlayer extends EntityCharacter {

    private float dashesLeft;
    private double timeDashingLeft, dashParticleDelay;

    public EntityPlayer(Vec2D position) {
        super(new BoundingBox(position.clone(), new Vec2D(50, 20).multiply(1.125)));
        this.texScale = 1.5;
        this.lightSource = new LightSource(Color.WHITE, 25.0);
    }

    @Override
    public void draw(Scene scene, Graphics2D g2d, double dt) {
        super.draw(scene, g2d, dt);
    }

    @Override
    public void update(Scene scene, double dt) {
        super.update(scene, dt);

        // Slowly recharge dashes
        SkillDash skillDash = scene.instance.world.saveGame.getSkills().skillDash;
        dashesLeft = Math.min((dashesLeft += scene.time(0.000025f * skillDash.getChargeFactor(), dt)), skillDash.getMaximumDashCount());
    }

    @Override
    protected void move(Scene scene, double dt, Vec2D moveMotion) {
        double moveSpeed = scene.time(0.2, dt);

        Input input = scene.instance.display.getInput();
        InputConfig inputConfig = scene.instance.inputConfig;
        KeyBind moveUp = inputConfig.getBind("moveUp");
        KeyBind moveLeft = inputConfig.getBind("moveLeft");
        KeyBind moveRight = inputConfig.getBind("moveRight");
        KeyBind moveDown = inputConfig.getBind("moveDown");
        KeyBind dash = inputConfig.getBind("dash");

        // Player Axis Movement

        if (!moveUp.isLocked() && input.isBindPressed(moveUp)) {
            moveMotion.y -= moveSpeed;
        }
        if (!moveDown.isLocked() && input.isBindPressed(moveDown)) {
            moveMotion.y += moveSpeed;
        }
        if (!moveRight.isLocked() && input.isBindPressed(moveRight)) {
            moveMotion.x += moveSpeed;
        }
        if (!moveLeft.isLocked() && input.isBindPressed(moveLeft)) {
            moveMotion.x -= moveSpeed;
        }
        // To prevent too fast speeds when moving diagonally, divide by 1.1 if thats the case
        if (Math.abs(moveMotion.x) > 0.01 && Math.abs(moveMotion.y) > 0.01)
            moveMotion.multiply(1 / 1.1);

        // Dashing

        SkillDash skillDash = scene.instance.world.saveGame.getSkills().skillDash;
        final double dashTime = 300.0;
        final double dashSpeedFactor = skillDash.getMoveSpeedFactor();
        final double dashSustainFactor = skillDash.getSustainFactor();
        if (!dash.isLocked() && input.isBindPressed(dash)
                && this.dashesLeft >= 1 // has dashes left
                && this.dashesLeft <= skillDash.getMaximumDashCount() // prevent any cheats/bugs
                && this.timeDashingLeft <= 0 // only allow to dash if not already dashing
                && moveMotion.length() > 0.01 // and player is moving
        ) {

            // Raycast to not dash player through object
            World world = scene.instance.world;
            double rotation = moveMotion.angle();
            LevelObject casted = MathUtils.rayCast(world, this.boundingBox, rotation, 10.0, moveMotion.clone().multiply(dashSpeedFactor).length());
            if (casted == null) {
                this.dashParticleDelay = 0.0;
                this.timeDashingLeft = dashTime; //ms
                this.dashesLeft--;

                // Lock key to not be held down to spam dashes
                dash.lock();
            }
            // Do nothing if player is not eligible to dash
        }

        if (this.timeDashingLeft > 0) {
            // Cancel dash if not moving
            if (moveMotion.length() < 0.01) {
                this.timeDashingLeft = 0.0;
                return;
            }

            this.timeDashingLeft -= dt;
            double f = dashSustainFactor * (this.timeDashingLeft / dashTime);
            double scalar = 1.0 + ((dashSpeedFactor - 1.0) * Math.min(1, f));

            // Raycast to not dash player through object
            World world = scene.instance.world;
            double rotation = moveMotion.angle();
            LevelObject casted = MathUtils.rayCast(world, this.boundingBox, rotation, 10.0, moveMotion.clone().multiply(scalar).length());

            if (casted != null) {
                this.timeDashingLeft = 0.0;
            } else {
                moveMotion.multiply(scalar);

                if ((dashParticleDelay += dt) > 20.0) {
                    double particleSize = 50.0;
                    EntityParticle particle = new EntityParticle(
                            new BoundingBox(this.boundingBox.center().clone().subtract(new Vec2D(particleSize / 2, particleSize / 2)), new Vec2D(particleSize, particleSize)),
                            scene.instance.atlas.animSlash, rotation, Math.sqrt(moveMotion.length()) / 100.0, 150.0, getZ() + 0, true);
                    world.entitiesLoaded.add(particle);
                    this.dashParticleDelay = 0.0;
                }

            }
        }

    }

    @Override
    protected void updateViewDirection(Scene scene, Vec2D moveMotion) {
        if (scene instanceof SceneInWorld sig)
            this.viewDirection = Direction.getDirectionFromRotation(sig.mouseWorldPosition.rotationTo(this.boundingBox.center()));
    }

    @Override
    public boolean collide(Scene scene, LevelObject levelObject, BoundingBox objectBoundingBox) {
        return true;
    }

    @Override
    public boolean collide(Scene scene, Entity entity) {
        return true;
    }

    @Override
    public int getZ() {
        return 0;
    }

    public void setDashesLeft(float dashesLeft) {
        this.dashesLeft = dashesLeft;
    }

    public float getDashesLeft() {
        return dashesLeft;
    }
}
