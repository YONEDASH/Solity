package de.yonedash.smash.entity;

import de.yonedash.smash.*;
import de.yonedash.smash.config.InputConfig;
import de.yonedash.smash.config.KeyBind;
import de.yonedash.smash.graphics.LightSource;

import java.awt.*;
import java.awt.event.KeyEvent;

public class EntityPlayer extends EntityCharacter {

    private int dashes;

    public EntityPlayer(BoundingBox boundingBox) {
        super(boundingBox);
        this.lightSource = new LightSource(Color.WHITE, 25.0);
    }


    @Override
    public void draw(Scene scene, Graphics2D g2d, double dt) {
        super.draw(scene, g2d, dt);
    }

    @Override
    public void update(Scene scene, double dt) {
        super.update(scene, dt);
    }

    @Override
    protected void move(Scene scene, double dt, Vec2D moveMotion) {
        double moveSpeed = scene.time(0.3, dt);

        Input input = scene.instance.display.getInput();
        InputConfig inputConfig = scene.instance.inputConfig;
        KeyBind moveUp = inputConfig.getBind("moveUp");
        KeyBind moveLeft = inputConfig.getBind("moveLeft");
        KeyBind moveRight = inputConfig.getBind("moveRight");
        KeyBind moveDown = inputConfig.getBind("moveDown");

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
    }

    @Override
    protected void updateViewDirection(Scene scene, Vec2D moveMotion) {
        if (scene instanceof SceneInGame sig)
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

}
