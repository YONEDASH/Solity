package de.yonedash.smash.entity;

import de.yonedash.smash.*;
import de.yonedash.smash.item.Item;
import de.yonedash.smash.resource.Texture;
import de.yonedash.smash.resource.TextureAnimated;
import de.yonedash.smash.resource.TextureAtlas;

import java.awt.*;

public abstract class EntityCharacter extends EntityBase {

    protected int animationState;

    protected Item itemInHand;
    protected double health;

    public EntityCharacter(BoundingBox boundingBox) {
        super(boundingBox);
    }

    protected Texture lastDrawnTexture;

    @Override
    public void draw(Scene scene, Graphics2D g2d, double dt) {
        Texture texture = switch (this.viewDirection) {
            case NORTH -> scene.instance.atlas.getTexture(getClass(), 0x001 + this.animationState);
            case EAST -> scene.instance.atlas.getTexture(getClass(), 0x002 + this.animationState);
            case SOUTH -> scene.instance.atlas.getTexture(getClass(), 0x003 + this.animationState);
            case WEST -> scene.instance.atlas.getTexture(getClass(), 0x004 + this.animationState);
        };

        // If texture was changed, restart animation
        if (texture != this.lastDrawnTexture && texture instanceof TextureAnimated textureAnimated)
                textureAnimated.restart();
        this.lastDrawnTexture = texture;

        double texSize = this.boundingBox.size.x * 1.25;
        Vec2D vecSize = new Vec2D(texSize, texSize);

        // Draw shadow
//        g2d.setColor(new Color(0, 0, 0, 100));
//        g2d.fillOval(
//                scene.scaleToDisplay(this.boundingBox.position.x),
//                scene.scaleToDisplay(this.boundingBox.position.y),
//                scene.scaleToDisplay(this.boundingBox.size.x),
//                scene.scaleToDisplay(this.boundingBox.size.y)
//        );

        g2d.drawImage(
                texture.getBufferedImage(),
                scene.scaleToDisplay(this.boundingBox.position.x + this.boundingBox.size.x * 0.5 - vecSize.x * 0.5),
                scene.scaleToDisplay(this.boundingBox.position.y - vecSize.y + this.boundingBox.size.y * 0.5),
                scene.scaleToDisplay(vecSize.x),
                scene.scaleToDisplay(vecSize.y),
                null
        );

        if (this.itemInHand != null) {
            Texture itemTexture = this.itemInHand.getTexture();
            double itemSize = this.boundingBox.size.average() * 0.3;
            BoundingBox itemBB = new BoundingBox(this.boundingBox.center().clone().add(vecSize.clone().multiply(new Vec2D(0.5 * 0.8 * (viewDirection == Direction.WEST ? -1 : 1), 0.5 * 0.3))).subtract(new Vec2D(itemSize / 2.0, itemSize / 2.0)), new Vec2D(itemSize, itemSize));
            Vec2D itemBBCenter = itemBB.center();
            double rotationDegView = 0;
            if (scene instanceof SceneInGame sig)
                rotationDegView = itemBBCenter.rotationTo(sig.mouseWorldPosition) + 90.0;
            GraphicsUtils.rotate(g2d, rotationDegView, scene.scaleToDisplay(itemBBCenter.x), scene.scaleToDisplay(itemBBCenter.y));
            g2d.drawImage(
                    itemTexture.getBufferedImage(),
                    scene.scaleToDisplay(itemBB.position.x),
                    scene.scaleToDisplay(itemBB.position.y),
                    scene.scaleToDisplay(itemBB.size.x),
                    scene.scaleToDisplay(itemBB.size.y),
                    null
            );
            GraphicsUtils.rotate(g2d, -rotationDegView, scene.scaleToDisplay(itemBBCenter.x), scene.scaleToDisplay(itemBBCenter.y));
        }
    }



    @Override
    public void update(Scene scene, double dt) {
        Vec2D moveMotion = Vec2D.zero();

        move(scene, dt, moveMotion);

        this.motion.add(moveMotion);

        updateViewDirection(scene, moveMotion);
        updateAnimationState(scene.instance.atlas, !moveMotion.isZero());

        super.update(scene, dt);
    }

    protected abstract void move(Scene scene, double dt, Vec2D moveMotion);

    protected void updateViewDirection(Scene scene, Vec2D moveMotion) {
        if (!moveMotion.isZero()) {
            if (Math.abs(moveMotion.x) > Math.abs(moveMotion.y)) {
                if (moveMotion.x > 0)
                    this.viewDirection = Direction.EAST;
                else
                    this.viewDirection = Direction.WEST;
            } else {
                if (moveMotion.y > 0)
                    this.viewDirection = Direction.SOUTH;
                else
                    this.viewDirection = Direction.NORTH;
            }
        }
    }

    protected void updateAnimationState(TextureAtlas atlas, boolean moving) {
        if (moving) {
            animationState = atlas.walkSubId;
        } else {
            animationState = atlas.idleSubId;
        }
    }

    public void setItemInHand(Item itemInHand) {
        this.itemInHand = itemInHand;
    }

    public Item getItemInHand() {
        return itemInHand;
    }

    @Override
    public boolean collide(Scene scene, LevelObject levelObject) {
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
