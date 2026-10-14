import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldMap {
    private final int width;
    private final int height;
    private final TileType[][] tiles;
    private final boolean[][] discovered;
    private final boolean[][] visible;
    private final Random random = new Random();
    private final List<Rectangle> rooms = new ArrayList<>();

    public double spawnX, spawnY;

    public WorldMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new TileType[width][height];
        this.discovered = new boolean[width][height];
        this.visible = new boolean[width][height];
        generateDungeon();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Rectangle> getRooms() {
        return rooms;
    }

    public boolean isVisible(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return false;
        return visible[x][y];
    }

    public boolean isDiscovered(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return false;
        return discovered[x][y];
    }

    public void generateDungeon() {
        rooms.clear();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = TileType.WALL;
                discovered[x][y] = false;
                visible[x][y] = false;
            }
        }

        int maxRooms = 18;
        int minSize = 5;
        int maxSize = 11;

        for (int i = 0; i < maxRooms; i++) {
            int roomW = random.nextInt(maxSize - minSize + 1) + minSize;
            int roomH = random.nextInt(maxSize - minSize + 1) + minSize;
            int roomX = random.nextInt(width - roomW - 4) + 2;
            int roomY = random.nextInt(height - roomH - 4) + 2;

            Rectangle newRoom = new Rectangle(roomX, roomY, roomW, roomH);

            boolean overlaps = false;
            Rectangle paddedRoom = new Rectangle(roomX - 1, roomY - 1, roomW + 2, roomH + 2);
            for (Rectangle existingRoom : rooms) {
                if (paddedRoom.intersects(existingRoom)) {
                    overlaps = true;
                    break;
                }
            }

            if (!overlaps) {
                carveRoom(newRoom);

                if (rooms.isEmpty()) {
                    spawnX = newRoom.getCenterX() * Engine.TILE_SIZE;
                    spawnY = newRoom.getCenterY() * Engine.TILE_SIZE;
                } else {
                    Rectangle prevRoom = rooms.get(rooms.size() - 1);
                    int prevCenterX = (int) prevRoom.getCenterX();
                    int prevCenterY = (int) prevRoom.getCenterY();
                    int newCenterX = (int) newRoom.getCenterX();
                    int newCenterY = (int) newRoom.getCenterY();

                    if (random.nextBoolean()) {
                        carveHorizontalCorridor(prevCenterX, newCenterX, prevCenterY);
                        carveVerticalCorridor(prevCenterY, newCenterY, newCenterX);
                    } else {
                        carveVerticalCorridor(prevCenterY, newCenterY, prevCenterX);
                        carveHorizontalCorridor(prevCenterX, newCenterX, newCenterY);
                    }
                }
                rooms.add(newRoom);
            }
        }
    }

    // Fog of War and Field of View calculation
    public void updateVisibility(int playerTileX, int playerTileY, int viewRadius) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                visible[x][y] = false;
            }
        }

        int minX = Math.max(0, playerTileX - viewRadius);
        int maxX = Math.min(width - 1, playerTileX + viewRadius);
        int minY = Math.max(0, playerTileY - viewRadius);
        int maxY = Math.min(height - 1, playerTileY + viewRadius);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (Math.hypot(x - playerTileX, y - playerTileY) <= viewRadius) {
                    if (hasLineOfSight(playerTileX, playerTileY, x, y)) {
                        visible[x][y] = true;
                        discovered[x][y] = true;
                    }
                }
            }
        }
    }

    // Bresenham's line algorithm for line of sight verification
    public boolean hasLineOfSight(int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        int cx = x0;
        int cy = y0;

        while (true) {
            if (cx == x1 && cy == y1)
                return true; // Target reached (wall surface itself is visible)
            if (getTile(cx, cy).solid && (cx != x0 || cy != y0)) {
                return false; // Solid tile encountered, ray blocked
            }
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                cx += sx;
            }
            if (e2 < dx) {
                err += dx;
                cy += sy;
            }
        }
    }

    private void carveRoom(Rectangle room) {
        for (int x = room.x; x < room.x + room.width; x++) {
            for (int y = room.y; y < room.y + room.height; y++) {
                tiles[x][y] = TileType.FLOOR;
            }
        }
    }

    private void carveHorizontalCorridor(int x1, int x2, int y) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            tiles[x][y] = TileType.FLOOR;
            if (y + 1 < height - 1)
                tiles[x][y + 1] = TileType.FLOOR;
        }
    }

    private void carveVerticalCorridor(int y1, int y2, int x) {
        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
            tiles[x][y] = TileType.FLOOR;
            if (x + 1 < width - 1)
                tiles[x + 1][y] = TileType.FLOOR;
        }
    }

    public boolean isColliding(double x, double y, int hitboxW, int hitboxH) {
        int leftTile = (int) Math.floor(x / Engine.TILE_SIZE);
        int rightTile = (int) Math.floor((x + hitboxW - 1) / Engine.TILE_SIZE);
        int topTile = (int) Math.floor(y / Engine.TILE_SIZE);
        int bottomTile = (int) Math.floor((y + hitboxH - 1) / Engine.TILE_SIZE);

        for (int tx = leftTile; tx <= rightTile; tx++) {
            for (int ty = topTile; ty <= bottomTile; ty++) {
                if (getTile(tx, ty).solid)
                    return true;
            }
        }
        return false;
    }

    public TileType getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return TileType.WALL;
        return tiles[x][y];
    }

    public void render(Graphics2D g, double camX, double camY, boolean debug) {
        int startX = Math.max(0, (int) (camX / Engine.TILE_SIZE));
        int startY = Math.max(0, (int) (camY / Engine.TILE_SIZE));
        int endX = Math.min(width, (int) ((camX + Engine.WIDTH) / Engine.TILE_SIZE) + 2);
        int endY = Math.min(height, (int) ((camY + Engine.HEIGHT) / Engine.TILE_SIZE) + 2);

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                int drawX = x * Engine.TILE_SIZE;
                int drawY = y * Engine.TILE_SIZE;

                // Render undiscovered areas as pitch black unless debug is active
                if (!debug && !discovered[x][y]) {
                    g.setColor(new Color(10, 10, 14));
                    g.fillRect(drawX, drawY, Engine.TILE_SIZE, Engine.TILE_SIZE);
                    continue;
                }

                TileType tile = tiles[x][y];
                g.setColor(tile.baseColor);
                g.fillRect(drawX, drawY, Engine.TILE_SIZE, Engine.TILE_SIZE);

                if (tile == TileType.WALL) {
                    g.setColor(tile.borderColor);
                    g.fillRect(drawX, drawY + Engine.TILE_SIZE - 10, Engine.TILE_SIZE, 10);
                }

                g.setColor(tile.borderColor);
                g.drawRect(drawX, drawY, Engine.TILE_SIZE, Engine.TILE_SIZE);

                // Apply translucent shroud to previously discovered tiles outside current view
                if (!debug && !visible[x][y]) {
                    g.setColor(new Color(10, 10, 16, 165));
                    g.fillRect(drawX, drawY, Engine.TILE_SIZE, Engine.TILE_SIZE);
                }
            }
        }
    }
}