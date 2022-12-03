package de.yonedash.solity.item;

import de.yonedash.solity.resource.Texture;

public class ItemWeapon extends Item {

    private final double attackDistance, attackDamage;
    private final boolean isThrown;

    public ItemWeapon(String name, Texture texture, double attackDistance, double attackDamage, boolean isThrown) {
        super(name, texture);
        this.attackDistance = attackDistance;
        this.attackDamage = attackDamage;
        this.isThrown = isThrown;
    }

    public double getAttackDamage() {
        return attackDamage;
    }

    public double getAttackDistance() {
        return attackDistance;
    }

    public boolean isThrown() {
        return isThrown;
    }

}
