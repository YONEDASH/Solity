package de.yonedash.solity.entity;

import de.yonedash.solity.*;
import de.yonedash.solity.graphics.GraphicsUtils;
import de.yonedash.solity.item.Item;
import de.yonedash.solity.resource.Texture;
import de.yonedash.solity.graphics.TextureAnimated;
import de.yonedash.solity.graphics.TextureAtlas;
import de.yonedash.solity.scene.Scene;
import de.yonedash.solity.scene.SceneInWorld;

import java.awt.*;

public abstract class EntityCharacter extends EntityBase implements EntityDamageable {

    protected int animationState;

    protected Item itemInHand;
    protected double health, maxHealth;

    protected double texScale;

    private double healthDisplayed;

    public EntityCharacter(BoundingBox boundingBox) {
        super(boundingBox);
        this.texScale = 1.0;
    }

    protected Texture lastDrawnTexture;

    private double shake;

    @Override
    public void draw(Scene scene, Graphics2D g2d, double dt) {
        Texture texture = getTexture(scene);

        Vec2D shakeVec = null;
        if (this.shake > 0 && scene instanceof SceneInWorld sceneInWorld) {
            double d = this.shake / Constants.SHAKE_LENGTH;
            double scalar = d < 0.5 ? 1.0 - d * 2 : (d - 0.5) * 2;

            shakeVec = new Vec2D(Math.sin(this.shake), Math.cos(this.shake)).multiply(2).multiply(scalar);

            if (this instanceof EntityPlayer) {
                sceneInWorld.cameraPos.add(shakeVec.multiply(1 / scene.calculateDisplayScaleFactor()));
                shakeVec = null;
            } else
                shakeVec = shakeVec.multiply(scene.calculateDisplayScaleFactor());

            this.shake -= dt;
        }

        if (shakeVec != null) g2d.translate(shakeVec.x, shakeVec.y);

        // If texture was changed, restart animation
        if (texture != this.lastDrawnTexture && texture instanceof TextureAnimated textureAnimated)
                textureAnimated.restart();
        this.lastDrawnTexture = texture;

        double texSize = this.boundingBox.size.x * 1.25;
        double texAspectRatio = texture.getHeight() / (double) texture.getWidth();
        Vec2D vecSize = new Vec2D(texSize, texSize * texAspectRatio).multiply(texScale);
        Vec2D texPos = new Vec2D(this.boundingBox.position.x + this.boundingBox.size.x * 0.5 - vecSize.x * 0.5,
                this.boundingBox.position.y - vecSize.y + this.boundingBox.size.y * 0.5);
        BoundingBox texBB = new BoundingBox(texPos, vecSize);

        // Draw shadow
        g2d.setStroke(new BasicStroke(scene.scaleToDisplay(4.0)));
        if (this instanceof EntityPlayer) {
            g2d.setColor(Color.GREEN.darker());
        } else if (this instanceof EntityEnemy) {
            g2d.setColor(Color.RED.darker());
        }
        g2d.drawOval(
                scene.scaleToDisplay(this.boundingBox.position.x),
                scene.scaleToDisplay(this.boundingBox.position.y),
                scene.scaleToDisplay(this.boundingBox.size.x),
                scene.scaleToDisplay(this.boundingBox.size.y)
        );

        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 75));
        g2d.fillOval(
                scene.scaleToDisplay(this.boundingBox.position.x),
                scene.scaleToDisplay(this.boundingBox.position.y),
                scene.scaleToDisplay(this.boundingBox.size.x),
                scene.scaleToDisplay(this.boundingBox.size.y)
        );

        g2d.drawImage(
                texture.getImage(),
                scene.scaleToDisplay(texBB.position.x),
                scene.scaleToDisplay(texBB.position.y),
                scene.scaleToDisplay(texBB.size.x),
                scene.scaleToDisplay(texBB.size.y),
                null
        );

        // Don't draw item in hand, not needed anymore
//        if (this.itemInHand != null) {
//            Texture itemTexture = this.itemInHand.getTexture();
//            double itemSize = texBB.size.average() * 0.3;
//            BoundingBox itemBB = new BoundingBox(texBB.center().clone().add(vecSize.clone().multiply(new Vec2D(0.5 * 0.8 * (viewDirection == Direction.WEST ? -1 : 1), 0.5 * 0.3))).subtract(new Vec2D(itemSize / 2.0, itemSize / 2.0)), new Vec2D(itemSize, itemSize));
//            Vec2D itemBBCenter = itemBB.center();
//            double rotationDegView = 0;
//            if (scene instanceof SceneInWorld sig)
//                rotationDegView = itemBBCenter.rotationTo(sig.mouseWorldPosition) + 90.0;
//            GraphicsUtils.rotate(g2d, rotationDegView, scene.scaleToDisplay(itemBBCenter.x), scene.scaleToDisplay(itemBBCenter.y));
//            g2d.drawImage(
//                    itemTexture.getImage(),
//                    scene.scaleToDisplay(itemBB.position.x),
//                    scene.scaleToDisplay(itemBB.position.y),
//                    scene.scaleToDisplay(itemBB.size.x),
//                    scene.scaleToDisplay(itemBB.size.y),
//                    null
//            );
//            GraphicsUtils.rotate(g2d, -rotationDegView, scene.scaleToDisplay(itemBBCenter.x), scene.scaleToDisplay(itemBBCenter.y));
//        }

        if (shakeVec != null) g2d.translate(-shakeVec.x, -shakeVec.y);
    }

    public void drawHealthBar(Graphics2D g2d, Scene scene) {
        Texture texture = getTexture(scene);
        double texSize = this.boundingBox.size.x * 1.25;
        double texAspectRatio = texture.getHeight() / (double) texture.getWidth();
        Vec2D vecSize = new Vec2D(texSize, texSize * texAspectRatio);
        Vec2D texPos = new Vec2D(this.boundingBox.position.x + this.boundingBox.size.x * 0.5 - vecSize.x * 0.5,
                this.boundingBox.position.y - vecSize.y + this.boundingBox.size.y * 0.5);
        BoundingBox texBB = new BoundingBox(texPos, vecSize);

        // Draw health bar
        double healthBarOffsetY = 10.0;
        double healthBarWidth = boundingBox.size.x * 1.1;
        double healthBarHeight = 12.0;
        double healthBarLineThickness = 1;
        double healthBarArc = 15.0;
        this.healthDisplayed += (this.health - this.healthDisplayed) * Constants.HUD_VALUE_ANIMATION_SPEED; // Animates health add/remove

        // Background
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRoundRect(
                scene.scaleToDisplay(texBB.position.x + texBB.size.x * 0.5 - healthBarWidth * 0.5),
                scene.scaleToDisplay(this.boundingBox.position.y + this.boundingBox.size.y + healthBarOffsetY),
                scene.scaleToDisplay(healthBarWidth),
                scene.scaleToDisplay(healthBarHeight),
                scene.scaleToDisplay(healthBarArc),
                scene.scaleToDisplay(healthBarArc)
        );
        g2d.setStroke(new BasicStroke(scene.scaleToDisplay(healthBarLineThickness * 6)));
        g2d.drawRoundRect(
                scene.scaleToDisplay(texBB.position.x + texBB.size.x * 0.5 - healthBarWidth * 0.5),
                scene.scaleToDisplay(this.boundingBox.position.y + this.boundingBox.size.y + healthBarOffsetY),
                scene.scaleToDisplay(healthBarWidth),
                scene.scaleToDisplay(healthBarHeight),
                scene.scaleToDisplay(healthBarArc),
                scene.scaleToDisplay(healthBarArc)
        );

        // Colored Bar
        float f = (float) Math.max(Math.min(this.healthDisplayed / this.maxHealth, 1), 0);
        Color color = new Color(f, 1f - f, 0f,1f).brighter();
        g2d.setColor(color);
        g2d.fillRoundRect(
                scene.scaleToDisplay(texBB.position.x + texBB.size.x * 0.5 - healthBarWidth * 0.5),
                scene.scaleToDisplay(this.boundingBox.position.y + this.boundingBox.size.y + healthBarOffsetY),
                scene.scaleToDisplay(healthBarWidth * f),
                scene.scaleToDisplay(healthBarHeight),
                scene.scaleToDisplay(healthBarArc),
                scene.scaleToDisplay(healthBarArc)
        );

        // Foreground
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(scene.scaleToDisplay(healthBarLineThickness)));
        g2d.drawRoundRect(
                scene.scaleToDisplay(texBB.position.x + texBB.size.x * 0.5 - healthBarWidth * 0.5),
                scene.scaleToDisplay(this.boundingBox.position.y + this.boundingBox.size.y + healthBarOffsetY),
                scene.scaleToDisplay(healthBarWidth),
                scene.scaleToDisplay(healthBarHeight),
                scene.scaleToDisplay(healthBarArc),
                scene.scaleToDisplay(healthBarArc)
        );
    }

    private Texture getTexture(Scene scene) {
        return switch (this.viewDirection) {
            case NORTH -> scene.instance.atlas.getTexture(getClass(), 0x001 + this.animationState);
            case EAST -> scene.instance.atlas.getTexture(getClass(), 0x002 + this.animationState);
            case SOUTH -> scene.instance.atlas.getTexture(getClass(), 0x003 + this.animationState);
            case WEST -> scene.instance.atlas.getTexture(getClass(), 0x004 + this.animationState);
        };
    }

    @Override
    public void update(Scene scene, double dt) {
        super.update(scene, dt);

        Vec2D moveMotion = Vec2D.zero();

        move(scene, dt, moveMotion);

        this.motion.add(moveMotion);

        updateViewDirection(scene, moveMotion);
        updateAnimationState(scene.instance.atlas, !moveMotion.isZero());
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

    @Override
    public double getHealth() {
        return health;
    }

    @Override
    public double getMaxHealth() {
        return maxHealth;
    }

    @Override
    public void setHealth(double health) {
        if (health < this.health)
            shake = Constants.SHAKE_LENGTH;
        this.health = health;
    }

    @Override
    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

}
