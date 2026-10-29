import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Enemy extends Entity {
    public enum AIState {
        PATROL, CHASE, FLEE
    }

    private AIState state = AIState.PATROL;
    private final double baseSpeed;
    private final int contactDamage;
    private final double aggroRange = 350.0;

    private List<Point> currentPath = Collections.emptyList();
    private int pathUpdateTimer = 0;

    private double patrolTargetX, patrolTargetY;
    private int patrolWaitTimer = 0;
    private final Random random = new Random();

    public Enemy(double x, double y) {
        this(x, y, 1);
    }

    public Enemy(double x, double y, int stageLevel) {
        super(x, y, 30, 30, 75 + (stageLevel - 1) * 14);
        this.isHostile = true;
        this.baseSpeed = Math.min(3.1, 2.2 + (stageLevel - 1) * 0.04);
        this.contactDamage = 12 + (stageLevel - 1) * 2;
        this.patrolTargetX = x;
        this.patrolTargetY = y;
    }

    @Override
    public void update(WorldMap map, Player player) {
        // Hitstun or Stun: Enemy cannot move or attack while recovering
        if (hurtTimer > 0 || stunTimer > 0) {
            moveWithCollision(0, 0, map);
            return;
        }

        double distToPlayer = Math.hypot(player.getCenterX() - getCenterX(), player.getCenterY() - getCenterY());

        // 1. FSM state transitions
        if (hp <= maxHp * 0.22 && distToPlayer < aggroRange) {
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
                    if (patrolWaitTimer > 90) {
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

        if (state == AIState.PATROL && Math.hypot(x - oldX, y - oldY) < 0.1) {
            patrolWaitTimer = 91;
        }

        if (getBounds().intersects(player.getBounds())) {
            player.takeDamage(contactDamage, getCenterX(), getCenterY(), 8.5);
        }
    }

    @Override
    public void render(Graphics2D g, boolean debug) {
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

        if (hurtTimer > 0) {
            g.setColor(Color.WHITE);
        } else if (stunTimer > 0) {
            g.setColor(new Color(255, 230, 80));
        } else if (state == AIState.FLEE) {
            g.setColor(new Color(180, 70, 230));
        } else {
            g.setColor(new Color(220, 50, 50));
        }
        g.fillRoundRect((int) x, (int) y, width, height, 6, 6);

        g.setColor(Color.YELLOW);
        g.fillRect((int) x + 6, (int) y + 8, 5, 5);
        g.fillRect((int) x + width - 11, (int) y + 8, 5, 5);

        drawHealthBar(g);

        if (debug) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Consolas", Font.BOLD, 10));
            g.drawString(state.name(), (int) x, (int) y - 14);
        }
    }
}
