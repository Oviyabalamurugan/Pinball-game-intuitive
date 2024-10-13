package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.*;

public class App extends PApplet {

    public static final int CELLSIZE = 32;
    public static final int TOPBAR = 64;
    public static final int WIDTH = 576;
    public static final int HEIGHT = 640;
    public static final int BOARD_WIDTH = WIDTH / CELLSIZE;
    public static final int BOARD_HEIGHT = (HEIGHT - TOPBAR) / CELLSIZE;
    public static final int FPS = 30;
    private PImage spawnerImage;
    private PImage[] wallImages;
    private PImage[] ballImages;
    private PImage[] holeImages;
    private Tile[][] board;
    private PImage tileImage;

    public String configPath;
    public static Random random = new Random();
    
    private JSONObject config;
    private List<Level> levels;
    private int currentLevelIndex;
    private Level currentLevel;
    private int score;
    private boolean paused;
    private boolean gameEnded;

    public App() {
        this.configPath = "config.json";
    }

    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    @Override
    public void setup() {
        frameRate(FPS);
        loadResources();
        loadConfig();
        initializeLevels();
        initializeBoard();
        startGame();
    }

    public JSONObject getConfig() {
        return config;
    }

    private void loadResources() {
        String resourcePath = "inkball/";
    
        wallImages = new PImage[5];
        ballImages = new PImage[5];
        holeImages = new PImage[5];
    
        for (int i = 0; i < 5; i++) {
            wallImages[i] = loadImage(resourcePath + "wall" + i + ".png");
            ballImages[i] = loadImage(resourcePath + "ball" + i + ".png");
            holeImages[i] = loadImage(resourcePath + "hole" + i + ".png");
        }
        spawnerImage = loadImage("inkball/entrypoint.png");

        tileImage = loadImage("inkball/tile.png");
        if (tileImage == null) {
            tileImage = createImage(CELLSIZE, CELLSIZE, RGB);
            tileImage.loadPixels();
            Arrays.fill(tileImage.pixels, color(200));
            tileImage.updatePixels();
        }
    }

    private void loadConfig() {
        config = loadJSONObject(configPath);
    }

    private void initializeBoard() {
        board = new Tile[BOARD_WIDTH][BOARD_HEIGHT];
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                float x = i * CELLSIZE;
                float y = j * CELLSIZE + TOPBAR;
                board[i][j] = new Tile(x, y, color(200), tileImage);
            }
        }
    }

    public PImage getSpawnerImage() {
        return spawnerImage;
    }

    private void initializeLevels() {
        levels = new ArrayList<>();
        JSONArray levelsConfig = config.getJSONArray("levels");
        for (int i = 0; i < levelsConfig.size(); i++) {
            JSONObject levelConfig = levelsConfig.getJSONObject(i);
            levels.add(new Level(levelConfig, this));
        }
    }

    private void startGame() {
        currentLevelIndex = 0;
        score = 0;
        paused = false;
        gameEnded = false;
        startLevel();
    }

    private void startLevel() {
        currentLevel = levels.get(currentLevelIndex);
    }

    @Override
    public void draw() {
        background(255);
        
        if (!paused && !gameEnded) {
            currentLevel.update();
        }

        currentLevel.display();
        displayTopBar();

        if (paused) {
            displayPausedMessage();
        } else if (gameEnded) {
            displayGameEndMessage();
        } else if (currentLevel.isTimedOut()) {
            displayTimesUpMessage();
        }

        if (currentLevel.isCompleted()) {
            handleLevelCompletion();
        }
    }

    private void displayTopBar() {
    fill(200); // Background color of the top bar
    rect(0, 0, WIDTH, TOPBAR); // Top bar background

    // Display Next Balls and Spawn Time (left side)
    fill(0); // Black text color
    textAlign(LEFT, CENTER); // Left align the spawn time
    textSize(20); // Text size for the timer
    text(currentLevel.getSpawnTime(), 130, TOPBAR / 2); // Display spawn time at x = 130

    // Display next balls
    float ballXOffset = 10; // Initial x-offset for balls
    float ballYOffset = 10; // Initial y-offset for balls (centered vertically)
    currentLevel.displayNextBalls(ballXOffset, ballYOffset); // Display the next balls at the left side of the top bar

    // Display the spawn time progress bar (optional)
    fill(0); // Black color for the bar
    rect(90, TOPBAR / 2 - 10, 40, 20); // Rectangular progress bar at x = 90

    // Display the Score and Time (right side)
    fill(0); // Black text color
    textAlign(RIGHT, CENTER); // Right align for score and time
    textSize(20); // Text size for score and time
    text("Score: " + score, WIDTH - 10, TOPBAR / 2); // Display score near the right edge
    text("Time: " + currentLevel.getRemainingTime(), WIDTH - 150, TOPBAR / 2); // Display time near the score
}



    private void displayPausedMessage() {
        fill(0);
        textAlign(CENTER, CENTER);
        textSize(20);
        text("*** PAUSED ***", WIDTH / 2, TOPBAR / 2);
    }

    private void displayGameEndMessage() {
        fill(0);
        textAlign(CENTER, CENTER);
        textSize(20);
        text("=== ENDED ===", WIDTH / 2, TOPBAR / 2);
    }

    private void displayTimesUpMessage() {
        fill(0);
        textAlign(CENTER, CENTER);
        textSize(20);
        text("=== TIME'S UP ===", WIDTH / 2, TOPBAR / 2);
    }

    private void handleLevelCompletion() {
        int timeBonus = currentLevel.completeLevel();
        score += timeBonus;
        currentLevelIndex++;
        if (currentLevelIndex < levels.size()) {
            startLevel();
        } else {
            gameEnded = true;
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKey() == 'r') {
            if (gameEnded) {
                startGame();
            } else {
                currentLevel.restart();
            }
        } else if (event.getKey() == ' ') {
            paused = !paused;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!paused && !gameEnded) {
            currentLevel.startDrawingLine(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!paused && !gameEnded) {
            if (e.getButton() == LEFT) {
                currentLevel.continueDrawingLine(e.getX(), e.getY());
            } else if (e.getButton() == RIGHT) {
                currentLevel.removeLineAt(e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!paused && !gameEnded) {
            currentLevel.finishDrawingLine();
        }
    }

    public PImage getWallImage(int color) {
        return wallImages[color];
    }

    public PImage getBallImage(int color) {
        return ballImages[color];
    }

    public PImage getHoleImage(int color) {
        return holeImages[color];
    }

    public PImage getTileImage() {
        return tileImage;
    }

    public void updateScore(int scoreChange) {
        score += scoreChange;
        if (score < 0) {
            score = 0;
        }
    }

    public int getScoreIncrease(int ballColor) {
        JSONObject levelConfig = config.getJSONObject("levels").getJSONObject(String.valueOf(currentLevelIndex));
        JSONObject scoreIncreaseConfig = levelConfig.getJSONObject("score_increase_from_hole_capture");

        int baseScore = scoreIncreaseConfig.getInt(String.valueOf(ballColor));
        float modifier = levelConfig.getFloat("score_increase_from_hole_capture_modifier");

        return Math.round(baseScore * modifier);
    }

    public int getScoreDecrease(int ballColor) {
        JSONObject levelConfig = config.getJSONObject("levels").getJSONObject(String.valueOf(currentLevelIndex));
        JSONObject scoreDecreaseConfig = levelConfig.getJSONObject("score_decrease_from_wrong_hole");

        int baseScore = scoreDecreaseConfig.getInt(String.valueOf(ballColor));
        float modifier = levelConfig.getFloat("score_decrease_from_wrong_hole_modifier");

        return Math.round(baseScore * modifier);
    }

    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }
}
