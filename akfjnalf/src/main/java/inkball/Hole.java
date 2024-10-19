package inkball;

import processing.core.PVector;
import processing.core.PImage;
import processing.data.JSONObject;

public class Hole extends Tile {
    private static final float ATTRACTION_RADIUS = 32;
    private static final float ATTRACTION_FORCE = 0.005f;
    private static final float CAPTURE_RADIUS = 16;
    
    public boolean captures(Ball ball) {
        float distance = PVector.dist(ball.getPosition(), position);
        return distance < CAPTURE_RADIUS;
    }

    public void applyAttraction(Ball ball) {
        PVector attractionVector = PVector.sub(position, ball.getPosition());
        attractionVector.normalize();
        attractionVector.mult(0.5f); // Adjust this value to change attraction strength
        ball.applyForce(attractionVector);
        System.out.println("Applying attraction to ball. Force: " + attractionVector);
    }




   
    
    public Hole(float x, float y, int color, App app) {
        super(x, y, color, app.getHoleImage(color));
        this.position = new PVector(x + App.CELLSIZE, y + App.CELLSIZE); // Center of 2x2 tile
    }

    public boolean attracts(Ball ball) {
        float distance = PVector.dist(ball.getPosition(), position);
        return distance < ATTRACTION_RADIUS;
    }

   

   
    @Override
    public void display(App app) {
        PImage holeImage = app.getHoleImage(getColor());
        if (holeImage != null) {
            app.image(holeImage, position.x - App.CELLSIZE, position.y - App.CELLSIZE, App.CELLSIZE * 2, App.CELLSIZE * 2);
        } else {
            // Fallback rendering if image is not available
            app.noStroke();
            app.fill(200); // Light grey color
            app.ellipse(position.x, position.y, App.CELLSIZE * 2, App.CELLSIZE * 2);
        }
    }

    @Override
    public int calculateScoreChange(Ball ball, App app) {
        JSONObject config = app.getConfig();
        boolean success = ball.getColor() == getColor() || ball.getColor() == 0 || getColor() == 0;

        if (success) {
            int scoreIncrease = config.getJSONObject("score_increase_from_hole_capture").getInt(getColorName(ball.getColor()));
            float modifier = config.getFloat("score_increase_from_hole_capture_modifier", 1.0f);
            return (int) (scoreIncrease * modifier);
        } else {
            int scoreDecrease = config.getJSONObject("score_decrease_from_wrong_hole").getInt(getColorName(ball.getColor()));
            float modifier = config.getFloat("score_decrease_from_wrong_hole_modifier", 1.0f);
            return (int) (-scoreDecrease * modifier);
        }
    }

    private String getColorName(int colorIndex) {
        switch (colorIndex) {
            case 0: return "grey";
            case 1: return "orange";
            case 2: return "blue";
            case 3: return "green";
            case 4: return "yellow";
            default: return "grey";
        }
    }
}