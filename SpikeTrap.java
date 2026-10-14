import java.awt.*;

public class SpikeTrap extends Entity {
    private int timer = 0;
    private boolean spikesUp = false;

    public SpikeTrap(double x, double y) {
        super(x, y, 36, 36, 1);
        this.solid = false; // Üzerinden yürünebilir
    }

    @Override
    public void update(WorldMap map, Player player) {
        timer = (timer + 1) % 150; // 2.5 saniyelik döngü (150 tick)
        spikesUp = timer > 95; // Son 55 tick boyunca dikenler aktif

        if (spikesUp && getBounds().intersects(player.getBounds())) {
            player.takeDamage(20, getCenterX(), getCenterY(), 6.0);
        }
    }

    @Override
    public void render(Graphics2D g, boolean debug) {
        g.setColor(new Color(32, 32, 40));
        g.fillRect((int) x, (int) y, width, height);

        // Tuzak tetiklenmeden hemen önce sarı uyarı verir, çıkınca parlak gri/kırmızı
        // olur
        if (spikesUp) {
            g.setColor(new Color(230, 80, 80));
        } else if (timer > 70) {
            g.setColor(new Color(220, 180, 50)); // Uyarı aşaması
        } else {
            g.setColor(new Color(65, 65, 75));
        }

        // 4 adet diken deliği/ucu çizimi
        int offset = 7;
        g.fillOval((int) x + offset, (int) y + offset, 7, 7);
        g.fillOval((int) x + width - offset - 7, (int) y + offset, 7, 7);
        g.fillOval((int) x + offset, (int) y + height - offset - 7, 7, 7);
        g.fillOval((int) x + width - offset - 7, (int) y + height - offset - 7, 7, 7);
    }
}