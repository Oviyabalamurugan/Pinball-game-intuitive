package inkball;

import processing.core.PVector;

public class Ball {
    private PVector position;
    private PVector velocity;
    private float size;
    private int color;
    private boolean removed;
    private float attractionFactor;

    public Ball(float x, float y, int color) {
        this.position = new PVector(x, y);
        this.velocity = new PVector(App.random.nextBoolean() ? 2 : -2, App.random.nextBoolean() ? 2 : -2);
        this.size = App.CELLSIZE;
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
        return size / 2;
    }

    public PVector getVelocity() {
        return velocity.copy();
    }
    private static final float MAX_SPEED = 5;

    // ... other Ball methods ...

    public void applyForce(PVector force) {
        velocity.add(force);
        // Limit the maximum speed
        if (velocity.mag() > MAX_SPEED) {
            velocity.normalize();
            velocity.mult(MAX_SPEED);
        }
        System.out.println("Ball velocity after force: " + velocity);
    }
    public void display(App app) {
        if (!removed) {
            app.image(app.getBallImage(color), position.x - size/2, position.y - size/2, size, size);
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


    
}