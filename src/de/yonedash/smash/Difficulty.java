package de.yonedash.smash;

public enum Difficulty {

    VETERAN("veteran", 2.0), NORMAL("normal", 1.0), EXPLORER("explorer", 0.5);

    public final String name;
    public final double value;

    Difficulty(String name, double value) {
        this.name = name;
        this.value = value;
    }

}
