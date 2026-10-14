import java.awt.*;
import java.util.Random;

public class Particle {
    public double x, y;
    private double vx, vy;
    private int life, maxLife;
    private final int size;
    private final Color color;
    public boolean active = true;

    private static final Random random = new Random();

    public Particle(double x, double y, Color color, double speedMin, double speedMax, int size, int life) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.size = size;
        this.maxLife = life + random.nextInt(10);
        this.life = this.maxLife;

        // 360 derece rastgele dağılım açısı
        double angle = random.nextDouble() * Math.PI * 2;
        double speed = speedMin + random.nextDouble() * (speedMax - speedMin);
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
    }

    public void update(WorldMap map) {
        if (!map.isColliding(x + vx, y, size, size)) {
            x += vx;
        } else {
            vx = -vx * 0.5; // Duvara çarparsa sek
        }

        if (!map.isColliding(x, y + vy, size, size)) {
            y += vy;
        } else {
            vy = -vy * 0.5;
        }

        // Hava sürtünmesi ile yavaşlama
        vx *= 0.88;
        vy *= 0.88;

        life--;
        if (life <= 0)
            active = false;
    }

    public void render(Graphics2D g) {
        if (!active)
            return;
        int alpha = (int) (255 * ((double) life / maxLife));
        alpha = Math.max(0, Math.min(255, alpha));
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g.fillRect((int) x, (int) y, size, size);
    }
}