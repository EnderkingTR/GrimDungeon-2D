import java.awt.*;

public class FloatingText {
    public double x, y;
    private double vy = -1.35;
    public final String text;
    public final Color color;
    public final boolean isCrit;
    private int life = 46;
    private final int maxLife = 46;
    public boolean active = true;

    public FloatingText(double x, double y, String text, Color color, boolean isCrit) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.isCrit = isCrit;
    }

    public void update() {
        y += vy;
        vy *= 0.94;
        life--;
        if (life <= 0) {
            active = false;
        }
    }

    public void render(Graphics2D g) {
        if (!active) return;
        int alpha = Math.max(0, Math.min(255, (int) ((life / (double) maxLife) * 255)));
        g.setFont(new Font("Consolas", Font.BOLD, isCrit ? 16 : 13));
        FontMetrics fm = g.getFontMetrics();
        int drawX = (int) x - fm.stringWidth(text) / 2;
        int drawY = (int) y;

        g.setColor(new Color(0, 0, 0, alpha));
        g.drawString(text, drawX + 1, drawY + 1);

        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g.drawString(text, drawX, drawY);
    }
}

