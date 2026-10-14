import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Enemy extends Entity {
    public enum AIState {
        PATROL, CHASE, FLEE
    }

    private AIState state = AIState.PATROL;
    private final double baseSpeed = 2.25;
    private final double aggroRange = 340.0;

    private List<Point> currentPath = Collections.emptyList();
    private int pathUpdateTimer = 0;

    private double patrolTargetX, patrolTargetY;
    private int patrolWaitTimer = 0;
    private final Random random = new Random();

    public Enemy(double x, double y) {
        super(x, y, 30, 30, 100);
        this.patrolTargetX = x;
        this.patrolTargetY = y;
    }

    @Override
    public void update(WorldMap map, Player player) {
        // Hitstun: Enemy cannot move or attack while recovering from a hit (hurtTimer > 0)
        if (hurtTimer > 0) {
            moveWithCollision(0, 0, map);
            return;
        }

        double distToPlayer = Math.hypot(player.getCenterX() - getCenterX(), player.getCenterY() - getCenterY());

        // 1. FSM state transitions
        if (hp <= maxHp * 0.25 && distToPlayer < aggroRange) {
            state = AIState.FLEE;
        } else if (distToPlayer < aggroRange) {
            state = AIState.CHASE;
        } else {
            state = AIState.PATROL;
        }

        double dx = 0, dy = 0;
        double currentSpeed = baseSpeed;

        switch (state) {
            case CHASE -> {
                pathUpdateTimer--;
                // Throttle A* path calculations for performance (every 12-16 ticks)
                if (pathUpdateTimer <= 0) {
                    pathUpdateTimer = 12 + random.nextInt(5);
                    int startTileX = (int) (getCenterX() / Engine.TILE_SIZE);
                    int startTileY = (int) (getCenterY() / Engine.TILE_SIZE);
                    int targetTileX = (int) (player.getCenterX() / Engine.TILE_SIZE);
                    int targetTileY = (int) (player.getCenterY() / Engine.TILE_SIZE);

                    currentPath = Pathfinder.findPath(map, startTileX, startTileY, targetTileX, targetTileY, 250);
                }

                if (!currentPath.isEmpty()) {
                    Point nextStep = currentPath.get(0);
                    double targetPx = (nextStep.x + 0.5) * Engine.TILE_SIZE;
                    double targetPy = (nextStep.y + 0.5) * Engine.TILE_SIZE;

                    double stepDist = Math.hypot(targetPx - getCenterX(), targetPy - getCenterY());
                    if (stepDist < 8) {
                        currentPath.remove(0); // Waypoint reached, advance
                    } else {
                        dx = (targetPx - getCenterX()) / stepDist;
                        dy = (targetPy - getCenterY()) / stepDist;
                    }
                } else if (distToPlayer > 16) {
                    dx = (player.getCenterX() - getCenterX()) / distToPlayer;
                    dy = (player.getCenterY() - getCenterY()) / distToPlayer;
                }
            }

            case FLEE -> {
                currentPath = Collections.emptyList();
                currentSpeed = baseSpeed * 0.9;
                if (distToPlayer > 1) {
                    // Move in opposite direction of player
                    dx = (getCenterX() - player.getCenterX()) / distToPlayer;
                    dy = (getCenterY() - player.getCenterY()) / distToPlayer;
                }
            }

            case PATROL -> {
                currentPath = Collections.emptyList();
                currentSpeed = baseSpeed * 0.55;
                double distToPatrol = Math.hypot(patrolTargetX - getCenterX(), patrolTargetY - getCenterY());

                if (distToPatrol < 10 || patrolWaitTimer > 0) {
                    patrolWaitTimer++;
                    if (patrolWaitTimer > 90) { // Idle 1.5 seconds then pick next patrol waypoint
                        patrolWaitTimer = 0;
                        int randOffsetX = random.nextInt(5) - 2;
                        int randOffsetY = random.nextInt(5) - 2;
                        int candidateX = (int) (getCenterX() / Engine.TILE_SIZE) + randOffsetX;
                        int candidateY = (int) (getCenterY() / Engine.TILE_SIZE) + randOffsetY;

                        if (!map.getTile(candidateX, candidateY).solid) {
                            patrolTargetX = (candidateX + 0.5) * Engine.TILE_SIZE;
                            patrolTargetY = (candidateY + 0.5) * Engine.TILE_SIZE;
                        }
                    }
                } else {
                    dx = (patrolTargetX - getCenterX()) / distToPatrol;
                    dy = (patrolTargetY - getCenterY()) / distToPatrol;
                }
            }
        }

        double oldX = x, oldY = y;
        moveWithCollision(dx * currentSpeed, dy * currentSpeed, map);

        // Force new patrol waypoint if stuck against a wall
        if (state == AIState.PATROL && Math.hypot(x - oldX, y - oldY) < 0.1) {
            patrolWaitTimer = 91;
        }

        // Contact damage to player
        if (getBounds().intersects(player.getBounds())) {
            player.takeDamage(15, getCenterX(), getCenterY(), 8.5);
        }
    }

    @Override
    public void render(Graphics2D g, boolean debug) {
        // Debug mode: Live render of calculated A* path
        if (debug && !currentPath.isEmpty()) {
            g.setColor(new Color(0, 255, 120, 190));
            int prevX = (int) getCenterX();
            int prevY = (int) getCenterY();
            for (Point p : currentPath) {
                int cx = p.x * Engine.TILE_SIZE + (Engine.TILE_SIZE / 2);
                int cy = p.y * Engine.TILE_SIZE + (Engine.TILE_SIZE / 2);
                g.drawLine(prevX, prevY, cx, cy);
                g.fillOval(cx - 3, cy - 3, 6, 6);
                prevX = cx;
                prevY = cy;
            }
        }

        // Color coding by state (Purple when fleeing, Red when normal)
        if (hurtTimer > 0) {
            g.setColor(Color.WHITE);
        } else if (state == AIState.FLEE) {
            g.setColor(new Color(175, 70, 220));
        } else {
            g.setColor(new Color(220, 65, 65));
        }
        g.fillRoundRect((int) x, (int) y, width, height, 8, 8);

        g.setColor(new Color(60, 10, 10));
        g.drawRoundRect((int) x, (int) y, width, height, 8, 8);

        // Debug state tag above enemy
        if (debug) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Consolas", Font.BOLD, 11));
            g.drawString(state.name(), (int) x - 4, (int) y - 14);
        }

        drawHealthBar(g);
    }
}