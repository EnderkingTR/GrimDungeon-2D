import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collective Intelligence (Hive Mind) controller for a pack of SwarmEnemy units.
 * Instead of running A* pathfinding for every single swarm unit, the Hive Mind
 * computes the pack's center of mass (centroid) once per tick and maintains a
 * SINGLE shared A* path toward the player. Individual units combine this shared
 * directive with local Boids separation and cohesion vectors.
 */
public class SwarmHiveMind {
    private final List<SwarmEnemy> members = new ArrayList<>();
    private List<Point> sharedPath = Collections.emptyList();
    private int pathUpdateTimer = 0;

    public double centroidX = 0;
    public double centroidY = 0;
    public double currentWaypointX = 0;
    public double currentWaypointY = 0;
    public boolean activeAlert = false;

    private long lastUpdatedTick = -1;

    public void registerMember(SwarmEnemy member) {
        members.add(member);
    }

    public List<SwarmEnemy> getMembers() {
        return members;
    }

    public List<Point> getSharedPath() {
        return sharedPath;
    }

    public void tickHiveMindIfNeeded(long engineTick, WorldMap map, Player player) {
        if (lastUpdatedTick == engineTick) {
            return; // Already computed this frame for the entire swarm!
        }
        lastUpdatedTick = engineTick;

        // 1. Recompute active pack centroid
        double sumX = 0, sumY = 0;
        int aliveCount = 0;
        for (SwarmEnemy m : members) {
            if (m.active) {
                sumX += m.getCenterX();
                sumY += m.getCenterY();
                aliveCount++;
            }
        }
        if (aliveCount == 0) return;

        centroidX = sumX / aliveCount;
        centroidY = sumY / aliveCount;

        double distToPlayer = Math.hypot(player.getCenterX() - centroidX, player.getCenterY() - centroidY);
        activeAlert = distToPlayer < 420.0;

        if (!activeAlert) {
            sharedPath = Collections.emptyList();
            currentWaypointX = centroidX;
            currentWaypointY = centroidY;
            return;
        }

        // 2. Single A* computation for the entire swarm every 14 ticks
        pathUpdateTimer--;
        if (pathUpdateTimer <= 0) {
            pathUpdateTimer = 14;
            int startTileX = (int) (centroidX / Engine.TILE_SIZE);
            int startTileY = (int) (centroidY / Engine.TILE_SIZE);
            int targetTileX = (int) (player.getCenterX() / Engine.TILE_SIZE);
            int targetTileY = (int) (player.getCenterY() / Engine.TILE_SIZE);

            sharedPath = Pathfinder.findPath(map, startTileX, startTileY, targetTileX, targetTileY, 220);
        }

        // 3. Advance shared waypoint
        if (!sharedPath.isEmpty()) {
            Point nextStep = sharedPath.get(0);
            double wx = (nextStep.x + 0.5) * Engine.TILE_SIZE;
            double wy = (nextStep.y + 0.5) * Engine.TILE_SIZE;
            if (Math.hypot(wx - centroidX, wy - centroidY) < 22 && sharedPath.size() > 1) {
                sharedPath.remove(0);
                nextStep = sharedPath.get(0);
                wx = (nextStep.x + 0.5) * Engine.TILE_SIZE;
                wy = (nextStep.y + 0.5) * Engine.TILE_SIZE;
            }
            currentWaypointX = wx;
            currentWaypointY = wy;
        } else {
            currentWaypointX = player.getCenterX();
            currentWaypointY = player.getCenterY();
        }
    }

    public double[] computeSteering(SwarmEnemy self, Player player) {
        if (!activeAlert) {
            return new double[]{0, 0};
        }

        // Target is either direct player (if very close) or the Hive Mind's shared waypoint
        double distSelfToPlayer = Math.hypot(player.getCenterX() - self.getCenterX(), player.getCenterY() - self.getCenterY());
        double goalX = distSelfToPlayer < 130 ? player.getCenterX() : currentWaypointX;
        double goalY = distSelfToPlayer < 130 ? player.getCenterY() : currentWaypointY;

        double dirX = goalX - self.getCenterX();
        double dirY = goalY - self.getCenterY();
        double dirLen = Math.hypot(dirX, dirY);
        if (dirLen > 0.001) {
            dirX /= dirLen;
            dirY /= dirLen;
        }

        // Boids Separation (prevent stacking) & Cohesion (keep swarm together)
        double sepX = 0, sepY = 0;
        for (SwarmEnemy other : members) {
            if (other != self && other.active) {
                double dx = self.getCenterX() - other.getCenterX();
                double dy = self.getCenterY() - other.getCenterY();
                double dist = Math.hypot(dx, dy);
                if (dist > 0.01 && dist < 24.0) {
                    double push = (24.0 - dist) / 24.0;
                    sepX += (dx / dist) * push;
                    sepY += (dy / dist) * push;
                }
            }
        }

        double cohX = (centroidX - self.getCenterX()) * 0.008;
        double cohY = (centroidY - self.getCenterY()) * 0.008;

        double totalX = dirX * 1.15 + sepX * 0.95 + cohX;
        double totalY = dirY * 1.15 + sepY * 0.95 + cohY;
        double totalLen = Math.hypot(totalX, totalY);
        if (totalLen > 0.001) {
            totalX /= totalLen;
            totalY /= totalLen;
        }

        return new double[]{totalX, totalY};
    }
}

