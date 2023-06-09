package SuperMario.model.obstacle;

import SuperMario.graphic.view.animation.Animation;
import SuperMario.input.ImageLoader;
import SuperMario.logic.GameEngine;
import SuperMario.logic.MapManager;
import SuperMario.model.prize.Prize;

import java.awt.image.BufferedImage;

public class OrdinaryBrick extends Brick {

    private Animation animation;
    private boolean breaking;
    private int frames;

    public OrdinaryBrick(double x, double y, BufferedImage style) {
        super(x, y, style);
        setBreakable(true);
        setEmpty(true);

        setAnimation();
        breaking = false;
        frames = animation.getLength() - 1;
    }

    private void setAnimation() {
        ImageLoader imageLoader = ImageLoader.getInstance();
        BufferedImage[] frames = imageLoader.getBrickFrames();

        animation = new Animation(frames);
    }

    @Override
    public Prize reveal(GameEngine engine) {
        MapManager manager = engine.getMapManager();
        if (!manager.getHero().isSuper()) {
            return null;
        }
        breaking = true;
        manager.addRevealedBrick(this);
        engine.playBreakBrick();

        double newX = getX() - 27, newY = getY() - 27;
        setLocation(newX, newY);

        return null;
    }

    public int getFrames() {
        return frames;
    }

    public void animate() {
        if (breaking) {
            boolean isAnimationTicked = animation.animate(30);
            if (isAnimationTicked) {
                setStyle(animation.getCurrentFrame());
                frames--;
            }
        }
    }
}
