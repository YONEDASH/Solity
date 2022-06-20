package de.yonedash.smash;

public enum ParticleType {
    LEAF(4500.0);

    public final double maxDelay;
    ParticleType(double maxDelay) {
        this.maxDelay = maxDelay;
    }
}
