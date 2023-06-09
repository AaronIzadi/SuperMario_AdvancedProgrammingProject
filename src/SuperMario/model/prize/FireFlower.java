package SuperMario.model.prize;

import SuperMario.logic.GameEngine;
import SuperMario.model.hero.Hero;

import java.awt.image.BufferedImage;

public class FireFlower extends PrizeItems {

    public FireFlower(double x, double y, BufferedImage style) {
        super(x, y, style);
        setPoint(20);
    }

    @Override
    public void updateLocation() { }

    @Override
    public void onTouch(Hero hero, GameEngine engine) {
        super.onTouch(hero, engine);
        engine.playPowerUp();
    }
}
