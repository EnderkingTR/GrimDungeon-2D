import java.awt.Point;
import java.util.*;

public class Pathfinder {

    private static class Node implements Comparable<Node> {
        int x, y;
        double gCost, hCost;
        Node parent;

        Node(int x, int y, double gCost, double hCost, Node parent) {
            this.x = x;
            this.y = y;
            this.gCost = gCost;
            this.hCost = hCost;
            this.parent = parent;
        }

        double fCost() {
            return gCost + hCost;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.fCost(), other.fCost());
        }
    }

    public static List<Point> findPath(WorldMap map, int startX, int startY, int targetX, int targetY, int maxSteps) {
        if (map.getTile(targetX, targetY).solid)
            return Collections.emptyList();

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        boolean[][] closedSet = new boolean[map.getWidth()][map.getHeight()];
        double[][] bestG = new double[map.getWidth()][map.getHeight()];

        for (double[] row : bestG)
            Arrays.fill(row, Double.MAX_VALUE);

        openSet.add(new Node(startX, startY, 0, heuristic(startX, startY, targetX, targetY), null));
        bestG[startX][startY] = 0;

        // 8-directional movement (Straight: 1.0 cost, Diagonal: 1.414 cost)
        int[] dx = { 0, 0, -1, 1, -1, 1, -1, 1 };
        int[] dy = { -1, 1, 0, 0, -1, -1, 1, 1 };

        int steps = 0;
        while (!openSet.isEmpty() && steps++ < maxSteps) {
            Node current = openSet.poll();

            if (current.x == targetX && current.y == targetY) {
                return reconstructPath(current);
            }

            if (closedSet[current.x][current.y])
                continue;
            closedSet[current.x][current.y] = true;

            for (int i = 0; i < 8; i++) {
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];

                if (map.getTile(nx, ny).solid || closedSet[nx][ny])
                    continue;

                // Prevent clipping through diagonal wall corners (Corner Cutting prevention)
                if (i >= 4) {
                    if (map.getTile(current.x + dx[i], current.y).solid ||
                            map.getTile(current.x, current.y + dy[i]).solid) {
                        continue;
                    }
                }

                double stepCost = (i < 4) ? 1.0 : 1.414;
                double newG = current.gCost + stepCost;

                if (newG < bestG[nx][ny]) {
                    bestG[nx][ny] = newG;
                    double h = heuristic(nx, ny, targetX, targetY);
                    openSet.add(new Node(nx, ny, newG, h, current));
                }
            }
        }
        return Collections.emptyList();
    }

    private static double heuristic(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private static List<Point> reconstructPath(Node targetNode) {
        List<Point> path = new ArrayList<>();
        Node curr = targetNode;
        while (curr != null && curr.parent != null) { // Skip starting tile, direct to next waypoint
            path.add(new Point(curr.x, curr.y));
            curr = curr.parent;
        }
        Collections.reverse(path);
        return path;
    }
}