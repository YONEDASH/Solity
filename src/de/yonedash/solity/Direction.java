package de.yonedash.solity;

public enum Direction {

    NORTH, EAST, SOUTH, WEST;

    public static Direction getDirectionFromRotation(double rotationDeg) {
        rotationDeg -= 90;

        if (rotationDeg >= -45.0 && rotationDeg <= 45.0)
            return Direction.NORTH;
        else if (rotationDeg >= -180.0 + 45.0 && rotationDeg <= 0 - 45.0)
            return Direction.WEST;
        else if (rotationDeg >=  0 + 45.0 && rotationDeg <= 180.0 - 45.0)
            return Direction.EAST;
        return Direction.SOUTH;
    }

}
