import java.awt.*;

/**
 * Pressure-activated floor/wall Dart Trap that fires a fast poisoned dart across the room
 * when the player walks across its trigger line or steps near it.
 */
public class DartTrap extends Entity {
    private int cooldown = 0;
    private final double fireAngle;

    public DartTrap(double x, double y, double fireAngle) {
        super(x, y, 32, 32, 1);
        this.solid = false;
        this.isHostile = false;
        this.fireAngle = fireAngle;
    }

    @Override
    public void update(WorldMap map, Player player) {
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        double dist = Math.hypot(player.getCenterX() - getCenterX(), player.getCenterY() - getCenterY());
        if (dist < 170 && engineRef != null) {
            cooldown = 95;
            double angleToPlayer = Math.atan2(player.getCenterY() - getCenterY(), player.getCenterX() - getCenterX());
            double actualAngle = Double.isNaN(fireAngle) ? angleToPlayer : fireAngle;
            engineRef.spawnCustomProjectile(new Projectile(
                    getCenterX(), getCenterY(), actualAngle,
                    7.8, 14, 4, 65, false, false,
                    false, true,
                    new Color(180, 255, 90), new Color(90, 200, 40, 110)
            ));
        }
    }

    @Override
    public void render(Graphics2D g, boolean debug) {
        g.setColor(new Color(42, 38, 34));
        g.fillRoundRect((int) x, (int) y, width, height, 6, 6);
        g.setColor(cooldown < 15 ? new Color(180, 255, 90) : new Color(90, 85, 75));
        g.drawRoundRect((int) x, (int) y, width, height, 6, 6);

        // Launcher nozzle hole
        g.setColor(Color.BLACK);
        g.fillOval((int) getCenterX() - 5, (int) getCenterY() - 5, 10, 10);
    }
}

