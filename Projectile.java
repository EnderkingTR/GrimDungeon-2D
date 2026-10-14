import java.awt.*;
import java.util.List;

public class Projectile {
    public double x, y;
    private final double vx, vy;
    private final int radius = 6;
    private int life = 75; // Maximum range lifespan (75 ticks)
    public boolean active = true;

    public Projectile(double startX, double startY, double targetWorldX, double targetWorldY) {
        this.x = startX;
        this.y = startY;
        double speed = 8.5;
        double angle = Math.atan2(targetWorldY - startY, targetWorldX - startX);
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
    }

    public void update(WorldMap map, List<Entity> entities, Engine engine) {
        x += vx;
        y += vy;
        life--;

        // Explode and emit sparks if colliding with walls
        if (map.isColliding(x - radius, y - radius, radius * 2, radius * 2)) {
            active = false;
            engine.spawnParticles(x, y, new Color(100, 220, 255), 10);
            return;
        }

        // Enemy collision check
        Rectangle bounds = new Rectangle((int) x - radius, (int) y - radius, radius * 2, radius * 2);
        for (Entity e : entities) {
            if (e.active && e instanceof Enemy && bounds.intersects(e.getBounds())) {
                e.takeDamage(30, x - vx, y - vy, 7.5);
                active = false;
                engine.spawnParticles(x, y, new Color(255, 90, 60), 16);
                engine.triggerShake(4.0);
                return;
            }
        }

        if (life <= 0)
            active = false;
    }

    public void render(Graphics2D g) {
        if (!active)
            return;
        // Outer glow halo
        g.setColor(new Color(0, 210, 255, 110));
        g.fillOval((int) x - radius - 4, (int) y - radius - 4, (radius + 4) * 2, (radius + 4) * 2);
        // Projectile core
        g.setColor(new Color(210, 250, 255));
        g.fillOval((int) x - radius, (int) y - radius, radius * 2, radius * 2);
    }
}