package de.yonedash.solity;

// This class stores 2x Vec2D
// 1. for x & y position
// 2. for width & height
// and has utility functions to handle collisions
public class BoundingBox {

    public Vec2D position, size;

    public BoundingBox(Vec2D position, Vec2D size) {
        this.position = position;
        this.size = size;
    }

    // Returns Vec2D with intersection distance
    public Vec2D intersect(BoundingBox boundingBox) {
        Vec2D result = new Vec2D(0, 0);

        // We want to use integers for better performance
        // and want to floor the values in order to prevent any bugs

//        // Store floored x & y positions
//        int x1 = (int) Math.floor(this.position.x);
//        int x2 = (int) Math.floor(boundingBox.position.x) - 1;
//        int y1 = (int) Math.floor(this.position.y);
//        int y2 = (int) Math.floor(boundingBox.position.y) - 1;
//
//        // Store floored width & heights
//        int w1 = (int) Math.floor(this.size.x);
//        int w2 = (int) Math.floor(boundingBox.size.x);
//        int h1 = (int) Math.floor(this.size.y);
//        int h2 = (int) Math.floor(boundingBox.size.y);

        // Store floored x & y positions
        double x1 = (this.position.x);
        double x2 = (boundingBox.position.x);
        double y1 = (this.position.y);
        double y2 = (boundingBox.position.y);

        // Store floored width & heights
        double w1 = (this.size.x);
        double w2 = (boundingBox.size.x);
        double h1 = (this.size.y);
        double h2 = (boundingBox.size.y);

        // Check if bounding boxes are intersecting
        if (x1 + w1 >= x2 && x1 <= x2 + w2 // horizontally inside other bounding box
            && y1 + h1 >= y2 && y1 <= y2 + h2 // vertically inside other bounding box
            ) {
            // Calculate distance between bounding box & left and right side of other bounding box
            double distanceToLeftSide = x1 + w1 - x2;
            double distanceToRightSide = x1 - x2 - w2;
            result.x = Math.abs(distanceToLeftSide) > Math.abs(distanceToRightSide)
                    ? distanceToRightSide : distanceToLeftSide;

            // Calculate distance between bounding box & bottom and top side of other bounding box
            double distanceToBottomSide = y1 + h1 - y2;
            double distanceToTopSide = y1 - y2 - h2;
            result.y = Math.abs(distanceToTopSide) > Math.abs(distanceToBottomSide)
                    ? distanceToBottomSide : distanceToTopSide;

            // Return result
            return result;
        }

        // If bounding boxes are not intersecting, return null
        return null;
    }

    // Check if bounding boxes are colliding
    public boolean isColliding(BoundingBox boundingBox, double tolerance) {
        // We want to use integers for better performance
        // and want to floor the values in order to prevent any bugs

        // Store x & y positions
        double x1 = (this.position.x);
        double x2 = (boundingBox.position.x);
        double y1 = (this.position.y);
        double y2 = (boundingBox.position.y);

        // Store width & heights
        double w1 = (this.size.x);
        double w2 = (boundingBox.size.x);
        double h1 = (this.size.y);
        double h2 = (boundingBox.size.y);

        // Check if bounding boxes are intersecting
        return x1 + w1 >= x2 - tolerance && x1 <= x2 + w2 + tolerance // horizontally inside other bounding box
                && y1 + h1 >= y2 - tolerance && y1 <= y2 + h2 + tolerance; // vertically inside other bounding box

    }

    // Check if bounding box has vec2D inside
    public boolean contains(Vec2D vec2D) {
        // We want to use integers for better performance
        // and want to floor the values in order to prevent any bugs

        // Store floored x & y positions
        int x1 = (int) Math.floor(this.position.x);
        int x2 = (int) Math.floor(vec2D.x);
        int y1 = (int) Math.floor(this.position.y);
        int y2 = (int) Math.floor(vec2D.y);

        // Store floored width & heights
        int w1 = (int) Math.floor(this.size.x);
        int h1 = (int) Math.floor(this.size.y);

        // Check if x2 & y2 are insided of the bounding box
        return x1 + w1 >= x2 && x1 <= x2 // horizontally inside other bounding box
                && y1 + h1 >= y2 && y1 <= y2; // vertically inside other bounding box

    }

    // Handle collision with bounding box, prevent bounding boxes from moving into each other
    // Returns result
    public boolean handleCollision(BoundingBox boundingBox) {
        // Get intersection distances of each other
        Vec2D distances = this.intersect(boundingBox);

        // Check if there even is a collision
        if (distances != null) {
            // Check which distance is smaller (to prevent from correcting wrong position) and correct position
            if (Math.abs(distances.x) < Math.abs(distances.y))
                this.position.x -= distances.x;
            else
                this.position.y -= distances.y;

            return true;
        }

        return false;
    }

    // Returns center of bounding box
    public Vec2D center() {
        return new Vec2D(
                this.position.x + this.size.x * 0.5,
                this.position.y + this.size.y * 0.5
        );
    }

    // Adds bounding box values
    public BoundingBox add(BoundingBox boundingBox) {
        this.position.add(boundingBox.position);
        this.size.add(boundingBox.size);
        return this;
    }

    // Scale
    public BoundingBox scale(double factor) {
        Vec2D originSize = this.size.clone();
        this.size.multiply(factor);
        double dx = this.size.x - originSize.x;
        double dy = this.size.y - originSize.y;
        this.position.subtract(new Vec2D(dx, dy).multiply(0.5));
        return this;
    }

    // Adds all absolute sums to a Vec2D, useful for text bounds
    public Vec2D abs() {
        return new Vec2D(
                Math.abs(this.position.x) + Math.abs(this.size.x),
                Math.abs(this.position.y) + Math.abs(this.size.y)
        );
    }

    @Override
    public BoundingBox clone() {
        BoundingBox clone = new BoundingBox(null, null);
        clone.position = this.position.clone();
        clone.size = this.size.clone();
        return clone;
    }
}
