import java.awt.*;
import java.awt.image.BufferedImage;
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
    private final List<Point> torches = new ArrayList<>();

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
        generateDungeon(10);
    }

    public void generateDungeon(int maxRooms) {
        rooms.clear();
        torches.clear();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = TileType.WALL;
                discovered[x][y] = false;
                visible[x][y] = false;
            }
        }

        int minSize = 5;
        int maxSize = 10;

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
        // Place atmospheric wall torches on the top corners of the room
        torches.add(new Point(room.x, room.y));
        if (room.width >= 6) {
            torches.add(new Point(room.x + room.width - 1, room.y));
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

        BufferedImage floorImg = SpriteManager.getSprite("floor.png");
        BufferedImage wallImg = SpriteManager.getSprite("wall.png");
        long animTick = System.currentTimeMillis() / 90;

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
                int hash = (x * 73856093) ^ (y * 19349663);

                if (tile == TileType.FLOOR) {
                    if (floorImg != null) {
                        g.drawImage(floorImg, drawX, drawY, Engine.TILE_SIZE, Engine.TILE_SIZE, null);
                    } else {
                        // Procedural stone dungeon cobble floor with subtle variation & moss accents
                        int shade = Math.abs(hash % 7) - 3;
                        g.setColor(new Color(
                                Math.max(0, tile.baseColor.getRed() + shade),
                                Math.max(0, tile.baseColor.getGreen() + shade),
                                Math.max(0, tile.baseColor.getBlue() + shade)
                        ));
                        g.fillRect(drawX, drawY, Engine.TILE_SIZE, Engine.TILE_SIZE);

                        // Stone tile inner bevel & crack/moss details
                        g.setColor(new Color(32, 33, 42));
                        g.drawRect(drawX + 4, drawY + 4, Engine.TILE_SIZE - 8, Engine.TILE_SIZE - 8);
                        if ((hash & 15) == 0) {
                            g.setColor(new Color(45, 72, 48, 120));
                            g.fillOval(drawX + 10, drawY + 14, 8, 6);
                        }
                    }
                } else {
                    if (wallImg != null) {
                        g.drawImage(wallImg, drawX, drawY, Engine.TILE_SIZE, Engine.TILE_SIZE, null);
                    } else {
                        // Procedural 2.5D dungeon brick wall
                        g.setColor(tile.baseColor);
                        g.fillRect(drawX, drawY, Engine.TILE_SIZE, Engine.TILE_SIZE);

                        // Top highlight edge
                        g.setColor(new Color(46, 48, 62));
                        g.fillRect(drawX, drawY, Engine.TILE_SIZE, 5);

                        // Brick mortar lines
                        g.setColor(tile.borderColor);
                        g.drawLine(drawX, drawY + 18, drawX + Engine.TILE_SIZE, drawY + 18);
                        g.drawLine(drawX + 24, drawY, drawX + 24, drawY + 18);
                        g.drawLine(drawX + 12, drawY + 18, drawX + 12, drawY + 36);

                        // 2.5D bottom wall face shadow
                        g.setColor(tile.borderColor);
                        g.fillRect(drawX, drawY + Engine.TILE_SIZE - 11, Engine.TILE_SIZE, 11);
                    }
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

        // Render wall torches & warm flickering light halos
        for (Point t : torches) {
            if (t.x >= startX - 2 && t.x <= endX + 2 && t.y >= startY - 2 && t.y <= endY + 2) {
                if (debug || visible[t.x][t.y]) {
                    int tx = t.x * Engine.TILE_SIZE + Engine.TILE_SIZE / 2;
                    int ty = t.y * Engine.TILE_SIZE + 8;
                    int flicker = (int) (Math.sin(animTick + t.x * 3 + t.y) * 6);
                    int radius = 78 + flicker;

                    RadialGradientPaint glow = new RadialGradientPaint(
                            new Point(tx, ty), radius,
                            new float[]{0f, 0.55f, 1f},
                            new Color[]{
                                new Color(255, 185, 70, 75),
                                new Color(240, 115, 30, 28),
                                new Color(0, 0, 0, 0)
                            }
                    );
                    Paint oldPaint = g.getPaint();
                    g.setPaint(glow);
                    g.fillOval(tx - radius, ty - radius, radius * 2, radius * 2);
                    g.setPaint(oldPaint);

                    // Torch bracket & animated flame core
                    g.setColor(new Color(95, 65, 35));
                    g.fillRect(tx - 2, ty, 4, 9);
                    g.setColor(new Color(255, 140, 30));
                    g.fillOval(tx - 5, ty - 7, 10, 10);
                    g.setColor(new Color(255, 240, 130));
                    g.fillOval(tx - 3, ty - 5, 6, 6);
                }
            }
        }
    }
}