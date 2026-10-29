import java.awt.*;

/**
 * Toxic Acid Pool hazard that slows the player and inflicts periodic poison damage.
 */
public class AcidTrap extends Entity {
    private int bubbleTick = 0;

    public AcidTrap(double x, double y) {
        super(x, y, 42, 42, 1);
        this.solid = false;
        this.isHostile = false;
    }

    @Override
    public void update(WorldMap map, Player player) {
        bubbleTick = (bubbleTick + 1) % 120;
        if (getBounds().intersects(player.getBounds())) {
            player.slowTimer = Math.max(player.slowTimer, 25);
            if (bubbleTick % 35 == 0) {
                player.takeDamage(6, getCenterX(), getCenterY(), 0.0);
            }
        }
    }

    @Override
    public void render(Graphics2D g, boolean debug) {
        g.setColor(new Color(40, 145, 55, 160));
        g.fillOval((int) x, (int) y, width, height);
        g.setColor(new Color(95, 235, 85, 200));
        g.drawOval((int) x + 2, (int) y + 2, width - 4, height - 4);

        // Animated toxic bubbles
        int bOffset = (bubbleTick / 15) % 6;
        g.fillOval((int) x + 10 + bOffset, (int) y + 12, 6, 6);
        g.fillOval((int) x + 24 - bOffset, (int) y + 22, 5, 5);
    }
}

