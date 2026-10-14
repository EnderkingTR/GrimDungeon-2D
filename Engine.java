import java.awt.*;
import java.awt.image.BufferStrategy;
import java.io.*;
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

    private final InputHandler input;
    private final WorldMap worldMap;
    private final List<Entity> entities = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private Player player;

    private double camX = 0, camY = 0;
    private double shakeIntensity = 0;
    private String statusMessage = "Zindana Hoş Geldin! [SPACE]: Kılıç | [Sol Tık]: Büyü";
    private int statusTimer = 240;

    private final Random random = new Random();

    public Engine() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        input = new InputHandler();
        addKeyListener(input);
        addMouseListener(input);
        addMouseMotionListener(input);
        input.debugToggle = false; // Başlangıçta atmosferik Fog of War açık başlasın!

        worldMap = new WorldMap(60, 60);
        populateWorld();
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

    private void populateWorld() {
        entities.clear();
        particles.clear();
        projectiles.clear();

        player = new Player(worldMap.spawnX - 16, worldMap.spawnY - 16, input);
        entities.add(player);

        camX = player.getCenterX() - (WIDTH / 2.0);
        camY = player.getCenterY() - (HEIGHT / 2.0);

        List<Rectangle> rooms = worldMap.getRooms();
        for (int i = 1; i < rooms.size(); i++) {
            Rectangle room = rooms.get(i);
            double centerX = room.getCenterX() * TILE_SIZE;
            double centerY = room.getCenterY() * TILE_SIZE;

            entities.add(new Enemy(centerX - 15, centerY - 15));
            if (room.width >= 8 && random.nextBoolean()) {
                entities.add(new Enemy(centerX + 25, centerY - 15));
            }

            if (random.nextDouble() < 0.45) {
                double chestX = (room.x + 1) * TILE_SIZE + 8;
                double chestY = (room.y + 1) * TILE_SIZE + 10;
                entities.add(new Chest(chestX, chestY));
            }

            if (random.nextDouble() < 0.50) {
                double trapX = (room.x + room.width - 2) * TILE_SIZE + 6;
                double trapY = (room.y + room.height - 2) * TILE_SIZE + 6;
                entities.add(new SpikeTrap(trapX, trapY));
            }
        }
    }

    // F5: Oyun Durumunu Diske Kaydet
    private void saveGameState() {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream("savegame.dat"))) {
            out.writeDouble(player.x);
            out.writeDouble(player.y);
            out.writeInt(player.hp);
            out.writeInt(player.gold);
            showStatusMessage("Oyun Kaydedildi (savegame.dat)!");
        } catch (IOException e) {
            showStatusMessage("Kayıt Hatası!");
        }
    }

    // F9: Kayıtlı Oyunu Yükle
    private void loadGameState() {
        File file = new File("savegame.dat");
        if (!file.exists()) {
            showStatusMessage("Kayıt Dosyası Bulunamadı!");
            return;
        }
        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
            player.x = in.readDouble();
            player.y = in.readDouble();
            player.hp = in.readInt();
            player.gold = in.readInt();
            showStatusMessage("Kayıt Yüklendi!");
        } catch (IOException e) {
            showStatusMessage("Yükleme Hatası!");
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
        input.update();

        if (input.saveGame)
            saveGameState();
        if (input.loadGame)
            loadGameState();

        if (input.regenerateMap || !player.active) {
            worldMap.generateDungeon();
            populateWorld();
            showStatusMessage("Yeni Prosedürel Zindan Üretildi!");
            return;
        }

        player.checkCombatAndInteractions(entities, this);

        for (Entity e : entities) {
            if (e.active) {
                e.update(worldMap, player);
                if (!e.active && e instanceof Enemy) {
                    // Düşman öldüğünde patlama partikülleri ve altın ödülü
                    spawnParticles(e.getCenterX(), e.getCenterY(), new Color(220, 40, 40), 28);
                    player.gold += 25;
                    triggerShake(5.0);
                }
            }
        }

        entities.removeIf(e -> !e.active && e != player);

        for (Projectile proj : projectiles)
            proj.update(worldMap, entities, this);
        projectiles.removeIf(p -> !p.active);

        for (Particle part : particles)
            part.update(worldMap);
        particles.removeIf(p -> !p.active);

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

        g.setColor(new Color(10, 10, 14));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Kamera Sarsıntısı (Screen Shake) Ofseti
        double shakeX = (random.nextDouble() - 0.5) * 2 * shakeIntensity;
        double shakeY = (random.nextDouble() - 0.5) * 2 * shakeIntensity;
        double renderCamX = camX + shakeX;
        double renderCamY = camY + shakeY;

        // --- DÜNYA ÇİZİMİ BAŞLANGICI ---
        g.translate(-renderCamX, -renderCamY);

        worldMap.render(g, renderCamX, renderCamY, input.debugToggle);

        entities.sort(Comparator.comparingDouble(e -> (e instanceof SpikeTrap) ? -10000 : e.y + e.height));
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

        // --- DÜNYA ÇİZİMİ BİTİŞİ ---
        g.translate(renderCamX, renderCamY);

        drawPlayerHUD(g);
        drawMinimap(g);
        if (input.debugToggle) {
            drawDebugUI(g);
        }

        g.dispose();
        bs.show();
    }

    private void drawPlayerHUD(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 185));
        g.fillRoundRect(WIDTH - 235, 15, 215, 95, 10, 10);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 14));
        g.drawString("CAN: " + player.hp + " / " + player.maxHp, WIDTH - 220, 38);

        // Görsel Can Barı
        g.setColor(new Color(50, 20, 20));
        g.fillRoundRect(WIDTH - 220, 45, 185, 10, 5, 5);
        g.setColor(new Color(230, 55, 55));
        int hpBarW = (int) ((player.hp / (double) player.maxHp) * 185);
        g.fillRoundRect(WIDTH - 220, 45, hpBarW, 10, 5, 5);

        g.setColor(new Color(255, 215, 0));
        g.drawString("ALTIN: " + player.gold, WIDTH - 220, 76);

        long enemiesLeft = entities.stream().filter(e -> e instanceof Enemy).count();
        g.setColor(enemiesLeft == 0 ? new Color(0, 255, 140) : Color.LIGHT_GRAY);
        g.setFont(new Font("Consolas", Font.PLAIN, 12));
        g.drawString(enemiesLeft == 0 ? "ZİNDAN TEMİZLENDİ!" : "Kalan Düşman: " + enemiesLeft, WIDTH - 220, 98);

        // Ekran Altı Durum Bildirimi
        if (statusTimer > 0) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRoundRect(WIDTH / 2 - 190, HEIGHT - 55, 380, 34, 8, 8);
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

        // Görünür Düşmanları Minimap'te Kırmızı Nokta Olarak Göster
        for (Entity e : entities) {
            if (e instanceof Enemy) {
                int ex = (int) (e.getCenterX() / TILE_SIZE);
                int ey = (int) (e.getCenterY() / TILE_SIZE);
                if (input.debugToggle || worldMap.isVisible(ex, ey)) {
                    g.setColor(Color.RED);
                    g.fillRect(startX + ex * scale, startY + ey * scale, scale + 1, scale + 1);
                }
            }
        }

        // Oyuncuyu Mavi Nokta Olarak Göster
        int px = (int) (player.getCenterX() / TILE_SIZE);
        int py = (int) (player.getCenterY() / TILE_SIZE);
        g.setColor(Color.CYAN);
        g.fillRect(startX + px * scale - 1, startY + py * scale - 1, scale + 2, scale + 2);
    }

    private void drawDebugUI(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 185));
        g.fillRoundRect(15, 15, 415, 145, 10, 10);

        g.setColor(new Color(0, 255, 140));
        g.setFont(new Font("Consolas", Font.BOLD, 14));
        g.drawString("FPS: " + currentFps + " | UPS: " + currentUps + " | Partikül: " + particles.size(), 25, 38);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 13));
        g.drawString(String.format("Oyuncu (X, Y): %.1f, %.1f", player.x, player.y), 25, 62);

        int tileX = (int) (player.getCenterX() / TILE_SIZE);
        int tileY = (int) (player.getCenterY() / TILE_SIZE);
        g.drawString("Karo: [" + tileX + ", " + tileY + "] | Aktif Varlık: " + entities.size(), 25, 84);
        g.drawString("Kayıt: [F5] Kaydet | [F9] Yükle | [R] Yeni Zindan", 25, 106);

        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Consolas", Font.ITALIC, 11));
        g.drawString("[SPACE]: Kılıç | [Sol Tık]: Büyü | [E]: Sandık | [F3]: Debug", 25, 132);
    }
}