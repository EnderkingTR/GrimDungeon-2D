import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Engine extends Canvas implements Runnable {
    public static final int WIDTH = 960;
    public static final int HEIGHT = 640;
    public static final int TILE_SIZE = 48;

    private Thread gameThread;
    private boolean running = false;

    private int currentFps = 0;
    private int currentUps = 0;
    private long tickCount = 0;

    private GameState gameState = GameState.MAIN_MENU;
    private final InputHandler input;
    private final ProfileData profile;
    private final MenuSystem menuSystem;
    private final CombatTracker combatTracker = new CombatTracker();

    private final WorldMap worldMap;
    private final List<Entity> entities = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();
    private Player player;

    private double camX = 0, camY = 0;
    private double shakeIntensity = 0;
    private String statusMessage = "GrimDungeon 2D'ye Hoş Geldiniz!";
    private int statusTimer = 180;

    private final Random random = new Random();

    public Engine() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        input = new InputHandler();
        addKeyListener(input);
        addMouseListener(input);
        addMouseMotionListener(input);
        input.debugToggle = false;

        profile = new ProfileData();
        profile.load();

        worldMap = new WorldMap(60, 60);
        menuSystem = new MenuSystem(this, profile, input);
        populateWorld(profile.selectedStage);
    }

    public long getTickCount() {
        return tickCount;
    }

    public CombatTracker getCombatTracker() {
        return combatTracker;
    }

    public void setGameState(GameState newState) {
        this.gameState = newState;
    }

    public double getCamX() {
        return camX;
    }

    public double getCamY() {
        return camY;
    }

    public void triggerShake(double intensity) {
        this.shakeIntensity = Math.min(18.0, this.shakeIntensity + intensity);
    }

    public void showStatusMessage(String msg) {
        this.statusMessage = msg;
        this.statusTimer = 150;
    }

    public void spawnParticles(double x, double y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(x, y, color, 1.5, 6.0, 4, 18));
        }
    }

    public void spawnProjectile(double startX, double startY, double targetX, double targetY) {
        projectiles.add(new Projectile(startX, startY, targetX, targetY));
    }

    public void spawnCustomProjectile(Projectile p) {
        projectiles.add(p);
    }

    public void spawnFloatingText(double x, double y, String text, Color color, boolean isCrit) {
        floatingTexts.add(new FloatingText(x, y, text, color, isCrit));
    }

    public void startStage(int stageLevel) {
        profile.selectedStage = stageLevel;
        int targetRooms = Math.min(16, 6 + stageLevel / 2);
        worldMap.generateDungeon(targetRooms);
        populateWorld(stageLevel);
        combatTracker.reset();
        gameState = GameState.PLAYING;
        showStatusMessage("Bölüm " + stageLevel + " Başladı! [Sol Tık/Space]: Saldırı | [Sağ Tık/Q]: Yetenek");
    }

    private void populateWorld(int stageLevel) {
        entities.clear();
        particles.clear();
        projectiles.clear();
        floatingTexts.clear();

        HeroClass hero = profile.selectedHero;
        ProfileData.ComputedStats stats = profile.computeStats(hero);
        player = new Player(worldMap.spawnX - 16, worldMap.spawnY - 16, input, hero, stats);
        player.engineRef = this;
        entities.add(player);

        camX = player.getCenterX() - (WIDTH / 2.0);
        camY = player.getCenterY() - (HEIGHT / 2.0);

        List<Rectangle> rooms = worldMap.getRooms();
        for (int i = 1; i < rooms.size(); i++) {
            Rectangle room = rooms.get(i);
            double centerX = room.getCenterX() * TILE_SIZE;
            double centerY = room.getCenterY() * TILE_SIZE;

            int roll = random.nextInt(100);

            if (roll < 35) {
                // Standard Melee Guards + optional Ranged support
                Enemy melee = new Enemy(centerX - 15, centerY - 15, stageLevel);
                melee.engineRef = this;
                entities.add(melee);

                if (stageLevel >= 2 && random.nextBoolean()) {
                    RangedEnemy ranged = new RangedEnemy(centerX + 24, centerY - 15, stageLevel);
                    ranged.engineRef = this;
                    entities.add(ranged);
                }
            } else if (roll < 70) {
                // Swarm Pack controlled by a single shared SwarmHiveMind!
                SwarmHiveMind hiveMind = new SwarmHiveMind();
                int swarmSize = 4 + Math.min(4, stageLevel / 3) + random.nextInt(2);
                for (int s = 0; s < swarmSize; s++) {
                    double angle = (2 * Math.PI * s) / swarmSize;
                    double sx = centerX - 9 + Math.cos(angle) * 26;
                    double sy = centerY - 9 + Math.sin(angle) * 26;
                    if (!worldMap.isColliding(sx, sy, 18, 18)) {
                        SwarmEnemy se = new SwarmEnemy(sx, sy, hiveMind, stageLevel, s == 0);
                        se.engineRef = this;
                        entities.add(se);
                    }
                }
            } else {
                // Mixed Room: Turret (Stage 3+) or Ranged + Melee
                if (stageLevel >= 3) {
                    TurretEnemy turret = new TurretEnemy(centerX - 17, centerY - 17, stageLevel);
                    turret.engineRef = this;
                    entities.add(turret);
                } else {
                    Enemy melee = new Enemy(centerX - 15, centerY - 15, stageLevel);
                    melee.engineRef = this;
                    entities.add(melee);
                }

                if (stageLevel >= 2) {
                    RangedEnemy ranged = new RangedEnemy(centerX - 35, centerY + 18, stageLevel);
                    ranged.engineRef = this;
                    entities.add(ranged);
                }
            }

            // Spawn Chests
            if (random.nextDouble() < 0.42) {
                double chestX = (room.x + 1) * TILE_SIZE + 8;
                double chestY = (room.y + 1) * TILE_SIZE + 10;
                Chest chest = new Chest(chestX, chestY);
                chest.engineRef = this;
                entities.add(chest);
            }

            // Spawn Traps (SpikeTrap, AcidTrap, or DartTrap)
            if (random.nextDouble() < 0.58) {
                double trapX = (room.x + room.width - 2) * TILE_SIZE + 6;
                double trapY = (room.y + room.height - 2) * TILE_SIZE + 6;
                int trapType = random.nextInt(3);
                if (trapType == 0) {
                    SpikeTrap st = new SpikeTrap(trapX, trapY);
                    st.engineRef = this;
                    entities.add(st);
                } else if (trapType == 1) {
                    AcidTrap at = new AcidTrap(trapX, trapY);
                    at.engineRef = this;
                    entities.add(at);
                } else {
                    DartTrap dt = new DartTrap(trapX, trapY, Double.NaN);
                    dt.engineRef = this;
                    entities.add(dt);
                }
            }
        }
    }

    public synchronized void start() {
        if (running)
            return;
        running = true;
        gameThread = new Thread(this, "Main-Engine-Thread");
        gameThread.start();
    }

    public synchronized void stop() {
        if (!running)
            return;
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        final double timePerTick = 1_000_000_000.0 / 60.0;
        long lastTime = System.nanoTime();
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        int ticks = 0;

        requestFocus();

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / timePerTick;
            lastTime = now;

            while (delta >= 1) {
                update();
                ticks++;
                delta--;
            }

            render();
            frames++;

            if (System.currentTimeMillis() - timer >= 1000) {
                currentFps = frames;
                currentUps = ticks;
                frames = 0;
                ticks = 0;
                timer += 1000;
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        tickCount++;
        input.update();

        if (input.saveGame) {
            profile.save();
            showStatusMessage("Profil Kaydedildi (savegame.dat)!");
        }
        if (input.loadGame) {
            if (profile.load()) {
                showStatusMessage("Profil Yüklendi!");
            }
        }

        if (gameState != GameState.PLAYING) {
            menuSystem.update(gameState);
            return;
        }

        // In PLAYING state:
        if (input.escapeJustPressed) {
            profile.save();
            gameState = GameState.MAIN_MENU;
            return;
        }

        if (input.regenerateMap) {
            startStage(profile.selectedStage);
            return;
        }

        if (!player.active) {
            profile.gold += player.gold;
            profile.save();
            gameState = GameState.GAME_OVER;
            return;
        }

        combatTracker.elapsedTicks++;
        player.checkCombatAndInteractions(entities, this);

        for (Entity e : entities) {
            if (e.active) {
                e.engineRef = this;
                e.update(worldMap, player);
                if (!e.active && e.isHostile) {
                    int goldDrop = (e instanceof SwarmEnemy) ? 8 : (e instanceof TurretEnemy ? 28 : 18);
                    spawnParticles(e.getCenterX(), e.getCenterY(), new Color(220, 40, 40), 22);
                    player.gold += goldDrop;
                    combatTracker.enemiesKilled++;
                    combatTracker.goldEarnedInStage += goldDrop;
                    spawnFloatingText(e.getCenterX(), e.y, "+" + goldDrop + "G", new Color(255, 215, 50), false);
                    triggerShake(3.5);
                }
            }
        }

        entities.removeIf(e -> !e.active && e != player);

        for (Projectile proj : projectiles)
            proj.update(worldMap, entities, player, this);
        projectiles.removeIf(p -> !p.active);

        for (Particle part : particles)
            part.update(worldMap);
        particles.removeIf(p -> !p.active);

        for (FloatingText ft : floatingTexts)
            ft.update();
        floatingTexts.removeIf(ft -> !ft.active);

        // Check automatic Stage Clear condition: all hostile enemies defeated!
        long remainingHostiles = entities.stream().filter(e -> e.active && e.isHostile).count();
        if (remainingHostiles == 0) {
            int stageBonusGold = 50 + profile.selectedStage * 20;
            combatTracker.goldEarnedInStage += stageBonusGold;
            profile.gold += player.gold + stageBonusGold;
            profile.victoryChests += 1;
            if (profile.selectedStage >= profile.unlockedStage && profile.unlockedStage < ProfileData.MAX_STAGES) {
                profile.unlockedStage++;
            }
            profile.save();
            gameState = GameState.STAGE_CLEAR;
            return;
        }

        int pTileX = (int) (player.getCenterX() / TILE_SIZE);
        int pTileY = (int) (player.getCenterY() / TILE_SIZE);
        worldMap.updateVisibility(pTileX, pTileY, 8);

        double targetCamX = player.getCenterX() - (WIDTH / 2.0);
        double targetCamY = player.getCenterY() - (HEIGHT / 2.0);
        camX += (targetCamX - camX) * 0.12;
        camY += (targetCamY - camY) * 0.12;

        if (shakeIntensity > 0) {
            shakeIntensity *= 0.85;
            if (shakeIntensity < 0.2)
                shakeIntensity = 0;
        }

        if (statusTimer > 0)
            statusTimer--;
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        Graphics2D g = (Graphics2D) bs.getDrawGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (gameState == GameState.MAIN_MENU || gameState == GameState.CHEST_OPENING) {
            menuSystem.render(g, gameState, combatTracker);
            g.dispose();
            bs.show();
            return;
        }

        g.setColor(new Color(10, 10, 14));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        double shakeX = (random.nextDouble() - 0.5) * 2 * shakeIntensity;
        double shakeY = (random.nextDouble() - 0.5) * 2 * shakeIntensity;
        double renderCamX = camX + shakeX;
        double renderCamY = camY + shakeY;

        // --- WORLD RENDERING START ---
        g.translate(-renderCamX, -renderCamY);

        worldMap.render(g, renderCamX, renderCamY, input.debugToggle);

        entities.sort(Comparator.comparingDouble(e -> (e instanceof SpikeTrap || e instanceof AcidTrap || e instanceof DartTrap) ? -10000 : e.y + e.height));
        for (Entity e : entities) {
            int eTileX = (int) (e.getCenterX() / TILE_SIZE);
            int eTileY = (int) (e.getCenterY() / TILE_SIZE);
            if (input.debugToggle || e == player || worldMap.isVisible(eTileX, eTileY)) {
                e.render(g, input.debugToggle);
            }
        }

        for (Projectile proj : projectiles)
            proj.render(g);
        for (Particle part : particles)
            part.render(g);
        for (FloatingText ft : floatingTexts)
            ft.render(g);

        // --- WORLD RENDERING END ---
        g.translate(renderCamX, renderCamY);

        drawPlayerHUD(g);
        drawMinimap(g);
        if (input.debugToggle) {
            drawDebugUI(g);
        }

        if (gameState == GameState.STAGE_CLEAR || gameState == GameState.GAME_OVER) {
            menuSystem.render(g, gameState, combatTracker);
        }

        g.dispose();
        bs.show();
    }

    private void drawPlayerHUD(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 195));
        g.fillRoundRect(WIDTH - 250, 15, 232, 130, 10, 10);
        g.setColor(player.heroClass.themeColor);
        g.drawRoundRect(WIDTH - 250, 15, 232, 130, 10, 10);

        g.setColor(player.heroClass.themeColor);
        g.setFont(new Font("Consolas", Font.BOLD, 13));
        g.drawString("BÖLÜM " + profile.selectedStage + " | " + player.heroClass.displayName, WIDTH - 236, 35);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 13));
        g.drawString("CAN: " + player.hp + " / " + player.maxHp, WIDTH - 236, 54);

        // Health bar
        g.setColor(new Color(50, 20, 20));
        g.fillRoundRect(WIDTH - 236, 60, 200, 10, 5, 5);
        g.setColor(new Color(230, 55, 55));
        int hpBarW = (int) ((player.hp / (double) player.maxHp) * 200);
        g.fillRoundRect(WIDTH - 236, 60, hpBarW, 10, 5, 5);

        // Active Skill cooldown bar
        g.setColor(new Color(25, 35, 55));
        g.fillRoundRect(WIDTH - 236, 78, 200, 8, 4, 4);
        double skillReadyRatio = 1.0 - (player.getSkillCooldown() / (double) Math.max(1, player.getMaxSkillCooldown()));
        g.setColor(skillReadyRatio >= 1.0 ? new Color(80, 235, 255) : new Color(60, 130, 190));
        g.fillRoundRect(WIDTH - 236, 78, (int) (skillReadyRatio * 200), 8, 4, 4);

        g.setFont(new Font("Consolas", Font.PLAIN, 11));
        g.setColor( new Color(180, 225, 255));
        g.drawString("[Sağ Tık/Q] " + player.heroClass.skillName, WIDTH - 236, 100);

        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Consolas", Font.BOLD, 12));
        g.drawString("ALTIN: " + (profile.gold + player.gold), WIDTH - 236, 118);

        long enemiesLeft = entities.stream().filter(e -> e.active && e.isHostile).count();
        g.setColor(enemiesLeft == 0 ? new Color(0, 255, 140) : new Color(255, 120, 120));
        g.drawString("Kalan Düşman: " + enemiesLeft, WIDTH - 236, 136);

        // Top-left quick menu hint
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRoundRect(15, 15, 175, 28, 8, 8);
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Consolas", Font.BOLD, 12));
        g.drawString("[ESC]: Lobi / Ana Menü", 25, 33);

        // Bottom status message toast
        if (statusTimer > 0) {
            g.setColor(new Color(0, 0, 0, 185));
            g.fillRoundRect(WIDTH / 2 - 245, HEIGHT - 55, 490, 34, 8, 8);
            g.setColor(new Color(0, 235, 255));
            g.setFont(new Font("Consolas", Font.BOLD, 13));
            FontMetrics fm = g.getFontMetrics();
            int textX = (WIDTH - fm.stringWidth(statusMessage)) / 2;
            g.drawString(statusMessage, textX, HEIGHT - 33);
        }
    }

    private void drawMinimap(Graphics2D g) {
        int scale = 3;
        int mapW = worldMap.getWidth() * scale;
        int mapH = worldMap.getHeight() * scale;
        int startX = WIDTH - mapW - 20;
        int startY = HEIGHT - mapH - 20;

        g.setColor(new Color(10, 10, 15, 200));
        g.fillRoundRect(startX - 6, startY - 6, mapW + 12, mapH + 12, 8, 8);
        g.setColor(new Color(80, 80, 100));
        g.drawRoundRect(startX - 6, startY - 6, mapW + 12, mapH + 12, 8, 8);

        for (int x = 0; x < worldMap.getWidth(); x++) {
            for (int y = 0; y < worldMap.getHeight(); y++) {
                if (input.debugToggle || worldMap.isDiscovered(x, y)) {
                    TileType t = worldMap.getTile(x, y);
                    if (t == TileType.FLOOR) {
                        g.setColor(worldMap.isVisible(x, y) ? new Color(90, 90, 115) : new Color(45, 45, 60));
                        g.fillRect(startX + x * scale, startY + y * scale, scale, scale);
                    }
                }
            }
        }

        for (Entity e : entities) {
            if (e.active && e.isHostile) {
                int ex = (int) (e.getCenterX() / TILE_SIZE);
                int ey = (int) (e.getCenterY() / TILE_SIZE);
                if (input.debugToggle || worldMap.isVisible(ex, ey)) {
                    g.setColor(e instanceof SwarmEnemy ? Color.ORANGE : Color.RED);
                    g.fillRect(startX + ex * scale, startY + ey * scale, scale + 1, scale + 1);
                }
            }
        }

        int px = (int) (player.getCenterX() / TILE_SIZE);
        int py = (int) (player.getCenterY() / TILE_SIZE);
        g.setColor(Color.CYAN);
        g.fillRect(startX + px * scale - 1, startY + py * scale - 1, scale + 2, scale + 2);
    }

    private void drawDebugUI(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 185));
        g.fillRoundRect(15, 50, 425, 130, 10, 10);

        g.setColor(new Color(0, 255, 140));
        g.setFont(new Font("Consolas", Font.BOLD, 14));
        g.drawString("FPS: " + currentFps + " | UPS: " + currentUps + " | Entities: " + entities.size(), 25, 72);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 12));
        g.drawString(String.format("Player: (%.1f, %.1f) | DmgDealt: %d", player.x, player.y, combatTracker.totalDamageDealt), 25, 95);
        g.drawString("Swarm: Hive Mind A* Active | [F5]: Kaydet | [F9]: Yükle", 25, 116);

        g.setColor(new Color(255, 215, 0));
        g.drawString("[Sol Tık/Space]: Saldır | [Sağ Tık/Q]: Yetenek | [E]: Sandık", 25, 140);
    }
}
