import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

/**
 * Ranged Cultist/Archer enemy that maintains distance (kites away when player is too close)
 * and shoots dark energy projectiles at the player.
 */
public class RangedEnemy extends Entity {
    private final double speed;
    private final int projectileDamage;
    private int shootCooldown = 60;
    private int pathTimer = 0;
    private List<Point> currentPath = Collections.emptyList();

    public RangedEnemy(double x, double y, int stageLevel) {
        super(x, y, 28, 28, 55 + (stageLevel - 1) * 10);
        this.isHostile = true;
        this.speed = 2.05;
        this.projectileDamage = 11 + (stageLevel - 1) * 2;
    }

    @Override
    public void update(WorldMap map, Player player) {
        if (hurtTimer > 0 || stunTimer > 0) {
            moveWithCollision(0, 0, map);
            return;
        }

        double dist = Math.hypot(player.getCenterX() - getCenterX(), player.getCenterY() - getCenterY());
        double dx = 0, dy = 0;

        if (dist < 380) {
            if (dist < 135 && dist > 1) {
                // Player is too close: Kite backwards!
                dx = (getCenterX() - player.getCenterX()) / dist;
                dy = (getCenterY() - player.getCenterY()) / dist;
            } else if (dist > 250) {
                // Advance until within firing range
                pathTimer--;
                if (pathTimer <= 0) {
                    pathTimer = 18;
                    int sx = (int) (getCenterX() / Engine.TILE_SIZE);
                    int sy = (int) (getCenterY() / Engine.TILE_SIZE);
                    int tx = (int) (player.getCenterX() / Engine.TILE_SIZE);
                    int ty = (int) (player.getCenterY() / Engine.TILE_SIZE);
                    currentPath = Pathfinder.findPath(map, sx, sy, tx, ty, 200);
                }
                if (!currentPath.isEmpty()) {
                    Point p = currentPath.get(0);
                    double wx = (p.x + 0.5) * Engine.TILE_SIZE;
                    double wy = (p.y + 0.5) * Engine.TILE_SIZE;
                    double sd = Math.hypot(wx - getCenterX(), wy - getCenterY());
                    if (sd < 8) {
                        currentPath.remove(0);
                    } else {
                        dx = (wx - getCenterX()) / sd;
                        dy = (wy - getCenterY()) / sd;
                    }
                }
            }

            // Shoot at player when cooldown ready
            if (shootCooldown > 0) {
                shootCooldown--;
            } else if (engineRef != null && dist < 340) {
                shootCooldown = 85;
                double angle = Math.atan2(player.getCenterY() - getCenterY(), player.getCenterX() - getCenterX());
                engineRef.spawnCustomProjectile(new Projectile(
                        getCenterX(), getCenterY(), angle,
                        5.6, projectileDamage, 6, 85, false, false,
                        false, false,
                        new Color(255, 90, 220), new Color(180, 30, 160, 120)
                ));
            }
        }

        moveWithCollision(dx * speed, dy * speed, map);
    }

    @Override
    public void render(Graphics2D g, boolean debug) {
        g.setColor(new Color(0, 0, 0, 110));
        g.fillOval((int) x + 2, (int) y + height - 6, width - 4, 9);

        BufferedImage sprite = SpriteManager.getSprite("enemy_ranged.png");
        if (sprite != null && hurtTimer == 0) {
            g.drawImage(sprite, (int) x, (int) y, width, height, null);
        } else {
            if (hurtTimer > 0) {
                g.setColor(Color.WHITE);
            } else if (stunTimer > 0) {
                g.setColor(new Color(255, 230, 90));
            } else {
                g.setColor(new Color(145, 45, 195));
            }
            // Hooded diamond/robe silhouette
            g.fillRoundRect((int) x, (int) y, width, height, 10, 10);
            g.setColor(new Color(50, 15, 75));
            g.drawRoundRect((int) x, (int) y, width, height, 10, 10);

            // Glowing magenta arcane eyes
            g.setColor(new Color(255, 110, 240));
            g.fillOval((int) x + 6, (int) y + 8, 5, 5);
            g.fillOval((int) x + width - 11, (int) y + 8, 5, 5);
        }

        drawHealthBar(g);
    }
}

