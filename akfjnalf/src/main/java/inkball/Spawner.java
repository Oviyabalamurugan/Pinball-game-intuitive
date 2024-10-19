package inkball;

import processing.core.PImage;  // Import PImage

public class Spawner {
    private float x, y;
    private PImage image;

    // Constructor with x, y, and image
    public Spawner(float x, float y, PImage image) {
        this.x = x;
        this.y = y;
        this.image = image;
    }

    // Display method to draw the spawner image
    public void display(App app) {
        if (image != null) {
            app.image(image, x, y, App.CELLSIZE, App.CELLSIZE);
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
