import java.awt.*;

public class Chest extends Entity {
    private boolean opened = false;

    public Chest(double x, double y) {
        super(x, y, 32, 26, 1);
    }

    public int open() {
        if (!opened) {
            opened = true;
            return 50; // Sandıktan çıkan altın miktarı
        }
        return 0;
    }

    @Override
    public void update(WorldMap map, Player player) {
    }

    @Override
    public void render(Graphics2D g, boolean debug) {
        g.setColor(opened ? new Color(90, 65, 45) : new Color(215, 155, 40));
        g.fillRoundRect((int) x, (int) y, width, height, 6, 6);
        g.setColor(new Color(45, 30, 15));
        g.drawRoundRect((int) x, (int) y, width, height, 6, 6);

        // Kilit detayı
        g.setColor(opened ? Color.DARK_GRAY : Color.YELLOW);
        g.fillRect((int) getCenterX() - 3, (int) y + 8, 6, 8);
    }
}