package SuperMario.logic;

import SuperMario.graphic.manager.InputManager;
import SuperMario.graphic.manager.MapCreator;
import SuperMario.graphic.view.states.GameState;
import SuperMario.graphic.view.states.MapSelection;
import SuperMario.input.ImageLoader;
import SuperMario.model.GameObject;
import SuperMario.model.map.Map;
import SuperMario.model.enemy.*;
import SuperMario.model.hero.Hero;
import SuperMario.model.obstacle.*;
import SuperMario.model.prize.Coin;
import SuperMario.model.prize.Prize;
import SuperMario.model.prize.PrizeItems;
import SuperMario.model.weapon.Axe;
import SuperMario.model.weapon.Fireball;


import java.awt.*;
import java.util.ArrayList;

public class MapManager {

    private Map map = new Map();
    private Map crossover;
    private Hero hero;
    private double xBeforeCrossover;
    private double yBeforeCrossover;
    private double xHero;
    private double yHero;
    private double progressRate;
    private boolean isChecked = false;
    private static final MapManager instance = new MapManager();
    private final ArrayList<GameObject> toBeRemoved = new ArrayList<>();

    private MapManager() {
    }

    public static MapManager getInstance() {
        return instance;
    }

    public void updateLocations() {
        if (map == null) {
            return;
        }
        map.updateLocations();
        progressRate = getHero().getX() / getMap().getEndPoint().getX();
    }

    public void updateLocationsForCrossover() {
        if (crossover == null) {
            return;
        }
        crossover.updateLocationsForCrossover();
    }

    public void resetCurrentMap(GameEngine engine) {
        Hero hero = getHero();
        hero.resetLocation();
        engine.resetCamera();
    }

    private void reLoadCheckPoint(double x, double y) {
        getHero().reLoadCheckPoint(x, y);
        GameEngine.getInstance().reLoadCheckPoint(x);
    }

    public void creatCrossover(String path, Hero hero) {
        ImageLoader.getInstance().setHeroType(hero.getType());
        MapCreator mapCreator = new MapCreator();
        crossover = mapCreator.createCrossOver("/maps/" + path, hero);
        crossover.setHero(hero);
    }

    public boolean createMap(String path) {
        MapCreator mapCreator = new MapCreator();
        map = mapCreator.createMap("/maps/" + path);
        hero = map.getHero();
        crossover = new Map(hero);
        return map != null;
    }

    public boolean createMap(String path, Hero hero) {
        ImageLoader.getInstance().setHeroType(hero.getType());
        MapCreator mapCreator = new MapCreator(hero);
        map = mapCreator.createMap("/maps/" + path);
        map.setHero(hero);
        setHero(hero);
        crossover = new Map(hero);
        return map != null;
    }

    public void acquirePoints(int point) {
        map.getHero().acquirePoints(point);
    }

    public Hero getHero() {
        return hero;
    }

    public void setHero(Hero hero) {
        this.hero = hero;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public void fire() {
        Fireball fireball = getHero().fire();
        Map currentMap;
        if (GameEngine.getInstance().getGameState() == GameState.RUNNING) {
            currentMap = map;
        } else {
            currentMap = crossover;
        }
        if (fireball != null) {
            currentMap.addFireball(fireball);
            GameEngine.getInstance().playFireball();
        }
    }

    public void axe() {

        if (getHero().getAxe() == null) {
            map.removeAxe();
        }

        if (map.getAxe() == null) {
            getHero().activateAxe();
            Map currentMap;
            if (GameEngine.getInstance().getGameState() == GameState.RUNNING) {
                currentMap = map;
            } else {
                currentMap = crossover;
            }

            if (getHero().getAxe() != null) {
                getHero().setAxeActivated(true);
                currentMap.addAxe(getHero().getAxe());
            }
        } else {
            getHero().throwAxe();
        }
    }

    public boolean isGameOver() {
        return hero.getRemainingLives() == 0 || map.isTimeOver();
    }

    public int getScore() {
        return hero.getPoints();
    }

    public int getRemainingLives() {
        return hero.getRemainingLives();
    }

    public int getCoins() {
        return hero.getCoins();
    }

    public void drawMap(Graphics2D g2) {
        map.drawMap(g2);
    }

    public void drawCrossover(Graphics2D g2) {
        crossover.drawCrossover(g2);
    }

    public int passMission() {
        if (hero.getX() >= map.getEndPoint().getX() && !map.getEndPoint().isTouched()) {
            map.getEndPoint().setTouched(true);
            GameEngine.getInstance().playFlagPole();
            int height = (int) getHero().getY();
            return height * 2;
        } else {
            return -1;
        }
    }

    public boolean endLevel() {
        return hero.getX() >= map.getEndPoint().getX() + 320;
    }

    public void checkCollisions(GameEngine engine) {
        if (map == null) {
            return;
        }
        checkBottomCollisions(engine);
        checkTopCollisions(engine);
        checkHeroHorizontalCollision(engine);
        checkEnemyCollisions();
        checkPrizeCollision();
        checkPrizeContact(engine);
        checkWeaponContact();
    }


    private void checkBottomCollisions(GameEngine engine) {
        Map currentMap;
        if (GameEngine.getInstance().getGameState() == GameState.RUNNING) {
            currentMap = map;
        } else {
            currentMap = crossover;
        }
        ArrayList<Brick> bricks = currentMap.getAllBricks();
        ArrayList<Enemy> enemies = currentMap.getEnemies();
        Rectangle heroBottomBounds = hero.getBottomBounds();

        boolean heroHasBottomIntersection = false;

        for (Brick brick : bricks) {
            Rectangle brickTopBounds = brick.getTopBounds();
            if (heroBottomBounds.intersects(brickTopBounds)) {
                if (!(brick instanceof Hole)) {
                    hero.setY(brick.getY() - hero.getDimension().height + 1);
                    hero.setFalling(false);
                    hero.setVelY(0);
                    heroHasBottomIntersection = true;
                    if (brick instanceof Slime) {
                        ((Slime) brick).setOnTouch(true);
                        hero.jumpOnSlime();
                    }
                    if (brick instanceof CrossoverTunnel && !((CrossoverTunnel) brick).isRevealed() && InputManager.getInstance().isDown()) {
                        if (engine.getGameState() == GameState.RUNNING) {
                            xBeforeCrossover = hero.getX();
                            yBeforeCrossover = hero.getY();
                            ((CrossoverTunnel) brick).setRevealed(true);
                            engine.setGameState(GameState.CROSSOVER);
                            if (engine.getUserData().getWorldNumber() == 0) {
                                creatCrossover(MapSelection.CROSSOVER_1.getMapPath(MapSelection.CROSSOVER_1.getWorldNumber()), hero);
                            } else if (engine.getUserData().getWorldNumber() == 1) {
                                creatCrossover(MapSelection.CROSSOVER_2.getMapPath(MapSelection.CROSSOVER_2.getWorldNumber()), hero);
                            } else {
                                creatCrossover(MapSelection.CROSSOVER_3.getMapPath(MapSelection.CROSSOVER_3.getWorldNumber()), hero);
                            }
                        } else {
                            engine.setGameState(GameState.RUNNING);
                            hero.setSitting(false);
                            hero.setLocation(xBeforeCrossover, yBeforeCrossover);
                        }
                    }
                } else {
                    hero.setFalling(true);
                }
            }
        }

        hero.setFalling(!heroHasBottomIntersection);

        for (Enemy enemy : enemies) {
            Rectangle enemyTopBounds = enemy.getTopBounds();
            if (heroBottomBounds.intersects(enemyTopBounds) && !(enemy instanceof Spiny) && !(enemy instanceof Piranha)) {
                if (enemy instanceof KoopaTroopa) {
                    KoopaTroopa koopaTroopa = ((KoopaTroopa) enemy);
                    if (!koopaTroopa.isHit()) {
                        koopaTroopa.setHit(true);
                        koopaTroopa.moveAfterHit();
                        hero.setTimerToRun();
                    } else {
                        acquirePoints(2);
                        toBeRemoved.add(enemy);
                        engine.playStomp();
                    }
                } else {
                    acquirePoints(1);
                    toBeRemoved.add(enemy);
                    engine.playStomp();
                }
                hero.setFalling(false);
                hero.jumpOnEnemy();
            }
        }

        if (hero.getY() >= map.getBottomBorder()) {
            hero.onTouchBorder(engine, calculateLosingCoins());
            if (isChecked) {
                reLoadCheckPoint(xHero, yHero);
            } else {
                resetCurrentMap(engine);
            }
        }

        removeObjects(toBeRemoved);
    }


    private void checkTopCollisions(GameEngine engine) {
        Map currentMap;
        if (GameEngine.getInstance().getGameState() == GameState.RUNNING) {
            currentMap = map;
        } else {
            currentMap = crossover;
        }
        ArrayList<Brick> bricks = currentMap.getAllBricks();
        Rectangle heroTopBounds = hero.getTopBounds();

        for (Brick brick : bricks) {
            Rectangle brickBottomBounds = brick.getBottomBounds();
            if (!(brick instanceof Hole) && !(brick instanceof CheckPoint) && heroTopBounds.intersects(brickBottomBounds)) {
                hero.setVelY(0);
                hero.setY(brick.getY() + brick.getDimension().height);
                Prize prize = brick.reveal(engine);
                if (prize != null)
                    currentMap.addRevealedPrize(prize);
            } else if (brick instanceof CheckPoint && heroTopBounds.intersects(brickBottomBounds)) {
                if (!((CheckPoint) brick).isRevealed()) {
                    engine.setGameState(GameState.CHECKPOINT);
                }else{
                    hero.setVelY(0);
                    hero.setY(brick.getY() + brick.getDimension().height);
                }
            }
        }
    }

    private void checkHeroHorizontalCollision(GameEngine engine) {
        Map currentMap;
        if (GameEngine.getInstance().getGameState() == GameState.RUNNING) {
            currentMap = map;
        } else {
            currentMap = crossover;
        }

        ArrayList<Brick> bricks = currentMap.getAllBricks();
        ArrayList<Enemy> enemies = currentMap.getEnemies();

        boolean heroDies = false;
        boolean toRight = hero.getToRight();

        Rectangle heroBounds = toRight ? hero.getRightBounds() : hero.getLeftBounds();

        for (Brick brick : bricks) {
            Rectangle brickBounds = !toRight ? brick.getRightBounds() : brick.getLeftBounds();

            if (heroBounds.intersects(brickBounds)) {
                hero.setVelX(0);
                if (toRight) {
                    hero.setX(brick.getX() - hero.getDimension().width);
                } else {
                    hero.setX(brick.getX() + brick.getDimension().width);
                }
            }
        }

        for (Enemy enemy : enemies) {
            Rectangle enemyBounds = enemy.getBounds();
            if (heroBounds.intersects(enemyBounds) && !hero.isFalling()) {
                heroDies = hero.onTouchEnemy(engine, calculateLosingCoins());

                toBeRemoved.add(enemy);
            }
        }
        removeObjects(toBeRemoved);

        if (heroDies) {
            if (isChecked) {
                reLoadCheckPoint(xHero, yHero);
            } else {
                resetCurrentMap(engine);
            }
        }
    }

    private void checkEnemyCollisions() {
        Map currentMap;
        if (GameEngine.getInstance().getGameState() == GameState.RUNNING) {
            currentMap = map;
        } else {
            currentMap = crossover;
        }

        ArrayList<Brick> bricks = currentMap.getAllBricks();
        ArrayList<Enemy> enemies = currentMap.getEnemies();

        for (Enemy enemy : enemies) {
            if (enemy instanceof Spiny && ((getHero().getY() + getHero().getStyle().getHeight()) == (enemy.getY() + enemy.getStyle().getHeight() + 1))) {
                Spiny spiny = (Spiny) enemy;
                if (Math.abs(spiny.getX() - getHero().getX()) <= 192) {
                    spiny.moveFaster();
                } else {
                    spiny.moveNormal();
                }
            }
        }

        for (Enemy enemy : enemies) {
            if (!(enemy instanceof Piranha)) {
                boolean standsOnBrick = false;

                for (Brick brick : bricks) {
                    Rectangle enemyBounds = enemy.getLeftBounds();
                    Rectangle brickBounds = brick.getRightBounds();

                    Rectangle enemyBottomBounds = enemy.getBottomBounds();
                    Rectangle brickTopBounds = brick.getTopBounds();

                    if (enemy.getVelX() > 0) {
                        enemyBounds = enemy.getRightBounds();
                        brickBounds = brick.getLeftBounds();
                    }

                    if (enemyBounds.intersects(brickBounds)) {
                        enemy.setVelX(-enemy.getVelX());
                    }

                    if (enemyBottomBounds.intersects(brickTopBounds)) {
                        enemy.setFalling(false);
                        enemy.setVelY(0);
                        enemy.setY(brick.getY() - enemy.getDimension().height);
                        standsOnBrick = true;
                    }
                }

                if (enemy.getY() + enemy.getDimension().height > map.getBottomBorder()) {
                    enemy.setFalling(false);
                    enemy.setVelY(0);
                    enemy.setY(map.getBottomBorder() - enemy.getDimension().height);
                }

                if (!standsOnBrick && enemy.getY() < map.getBottomBorder()) {
                    enemy.setFalling(true);
                }
            }
        }
    }

    private void checkPrizeCollision() {
        Map currentMap;
        if (GameEngine.getInstance().getGameState() == GameState.RUNNING) {
            currentMap = map;
        } else {
            currentMap = crossover;
        }

        ArrayList<Prize> prizes = currentMap.getRevealedPrizes();
        ArrayList<Brick> bricks = currentMap.getAllBricks();

        for (Prize prize : prizes) {
            if (prize instanceof PrizeItems) {
                PrizeItems boost = (PrizeItems) prize;
                Rectangle prizeBottomBounds = boost.getBottomBounds();
                Rectangle prizeRightBounds = boost.getRightBounds();
                Rectangle prizeLeftBounds = boost.getLeftBounds();
                boost.setFalling(true);

                for (Brick brick : bricks) {
                    Rectangle brickBounds;

                    if (boost.isFalling()) {
                        brickBounds = brick.getTopBounds();

                        if (brickBounds.intersects(prizeBottomBounds)) {
                            boost.setFalling(false);
                            boost.setVelY(0);
                            boost.setY(brick.getY() - boost.getDimension().height + 1);
                            if (boost.getVelX() == 0)
                                boost.setVelX(2);
                        }
                    }

                    if (boost.getVelX() > 0) {
                        brickBounds = brick.getLeftBounds();

                        if (brickBounds.intersects(prizeRightBounds)) {
                            boost.setVelX(-boost.getVelX());
                        }
                    } else if (boost.getVelX() < 0) {
                        brickBounds = brick.getRightBounds();

                        if (brickBounds.intersects(prizeLeftBounds)) {
                            boost.setVelX(-boost.getVelX());
                        }
                    }
                }

                if (boost.getY() + boost.getDimension().height > map.getBottomBorder()) {
                    boost.setFalling(false);
                    boost.setVelY(0);
                    boost.setY(map.getBottomBorder() - boost.getDimension().height);
                    if (boost.getVelX() == 0)
                        boost.setVelX(2);
                }

            }
        }
    }

    private void checkPrizeContact(GameEngine engine) {
        Map currentMap;
        if (GameEngine.getInstance().getGameState() == GameState.RUNNING) {
            currentMap = map;
        } else {
            currentMap = crossover;
        }

        ArrayList<Prize> prizes = currentMap.getRevealedPrizes();

        Rectangle heroBounds = hero.getBounds();
        for (Prize prize : prizes) {
            Rectangle prizeBounds = prize.getBounds();
            if (prizeBounds.intersects(heroBounds)) {
                prize.onTouch(getHero(), engine);
                toBeRemoved.add((GameObject) prize);
            } else if (prize instanceof Coin) {
                prize.onTouch(getHero(), engine);
            }
        }

        removeObjects(toBeRemoved);
    }

    private void checkWeaponContact() {
        Map currentMap;
        if (GameEngine.getInstance().getGameState() == GameState.RUNNING) {
            currentMap = map;
        } else {
            currentMap = crossover;
        }

        ArrayList<Fireball> fireballs = currentMap.getFireballs();
        Axe axe = currentMap.getAxe();


        if (axe != null) {
            checkWeaponCollision(axe);
        }

        for (Fireball fireball : fireballs) {
            checkWeaponCollision(fireball);
        }

        removeObjects(toBeRemoved);

    }

    private void checkWeaponCollision(GameObject object) {

        Map currentMap;
        if (GameEngine.getInstance().getGameState() == GameState.RUNNING) {
            currentMap = map;
        } else {
            currentMap = crossover;
        }

        ArrayList<Enemy> enemies = currentMap.getEnemies();
        ArrayList<Brick> bricks = currentMap.getAllBricks();

        Rectangle objectBounds = object.getBounds();

        for (Enemy enemy : enemies) {
            Rectangle enemyBounds = enemy.getBounds();
            if (objectBounds.intersects(enemyBounds)) {
                if (enemy instanceof Goomba) {
                    acquirePoints(1);
                } else if (enemy instanceof KoopaTroopa) {
                    acquirePoints(2);
                } else if (enemy instanceof Spiny) {
                    acquirePoints(3);
                } else {
                    acquirePoints(1);
                }
                GameEngine.getInstance().playKickEnemy();
                toBeRemoved.add(enemy);
                toBeRemoved.add(object);
            }
        }
        for (Brick brick : bricks) {
            Rectangle brickBounds = brick.getBounds();
            if (objectBounds.intersects(brickBounds)) {
                toBeRemoved.add(object);
            }
        }

    }


    private void removeObjects(ArrayList<GameObject> list) {
        if (list == null) {
            return;
        }

        Map currentMap;
        if (GameEngine.getInstance().getGameState() == GameState.RUNNING) {
            currentMap = map;
        } else {
            currentMap = crossover;
        }

        for (GameObject object : list) {
            if (object instanceof Fireball) {
                currentMap.removeFireball((Fireball) object);
            } else if (object instanceof Enemy) {
                currentMap.removeEnemy((Enemy) object);
            } else if (object instanceof Coin || object instanceof PrizeItems) {
                currentMap.removePrize((Prize) object);
            }
        }
    }

    public void handleCheckPoint(boolean isChecked) {
        this.isChecked = isChecked;
        if (isChecked) {
            Point point = map.getCheckPoint().check(true);
            xHero = point.getX();
            yHero = point.getY() - 48 + 1;
            int price = (int) (progressRate * getCoins());
            getHero().setCoins(getCoins() - price);
        } else {
            map.getCheckPoint().check(false);
            int reward = (int) (progressRate * getCoins() * 0.25);
            getHero().setCoins(getCoins() + reward);
        }
    }

    private int calculateLosingCoins() {
        int n = isChecked ? 1 : 0;
        return (int) Math.floor((((n + 1) * getCoins()) + progressRate) / (n + 4));
    }

    public void addRevealedBrick(OrdinaryBrick ordinaryBrick) {
        Map currentMap;
        if (GameEngine.getInstance().getGameState() == GameState.RUNNING) {
            currentMap = map;
        } else {
            currentMap = crossover;
        }
        currentMap.addRevealedBrick(ordinaryBrick);
    }

    public void addRevealedBrick(CoinBrick coinBrick) {
        Map currentMap;
        if (GameEngine.getInstance().getGameState() == GameState.RUNNING) {
            currentMap = map;
        } else {
            currentMap = crossover;
        }
        currentMap.addRevealedBrick(coinBrick);
    }

    public void updateTime() {
        if (map != null) {
            map.updateTime(1);
        }
    }

    public int getRemainingTime() {
        return (int) map.getRemainingTime();
    }
}
