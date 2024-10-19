package inkball;

import processing.core.PVector;
import processing.core.PImage;

public class Tile {
    protected PVector position;
    protected int color;
    protected PImage image;

    public Tile(float x, float y, int color, PImage image) {
        this.position = new PVector(x, y);
        this.color = color;
        this.image = image;
    }

   
    public void display(App app) {
        if (image != null) {
            
            app.image(image, position.x, position.y, App.CELLSIZE, App.CELLSIZE);
        } else {
            System.out.println("Tile image is null, drawing fallback rectangle");
            app.fill(200);
            app.rect(position.x, position.y, App.CELLSIZE, App.CELLSIZE);
        }
    }
    
    
    public PVector getPosition() {
        return position.copy();
    }

    public int getColor() {
        return color;
    }

    public boolean collidesWith(Ball ball) {
        float closestX = Math.max(position.x, Math.min(ball.getX(), position.x + App.CELLSIZE));
        float closestY = Math.max(position.y, Math.min(ball.getY(), position.y + App.CELLSIZE));
        
        float distanceX = ball.getX() - closestX;
        float distanceY = ball.getY() - closestY;
        
        float distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);
        return distanceSquared < (ball.getRadius() * ball.getRadius());
    }

    public PVector getNormal(Ball ball) {
        PVector center = new PVector(position.x + App.CELLSIZE / 2, position.y + App.CELLSIZE / 2);
        PVector normal = PVector.sub(ball.getPosition(), center);
        normal.normalize();
        return normal;
    }

    public int calculateScoreChange(Ball ball, App app) {
        return 0;
    }
}