package graphic.view;

import graphic.manager.GameEngine;
import graphic.manager.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class UIManager extends JPanel {

    private final GameEngine engine;
    private Font gameFont;
    private final BufferedImage startScreenImage, aboutScreenImage, helpScreenImage, gameOverScreen, storeScreenImage, loadGameScreen, pauseScreen;
    private final BufferedImage heartIcon;
    private final BufferedImage coinIcon;
    private final BufferedImage selectIcon;
    private final MapSelection mapSelection;

    public UIManager(GameEngine engine, int width, int height) {
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));

        this.engine = engine;
        ImageLoader loader = engine.getImageLoader();

        mapSelection = new MapSelection();

        BufferedImage sprite = loader.loadImage("/sprite.png");
        this.heartIcon = loader.loadImage("/heart-icon.png");
        this.coinIcon = loader.getSubImage(sprite, 1, 5, 48, 48);
        this.selectIcon = loader.loadImage("/select-icon.png");
        this.startScreenImage = loader.loadImage("/start-screen.png");
        this.helpScreenImage = loader.loadImage("/help-screen.png");
        this.aboutScreenImage = loader.loadImage("/about-screen.png");
        this.gameOverScreen = loader.loadImage("/game-over.png");
        this.storeScreenImage = loader.loadImage("/store-screen.png");
        this.loadGameScreen = loader.loadImage("/load-screen.png");
        this.pauseScreen = loader.loadImage("/pause-screen.png");

        try {
            InputStream input = getClass().getResourceAsStream("/graphic/media/font/mario-font.ttf");
            assert input != null;
            gameFont = Font.createFont(Font.TRUETYPE_FONT, input);
        } catch (FontFormatException | IOException e) {
            gameFont = new Font("Verdana", Font.PLAIN, 12);
            e.printStackTrace();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        GameState gameState = engine.getGameState();

        if (gameState == GameState.START_SCREEN) {
            drawStartScreen(g2);
        } else if (gameState == GameState.LOAD_GAME) {
            drawLoadGameScreen(g2);
        } else if (gameState == GameState.STORE_SCREEN) {
            drawStoreScreen(g2);
        } else if (gameState == GameState.MAP_SELECTION) {
            drawMapSelectionScreen(g2);
        } else if (gameState == GameState.ABOUT_SCREEN) {
            drawAboutScreen(g2);
        } else if (gameState == GameState.HELP_SCREEN) {
            drawHelpScreen(g2);
        } else if (gameState == GameState.GAME_OVER) {
            drawGameOverScreen(g2);
        } else {
            Point camLocation = engine.getCameraLocation();
            g2.translate(-camLocation.x, -camLocation.y);
            engine.drawMap(g2);
            g2.translate(camLocation.x, camLocation.y);

            drawPoints(g2);
            drawRemainingLives(g2);
            drawAcquiredCoins(g2);
            drawRemainingTime(g2);

            if (gameState == GameState.PAUSED) {
                drawPauseScreen(g2);
            } else if (gameState == GameState.MISSION_PASSED) {
                drawVictoryScreen(g2);
            }
        }

        g2.dispose();
    }

    private void drawRemainingTime(Graphics2D g2) {
        g2.setFont(gameFont.deriveFont(25f));
        g2.setColor(Color.WHITE);
        String displayedStr = "TIME: " + engine.getRemainingTime();
        g2.drawString(displayedStr, 750, 50);
    }

    private void drawVictoryScreen(Graphics2D g2) {
        g2.setFont(gameFont.deriveFont(50f));
        g2.setColor(Color.WHITE);
        String displayedStr = "YOU WON!";
        int stringLength = g2.getFontMetrics().stringWidth(displayedStr);
        g2.drawString(displayedStr, (getWidth() - stringLength) / 2, getHeight() / 2);
    }

    private void drawHelpScreen(Graphics2D g2) {
        g2.drawImage(helpScreenImage, 0, 0, null);
    }

    private void drawAboutScreen(Graphics2D g2) {
        g2.drawImage(aboutScreenImage, 0, 0, null);
    }

    private void drawGameOverScreen(Graphics2D g2) {
        g2.drawImage(gameOverScreen, 0, 0, null);
        g2.setFont(gameFont.deriveFont(50f));
        g2.setColor(new Color(238, 28, 46));
        String acquiredPoints;
        if (engine.getHero() != null){
            acquiredPoints = "Score:" + engine.getHero().getPoints();
        }else{
            acquiredPoints = "Score:" + engine.getScore();
        }
        int stringLength = g2.getFontMetrics().stringWidth(acquiredPoints);
        int stringHeight = g2.getFontMetrics().getHeight();
        g2.drawString(acquiredPoints, (getWidth() - stringLength) / 2, getHeight() - stringHeight * 4);
        engine.playGameOver();
    }

    private void drawStoreScreen(Graphics2D g2) {
        int column = engine.getStoreScreenSelection().getColumnNumber();
        g2.drawImage(storeScreenImage, 0, 0, null);
        g2.drawImage(selectIcon, column * 225 + 70, 255, null);

        //Coins
        g2.setFont(gameFont.deriveFont(30f));
        g2.setColor(Color.WHITE);
        String coins = "" + engine.getCoins();
        g2.drawString(coins, 85, 70);

        //Price:

        //Luigi
        String buy = "Buy for";
        if (!engine.ownsLuigi()) {
            setFontAndColor(g2);
            g2.drawString(buy, 320, 500);

            setFontAndColor(g2);
            g2.drawString("25", 370, 535);
        }
        //Prince Peach
        if (!engine.ownsPrincePeach()) {
            setFontAndColor(g2);
            g2.drawString(buy, 560, 500);

            setFontAndColor(g2);
            g2.drawString("50", 610, 535);
        }
        //Ross
        if (!engine.ownsRoss()) {
            setFontAndColor(g2);
            g2.drawString(buy, 780, 500);

            setFontAndColor(g2);
            g2.drawString("40", 830, 535);
        }
        //Toad
        if (!engine.ownsToad()) {
            setFontAndColor(g2);
            g2.drawString(buy, 1000, 500);

            setFontAndColor(g2);
            g2.drawString("45", 1050, 535);
        }
    }

    private void setFontAndColor(Graphics2D g2) {
        g2.setFont(gameFont.deriveFont(20f));
        g2.setColor(Color.WHITE);
    }

    private void drawPauseScreen(Graphics2D g2) {
        int row = engine.getPauseScreenSelection().getLineNumber();
        g2.drawImage(pauseScreen, 0, 0, null);
        g2.drawImage(selectIcon, 285, row * 95 + 230, null);
    }

    private void drawAcquiredCoins(Graphics2D g2) {
        g2.setFont(gameFont.deriveFont(30f));
        g2.setColor(Color.WHITE);
        String displayedStr;
        if (engine.getHero() != null) {
            displayedStr = "" + engine.getHero().getCoins();
        } else {
            displayedStr = "" + engine.getCoins();
        }
        g2.drawImage(coinIcon, getWidth() - 115, 10, null);
        g2.drawString(displayedStr, getWidth() - 65, 50);
    }

    private void drawRemainingLives(Graphics2D g2) {
        g2.setFont(gameFont.deriveFont(30f));
        g2.setColor(Color.WHITE);
        String displayedStr;
        if (engine.getHero() != null) {
            displayedStr = "" + engine.getHero().getRemainingLives();
        } else {
            displayedStr = "" + engine.getRemainingLives();
        }
        g2.drawImage(heartIcon, 50, 10, null);
        g2.drawString(displayedStr, 100, 50);
    }

    private void drawPoints(Graphics2D g2) {
        g2.setFont(gameFont.deriveFont(25f));
        g2.setColor(Color.WHITE);
        String displayedStr;
        if (engine.getHero() != null) {
            displayedStr = "Points: " + engine.getHero().getPoints();
        } else {
            displayedStr = "Points: " + engine.getScore();
        }
        g2.drawString(displayedStr, 300, 50);
    }

    private void drawStartScreen(Graphics2D g2) {
        int row = engine.getStartScreenSelection().getLineNumber();
        g2.drawImage(startScreenImage, 0, 0, null);
        g2.drawImage(selectIcon, 375, row * 70 + 415, null);
    }

    private void drawMapSelectionScreen(Graphics2D g2) {
        g2.setFont(gameFont.deriveFont(50f));
        g2.setColor(Color.WHITE);
        mapSelection.draw(g2);
        int row = engine.getSelectedMap();
        int y_location = row * 100 + 300 - selectIcon.getHeight();
        g2.drawImage(selectIcon, 375, y_location, null);
    }

    private void drawLoadGameScreen(Graphics2D g2) {
        int row = engine.getLoadGameScreenSelection().getLineNumber();
        g2.drawImage(loadGameScreen, 0, 0, null);
        g2.drawImage(selectIcon, 450, row * 70 + 290, null);
    }

    public String selectMapViaMouse(Point mouseLocation) {
        return mapSelection.selectMap(mouseLocation);
    }

    public String selectMapViaKeyboard(int index) {
        return mapSelection.selectMap(index);
    }

    public int changeSelectedMap(int index, boolean up) {
        return mapSelection.changeSelectedMap(index, up);
    }

    public GameEngine getEngine() {
        return engine;
    }
}