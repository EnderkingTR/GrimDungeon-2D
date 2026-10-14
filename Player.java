import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Player extends Entity {
    private final InputHandler input;
    private final double speed = 4.6;
    public int gold = 0;

    private int facingX = 0, facingY = 1;

    // Yakın Dövüş (Melee) ve Menzilli Saldırı Zamanlayıcıları
    private int attackTimer = 0;
    private int attackCooldown = 0;
    private int rangedCooldown = 0;
    private Rectangle attackBox = null;

    // Aynı kılıç savurmasında bir düşmana sadece 1 kez hasar vermek için takip
    // kümesi
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
        // 1. SPACE: Geniş Menzilli Kılıç Saldırısını Başlat
        if (input.attack && attackCooldown == 0) {
            attackTimer = 12; // Kılıç 12 tick boyunca havada aktif kalır
            attackCooldown = 20; // Seri saldırı hızı
            hitEntitiesThisSwing.clear();
            updateAttackBox();
            engine.triggerShake(2.5);
        }

        // Kılıç aktif olduğu sürece (12 tick boyunca) menzile giren her düşmanı biç ve
        // geri savur!
        if (attackTimer > 0 && attackBox != null) {
            for (Entity e : entities) {
                if (e != this && e.active && e instanceof Enemy && !hitEntitiesThisSwing.contains(e)) {
                    if (attackBox.intersects(e.getBounds())) {
                        hitEntitiesThisSwing.add(e);
                        // Yüksek Knockback (13.5) ile düşmanı güvenli mesafeye fırlat
                        e.takeDamage(45, getCenterX(), getCenterY(), 13.5);
                        engine.spawnParticles(e.getCenterX(), e.getCenterY(), new Color(255, 70, 70), 18);
                        engine.triggerShake(6.5);
                    }
                }
            }
        }

        // 2. SOL TIK: Fare Yönüne Büyü / Ateş Topu Fırlat
        if (input.mouseAttack && rangedCooldown == 0) {
            rangedCooldown = 16;
            double targetWorldX = input.mouseX + engine.getCamX();
            double targetWorldY = input.mouseY + engine.getCamY();

            // Karakterin yüzünü ateş ettiği yöne çevir
            double angle = Math.atan2(targetWorldY - getCenterY(), targetWorldX - getCenterX());
            facingX = (int) Math.round(Math.cos(angle));
            facingY = (int) Math.round(Math.sin(angle));

            engine.spawnProjectile(getCenterX(), getCenterY(), targetWorldX, targetWorldY);
        }

        // 3. E: Sandık Açma ve Can Yenileme
        if (input.interact) {
            Rectangle interactArea = new Rectangle((int) x - 18, (int) y - 18, width + 36, height + 36);
            for (Entity e : entities) {
                if (e instanceof Chest chest && interactArea.intersects(chest.getBounds())) {
                    int gained = chest.open();
                    if (gained > 0) {
                        gold += gained;
                        hp = Math.min(maxHp, hp + 25); // Sandık açmak +25 Can yeniler!
                        engine.spawnParticles(chest.getCenterX(), chest.getCenterY(), new Color(255, 215, 0), 25);
                        engine.showStatusMessage("+" + gained + " Altın & +25 Can!");
                    }
                }
            }
        }
    }

    // DÜZELTİLMİŞ MELEE HITBOX: Çok daha uzun (52px ileri) ve geniş (68px yay)
    private void updateAttackBox() {
        int reach = 52; // İleri uzanma mesafesi (Önceki: 28)
        int sweep = 68; // Yana doğru kılıç savurma genişliği (Önceki: 38)
        int offset = 38; // Oyuncu merkezinden ileri kaydırma (Önceki: 22)

        int boxW = (facingX != 0 && facingY == 0) ? reach : (facingX == 0 && facingY != 0) ? sweep : 58;
        int boxH = (facingY != 0 && facingX == 0) ? reach : (facingY == 0 && facingX != 0) ? sweep : 58;

        int boxX = (int) getCenterX() + (facingX * offset) - (boxW / 2);
        int boxY = (int) getCenterY() + (facingY * offset) - (boxH / 2);
        attackBox = new Rectangle(boxX, boxY, boxW, boxH);
    }

    @Override
    public void render(Graphics2D g, boolean debug) {
        // Kılıç Savurma Görseli (Hilal/Yay Efekti)
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

        // Oyuncu Gövdesi
        g.setColor(hurtTimer > 0 ? Color.WHITE : new Color(0, 180, 240));
        g.fillRoundRect((int) x, (int) y, width, height, 8, 8);

        // Bakış Vizörü
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