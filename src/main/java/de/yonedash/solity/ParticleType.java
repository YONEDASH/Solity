package de.yonedash.solity;

public enum ParticleType {
    LEAF(3000.0, 5000.0);

    public final double maxDelay, timeAlive;
    ParticleType(double maxDelay, double timeAlive) {
        this.maxDelay = maxDelay;
        this.timeAlive = timeAlive;
    }
}
