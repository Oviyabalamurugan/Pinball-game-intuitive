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

    private PImage[] wallImages;
    private PImage[] ballImages;
    private PImage[] holeImages;
    private Tile[][] board;
    private PImage tileImage;
private PImage spawnerImage;
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

        // Load images for walls, balls, and holes
        for (int i = 0; i < 5; i++) {
            wallImages[i] = loadImage(resourcePath + "wall" + i + ".png");
            ballImages[i] = loadImage(resourcePath + "ball" + i + ".png");
            holeImages[i] = loadImage(resourcePath + "hole" + i + ".png");

            if (wallImages[i] == null || ballImages[i] == null || holeImages[i] == null) {
                System.err.println("Error: Unable to load image for index " + i);
            }
        }

        // Load the tile image
        tileImage = loadImage(resourcePath + "tile.png");
        if (tileImage == null) {
            System.err.println("Error: Unable to load tile.png from " + resourcePath);
            createFallbackTileImage();
        } else {
            System.out.println("Successfully loaded tile image");
        }

        // Load the spawner image (entrypoint.png)
        spawnerImage = loadImage(resourcePath + "entrypoint.png");
        if (spawnerImage == null) {
            System.err.println("Error: Unable to load entrypoint.png from " + resourcePath);
        } else {
            System.out.println("Successfully loaded spawner image");
        }
    }

    // Getter for the spawner image
    public PImage getSpawnerImage() {
        return spawnerImage;
    }

    // Helper method to create a fallback tile image
    private void createFallbackTileImage() {
        tileImage = createImage(CELLSIZE, CELLSIZE, RGB);
        tileImage.loadPixels();
        for (int i = 0; i < tileImage.pixels.length; i++) {
            tileImage.pixels[i] = color(200);  // Light grey color
        }
        tileImage.updatePixels();
        System.out.println("Created fallback tile image");
    }

    private void loadConfig() {
        config = loadJSONObject(configPath);
    }
    private void initializeBoard() {
        if (tileImage == null) {
            System.err.println("tileImage is null in initializeBoard()");
            return;
        }
        board = new Tile[BOARD_WIDTH][BOARD_HEIGHT];
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                float x = i * CELLSIZE;
                float y = j * CELLSIZE + TOPBAR;
                board[i][j] = new Tile(x, y, color(200), tileImage);
                if (board[i][j] == null) {
                    System.err.println("Failed to create Tile at " + i + ", " + j);
                }
            }
        }
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
        
    
    // Draw tiles
    for (int i = 0; i < BOARD_WIDTH; i++) {
        for (int j = 0; j < BOARD_HEIGHT; j++) {
            board[i][j].display(this);
        }
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
    // Draw the background of the top bar
    fill(200);
    rect(0, 0, WIDTH, TOPBAR);

    // Draw the black box for queued balls
    fill(0);
    rect(10, 10, 120, TOPBAR - 20);  // Adjusted black box dimensions

    // Display the next balls horizontally inside the black box
    currentLevel.displayNextBalls(20, 18);  // Adjusted alignment

    // Display the spawn timer **next to the black box**
    fill(0);  // Black text color
    textSize(16);
    textAlign(LEFT, CENTER);
    text(String.format("%.1f", currentLevel.getSpawnTimeRemaining()), 140, TOPBAR / 2);

    // Display score and time on the right side, aligned vertically
    fill(0);  // Black text color
    textSize(20);
    textAlign(RIGHT, CENTER);
    text("Score: " + score, WIDTH - 10, TOPBAR / 3);  // Adjusted alignment for score
    text("Time: " + currentLevel.getRemainingTime(), WIDTH - 10, 2 * TOPBAR / 3);
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
        System.out.println("Level completed! Time bonus: " + timeBonus);
        currentLevelIndex++;
        if (currentLevelIndex < levels.size()) {
            startLevel();
        } else {
            gameEnded = true;
            System.out.println("=== ENDED ===");
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
        System.out.println("Score updated: " + score);
    }

    public int getScore() {
        return score;
    }

    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }
}
