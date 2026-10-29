import java.awt.*;

public abstract class Entity {
    public double x, y;
    public int width, height;
    public int hp, maxHp;
    public int armor = 0;
    public boolean active = true;
    public boolean solid = true; // Can other entities pass through?
    public boolean isHostile = false; // True for all enemy types that must be cleared to win

    // Knockback velocity vectors and status timers
    protected double knockbackX = 0, knockbackY = 0;
    protected int hurtTimer = 0;
    public int slowTimer = 0;
    public int stunTimer = 0;
    public int invulnerableTimer = 0;

    public Engine engineRef;

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

    protected double getSpeedMultiplier() {
        return slowTimer > 0 ? 0.55 : 1.0;
    }

    protected void updateTimers() {
        if (hurtTimer > 0) hurtTimer--;
        if (slowTimer > 0) slowTimer--;
        if (stunTimer > 0) stunTimer--;
        if (invulnerableTimer > 0) invulnerableTimer--;
    }

    // Common AABB Wall Collision and Movement Routine
    protected void moveWithCollision(double dx, double dy, WorldMap map) {
        double mult = getSpeedMultiplier();
        double totalX = (stunTimer > 0 ? 0 : dx * mult) + knockbackX;
        double totalY = (stunTimer > 0 ? 0 : dy * mult) + knockbackY;

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

        updateTimers();
    }

    public void takeDamage(int amount, double sourceX, double sourceY, double knockbackPower) {
        takeDamage(amount, sourceX, sourceY, knockbackPower, false);
    }

    public void takeDamage(int amount, double sourceX, double sourceY, double knockbackPower, boolean isCrit) {
        if (hurtTimer > 0 || invulnerableTimer > 0 || !active)
            return; // Invincibility frames (i-frames)

        int finalDamage = Math.max(1, amount - armor);
        hp -= finalDamage;
        hurtTimer = (this instanceof Player) ? 18 : 9;

        if (engineRef != null) {
            if (this instanceof Player) {
                engineRef.getCombatTracker().totalDamageTaken += finalDamage;
                engineRef.spawnFloatingText(getCenterX(), y - 6, "-" + finalDamage, new Color(255, 75, 75), false);
                engineRef.triggerShake(4.0);
            } else if (isHostile) {
                engineRef.getCombatTracker().totalDamageDealt += finalDamage;
                if (isCrit) {
                    engineRef.getCombatTracker().criticalHits++;
                    engineRef.spawnFloatingText(getCenterX(), y - 8, "KRİTİK -" + finalDamage + "!", new Color(255, 215, 30), true);
                } else {
                    engineRef.spawnFloatingText(getCenterX(), y - 6, "-" + finalDamage, new Color(245, 245, 255), false);
                }
            }
        }

        // Unit vector pointing away from damage source (knockback direction)
        double dirX = getCenterX() - sourceX;
        double dirY = getCenterY() - sourceY;
        double len = Math.sqrt(dirX * dirX + dirY * dirY);
        if (len != 0 && knockbackPower > 0) {
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
        int barW = Math.max(20, width + 4);
        int barH = 5;
        int barX = (int) getCenterX() - barW / 2;
        int barY = (int) y - 10;

        g.setColor(new Color(30, 30, 30));
        g.fillRect(barX, barY, barW, barH);
        g.setColor(slowTimer > 0 ? new Color(80, 200, 255) : new Color(220, 50, 50));
        int fillW = (int) (((double) hp / maxHp) * barW);
        g.fillRect(barX, barY, fillW, barH);
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, barW, barH);
    }
}
