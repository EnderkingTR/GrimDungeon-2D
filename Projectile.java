import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Projectile {
    public double x, y;
    private final double vx, vy;
    private final int radius;
    private int life;
    public boolean active = true;

    public final boolean fromPlayer;
    public final int damage;
    public final boolean isCrit;
    public final boolean piercing;
    public final boolean slowsTarget;
    public final Color coreColor;
    public final Color glowColor;
    private final Set<Entity> hitEntities = new HashSet<>();

    // Legacy constructor for compatibility
    public Projectile(double startX, double startY, double targetWorldX, double targetWorldY) {
        this(startX, startY, Math.atan2(targetWorldY - startY, targetWorldX - startX),
                8.5, 30, 6, 75, true, false, false, false,
                new Color(210, 250, 255), new Color(0, 210, 255, 110));
    }

    public Projectile(double startX, double startY, double angle, double speed, int damage,
                      int radius, int life, boolean fromPlayer, boolean isCrit,
                      boolean piercing, boolean slowsTarget, Color coreColor, Color glowColor) {
        this.x = startX;
        this.y = startY;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        this.damage = damage;
        this.radius = radius;
        this.life = life;
        this.fromPlayer = fromPlayer;
        this.isCrit = isCrit;
        this.piercing = piercing;
        this.slowsTarget = slowsTarget;
        this.coreColor = coreColor;
        this.glowColor = glowColor;
    }

    public void update(WorldMap map, List<Entity> entities, Player player, Engine engine) {
        x += vx;
        y += vy;
        life--;

        // Explode and emit sparks if colliding with walls
        if (map.isColliding(x - radius, y - radius, radius * 2, radius * 2)) {
            active = false;
            engine.spawnParticles(x, y, coreColor, 8);
            return;
        }

        Rectangle bounds = new Rectangle((int) x - radius, (int) y - radius, radius * 2, radius * 2);

        if (fromPlayer) {
            for (Entity e : entities) {
                if (e.active && e.isHostile && !hitEntities.contains(e) && bounds.intersects(e.getBounds())) {
                    hitEntities.add(e);
                    e.takeDamage(damage, x - vx, y - vy, 6.5, isCrit);
                    if (slowsTarget) {
                        e.slowTimer = Math.max(e.slowTimer, 150);
                    }
                    engine.spawnParticles(x, y, coreColor, 12);
                    engine.triggerShake(isCrit ? 4.5 : 2.5);
                    if (!piercing) {
                        active = false;
                        return;
                    }
                }
            }
        } else {
            // Hostile projectile hitting the player
            if (player.active && bounds.intersects(player.getBounds())) {
                player.takeDamage(damage, x - vx, y - vy, 5.5, false);
                if (slowsTarget) {
                    player.slowTimer = Math.max(player.slowTimer, 90);
                }
                engine.spawnParticles(x, y, coreColor, 10);
                active = false;
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
        g.setColor(glowColor);
        g.fillOval((int) x - radius - 4, (int) y - radius - 4, (radius + 4) * 2, (radius + 4) * 2);
        // Projectile core
        g.setColor(coreColor);
        g.fillOval((int) x - radius, (int) y - radius, radius * 2, radius * 2);
    }
}
