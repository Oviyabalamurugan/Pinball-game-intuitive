package inkball;

import processing.core.PImage;
import processing.core.PVector;

public class Wall extends Tile {
   

    public Wall(float x, float y, int color, PImage image) {
        super(x, y, color, image);
    }

    @Override
    public boolean collidesWith(Ball ball) {
        float closestX = Math.max(position.x, Math.min(ball.getX(), position.x + App.CELLSIZE));
        float closestY = Math.max(position.y, Math.min(ball.getY(), position.y + App.CELLSIZE));
        
        float distanceX = ball.getX() - closestX;
        float distanceY = ball.getY() - closestY;
        
        float distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);
        return distanceSquared < (ball.getRadius() * ball.getRadius());
    }

    @Override
    public PVector getNormal(Ball ball) {
        PVector center = new PVector(position.x + App.CELLSIZE / 2, position.y + App.CELLSIZE / 2);
        PVector normal = PVector.sub(ball.getPosition(), center);
        normal.normalize();
        return normal;
    }
















    @Override
    public void display(App app) {
        app.image(image, position.x, position.y, App.CELLSIZE, App.CELLSIZE);
    }

    
    
}