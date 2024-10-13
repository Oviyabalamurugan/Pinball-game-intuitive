package inkball;

import processing.core.PVector;

public class Ball {
    private PVector position;
    private PVector velocity;
    private float size;  // Size represents the diameter of the ball
    private int color;
    private boolean removed;
    private float attractionFactor;

    private static final float MAX_SPEED = 5;
    private static final float INITIAL_DIAMETER = 24;  // Ball diameter starts at 24 pixels (12-pixel radius)

    public Ball(float x, float y, int color) {
        this.position = new PVector(x, y);
        this.velocity = new PVector(App.random.nextBoolean() ? 2 : -2, App.random.nextBoolean() ? 2 : -2);
        this.size = INITIAL_DIAMETER;  // Set initial ball size
        this.color = color;
        this.removed = false;
        this.attractionFactor = 0;
    }

    public void move() {
        position.add(velocity);
    }

    public void reflect(PVector normal) {
        PVector n = normal.copy().normalize();
        velocity.sub(PVector.mult(n, 2 * PVector.dot(velocity, n)));
    }

    public void reverseX() {
        velocity.x *= -1;
    }

    public void reverseY() {
        velocity.y *= -1;
    }

    public PVector getPosition() {
        return position.copy();
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public float getRadius() {
        return size / 2;  // The radius is half the diameter
    }

    public PVector getVelocity() {
        return velocity.copy();
    }

    public void setVelocity(PVector newVelocity) {
        this.velocity = newVelocity.copy();
    }

    public void setPosition(PVector newPosition) {
        this.position = newPosition.copy();
    }

    public void setSize(float newSize) {
        this.size = newSize;
    }

    public void applyForce(PVector force) {
        velocity.add(force);
        if (velocity.mag() > MAX_SPEED) {
            velocity.normalize();
            velocity.mult(MAX_SPEED);
        }
    }

    public void updateAttractionFactor(Hole hole) {
        float distance = PVector.dist(position, hole.getPosition());
        attractionFactor = Math.max(0, 1 - (distance / 32));
    }

    public void remove() {
        this.removed = true;
    }

    public boolean isRemoved() {
        return removed;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    // Display the ball with the updated size
    public void display(App app) {
        if (!removed) {
            app.image(app.getBallImage(color), position.x - size / 2, position.y - size / 2, size, size);  // Draw ball with updated size
        }
    }
}
