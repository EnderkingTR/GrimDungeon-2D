import java.awt.*;

/**
 * Small, fast, low-HP swarm creature guided by a shared SwarmHiveMind.
 */
public class SwarmEnemy extends Entity {
    private final SwarmHiveMind hiveMind;
    private final double speed;
    private final int contactDamage;
    private final boolean isLeaderRender;

    public SwarmEnemy(double x, double y, SwarmHiveMind hiveMind, int stageLevel, boolean isLeaderRender) {
        super(x, y, 18, 18, 22 + (stageLevel - 1) * 5);
        this.hiveMind = hiveMind;
        this.isHostile = true;
        this.speed = Math.min(4.3, 3.45 + (stageLevel - 1) * 0.05);
        this.contactDamage = 6 + stageLevel;
        this.isLeaderRender = isLeaderRender;
        hiveMind.registerMember(this);
    }

    @Override
    public void update(WorldMap map, Player player) {
        if (hurtTimer > 0 || stunTimer > 0) {
            moveWithCollision(0, 0, map);
            return;
        }

        long tick = engineRef != null ? engineRef.getTickCount() : System.currentTimeMillis() / 16;
        hiveMind.tickHiveMindIfNeeded(tick, map, player);

        double[] dir = hiveMind.computeSteering(this, player);
        moveWithCollision(dir[0] * speed, dir[1] * speed, map);

        if (getBounds().intersects(player.getBounds())) {
            player.takeDamage(contactDamage, getCenterX(), getCenterY(), 4.5);
        }
    }

    @Override
    public void render(Graphics2D g, boolean debug) {
        if (debug) {
            // Draw Hive Mind neural connection from unit to pack centroid
            g.setColor(new Color(255, 180, 0, 110));
            g.drawLine((int) getCenterX(), (int) getCenterY(), (int) hiveMind.centroidX, (int) hiveMind.centroidY);

            if (isLeaderRender && !hiveMind.getSharedPath().isEmpty()) {
                g.setColor(new Color(255, 165, 0, 200));
                int prevX = (int) hiveMind.centroidX;
                int prevY = (int) hiveMind.centroidY;
                for (Point p : hiveMind.getSharedPath()) {
                    int cx = p.x * Engine.TILE_SIZE + (Engine.TILE_SIZE / 2);
                    int cy = p.y * Engine.TILE_SIZE + (Engine.TILE_SIZE / 2);
                    g.drawLine(prevX, prevY, cx, cy);
                    prevX = cx;
                    prevY = cy;
                }
            }
        }

        if (hurtTimer > 0) {
            g.setColor(Color.WHITE);
        } else if (stunTimer > 0) {
            g.setColor(new Color(255, 230, 90));
        } else {
            g.setColor(new Color(240, 135, 35));
        }
        g.fillOval((int) x, (int) y, width, height);
        g.setColor(new Color(70, 30, 10));
        g.drawOval((int) x, (int) y, width, height);

        // Glowing red swarm eyes
        g.setColor(Color.RED);
        g.fillRect((int) x + 4, (int) y + 5, 3, 3);
        g.fillRect((int) x + width - 7, (int) y + 5, 3, 3);

        drawHealthBar(g);
    }
}

