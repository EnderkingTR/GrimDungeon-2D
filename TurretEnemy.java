import java.awt.*;

/**
 * Stationary Arcane Turret enemy that periodically fires 4-way cardinal bolts
 * or aimed shots at the player when within range.
 */
public class TurretEnemy extends Entity {
    private int fireTimer = 40;
    private final int fireInterval;
    private final int boltDamage;
    private boolean crossPatternToggle = false;

    public TurretEnemy(double x, double y, int stageLevel) {
        super(x, y, 34, 34, 95 + (stageLevel - 1) * 15);
        this.isHostile = true;
        this.solid = true;
        this.fireInterval = Math.max(65, 105 - stageLevel * 2);
        this.boltDamage = 13 + (stageLevel - 1) * 2;
    }

    @Override
    public void update(WorldMap map, Player player) {
        updateTimers();
        if (stunTimer > 0) return;

        // Turret is stationary so clear knockback
        knockbackX = 0;
        knockbackY = 0;

        double dist = Math.hypot(player.getCenterX() - getCenterX(), player.getCenterY() - getCenterY());
        if (dist < 380) {
            fireTimer--;
            if (fireTimer <= 0 && engineRef != null) {
                fireTimer = fireInterval;
                crossPatternToggle = !crossPatternToggle;
                double baseAngle = crossPatternToggle ? 0 : Math.PI / 4.0;
                for (int i = 0; i < 4; i++) {
                    double angle = baseAngle + i * (Math.PI / 2.0);
                    engineRef.spawnCustomProjectile(new Projectile(
                            getCenterX(), getCenterY(), angle,
                            5.0, boltDamage, 6, 80, false, false,
                            false, false,
                            new Color(255, 160, 50), new Color(220, 80, 20, 120)
                    ));
                }
            }
        }
    }

    @Override
    public void render(Graphics2D g, boolean debug) {
        // Stone pedestal base
        g.setColor(new Color(55, 60, 72));
        g.fillRoundRect((int) x, (int) y, width, height, 6, 6);
        g.setColor(new Color(30, 32, 40));
        g.drawRoundRect((int) x, (int) y, width, height, 6, 6);

        // Rotating/charging core
        if (hurtTimer > 0) {
            g.setColor(Color.WHITE);
        } else if (fireTimer < 22) {
            g.setColor(new Color(255, 210, 60)); // Telegraph warning glow before firing
        } else {
            g.setColor(new Color(235, 95, 35));
        }
        g.fillOval((int) x + 7, (int) y + 7, width - 14, height - 14);
        g.setColor(Color.WHITE);
        g.drawOval((int) x + 7, (int) y + 7, width - 14, height - 14);

        drawHealthBar(g);
    }
}

