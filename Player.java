import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Player extends Entity {
    private final InputHandler input;
    public final HeroClass heroClass;
    public final ProfileData.ComputedStats stats;

    public double speed;
    public int baseDamage;
    public double critChance;
    public int gold = 0;

    private int facingX = 0, facingY = 1;
    private double aimAngle = 0;

    // Attack & Skill timers
    private int attackTimer = 0;
    private int attackMaxDuration = 10;
    private int attackCooldown = 0;
    private int skillCooldown = 0;
    private int dashTimer = 0;
    private double dashVx = 0, dashVy = 0;

    private Rectangle attackBox = null;
    private final Set<Entity> hitEntitiesThisSwing = new HashSet<>();
    private final Random random = new Random();

    public Player(double x, double y, InputHandler input, HeroClass heroClass, ProfileData.ComputedStats stats) {
        super(x, y, 32, 32, stats.maxHp);
        this.input = input;
        this.heroClass = heroClass;
        this.stats = stats;
        this.speed = stats.speed;
        this.baseDamage = stats.damage;
        this.critChance = stats.critChance;
        this.armor = stats.armor;
        this.isHostile = false;
    }

    public int getSkillCooldown() {
        return skillCooldown;
    }

    public int getMaxSkillCooldown() {
        return stats.skillCooldown;
    }

    private int rollDamage(boolean[] outCrit) {
        boolean crit = random.nextDouble() < critChance;
        outCrit[0] = crit;
        double variance = 0.90 + random.nextDouble() * 0.20;
        int dmg = (int) Math.round(baseDamage * variance);
        if (crit) {
            dmg = (int) Math.round(dmg * (heroClass == HeroClass.ASSASSIN ? 2.15 : 1.75));
        }
        return Math.max(1, dmg);
    }

    @Override
    public void update(WorldMap map, Player self) {
        double dx = 0, dy = 0;
        if (input.up) dy -= 1;
        if (input.down) dy += 1;
        if (input.left) dx -= 1;
        if (input.right) dx += 1;

        if (dx != 0 || dy != 0) {
            facingX = (int) Math.signum(dx);
            facingY = (int) Math.signum(dy);

            double len = Math.sqrt(dx * dx + dy * dy);
            dx /= len;
            dy /= len;
        }

        if (dashTimer > 0) {
            dashTimer--;
            moveWithCollision(dashVx, dashVy, map);
        } else {
            moveWithCollision(dx * speed, dy * speed, map);
        }

        if (attackCooldown > 0) attackCooldown--;
        if (skillCooldown > 0) skillCooldown--;

        if (attackTimer > 0) {
            attackTimer--;
            updateAttackBox();
        } else {
            attackBox = null;
        }
    }

    public void checkCombatAndInteractions(List<Entity> entities, Engine engine) {
        this.engineRef = engine;

        double targetWorldX = input.mouseX + engine.getCamX();
        double targetWorldY = input.mouseY + engine.getCamY();
        aimAngle = Math.atan2(targetWorldY - getCenterY(), targetWorldX - getCenterX());

        // Primary Weapon Attack (Left-Click or Space)
        boolean triggerPrimary = input.mouseAttack || input.attack;
        if (triggerPrimary && attackCooldown == 0) {
            // Update facing direction toward mouse cursor when attacking with mouse
            if (input.mouseAttack) {
                double cos = Math.cos(aimAngle);
                double sin = Math.sin(aimAngle);
                facingX = Math.abs(cos) > 0.38 ? (int) Math.signum(cos) : 0;
                facingY = Math.abs(sin) > 0.38 ? (int) Math.signum(sin) : 0;
                if (facingX == 0 && facingY == 0) facingY = 1;
            }

            if (heroClass.isMelee()) {
                attackMaxDuration = switch (heroClass) {
                    case ASSASSIN -> 7;
                    case VIKING -> 14;
                    default -> 10;
                };
                attackTimer = attackMaxDuration;
                attackCooldown = stats.attackCooldown;
                hitEntitiesThisSwing.clear();
                updateAttackBox();
            } else {
                // Ranged classes: ARCHER or MAGE
                attackCooldown = stats.attackCooldown;
                boolean[] isCrit = new boolean[1];
                int dmg = rollDamage(isCrit);

                if (heroClass == HeroClass.ARCHER) {
                    engine.spawnCustomProjectile(new Projectile(
                            getCenterX(), getCenterY(), aimAngle,
                            11.0, dmg, 5, 65, true, isCrit[0],
                            false, false,
                            new Color(190, 255, 140), new Color(60, 210, 90, 110)
                    ));
                } else {
                    // MAGE energy orb
                    engine.spawnCustomProjectile(new Projectile(
                            getCenterX(), getCenterY(), aimAngle,
                            8.2, dmg, 8, 80, true, isCrit[0],
                            false, false,
                            new Color(220, 245, 255), new Color(0, 195, 255, 130)
                    ));
                }
            }
        }

        // Active Class Skill (Right-Click or Q / Shift)
        if (input.skillUse && skillCooldown == 0) {
            activateClassSkill(entities, engine);
        }

        // Assassin Dash damage check
        if (dashTimer > 0 && heroClass == HeroClass.ASSASSIN) {
            Rectangle dashBounds = new Rectangle((int) x - 8, (int) y - 8, width + 16, height + 16);
            for (Entity e : entities) {
                if (e.active && e.isHostile && !hitEntitiesThisSwing.contains(e) && dashBounds.intersects(e.getBounds())) {
                    hitEntitiesThisSwing.add(e);
                    int skillDmg = (int) Math.round(baseDamage * 1.65 * stats.skillPowerMultiplier);
                    e.takeDamage(skillDmg, getCenterX(), getCenterY(), 7.0, true);
                    engine.spawnParticles(e.getCenterX(), e.getCenterY(), new Color(195, 80, 255), 14);
                }
            }
        }

        // Melee hitbox collision check
        if (attackBox != null) {
            double kb = switch (heroClass) {
                case VIKING -> 14.0;
                case ASSASSIN -> 6.0;
                default -> 10.5;
            };
            for (Entity e : entities) {
                if (e.active && e.isHostile && !hitEntitiesThisSwing.contains(e) && attackBox.intersects(e.getBounds())) {
                    hitEntitiesThisSwing.add(e);
                    boolean[] isCrit = new boolean[1];
                    int dmg = rollDamage(isCrit);
                    e.takeDamage(dmg, getCenterX(), getCenterY(), kb, isCrit[0]);
                    engine.spawnParticles(e.getCenterX(), e.getCenterY(), heroClass.themeColor, 12);
                    engine.triggerShake(heroClass == HeroClass.VIKING ? 6.0 : 3.5);
                }
            }
        }

        // [E]: Interact with nearby treasure chests
        if (input.interact) {
            Rectangle interactArea = new Rectangle((int) x - 18, (int) y - 18, width + 36, height + 36);
            for (Entity e : entities) {
                if (e instanceof Chest chest && interactArea.intersects(chest.getBounds())) {
                    int reward = chest.open();
                    if (reward > 0) {
                        gold += reward;
                        engine.getCombatTracker().goldEarnedInStage += reward;
                        hp = Math.min(maxHp, hp + 25);
                        engine.spawnParticles(chest.getCenterX(), chest.getCenterY(), new Color(255, 215, 0), 22);
                        engine.spawnFloatingText(chest.getCenterX(), chest.y - 6, "+" + reward + " ALTIN & +25 CAN", new Color(255, 220, 40), true);
                        engine.showStatusMessage("Sandık Açıldı! +" + reward + " Altın & +25 Can");
                    }
                }
            }
        }
    }

    private void activateClassSkill(List<Entity> entities, Engine engine) {
        skillCooldown = stats.skillCooldown;
        int skillDmg = (int) Math.round(baseDamage * 1.75 * stats.skillPowerMultiplier);

        switch (heroClass) {
            case WARRIOR -> {
                // Shield Bash: AoE shockwave + stun
                engine.triggerShake(7.5);
                engine.spawnParticles(getCenterX(), getCenterY(), new Color(80, 175, 255), 28);
                engine.spawnFloatingText(getCenterX(), y - 14, "KALKAN DARBESİ!", new Color(80, 185, 255), true);
                for (Entity e : entities) {
                    if (e.active && e.isHostile) {
                        double dist = Math.hypot(e.getCenterX() - getCenterX(), e.getCenterY() - getCenterY());
                        if (dist <= 115) {
                            e.takeDamage(skillDmg, getCenterX(), getCenterY(), 15.0, false);
                            e.stunTimer = 75; // 1.25s stun
                        }
                    }
                }
            }
            case ASSASSIN -> {
                // Shadow Dash: Invulnerable high-speed dash cutting through enemies
                dashTimer = 10;
                invulnerableTimer = 32;
                dashVx = Math.cos(aimAngle) * 14.5;
                dashVy = Math.sin(aimAngle) * 14.5;
                hitEntitiesThisSwing.clear();
                engine.spawnParticles(getCenterX(), getCenterY(), new Color(190, 70, 255), 24);
                engine.spawnFloatingText(getCenterX(), y - 14, "GÖLGE ADIMI!", new Color(200, 100, 255), true);
            }
            case VIKING -> {
                // Battle Cry: 360 degree ground slam + self heal
                int heal = Math.min(maxHp - hp, (int) (25 * stats.skillPowerMultiplier));
                hp = Math.min(maxHp, hp + heal);
                engine.triggerShake(9.5);
                engine.spawnParticles(getCenterX(), getCenterY(), new Color(255, 110, 40), 34);
                engine.spawnFloatingText(getCenterX(), y - 14, "SAVAŞ NARASI! +" + heal + " CAN", new Color(255, 130, 50), true);
                for (Entity e : entities) {
                    if (e.active && e.isHostile) {
                        double dist = Math.hypot(e.getCenterX() - getCenterX(), e.getCenterY() - getCenterY());
                        if (dist <= 130) {
                            e.takeDamage(skillDmg, getCenterX(), getCenterY(), 17.0, true);
                        }
                    }
                }
            }
            case ARCHER -> {
                // Multishot Volley: 5 piercing arrows in a fan
                engine.spawnFloatingText(getCenterX(), y - 14, "YAYLIM ATEŞİ!", new Color(80, 240, 120), true);
                int arrowDmg = (int) Math.round(baseDamage * 1.15 * stats.skillPowerMultiplier);
                for (int i = -2; i <= 2; i++) {
                    double spreadAngle = aimAngle + (i * 0.13);
                    engine.spawnCustomProjectile(new Projectile(
                            getCenterX(), getCenterY(), spreadAngle,
                            12.0, arrowDmg, 6, 75, true, true,
                            true, false,
                            new Color(140, 255, 120), new Color(40, 220, 80, 130)
                    ));
                }
            }
            case MAGE -> {
                // Frost Nova: 8-way freezing projectiles + local chill
                engine.triggerShake(6.0);
                engine.spawnParticles(getCenterX(), getCenterY(), new Color(100, 235, 255), 30);
                engine.spawnFloatingText(getCenterX(), y - 14, "BUZ NOVASI!", new Color(90, 235, 255), true);
                int orbDmg = (int) Math.round(baseDamage * 1.25 * stats.skillPowerMultiplier);
                for (int i = 0; i < 8; i++) {
                    double angle = i * (Math.PI / 4.0);
                    engine.spawnCustomProjectile(new Projectile(
                            getCenterX(), getCenterY(), angle,
                            7.5, orbDmg, 7, 65, true, false,
                            true, true,
                            new Color(180, 245, 255), new Color(0, 180, 255, 120)
                    ));
                }
            }
        }
    }

    private void updateAttackBox() {
        int reach = switch (heroClass) {
            case VIKING -> 46;
            case ASSASSIN -> 32;
            default -> 38;
        };
        int sweep = switch (heroClass) {
            case VIKING -> 58;
            case ASSASSIN -> 36;
            default -> 46;
        };

        int bx = (int) getCenterX() - sweep / 2 + facingX * (reach / 2 + 12);
        int by = (int) getCenterY() - sweep / 2 + facingY * (reach / 2 + 12);
        attackBox = new Rectangle(bx, by, sweep, sweep);
    }

    @Override
    public void render(Graphics2D g, boolean debug) {
        // Draw melee weapon sweep animation
        if (attackBox != null) {
            Color c = heroClass.themeColor;
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 110));
            g.fillOval(attackBox.x - 4, attackBox.y - 4, attackBox.width + 8, attackBox.height + 8);
            g.setColor(new Color(255, 255, 255, 200));
            g.drawOval(attackBox.x, attackBox.y, attackBox.width, attackBox.height);
        }

        // Shadow under hero
        g.setColor(new Color(0, 0, 0, 110));
        g.fillOval((int) x + 2, (int) y + height - 7, width - 4, 10);

        // Body color based on HeroClass (or white flash on hurt / invulnerable glow)
        if (hurtTimer > 0) {
            g.setColor(Color.WHITE);
        } else if (invulnerableTimer > 0) {
            g.setColor(new Color(220, 160, 255));
        } else {
            g.setColor(heroClass.themeColor);
        }
        g.fillRoundRect((int) x, (int) y, width, height, 10, 10);

        // Class-specific visual armor/headgear details
        g.setColor(new Color(25, 28, 38));
        g.drawRoundRect((int) x, (int) y, width, height, 10, 10);

        switch (heroClass) {
            case WARRIOR -> {
                // Knight visor & silver trim
                g.setColor(new Color(210, 220, 235));
                g.fillRoundRect((int) x + 4, (int) y + 3, width - 8, 10, 4, 4);
                g.setColor(new Color(30, 40, 60));
                g.fillRect((int) x + 8, (int) y + 7, width - 16, 3);
            }
            case ASSASSIN -> {
                // Dark shadow hood & glowing crimson eyes
                g.setColor(new Color(45, 20, 65));
                g.fillRoundRect((int) x + 3, (int) y + 2, width - 6, 13, 6, 6);
                g.setColor(new Color(255, 70, 90));
                g.fillRect((int) x + 8, (int) y + 9, 5, 3);
                g.fillRect((int) x + width - 13, (int) y + 9, 5, 3);
            }
            case VIKING -> {
                // Horned helmet
                g.setColor(new Color(235, 225, 200));
                g.fillPolygon(new int[]{(int) x, (int) x - 7, (int) x + 5}, new int[]{(int) y + 8, (int) y - 4, (int) y + 2}, 3);
                g.fillPolygon(new int[]{(int) x + width, (int) x + width + 7, (int) x + width - 5}, new int[]{(int) y + 8, (int) y - 4, (int) y + 2}, 3);
                g.setColor(new Color(120, 70, 40));
                g.fillRect((int) x + 4, (int) y + 3, width - 8, 7);
            }
            case ARCHER -> {
                // Ranger feathered cap
                g.setColor(new Color(30, 125, 60));
                g.fillRoundRect((int) x + 3, (int) y + 2, width - 6, 9, 5, 5);
                g.setColor(new Color(255, 220, 70));
                g.drawLine((int) x + width - 5, (int) y + 4, (int) x + width + 4, (int) y - 4);
            }
            case MAGE -> {
                // Arcane pointed hat & glowing rune
                g.setColor(new Color(25, 55, 130));
                g.fillPolygon(new int[]{(int) x + 2, (int) getCenterX(), (int) x + width - 2}, new int[]{(int) y + 8, (int) y - 9, (int) y + 8}, 3);
                g.setColor(new Color(140, 245, 255));
                g.fillOval((int) getCenterX() - 3, (int) y + 14, 6, 6);
            }
        }

        // Draw Weapon in hand oriented toward aimAngle
        AffineTransform old = g.getTransform();
        g.translate(getCenterX(), getCenterY());
        double swingOffset = attackTimer > 0 ? Math.sin((attackTimer / (double) attackMaxDuration) * Math.PI) * 0.9 : 0;
        g.rotate(aimAngle + swingOffset);

        switch (heroClass) {
            case WARRIOR -> {
                g.setColor(new Color(220, 230, 245));
                g.fillRect(12, -2, 16, 4);
                g.setColor(new Color(215, 175, 50));
                g.fillRect(10, -5, 3, 10);
            }
            case ASSASSIN -> {
                g.setColor(new Color(200, 120, 255));
                g.fillPolygon(new int[]{12, 24, 12}, new int[]{-4, -1, 1}, 3);
                g.fillPolygon(new int[]{12, 24, 12}, new int[]{1, 4, 6}, 3);
            }
            case VIKING -> {
                g.setColor(new Color(140, 95, 50));
                g.fillRect(10, -2, 18, 4);
                g.setColor(new Color(210, 215, 225));
                g.fillArc(18, -10, 12, 20, -60, 120);
            }
            case ARCHER -> {
                g.setColor(new Color(175, 125, 65));
                g.drawArc(8, -10, 12, 20, -75, 150);
            }
            case MAGE -> {
                g.setColor(new Color(150, 105, 55));
                g.fillRect(10, -2, 16, 3);
                g.setColor(new Color(0, 240, 255));
                g.fillOval(23, -5, 8, 8);
            }
        }
        g.setTransform(old);

        drawHealthBar(g);

        if (debug) {
            g.setColor(Color.GREEN);
            g.drawRect((int) x, (int) y, width, height);
            if (attackBox != null) {
                g.setColor(Color.RED);
                g.drawRect(attackBox.x, attackBox.y, attackBox.width, attackBox.height);
            }
        }
    }
}
