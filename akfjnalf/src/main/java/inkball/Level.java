package inkball;

import processing.core.PVector;
import processing.data.JSONObject;
import processing.data.JSONArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Level {
    private App app;
    private JSONObject config;
    private List<Ball> balls;
    private List<Hole> holes;
    private List<Spawner> spawners;
    private List<Wall> walls;
    private List<String> nextBalls;
    private int remainingTime;
    private int spawnInterval;
    private int spawnCounter;
    private float scoreIncreaseModifier;
    private float scoreDecreaseModifier;
    private boolean levelCompleted;
    private boolean gameOver;
    private boolean timedOut;
    
    private List<PlayerLine> playerLines;
    private PlayerLine currentLine;

    public Level(JSONObject config, App app) {
        this.app = app;
        this.config = config;
        this.balls = new ArrayList<>();
        this.holes = new ArrayList<>();
        this.spawners = new ArrayList<>();
        this.walls = new ArrayList<>();
        this.nextBalls = new ArrayList<>();
        this.playerLines = new ArrayList<>();
        this.currentLine = null;
        loadLayout();
        initializeEntities();
    }

  private void loadLayout() {
    String layoutFile = config.getString("layout");
    String[] lines = app.loadStrings(layoutFile);

    for (int i = 0; i < lines.length; i++) {
        for (int j = 0; j < lines[i].length(); j++) {
            char c = lines[i].charAt(j);
            float x = j * App.CELLSIZE;
            float y = i * App.CELLSIZE + App.TOPBAR;

            switch (c) {
                case 'X':
                    walls.add(new Wall(x, y, 0, app.getWallImage(0)));
                    break;
                case '1':
                case '2':
                case '3':
                case '4':
                    int wallColor = Character.getNumericValue(c);
                    walls.add(new Wall(x, y, wallColor, app.getWallImage(wallColor)));
                    break;
                case 'S':
                    spawners.add(new Spawner(x, y, app.getSpawnerImage()));  // Correct usage
                    break;
                case 'H':
                    if (j + 1 < lines[i].length()) {
                        int holeColor = Character.getNumericValue(lines[i].charAt(j + 1));
                        holes.add(new Hole(x, y, holeColor, app));
                        j++;  // Skip the next character as it's the hole color
                    }
                    break;
                case 'B':
                    if (j + 1 < lines[i].length()) {
                        int ballColor = Character.getNumericValue(lines[i].charAt(j + 1));
                        balls.add(new Ball(x + App.CELLSIZE / 2, y + App.CELLSIZE / 2, ballColor));
                        j++;  // Skip the next character as it's the ball color
                    }
                    break;
            }
        }
    }
}



    private void initializeEntities() {
        scoreIncreaseModifier = config.getFloat("score_increase_from_hole_capture_modifier", 1.0f);
        scoreDecreaseModifier = config.getFloat("score_decrease_from_wrong_hole_modifier", 1.0f);
        remainingTime = config.getInt("time", -1) * App.FPS;
        spawnInterval = config.getInt("spawn_interval") * App.FPS;
        spawnCounter = spawnInterval;
        generateNextBalls();
    }

    private void generateNextBalls() {
        JSONArray ballsConfig = config.getJSONArray("balls");
        for (int i = 0; i < ballsConfig.size(); i++) {
            nextBalls.add(ballsConfig.getString(i));
        }
    }

    public void update() {
        if (levelCompleted || gameOver || timedOut) {
            return;
        }
        
        updateTimer();
        updateBallSpawning();
        updateBalls();
        checkLevelCompletion();
    }

    private void updateTimer() {
        if (remainingTime > 0) {
            remainingTime--;
            if (remainingTime == 0) {
                timedOut = true;
                System.out.println("=== TIME'S UP ===");
            }
        }
    }

    private void updateBallSpawning() {
        spawnCounter--;
        if (spawnCounter <= 0 && !nextBalls.isEmpty()) {
            spawnBall();
            spawnCounter = spawnInterval;
        }
    }

    private void spawnBall() {
        if (!spawners.isEmpty() && !nextBalls.isEmpty()) {
            Spawner spawner = spawners.get(App.random.nextInt(spawners.size()));
            String colorName = nextBalls.remove(0);
            int color = getColorIndex(colorName);
            balls.add(new Ball(spawner.getX(), spawner.getY(), color));
        }
    }
    private void updateBalls() {
        Iterator<Ball> ballIterator = balls.iterator();
        while (ballIterator.hasNext()) {
            Ball ball = ballIterator.next();
            if (!ball.isRemoved()) {
                ball.move();
                handleWallCollisions(ball);
                handlePlayerLineCollisions(ball);
                handleHoleCollisions(ball);
            }
            
            if (ball.isRemoved()) {
                ballIterator.remove();
            }
        }
    }
    private void handleHoleCollisions(Ball ball) {
        for (Hole hole : holes) {
            if (hole.captures(ball)) {
                handleBallCapture(ball, hole);
                return; // Exit the method immediately after capturing the ball
            } else if (hole.attracts(ball)) {
                hole.applyAttraction(ball);
                ball.updateAttractionFactor(hole);
            }
        }
    }
private void handleWallCollisions(Ball ball) {
    PVector totalNormal = new PVector(0, 0);  // To accumulate normals from multiple walls
    boolean collisionOccurred = false;

    // Check collisions with all walls
    for (Wall wall : walls) {
        if (wall.collidesWith(ball)) {
            PVector normal = wall.getNormal(ball).normalize();  // Normalize the normal vector
            totalNormal.add(normal);  // Accumulate normals to get the main reflection direction

            if (wall.getColor() != 0) {
                ball.setColor(wall.getColor());  // Change ball color if wall has a color
            }

            collisionOccurred = true;
        }
    }

    // If a collision occurred, calculate the final reflection
    if (collisionOccurred) {
        totalNormal.normalize();  // Get the average direction of collision
        ball.reflect(totalNormal);  // Reflect based on the combined normal

        // Apply correction to avoid getting stuck
        correctPosition(ball, totalNormal);
    }

    // Handle screen edge collisions
    handleEdgeCollisions(ball);
}

// Helper method to correct the ball's position to avoid sticking
private void correctPosition(Ball ball, PVector normal) {
    PVector correction = normal.copy().mult(1.0f);  // Move slightly along the normal vector
    ball.getPosition().add(correction);  // Nudge the ball to avoid getting stuck
}

// Helper method to handle edge collisions
private void handleEdgeCollisions(Ball ball) {
    float buffer = 0.5f;  // Small buffer to prevent edge sticking

    if (ball.getX() - ball.getRadius() < 0) {
        ball.setPosition(ball.getRadius() + buffer, ball.getY());
        ball.reverseX();
    } else if (ball.getX() + ball.getRadius() > App.WIDTH) {
        ball.setPosition(App.WIDTH - ball.getRadius() - buffer, ball.getY());
        ball.reverseX();
    }

    if (ball.getY() - ball.getRadius() < App.TOPBAR) {
        ball.setPosition(ball.getX(), App.TOPBAR + ball.getRadius() + buffer);
        ball.reverseY();
    } else if (ball.getY() + ball.getRadius() > App.HEIGHT) {
        ball.setPosition(ball.getX(), App.HEIGHT - ball.getRadius() - buffer);
        ball.reverseY();
    }
}



    private void handlePlayerLineCollisions(Ball ball) {
        Iterator<PlayerLine> lineIterator = playerLines.iterator();
        while (lineIterator.hasNext()) {
            PlayerLine line = lineIterator.next();
            if (line.collidesWith(ball)) {
                ball.reflect(line.getNormal(ball));
                lineIterator.remove();
                return;
            }
        }
    }

   
    private void handleBallCapture(Ball ball, Hole hole) {
        int scoreChange = hole.calculateScoreChange(ball, app);
        app.updateScore(scoreChange);
        if (scoreChange < 0) {
            nextBalls.add(getColorName(ball.getColor()));
        }
        ball.remove();
        System.out.println("Ball removed. Remaining balls: " + balls.size());
    }
    

    private void checkLevelCompletion() {
        if (balls.isEmpty() && nextBalls.isEmpty()) {
            levelCompleted = true;
        }
    }

    public void display() {
        for (Wall wall : walls) {
            wall.display(app);
        }
        for (Hole hole : holes) {
            hole.display(app);
        }
        for (Ball ball : balls) {
            ball.display(app);
        }
        for (PlayerLine line : playerLines) {
            line.display(app);
        }
        if (currentLine != null) {
            currentLine.display(app);
        }
    }

    public void displayNextBalls(float x, float y) {
        for (int i = 0; i < Math.min(5, nextBalls.size()); i++) {
            int color = getColorIndex(nextBalls.get(i));
            app.image(app.getBallImage(color), x + i * 20, y - 10, 20, 20);
        }
    }

    private int getColorIndex(String colorName) {
        switch (colorName.toLowerCase()) {
            case "grey": return 0;
            case "orange": return 1;
            case "blue": return 2;
            case "green": return 3;
            case "yellow": return 4;
            default: return 0;
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

    public int getRemainingTime() {
        return remainingTime / App.FPS;
    }

    public boolean isCompleted() {
        return levelCompleted;
    }

    public boolean isTimedOut() {
        return timedOut;
    }
    public float getSpawnTimeRemaining() {
    return spawnCounter / (float) App.FPS;  // Convert frames to seconds with one decimal place
}


    public void startDrawingLine(int x, int y) {
        currentLine = new PlayerLine(x, y);
    }

    public void continueDrawingLine(int x, int y) {
        if (currentLine != null) {
            currentLine.addPoint(x, y);
        }
    }

    public void finishDrawingLine() {
        if (currentLine != null && currentLine.getPoints().size() > 1) {
            playerLines.add(currentLine);
            currentLine = null;
        }
    }

    public void removeLineAt(int x, int y) {
        Iterator<PlayerLine> lineIterator = playerLines.iterator();
        while (lineIterator.hasNext()) {
            PlayerLine line = lineIterator.next();
            if (line.containsPoint(x, y)) {
                lineIterator.remove();
                return;
            }
        }
    }

    public int completeLevel() {
        int remainingTimeBonus = 0;
        if (remainingTime > 0) {
            remainingTimeBonus = (int) (remainingTime / (App.FPS * 0.067)); // Convert remaining frames to score
        }
        return remainingTimeBonus;
    }

    public void restart() {
        balls.clear();
        nextBalls.clear();
        playerLines.clear();
        currentLine = null;
        loadLayout();
        initializeEntities();
        levelCompleted = false;
        gameOver = false;
        timedOut = false;
    }
}
