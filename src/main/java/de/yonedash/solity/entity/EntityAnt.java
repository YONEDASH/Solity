package de.yonedash.solity.entity;

import de.yonedash.solity.BoundingBox;
import de.yonedash.solity.Vec2D;
import de.yonedash.solity.graphics.LightSource;

import java.awt.*;

public class EntityAnt extends EntityEnemy {
    public EntityAnt(Vec2D position) {
        super(new BoundingBox(position, new Vec2D(60, 12)));
        this.maxHealth = 3.0;
        this.health = maxHealth;
        this.entityMoveSpeed = 0.09;
        this.shotDamage = 0.5;
        this.lightSource = new LightSource(Color.RED, 8);
    }
}
