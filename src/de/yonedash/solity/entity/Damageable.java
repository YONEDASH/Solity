package de.yonedash.solity.entity;

// Any entity class that implements this interface will be able
// to have health & be damaged
public interface Damageable {

    void damage(double health);
    void setHealth(double health);
    double getHealth();

}
