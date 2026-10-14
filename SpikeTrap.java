import java.awt.*;

public class SpikeTrap extends Entity {
    private int timer = 0;
    private boolean spikesUp = false;

    public SpikeTrap(double x, double y) {
        super(x, y, 36, 36, 1);
        this.solid = false; // Walkable trigger zone
    }

    @Override
    public void update(WorldMap map, Player player) {
        timer = (timer + 1) % 150; // 2.5-second loop (150 ticks at 60 UPS)
        spikesUp = timer > 95; // Spikes extended for the last 55 ticks

        if (spikesUp && getBounds().intersects(player.getBounds())) {
            player.takeDamage(20, getCenterX(), getCenterY(), 6.0);
        }
    }

    @Override
    public void render(Graphics2D g, boolean debug) {
        g.setColor(new Color(32, 32, 40));
        g.fillRect((int) x, (int) y, width, height);

        // Warning state before extending, bright red/metallic when active
        if (spikesUp) {
            g.setColor(new Color(230, 80, 80));
        } else if (timer > 70) {
            g.setColor(new Color(220, 180, 50)); // Warning phase
        } else {
            g.setColor(new Color(65, 65, 75));
        }

        // Draw 4 spike holes / points
        int offset = 7;
        g.fillOval((int) x + offset, (int) y + offset, 7, 7);
        g.fillOval((int) x + width - offset - 7, (int) y + offset, 7, 7);
        g.fillOval((int) x + offset, (int) y + height - offset - 7, 7, 7);
        g.fillOval((int) x + width - offset - 7, (int) y + height - offset - 7, 7, 7);
    }
}