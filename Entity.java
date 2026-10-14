import java.awt.*;

public abstract class Entity {
    public double x, y;
    public int width, height;
    public int hp, maxHp;
    public boolean active = true;
    public boolean solid = true; // Can other entities pass through?

    // Knockback velocity vectors and hurt/damage flash timer
    protected double knockbackX = 0, knockbackY = 0;
    protected int hurtTimer = 0;

    public Entity(double x, double y, int width, int height, int maxHp) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    public abstract void update(WorldMap map, Player player);

    public abstract void render(Graphics2D g, boolean debug);

    // Common AABB Wall Collision and Movement Routine
    protected void moveWithCollision(double dx, double dy, WorldMap map) {
        double totalX = dx + knockbackX;
        double totalY = dy + knockbackY;

        if (!map.isColliding(x + totalX, y, width, height)) {
            x += totalX;
        } else {
            knockbackX = 0;
        }

        if (!map.isColliding(x, y + totalY, width, height)) {
            y += totalY;
        } else {
            knockbackY = 0;
        }

        // Friction damping for knockback forces
        knockbackX *= 0.8;
        knockbackY *= 0.8;
        if (Math.abs(knockbackX) < 0.1)
            knockbackX = 0;
        if (Math.abs(knockbackY) < 0.1)
            knockbackY = 0;

        if (hurtTimer > 0)
            hurtTimer--;
    }

    public void takeDamage(int amount, double sourceX, double sourceY, double knockbackPower) {
        if (hurtTimer > 0 || !active)
            return; // Invincibility frames (i-frames)
        hp -= amount;
        hurtTimer = 12; // 12 ticks (0.2s) white flash and damage immunity

        // Unit vector pointing away from damage source (knockback direction)
        double dirX = getCenterX() - sourceX;
        double dirY = getCenterY() - sourceY;
        double len = Math.sqrt(dirX * dirX + dirY * dirY);
        if (len != 0) {
            knockbackX = (dirX / len) * knockbackPower;
            knockbackY = (dirY / len) * knockbackPower;
        }

        if (hp <= 0) {
            hp = 0;
            active = false;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public double getCenterX() {
        return x + width / 2.0;
    }

    public double getCenterY() {
        return y + height / 2.0;
    }

    protected void drawHealthBar(Graphics2D g) {
        if (hp >= maxHp)
            return;
        int barW = width + 4;
        int barH = 5;
        int barX = (int) x - 2;
        int barY = (int) y - 10;

        g.setColor(new Color(30, 30, 30));
        g.fillRect(barX, barY, barW, barH);
        g.setColor(new Color(220, 50, 50));
        int fillW = (int) (((double) hp / maxHp) * barW);
        g.fillRect(barX, barY, fillW, barH);
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, barW, barH);
    }
}