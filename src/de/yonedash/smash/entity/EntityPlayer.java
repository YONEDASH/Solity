package de.yonedash.smash.entity;

import de.yonedash.smash.*;

import java.awt.*;
import java.awt.event.KeyEvent;

public class EntityPlayer extends EntityCharacter  {

    private int dashes;

    public EntityPlayer(BoundingBox boundingBox) {
        super(boundingBox);
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
        Input input = scene.instance.display.getInput();
        double moveSpeed = scene.time(input.isKeyDown(KeyEvent.VK_SHIFT) ? 0.8 : 0.3, dt);
        if (input.isKeyDown(KeyEvent.VK_W)) {
            moveMotion.y -= moveSpeed;
        }
        if (input.isKeyDown(KeyEvent.VK_S)) {
            moveMotion.y += moveSpeed;
        }
        if (input.isKeyDown(KeyEvent.VK_D)) {
            moveMotion.x += moveSpeed;
        }
        if (input.isKeyDown(KeyEvent.VK_A)) {
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
