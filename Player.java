import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Player extends Entity {
    private final InputHandler input;
    private final double speed = 4.6;
    public int gold = 0;

    private int facingX = 0, facingY = 1;

    // Melee and ranged attack timers
    private int attackTimer = 0;
    private int attackCooldown = 0;
    private int rangedCooldown = 0;
    private Rectangle attackBox = null;

    // Tracking set to ensure enemies are only hit once per sword swing
    private final Set<Entity> hitEntitiesThisSwing = new HashSet<>();

    public Player(double x, double y, InputHandler input) {
        super(x, y, 32, 32, 100);
        this.input = input;
    }

    @Override
    public void update(WorldMap map, Player self) {
        double dx = 0, dy = 0;
        if (input.up)
            dy -= 1;
        if (input.down)
            dy += 1;
        if (input.left)
            dx -= 1;
        if (input.right)
            dx += 1;

        if (dx != 0 || dy != 0) {
            facingX = (int) Math.signum(dx);
            facingY = (int) Math.signum(dy);

            double len = Math.sqrt(dx * dx + dy * dy);
            dx /= len;
            dy /= len;
        }

        moveWithCollision(dx * speed, dy * speed, map);

        if (attackCooldown > 0)
            attackCooldown--;
        if (rangedCooldown > 0)
            rangedCooldown--;

        if (attackTimer > 0) {
            attackTimer--;
            updateAttackBox();
        } else {
            attackBox = null;
        }
    }

    public void checkCombatAndInteractions(List<Entity> entities, Engine engine) {
        // 1. SPACE: Trigger wide melee sword slash
        if (input.attack && attackCooldown == 0) {
            attackTimer = 12; // Sword swing stays active for 12 ticks
            attackCooldown = 20; // Attack recovery cooldown
            hitEntitiesThisSwing.clear();
            updateAttackBox();
            engine.triggerShake(2.5);
        }

        // Damage and knock back any enemies entering the slash hitbox while active
        if (attackTimer > 0 && attackBox != null) {
            for (Entity e : entities) {
                if (e != this && e.active && e instanceof Enemy && !hitEntitiesThisSwing.contains(e)) {
                    if (attackBox.intersects(e.getBounds())) {
                        hitEntitiesThisSwing.add(e);
                        // High knockback (13.5) launches enemy away safely
                        e.takeDamage(45, getCenterX(), getCenterY(), 13.5);
                        engine.spawnParticles(e.getCenterX(), e.getCenterY(), new Color(255, 70, 70), 18);
                        engine.triggerShake(6.5);
                    }
                }
            }
        }

        // 2. LEFT CLICK: Cast magic projectile towards mouse coordinates
        if (input.mouseAttack && rangedCooldown == 0) {
            rangedCooldown = 16;
            double targetWorldX = input.mouseX + engine.getCamX();
            double targetWorldY = input.mouseY + engine.getCamY();

            // Face the character toward the cast direction
            double angle = Math.atan2(targetWorldY - getCenterY(), targetWorldX - getCenterX());
            facingX = (int) Math.round(Math.cos(angle));
            facingY = (int) Math.round(Math.sin(angle));

            engine.spawnProjectile(getCenterX(), getCenterY(), targetWorldX, targetWorldY);
        }

        // 3. E: Open chests and restore health
        if (input.interact) {
            Rectangle interactArea = new Rectangle((int) x - 18, (int) y - 18, width + 36, height + 36);
            for (Entity e : entities) {
                if (e instanceof Chest chest && interactArea.intersects(chest.getBounds())) {
                    int gained = chest.open();
                    if (gained > 0) {
                        gold += gained;
                        hp = Math.min(maxHp, hp + 25); // Opening chest restores +25 HP
                        engine.spawnParticles(chest.getCenterX(), chest.getCenterY(), new Color(255, 215, 0), 25);
                        engine.showStatusMessage("+" + gained + " Gold & +25 Health!");
                    }
                }
            }
        }
    }

    // Wide arc melee hitbox calculations
    private void updateAttackBox() {
        int reach = 52; // Forward reach
        int sweep = 68; // Lateral swing arc width
        int offset = 38; // Forward offset from center

        int boxW = (facingX != 0 && facingY == 0) ? reach : (facingX == 0 && facingY != 0) ? sweep : 58;
        int boxH = (facingY != 0 && facingX == 0) ? reach : (facingY == 0 && facingX != 0) ? sweep : 58;

        int boxX = (int) getCenterX() + (facingX * offset) - (boxW / 2);
        int boxY = (int) getCenterY() + (facingY * offset) - (boxH / 2);
        attackBox = new Rectangle(boxX, boxY, boxW, boxH);
    }

    @Override
    public void render(Graphics2D g, boolean debug) {
        // Sword slash visual effect (Crescent/Arc)
        if (attackTimer > 0 && attackBox != null) {
            g.setColor(new Color(255, 245, 160, 140));
            g.fillRoundRect(attackBox.x, attackBox.y, attackBox.width, attackBox.height, 28, 28);
            g.setColor(new Color(255, 255, 220, 220));
            g.setStroke(new BasicStroke(2.5f));
            g.drawRoundRect(attackBox.x, attackBox.y, attackBox.width, attackBox.height, 28, 28);
            g.setStroke(new BasicStroke(1f));

            if (debug) {
                g.setColor(Color.YELLOW);
                g.drawRect(attackBox.x, attackBox.y, attackBox.width, attackBox.height);
            }
        }

        // Player body
        g.setColor(hurtTimer > 0 ? Color.WHITE : new Color(0, 180, 240));
        g.fillRoundRect((int) x, (int) y, width, height, 8, 8);

        // Direction visor
        g.setColor(new Color(15, 30, 50));
        int eyeX = (int) getCenterX() + (facingX * 9) - 4;
        int eyeY = (int) getCenterY() + (facingY * 9) - 4;
        g.fillOval(eyeX, eyeY, 8, 8);

        if (debug) {
            g.setColor(Color.RED);
            g.drawRect((int) x, (int) y, width, height);
        } else {
            g.setColor(Color.WHITE);
            g.drawRoundRect((int) x, (int) y, width, height, 8, 8);
        }

        drawHealthBar(g);
    }
}