package de.yonedash.smash;

// This class is responsible for storing location data
public class Vec2D {

    public double x, y;

    public Vec2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Add, math operation for Vec2D
    public Vec2D add(Vec2D vec2D) {
        this.x += vec2D.x;
        this.y += vec2D.y;
        return this;
    }

    // Add certain distance in rotation
    public Vec2D add(double rotation, double distance) {
        double rad = Math.toRadians(rotation);
        return this.add(new Vec2D(distance * Math.cos(rad), distance * Math.sin(rad)));
    }

    // Subtract, math operation for Vec2D
    public Vec2D subtract(Vec2D vec2D) {
        this.x -= vec2D.x;
        this.y -= vec2D.y;
        return this;
    }

    // Multiply, math operation for Vec2D
    public Vec2D multiply(Vec2D vec2D) {
        this.x *= vec2D.x;
        this.y *= vec2D.y;
        return this;
    }

    // Scalar, math operation for Vec2D
    public Vec2D multiply(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    // Dot, dot product for Vec2D
    public double dot(Vec2D vec2D) {
        return this.x * vec2D.x + this.y * vec2D.y;
    }

    // Distance between both Vec2Ds
    public double distanceSqrt(Vec2D vec2D) {
        return Math.sqrt(Math.pow(this.x - vec2D.x, 2) + Math.pow(this.y - vec2D.y, 2));
    }

    // XY Distance between both Vec2Ds
    public double distanceAxis(Vec2D vec2D) {
        return Math.abs(this.x - vec2D.x) + Math.abs(this.y - vec2D.y);
    }

    // Calculate average of x & y
    public double average() {
        return (this.x + this.y) / 2.0;
    }

    // Calculate rotation needed to point at different vec2D
    public double rotationTo(Vec2D vec2D) {
        double theta = Math.atan2(vec2D.x - this.x, vec2D.y - this.y);
        // For some reason result of Math.atan2 is off by 90 degress and is inverted,
        // counteracted by following statements:
        return -Math.toDegrees(theta) + 90.0;
    }

    // Calculate length
    public double length() {
        return Math.sqrt((x * x) + (y * y));
    }

    // Returns whether x & y are zero
    public boolean isZero() {
        return this.x == 0.0 && this.y == 0.0;
    }

    // toString function, useful for debugging
    @Override
    public String toString() {
        return "Vec2D{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public Vec2D clone() {
        return new Vec2D(this.x, this. y);
    }

    public static Vec2D zero() {
        return new Vec2D(0, 0);
    }

    public static Vec2D fromAngle(double angle, double x) {
        return Vec2D.zero().add(angle, x);
    }
}
