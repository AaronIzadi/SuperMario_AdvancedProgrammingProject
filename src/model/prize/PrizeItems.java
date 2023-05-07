package model.prize;

import graphic.manager.GameEngine;
import graphic.view.Animation;
import graphic.view.ImageLoader;
import model.GameObject;
import model.hero.Hero;
import model.hero.HeroForm;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class PrizeItems extends GameObject implements Prize {

    private boolean revealed = false;
    private int point;

    public PrizeItems(double x, double y, BufferedImage style) {
        super(x, y, style);
        setDimension(48, 48);
    }

    public void onTouch(Hero hero, GameEngine engine){
        hero.acquirePoints(getPoint());
        if (engine.getHero() != null) {
            engine.getHero().acquirePoints(getPoint());
        }

        ImageLoader imageLoader = new ImageLoader(hero.getType());

        if (!hero.getHeroForm().isSuper()) {
            BufferedImage[] leftFrames = imageLoader.getLeftFrames(HeroForm.SUPER);
            BufferedImage[] rightFrames = imageLoader.getRightFrames(HeroForm.SUPER);

            Animation animation = new Animation(leftFrames, rightFrames);
            HeroForm newForm = new HeroForm(animation, true, false, hero.getType());
            hero.setHeroForm(newForm);
            hero.setDimension(48, 96);
            if (engine.getHero() != null) {
                engine.getHero().setHeroForm(newForm);
                engine.getHero().setDimension(48, 96);
            }
            engine.playSuperMushroom();
        }else if (hero.getHeroForm().isSuper() && !hero.getHeroForm().ifCanShootFire()){
            BufferedImage[] leftFrames = imageLoader.getLeftFrames(HeroForm.FIRE);
            BufferedImage[] rightFrames = imageLoader.getRightFrames(HeroForm.FIRE);

            Animation animation = new Animation(leftFrames, rightFrames);
            HeroForm newForm = new HeroForm(animation, true, true, hero.getType());
            hero.setHeroForm(newForm);
            hero.setDimension(48, 96);
            if (engine.getHero() != null) {
                engine.getHero().setHeroForm(newForm);
                engine.getHero().setDimension(48, 96);
            }
            engine.playSuperMushroom();
        }
    };

    @Override
    public int getPoint() {
        return point;
    }

    @Override
    public void updateLocation() {
        if (revealed) {
            super.updateLocation();
        }
    }

    @Override
    public void draw(Graphics g) {
        if (revealed) {
            g.drawImage(getStyle(), (int) getX(), (int) getY(), null);
        }
    }

    @Override
    public void reveal() {
        setY(getY() - 48);
        revealed = true;
    }

    public void setPoint(int point) {
        this.point = point;
    }
}
