import java.awt.*;
import java.util.Random;

public class MenuSystem {
    private final Engine engine;
    private final ProfileData profile;
    private final InputHandler input;
    private final Random random = new Random();

    public GameState.MenuTab currentTab = GameState.MenuTab.CAMPAIGN;
    private int selectedEquipmentId = 0;
    private ProfileData.ChestOpenResult lastChestResult = null;
    private String toastMessage = "";
    private int toastTimer = 0;

    // Campaign node coordinates (Candy Crush winding path for 15 stages)
    private final Point[] stageNodes = new Point[ProfileData.MAX_STAGES];

    public MenuSystem(Engine engine, ProfileData profile, InputHandler input) {
        this.engine = engine;
        this.profile = profile;
        this.input = input;
        initCampaignNodes();
    }

    private void initCampaignNodes() {
        // Create a winding S-curve path across the left/center campaign panel
        int[] xs = { 90, 185, 280, 375, 470, 520, 430, 330, 230, 130, 95, 195, 300, 410, 515 };
        int[] ys = { 495, 480, 455, 440, 405, 325, 300, 290, 280, 250, 170, 145, 140, 135, 125 };
        for (int i = 0; i < ProfileData.MAX_STAGES; i++) {
            stageNodes[i] = new Point(xs[i], ys[i]);
        }
    }

    public void showToast(String msg) {
        this.toastMessage = msg;
        this.toastTimer = 140;
    }

    public void update(GameState state) {
        if (toastTimer > 0) toastTimer--;
        if (!input.mouseJustClicked) return;

        int mx = input.mouseX;
        int my = input.mouseY;

        if (state == GameState.MAIN_MENU) {
            // Top Navigation Tabs (6 tabs)
            GameState.MenuTab[] tabs = GameState.MenuTab.values();
            int tabW = 142;
            int startX = 40;
            for (int i = 0; i < tabs.length; i++) {
                Rectangle tabRect = new Rectangle(startX + i * (tabW + 6), 52, tabW, 38);
                if (tabRect.contains(mx, my)) {
                    currentTab = tabs[i];
                    return;
                }
            }

            switch (currentTab) {
                case CAMPAIGN -> handleCampaignClicks(mx, my);
                case HEROES -> handleHeroesClicks(mx, my);
                case EQUIPMENT -> handleEquipmentClicks(mx, my);
                case SKILLS -> handleSkillsClicks(mx, my);
                case SHOP -> handleShopClicks(mx, my);
                case SETTINGS -> handleSettingsClicks(mx, my);
            }
        } else if (state == GameState.CHEST_OPENING) {
            Rectangle closeBtn = new Rectangle(Engine.WIDTH / 2 - 110, 520, 220, 46);
            if (closeBtn.contains(mx, my)) {
                engine.setGameState(GameState.MAIN_MENU);
            }
        } else if (state == GameState.STAGE_CLEAR) {
            Rectangle menuBtn = new Rectangle(Engine.WIDTH / 2 - 230, 495, 210, 48);
            Rectangle nextBtn = new Rectangle(Engine.WIDTH / 2 + 20, 495, 210, 48);

            if (menuBtn.contains(mx, my)) {
                currentTab = GameState.MenuTab.SHOP;
                engine.setGameState(GameState.MAIN_MENU);
            } else if (nextBtn.contains(mx, my)) {
                profile.selectedStage = Math.min(ProfileData.MAX_STAGES, profile.unlockedStage);
                engine.startStage(profile.selectedStage);
            }
        } else if (state == GameState.GAME_OVER) {
            Rectangle menuBtn = new Rectangle(Engine.WIDTH / 2 - 230, 495, 210, 48);
            Rectangle retryBtn = new Rectangle(Engine.WIDTH / 2 + 20, 495, 210, 48);

            if (menuBtn.contains(mx, my)) {
                engine.setGameState(GameState.MAIN_MENU);
            } else if (retryBtn.contains(mx, my)) {
                engine.startStage(profile.selectedStage);
            }
        }
    }

    private void handleCampaignClicks(int mx, int my) {
        // Check clicking on any unlocked stage node
        for (int i = 0; i < ProfileData.MAX_STAGES; i++) {
            Point p = stageNodes[i];
            if (Math.hypot(mx - p.x, my - p.y) <= 24) {
                int stageNum = i + 1;
                if (stageNum <= profile.unlockedStage) {
                    profile.selectedStage = stageNum;
                    profile.save();
                } else {
                    showToast("Bu bölüm henüz kilitli! Önce Bölüm " + profile.unlockedStage + "'i tamamlayın.");
                }
                return;
            }
        }

        // Start Stage Button on Right Panel
        Rectangle startBtn = new Rectangle(630, 495, 270, 54);
        if (startBtn.contains(mx, my)) {
            engine.startStage(profile.selectedStage);
        }
    }

    private void handleHeroesClicks(int mx, int my) {
        HeroClass[] heroes = HeroClass.values();
        int cardW = 166;
        int startX = 40;
        for (int i = 0; i < heroes.length; i++) {
            HeroClass hc = heroes[i];
            int cx = startX + i * (cardW + 12);
            int cy = 115;

            Rectangle selectBtn = new Rectangle(cx + 12, cy + 365, cardW - 24, 34);
            Rectangle upgradeBtn = new Rectangle(cx + 12, cy + 410, cardW - 24, 38);
            Rectangle cardArea = new Rectangle(cx, cy, cardW, 355);

            if (selectBtn.contains(mx, my) || cardArea.contains(mx, my)) {
                profile.selectedHero = hc;
                profile.save();
                showToast(hc.displayName + " seçildi!");
                return;
            }
            if (upgradeBtn.contains(mx, my)) {
                if (profile.upgradeHero(hc)) {
                    showToast(hc.displayName + " Seviye " + profile.heroLevels[i] + " oldu!");
                } else {
                    showToast("Yetersiz Kart veya Altın!");
                }
                return;
            }
        }
    }

    private void handleEquipmentClicks(int mx, int my) {
        // 27 items in 3 rows x 9 columns grid
        int cols = 9;
        int cellW = 62;
        int cellH = 62;
        int gridX = 40;
        int gridY = 230;

        for (int id = 0; id < EquipmentItem.CATALOG.length; id++) {
            int r = id / cols;
            int c = id % cols;
            Rectangle cell = new Rectangle(gridX + c * (cellW + 6), gridY + r * (cellH + 10), cellW, cellH);
            if (cell.contains(mx, my)) {
                selectedEquipmentId = id;
                return;
            }
        }

        // Right detail panel Equip & Upgrade buttons
        Rectangle equipBtn = new Rectangle(665, 435, 240, 40);
        Rectangle upgradeBtn = new Rectangle(665, 488, 240, 42);

        if (equipBtn.contains(mx, my)) {
            EquipmentItem item = EquipmentItem.CATALOG[selectedEquipmentId];
            if (profile.itemLevels[selectedEquipmentId] <= 0) {
                showToast("Bu ekipman henüz açılmadı! Sandıklardan kartını bulun.");
            } else {
                profile.equipItem(selectedEquipmentId);
                showToast(item.name + " kuşanıldı!");
            }
        } else if (upgradeBtn.contains(mx, my)) {
            EquipmentItem item = EquipmentItem.CATALOG[selectedEquipmentId];
            if (profile.upgradeItem(selectedEquipmentId)) {
                showToast(item.name + " Seviye " + profile.itemLevels[selectedEquipmentId] + " oldu!");
            } else {
                showToast("Yükseltme için yeterli Kart veya Altın yok!");
            }
        }
    }

    private void handleSkillsClicks(int mx, int my) {
        HeroClass[] heroes = HeroClass.values();
        for (int i = 0; i < heroes.length; i++) {
            int rowY = 120 + i * 92;
            Rectangle upgBtn = new Rectangle(710, rowY + 20, 195, 44);
            if (upgBtn.contains(mx, my)) {
                HeroClass hc = heroes[i];
                if (profile.upgradeSkill(hc)) {
                    showToast(hc.skillName + " yeteneği Seviye " + profile.skillLevels[i] + " oldu!");
                } else {
                    showToast("Yetersiz Altın!");
                }
                return;
            }
        }
    }

    private void handleShopClicks(int mx, int my) {
        // Open Victory Chest button
        Rectangle openChestBtn = new Rectangle(85, 390, 290, 52);
        if (openChestBtn.contains(mx, my)) {
            if (profile.victoryChests > 0) {
                lastChestResult = profile.openVictoryChest(random);
                engine.setGameState(GameState.CHEST_OPENING);
            } else {
                showToast("Açılacak Zafer Sandığınız yok! Bölüm geçerek kazanabilirsiniz.");
            }
            return;
        }

        // Buy Chest with Gold button (180 Gold)
        Rectangle buyChestBtn = new Rectangle(85, 458, 290, 44);
        if (buyChestBtn.contains(mx, my)) {
            if (profile.gold >= 180) {
                profile.gold -= 180;
                profile.victoryChests++;
                lastChestResult = profile.openVictoryChest(random);
                engine.setGameState(GameState.CHEST_OPENING);
            } else {
                showToast("Sandık almak için 180 Altın gerekli!");
            }
            return;
        }

        // Direct Card Offers on the Right Panel
        // Offer 1: Selected Hero 3x Cards (90 Gold)
        Rectangle offer1 = new Rectangle(520, 215, 360, 75);
        if (offer1.contains(mx, my)) {
            if (profile.gold >= 90) {
                profile.gold -= 90;
                profile.heroCards[profile.selectedHero.ordinal()] += 3;
                profile.save();
                showToast("+3 " + profile.selectedHero.displayName + " Kartı satın alındı!");
            } else {
                showToast("Yetersiz Altın (90 Altın gerekli)!");
            }
            return;
        }

        // Offer 2: Random Rare/Epic Equipment Card (120 Gold)
        Rectangle offer2 = new Rectangle(520, 315, 360, 75);
        if (offer2.contains(mx, my)) {
            if (profile.gold >= 120) {
                profile.gold -= 120;
                int[] rareOrEpic = { 1, 4, 7, 8, 10, 13, 16, 19, 22, 25 };
                int pickedId = rareOrEpic[random.nextInt(rareOrEpic.length)];
                profile.itemCards[pickedId] += 2;
                if (profile.itemLevels[pickedId] == 0) profile.itemLevels[pickedId] = 1;
                profile.save();
                showToast("+2 " + EquipmentItem.CATALOG[pickedId].name + " Kartı alındı!");
            } else {
                showToast("Yetersiz Altın (120 Altın gerekli)!");
            }
            return;
        }

        // Offer 3: Legendary Equipment Card Pack (300 Gold)
        Rectangle offer3 = new Rectangle(520, 415, 360, 75);
        if (offer3.contains(mx, my)) {
            if (profile.gold >= 300) {
                profile.gold -= 300;
                int[] legendaries = { 2, 5, 11, 14, 17, 20, 23, 26 };
                int pickedId = legendaries[random.nextInt(legendaries.length)];
                profile.itemCards[pickedId] += 2;
                if (profile.itemLevels[pickedId] == 0) profile.itemLevels[pickedId] = 1;
                profile.save();
                showToast("EFSANEVİ! +2 " + EquipmentItem.CATALOG[pickedId].name + " Kartı alındı!");
            } else {
                showToast("Yetersiz Altın (300 Altın gerekli)!");
            }
        }
    }

    private void handleSettingsClicks(int mx, int my) {
        // Left Panel: Display Modes (Windowed, Borderless Fullscreen, Fullscreen)
        GameState.DisplayMode[] modes = GameState.DisplayMode.values();
        for (int i = 0; i < modes.length; i++) {
            Rectangle modeBtn = new Rectangle(70, 185 + i * 70, 360, 52);
            if (modeBtn.contains(mx, my)) {
                profile.displayMode = modes[i];
                profile.save();
                engine.requestDisplayUpdate();
                showToast("Ekran Modu: " + modes[i].displayName);
                return;
            }
        }

        // Debug overlay quick toggle button
        Rectangle debugBtn = new Rectangle(70, 435, 360, 46);
        if (debugBtn.contains(mx, my)) {
            input.debugToggle = !input.debugToggle;
            showToast("Debug Modu (F3): " + (input.debugToggle ? "AÇIK" : "KAPALI"));
            return;
        }

        // Right Panel: Windowed Resolution Presets (5 presets)
        for (int i = 0; i < ProfileData.RES_PRESETS.length; i++) {
            Rectangle resBtn = new Rectangle(515, 185 + i * 62, 370, 48);
            if (resBtn.contains(mx, my)) {
                profile.resolutionIndex = i;
                profile.displayMode = GameState.DisplayMode.WINDOWED;
                profile.save();
                engine.requestDisplayUpdate();
                showToast("Çözünürlük: " + ProfileData.RES_LABELS[i] + " uygulandı!");
                return;
            }
        }
    }

    public void render(Graphics2D g, GameState state, CombatTracker tracker) {
        if (state == GameState.MAIN_MENU) {
            renderMainMenu(g);
        } else if (state == GameState.CHEST_OPENING) {
            renderMainMenu(g);
            renderChestOpeningModal(g);
        } else if (state == GameState.STAGE_CLEAR) {
            renderStageSummaryModal(g, tracker, true);
        } else if (state == GameState.GAME_OVER) {
            renderStageSummaryModal(g, tracker, false);
        }

        if (toastTimer > 0) {
            g.setColor(new Color(10, 12, 20, 230));
            g.fillRoundRect(Engine.WIDTH / 2 - 220, Engine.HEIGHT - 48, 440, 34, 10, 10);
            g.setColor(new Color(0, 230, 255));
            g.drawRoundRect(Engine.WIDTH / 2 - 220, Engine.HEIGHT - 48, 440, 34, 10, 10);
            g.setFont(new Font("Consolas", Font.BOLD, 13));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(toastMessage, (Engine.WIDTH - fm.stringWidth(toastMessage)) / 2, Engine.HEIGHT - 26);
        }
    }

    private void renderMainMenu(Graphics2D g) {
        // Dark atmospheric background
        GradientPaint bg = new GradientPaint(0, 0, new Color(14, 16, 26), 0, Engine.HEIGHT, new Color(24, 20, 38));
        g.setPaint(bg);
        g.fillRect(0, 0, Engine.WIDTH, Engine.HEIGHT);

        // Top Header Bar
        g.setColor(new Color(20, 24, 36, 240));
        g.fillRect(0, 0, Engine.WIDTH, 44);
        g.setColor(new Color(65, 75, 110));
        g.drawLine(0, 44, Engine.WIDTH, 44);

        g.setFont(new Font("Consolas", Font.BOLD, 18));
        g.setColor(new Color(255, 215, 60));
        g.drawString("⚔ GRIM DUNGEON 2D", 24, 28);

        g.setFont(new Font("Consolas", Font.BOLD, 14));
        g.setColor(new Color(120, 215, 255));
        g.drawString("Aktif Sınıf: " + profile.selectedHero.displayName + " (Sv." + profile.heroLevels[profile.selectedHero.ordinal()] + ")", 280, 28);

        g.setColor(new Color(255, 215, 0));
        g.drawString("ALTIN: " + profile.gold, 620, 28);

        g.setColor(new Color(255, 165, 50));
        g.drawString("ZAFER SANDIĞI: " + profile.victoryChests, 775, 28);

        // 6 Navigation Tabs
        GameState.MenuTab[] tabs = GameState.MenuTab.values();
        int tabW = 142;
        int startX = 40;
        for (int i = 0; i < tabs.length; i++) {
            int tx = startX + i * (tabW + 6);
            boolean active = (currentTab == tabs[i]);
            g.setColor(active ? new Color(60, 110, 205) : new Color(30, 35, 52));
            g.fillRoundRect(tx, 52, tabW, 38, 10, 10);
            g.setColor(active ? new Color(140, 210, 255) : new Color(75, 85, 115));
            g.drawRoundRect(tx, 52, tabW, 38, 10, 10);

            g.setFont(new Font("Consolas", Font.BOLD, 13));
            g.setColor(active ? Color.WHITE : new Color(180, 190, 210));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(tabs[i].title, tx + (tabW - fm.stringWidth(tabs[i].title)) / 2, 76);
        }

        switch (currentTab) {
            case CAMPAIGN -> renderCampaignTab(g);
            case HEROES -> renderHeroesTab(g);
            case EQUIPMENT -> renderEquipmentTab(g);
            case SKILLS -> renderSkillsTab(g);
            case SHOP -> renderShopTab(g);
            case SETTINGS -> renderSettingsTab(g);
        }
    }

    private void renderCampaignTab(Graphics2D g) {
        // Left Map Panel (Candy Crush Linear Node Path)
        g.setColor(new Color(18, 22, 34, 220));
        g.fillRoundRect(40, 105, 545, 480, 14, 14);
        g.setColor(new Color(65, 80, 120));
        g.drawRoundRect(40, 105, 545, 480, 14, 14);

        // Draw connecting path lines between stage nodes
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(4f));
        for (int i = 0; i < ProfileData.MAX_STAGES - 1; i++) {
            Point p1 = stageNodes[i];
            Point p2 = stageNodes[i + 1];
            if (i + 1 < profile.unlockedStage) {
                g.setColor(new Color(50, 205, 120));
            } else {
                g.setColor(new Color(60, 68, 90));
            }
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
        g.setStroke(oldStroke);

        // Draw each Stage Node (1..15)
        for (int i = 0; i < ProfileData.MAX_STAGES; i++) {
            int stageNum = i + 1;
            Point p = stageNodes[i];
            boolean completed = stageNum < profile.unlockedStage;
            boolean unlocked = stageNum <= profile.unlockedStage;
            boolean selected = stageNum == profile.selectedStage;

            if (selected) {
                g.setColor(new Color(255, 230, 80, 95));
                g.fillOval(p.x - 28, p.y - 28, 56, 56);
            }

            if (completed) {
                g.setColor(new Color(40, 185, 105));
            } else if (unlocked) {
                g.setColor(new Color(245, 175, 40));
            } else {
                g.setColor(new Color(45, 50, 68));
            }
            g.fillOval(p.x - 20, p.y - 20, 40, 40);

            g.setColor(selected ? Color.WHITE : new Color(160, 175, 205));
            g.drawOval(p.x - 20, p.y - 20, 40, 40);

            g.setFont(new Font("Consolas", Font.BOLD, 14));
            g.setColor(unlocked ? Color.WHITE : new Color(120, 125, 145));
            String label = String.valueOf(stageNum);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(label, p.x - fm.stringWidth(label) / 2, p.y + 5);
        }

        // Right Stage Briefing & Launch Panel
        g.setColor(new Color(22, 26, 42, 235));
        g.fillRoundRect(605, 105, 315, 480, 14, 14);
        g.setColor(new Color(80, 100, 150));
        g.drawRoundRect(605, 105, 315, 480, 14, 14);

        int st = profile.selectedStage;
        g.setFont(new Font("Consolas", Font.BOLD, 20));
        g.setColor(new Color(255, 215, 70));
        g.drawString("BÖLÜM " + st + " — ZİNDAN SEFERİ", 625, 145);

        g.setFont(new Font("Consolas", Font.PLAIN, 13));
        g.setColor(new Color(190, 205, 230));
        int roomEst = Math.min(15, 6 + st / 2);
        g.drawString("• Harita: Prosedürel (" + roomEst + " Oda)", 625, 185);
        g.drawString("• Görev: Tüm Düşmanları Temizle", 625, 210);
        g.drawString("• Ödül: 1x Zafer Sandığı + Altın", 625, 235);

        g.setColor(new Color(255, 140, 90));
        g.setFont(new Font("Consolas", Font.BOLD, 14));
        g.drawString("Bölümdeki Düşman & Tuzaklar:", 625, 275);

        g.setFont(new Font("Consolas", Font.PLAIN, 12));
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("- Yakın Dövüşçü İskelet/Muhafız", 630, 300);
        g.drawString("- Sürü [Hive Mind] (Bölüm 1+)", 630, 322);
        g.drawString(st >= 2 ? "- Menzilli Kara Büyücü (Aktif)" : "- Menzilli Büyücü (Bölüm 2'de açılır)", 630, 344);
        g.drawString(st >= 3 ? "- Kristal Taret Kulesi (Aktif)" : "- Kristal Taret (Bölüm 3'te açılır)", 630, 366);
        g.drawString("- Diken & Asit Tuzakları", 630, 388);

        // Selected Hero summary box
        ProfileData.ComputedStats cs = profile.computeStats(profile.selectedHero);
        g.setColor(new Color(32, 38, 58));
        g.fillRoundRect(625, 405, 275, 72, 8, 8);
        g.setColor(profile.selectedHero.themeColor);
        g.setFont(new Font("Consolas", Font.BOLD, 13));
        g.drawString("Kahraman: " + profile.selectedHero.displayName + " (Sv." + profile.heroLevels[profile.selectedHero.ordinal()] + ")", 635, 427);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 12));
        g.drawString("Can: " + cs.maxHp + " | Hasar: " + cs.damage + " | Zırh: " + cs.armor, 635, 448);
        g.drawString("Hız: " + String.format("%.1f", cs.speed) + " | Kritik: %" + (int) (cs.critChance * 100), 635, 466);

        // Launch Button
        g.setColor(new Color(45, 185, 95));
        g.fillRoundRect(630, 495, 270, 54, 12, 12);
        g.setColor(new Color(160, 255, 190));
        g.drawRoundRect(630, 495, 270, 54, 12, 12);
        g.setFont(new Font("Consolas", Font.BOLD, 17));
        g.setColor(Color.WHITE);
        g.drawString("⚔ BÖLÜM " + st + " BAŞLAT", 675, 528);
    }

    private void renderHeroesTab(Graphics2D g) {
        HeroClass[] heroes = HeroClass.values();
        int cardW = 166;
        int startX = 40;

        for (int i = 0; i < heroes.length; i++) {
            HeroClass hc = heroes[i];
            int cx = startX + i * (cardW + 12);
            int cy = 115;
            boolean isSelected = (profile.selectedHero == hc);

            g.setColor(isSelected ? new Color(34, 44, 72) : new Color(22, 26, 40));
            g.fillRoundRect(cx, cy, cardW, 465, 12, 12);
            g.setColor(isSelected ? hc.themeColor : new Color(65, 75, 105));
            g.drawRoundRect(cx, cy, cardW, 465, 12, 12);

            // Hero Icon Preview
            g.setColor(hc.themeColor);
            g.fillRoundRect(cx + cardW / 2 - 22, cy + 18, 44, 44, 10, 10);
            g.setColor(Color.WHITE);
            g.drawRoundRect(cx + cardW / 2 - 22, cy + 18, 44, 44, 10, 10);

            g.setFont(new Font("Consolas", Font.BOLD, 15));
            g.setColor(hc.themeColor);
            g.drawString(hc.displayName, cx + 14, cy + 84);

            g.setFont(new Font("Consolas", Font.BOLD, 12));
            g.setColor(Color.WHITE);
            g.drawString("Seviye: " + profile.heroLevels[i], cx + 14, cy + 104);

            ProfileData.ComputedStats st = profile.computeStats(hc);
            g.setFont(new Font("Consolas", Font.PLAIN, 12));
            g.setColor(new Color(200, 210, 230));
            g.drawString("Can:     " + st.maxHp, cx + 14, cy + 132);
            g.drawString("Hasar:   " + st.damage, cx + 14, cy + 150);
            g.drawString("Zırh:    " + st.armor, cx + 14, cy + 168);
            g.drawString("Hız:     " + String.format("%.1f", st.speed), cx + 14, cy + 186);
            g.drawString("Kritik:  %" + (int) (st.critChance * 100), cx + 14, cy + 204);
            g.drawString("Silah:   " + hc.weaponType.name(), cx + 14, cy + 224);

            g.setColor(new Color(255, 215, 90));
            g.drawString("Özel: " + hc.skillName, cx + 14, cy + 252);

            // Card progress bar
            int haveCards = profile.heroCards[i];
            int needCards = profile.getHeroUpgradeCardCost(hc);
            int goldCost = profile.getHeroUpgradeGoldCost(hc);

            g.setColor(new Color(25, 30, 45));
            g.fillRoundRect(cx + 14, cy + 315, cardW - 28, 18, 6, 6);
            int fillW = (int) (Math.min(1.0, haveCards / (double) needCards) * (cardW - 28));
            g.setColor(new Color(65, 195, 255));
            g.fillRoundRect(cx + 14, cy + 315, fillW, 18, 6, 6);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Consolas", Font.BOLD, 11));
            g.drawString("Kart: " + haveCards + " / " + needCards, cx + 32, cy + 328);

            // Select button
            g.setColor(isSelected ? new Color(45, 165, 90) : new Color(55, 70, 105));
            g.fillRoundRect(cx + 12, cy + 365, cardW - 24, 34, 8, 8);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Consolas", Font.BOLD, 12));
            g.drawString(isSelected ? "✓ SEÇİLDİ" : "KARAKTERİ SEÇ", cx + 30, cy + 386);

            // Upgrade button
            boolean canUpg = profile.canUpgradeHero(hc);
            g.setColor(canUpg ? new Color(230, 155, 30) : new Color(50, 52, 65));
            g.fillRoundRect(cx + 12, cy + 410, cardW - 24, 38, 8, 8);
            g.setColor(canUpg ? Color.WHITE : Color.GRAY);
            g.drawString("YÜKSELT (" + goldCost + "G)", cx + 22, cy + 433);
        }
    }

    private void renderEquipmentTab(Graphics2D g) {
        // Top Bar: 5 Currently Equipped Slots (Helmet, Chestplate, Leggings, Boots, Hero Weapon)
        g.setColor(new Color(20, 25, 40, 230));
        g.fillRoundRect(40, 105, 595, 105, 12, 12);
        g.setColor(new Color(75, 90, 135));
        g.drawRoundRect(40, 105, 595, 105, 12, 12);

        String[] slotLabels = { "Kask", "Göğüslük", "Pantolon", "Çizme", profile.selectedHero.displayName + " Silahı" };
        int[] equippedIds = {
            profile.equippedArmor[0],
            profile.equippedArmor[1],
            profile.equippedArmor[2],
            profile.equippedArmor[3],
            profile.equippedWeaponByHero[profile.selectedHero.ordinal()]
        };

        for (int i = 0; i < 5; i++) {
            int sx = 55 + i * 114;
            int sy = 118;
            EquipmentItem eq = EquipmentItem.CATALOG[equippedIds[i]];
            g.setColor(new Color(32, 38, 58));
            g.fillRoundRect(sx, sy, 104, 78, 8, 8);
            g.setColor(eq.rarity.color);
            g.drawRoundRect(sx, sy, 104, 78, 8, 8);

            g.setFont(new Font("Consolas", Font.BOLD, 11));
            g.setColor(new Color(180, 195, 220));
            g.drawString(slotLabels[i], sx + 6, sy + 16);

            g.setColor(eq.rarity.color);
            String shortName = eq.name.length() > 13 ? eq.name.substring(0, 13) : eq.name;
            g.drawString(shortName, sx + 6, sy + 42);

            g.setColor(Color.WHITE);
            g.drawString("Sv." + profile.itemLevels[eq.id], sx + 6, sy + 64);
        }

        // Equipment Catalog Grid (27 cards: 3 rows x 9 cols)
        int cols = 9;
        int cellW = 62;
        int cellH = 62;
        int gridX = 40;
        int gridY = 230;

        for (int id = 0; id < EquipmentItem.CATALOG.length; id++) {
            EquipmentItem item = EquipmentItem.CATALOG[id];
            int r = id / cols;
            int c = id % cols;
            int cx = gridX + c * (cellW + 6);
            int cy = gridY + r * (cellH + 10);

            boolean unlocked = profile.itemLevels[id] > 0;
            boolean equipped = profile.isEquipped(id);
            boolean selected = (selectedEquipmentId == id);

            g.setColor(unlocked ? new Color(30, 36, 56) : new Color(18, 20, 28));
            g.fillRoundRect(cx, cy, cellW, cellH, 8, 8);
            g.setColor(selected ? Color.WHITE : item.rarity.color);
            g.drawRoundRect(cx, cy, cellW, cellH, 8, 8);

            g.setFont(new Font("Consolas", Font.BOLD, 10));
            g.setColor(unlocked ? item.rarity.color : Color.GRAY);
            String tag = switch (item.slot) {
                case HELMET -> "KASK";
                case CHESTPLATE -> "ZIRH";
                case LEGGINGS -> "PANT";
                case BOOTS -> "ÇİZME";
                case WEAPON -> item.weaponType.name().substring(0, Math.min(4, item.weaponType.name().length()));
            };
            g.drawString(tag, cx + 6, cy + 16);

            g.setColor(unlocked ? Color.WHITE : Color.DARK_GRAY);
            g.drawString(unlocked ? "Sv." + profile.itemLevels[id] : "KİLİTLİ", cx + 6, cy + 36);

            if (equipped) {
                g.setColor(new Color(50, 235, 120));
                g.drawString("[TAKILI]", cx + 6, cy + 54);
            } else {
                g.setColor(new Color(130, 200, 255));
                g.drawString("Kart:" + profile.itemCards[id], cx + 6, cy + 54);
            }
        }

        // Right Detail Panel for Selected Equipment Card
        g.setColor(new Color(22, 26, 42, 235));
        g.fillRoundRect(650, 105, 270, 465, 14, 14);
        EquipmentItem sel = EquipmentItem.CATALOG[selectedEquipmentId];
        g.setColor(sel.rarity.color);
        g.drawRoundRect(650, 105, 270, 465, 14, 14);

        int ilvl = Math.max(1, profile.itemLevels[selectedEquipmentId]);
        g.setFont(new Font("Consolas", Font.BOLD, 16));
        g.setColor(sel.rarity.color);
        g.drawString(sel.name, 668, 140);

        g.setFont(new Font("Consolas", Font.BOLD, 12));
        g.setColor(Color.LIGHT_GRAY);
        g.drawString(sel.rarity.displayName + " — " + sel.slot.displayName, 668, 164);
        if (sel.slot == EquipmentItem.SlotType.WEAPON) {
            g.setColor(new Color(255, 205, 80));
            g.drawString("Sınıf: " + sel.weaponType.displayName, 668, 184);
        } else {
            g.setColor(new Color(140, 225, 160));
            g.drawString("Tüm Karakterler Kuşanabilir", 668, 184);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 13));
        g.drawString("Durum: " + (profile.itemLevels[selectedEquipmentId] > 0 ? "Seviye " + ilvl : "Kilitli"), 668, 216);
        g.drawString("+Can Bonusu:    +" + sel.getHpAtLevel(ilvl), 668, 245);
        g.drawString("+Hasar Bonusu:  +" + sel.getDamageAtLevel(ilvl), 668, 268);
        g.drawString("+Zırh Bonusu:   +" + sel.getArmorAtLevel(ilvl), 668, 291);
        g.drawString("+Hız Bonusu:    +" + String.format("%.2f", sel.getSpeedAtLevel(ilvl)), 668, 314);
        g.drawString("+Kritik Şansı:  +%" + (int) Math.round(sel.getCritAtLevel(ilvl) * 100), 668, 337);

        int haveC = profile.itemCards[selectedEquipmentId];
        int needC = profile.getItemUpgradeCardCost(selectedEquipmentId);
        int goldC = profile.getItemUpgradeGoldCost(selectedEquipmentId);
        g.setColor(new Color(110, 215, 255));
        g.drawString("Eldeki Kart: " + haveC + " / " + needC, 668, 385);
        g.setColor(new Color(255, 215, 60));
        g.drawString("Yükseltme Bedeli: " + goldC + " Altın", 668, 408);

        // Equip Button
        g.setColor(profile.itemLevels[selectedEquipmentId] > 0 ? new Color(50, 145, 225) : new Color(50, 55, 70));
        g.fillRoundRect(665, 435, 240, 40, 8, 8);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 13));
        g.drawString("EKİPMANI KUŞAN", 725, 460);

        // Upgrade Button
        boolean canUpg = profile.canUpgradeItem(selectedEquipmentId);
        g.setColor(canUpg ? new Color(230, 155, 30) : new Color(50, 55, 70));
        g.fillRoundRect(665, 488, 240, 42, 8, 8);
        g.setColor(canUpg ? Color.WHITE : Color.GRAY);
        g.drawString("KARTLA YÜKSELT", 725, 514);
    }

    private void renderSkillsTab(Graphics2D g) {
        HeroClass[] heroes = HeroClass.values();
        for (int i = 0; i < heroes.length; i++) {
            HeroClass hc = heroes[i];
            int rowY = 115 + i * 92;
            int skLvl = profile.skillLevels[i];
            int cost = profile.getSkillUpgradeGoldCost(hc);

            g.setColor(new Color(22, 27, 44, 230));
            g.fillRoundRect(40, rowY, 880, 80, 12, 12);
            g.setColor(hc.themeColor);
            g.drawRoundRect(40, rowY, 880, 80, 12, 12);

            g.setFont(new Font("Consolas", Font.BOLD, 15));
            g.setColor(hc.themeColor);
            g.drawString(hc.displayName + " — " + hc.skillName + " (Seviye " + skLvl + ")", 60, rowY + 28);

            g.setFont(new Font("Consolas", Font.PLAIN, 12));
            g.setColor(new Color(205, 215, 235));
            g.drawString(hc.skillDescription, 60, rowY + 50);

            g.setColor(new Color(130, 235, 170));
            int multPct = 100 + (skLvl - 1) * 20;
            g.drawString("Yetenek Gücü: %" + multPct + " | Kullanım: [Sağ Tık] veya [Q]", 60, rowY + 69);

            boolean canAfford = profile.gold >= cost;
            g.setColor(canAfford ? new Color(50, 175, 100) : new Color(50, 55, 70));
            g.fillRoundRect(710, rowY + 18, 195, 44, 10, 10);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Consolas", Font.BOLD, 13));
            g.drawString("GELİŞTİR (" + cost + "G)", 732, rowY + 45);
        }
    }

    private void renderShopTab(Graphics2D g) {
        // Left Panel: Victory Chest Opener
        g.setColor(new Color(24, 28, 46, 235));
        g.fillRoundRect(40, 115, 380, 450, 14, 14);
        g.setColor(new Color(255, 195, 50));
        g.drawRoundRect(40, 115, 380, 450, 14, 14);

        g.setFont(new Font("Consolas", Font.BOLD, 18));
        g.setColor(new Color(255, 215, 65));
        g.drawString("📦 ZAFER SANDIĞI MERKEZİ", 95, 155);

        // Draw Big Treasure Chest graphic
        g.setColor(new Color(215, 145, 35));
        g.fillRoundRect(160, 200, 140, 100, 16, 16);
        g.setColor(new Color(95, 60, 20));
        g.drawRoundRect(160, 200, 140, 100, 16, 16);
        g.setColor(new Color(255, 235, 90));
        g.fillRect(220, 235, 20, 28);

        g.setFont(new Font("Consolas", Font.BOLD, 15));
        g.setColor(Color.WHITE);
        g.drawString("Mevcut Sandık: " + profile.victoryChests + " Adet", 135, 338);
        g.setFont(new Font("Consolas", Font.PLAIN, 12));
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("Her sandık Altın + 3 Kart (Karakter & Ekipman) içerir!", 58, 365);

        // Open Chest Button
        g.setColor(profile.victoryChests > 0 ? new Color(235, 155, 30) : new Color(55, 60, 75));
        g.fillRoundRect(85, 390, 290, 52, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 15));
        g.drawString("SANDIĞI AÇ (" + profile.victoryChests + ")", 165, 422);

        // Buy Chest with Gold Button
        g.setColor(profile.gold >= 180 ? new Color(55, 135, 220) : new Color(45, 50, 65));
        g.fillRoundRect(85, 458, 290, 44, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 13));
        g.drawString("ALTINLA SANDIK AL (180 Altın)", 115, 485);

        // Right Panel: Card Shop
        g.setColor(new Color(24, 28, 46, 235));
        g.fillRoundRect(455, 115, 465, 450, 14, 14);
        g.setColor(new Color(90, 140, 220));
        g.drawRoundRect(455, 115, 465, 450, 14, 14);

        g.setFont(new Font("Consolas", Font.BOLD, 18));
        g.setColor(new Color(120, 215, 255));
        g.drawString("🏪 KART VE EKİPMAN MARKETİ", 545, 155);

        drawShopOffer(g, 520, 215, "3x " + profile.selectedHero.displayName + " Karakter Kartı",
                "Seçili sınıfınızı hızla yükseltmek için kart paketi", "90 ALTIN", profile.selectedHero.themeColor);

        drawShopOffer(g, 520, 315, "2x Nadir / Destansı Ekipman Kartı",
                "Rastgele Mavi veya Mor kask, zırh veya silah kartı", "120 ALTIN", new Color(175, 85, 255));

        drawShopOffer(g, 520, 415, "2x EFSANEVİ Ekipman Kartı Paketi",
                "Garantili Altın (Efsanevi) enderlikte ekipman/silah kartı!", "300 ALTIN", new Color(255, 195, 35));
    }

    private void drawShopOffer(Graphics2D g, int x, int y, String title, String desc, String price, Color borderColor) {
        g.setColor(new Color(32, 38, 60));
        g.fillRoundRect(x, y, 360, 75, 10, 10);
        g.setColor(borderColor);
        g.drawRoundRect(x, y, 360, 75, 10, 10);

        g.setFont(new Font("Consolas", Font.BOLD, 14));
        g.setColor(borderColor);
        g.drawString(title, x + 14, y + 25);

        g.setFont(new Font("Consolas", Font.PLAIN, 11));
        g.setColor(Color.LIGHT_GRAY);
        g.drawString(desc, x + 14, y + 46);

        g.setFont(new Font("Consolas", Font.BOLD, 13));
        g.setColor(new Color(255, 215, 50));
        g.drawString("SATIN AL: " + price, x + 14, y + 66);
    }

    private void renderSettingsTab(Graphics2D g) {
        // Left Panel: Display Modes
        g.setColor(new Color(22, 26, 44, 235));
        g.fillRoundRect(40, 115, 420, 460, 14, 14);
        g.setColor(new Color(90, 145, 235));
        g.drawRoundRect(40, 115, 420, 460, 14, 14);

        g.setFont(new Font("Consolas", Font.BOLD, 18));
        g.setColor(new Color(120, 215, 255));
        g.drawString("🖥 EKRAN MODU SEÇENEKLERİ", 75, 155);

        GameState.DisplayMode[] modes = GameState.DisplayMode.values();
        for (int i = 0; i < modes.length; i++) {
            boolean active = (profile.displayMode == modes[i]);
            int by = 185 + i * 70;
            g.setColor(active ? new Color(45, 135, 215) : new Color(32, 38, 58));
            g.fillRoundRect(70, by, 360, 52, 10, 10);
            g.setColor(active ? new Color(160, 230, 255) : new Color(75, 85, 115));
            g.drawRoundRect(70, by, 360, 52, 10, 10);

            g.setFont(new Font("Consolas", Font.BOLD, 14));
            g.setColor(active ? Color.WHITE : new Color(195, 205, 225));
            g.drawString((active ? "✓ " : "  ") + modes[i].displayName, 88, by + 31);
        }

        // Quick Debug Mode Toggle button
        g.setColor(input.debugToggle ? new Color(45, 165, 95) : new Color(45, 50, 70));
        g.fillRoundRect(70, 435, 360, 46, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 13));
        g.drawString("DEBUG GÖRÜNÜMÜ [F3]: " + (input.debugToggle ? "AÇIK" : "KAPALI"), 95, 463);

        g.setFont(new Font("Consolas", Font.PLAIN, 12));
        g.setColor(new Color(255, 215, 80));
        g.drawString("İpucu: Oyun içinde [F11] tuşuyla ekran modunu", 75, 515);
        g.drawString("anında değiştirebilirsiniz.", 75, 535);

        // Right Panel: Windowed Resolution Presets
        g.setColor(new Color(22, 26, 44, 235));
        g.fillRoundRect(480, 115, 440, 460, 14, 14);
        g.setColor(new Color(255, 195, 60));
        g.drawRoundRect(480, 115, 440, 460, 14, 14);

        g.setFont(new Font("Consolas", Font.BOLD, 18));
        g.setColor(new Color(255, 215, 70));
        g.drawString("📐 PENCERE ÇÖZÜNÜRLÜĞÜ", 525, 155);

        for (int i = 0; i < ProfileData.RES_PRESETS.length; i++) {
            boolean active = (profile.displayMode == GameState.DisplayMode.WINDOWED && profile.resolutionIndex == i);
            int by = 185 + i * 62;
            g.setColor(active ? new Color(45, 175, 100) : new Color(32, 38, 58));
            g.fillRoundRect(515, by, 370, 48, 10, 10);
            g.setColor(active ? new Color(160, 255, 190) : new Color(75, 85, 115));
            g.drawRoundRect(515, by, 370, 48, 10, 10);

            g.setFont(new Font("Consolas", Font.BOLD, 14));
            g.setColor(active ? Color.WHITE : new Color(205, 215, 230));
            g.drawString((active ? "✓ " : "  ") + ProfileData.RES_LABELS[i], 535, by + 29);
        }

        g.setFont(new Font("Consolas", Font.PLAIN, 12));
        g.setColor(new Color(175, 195, 225));
        g.drawString("Not: Tüm çözünürlüklerde görüntü oranı otomatik", 515, 515);
        g.drawString("korunur ve fare koordinatları ölçeklenir.", 515, 535);
    }

    private void renderChestOpeningModal(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 210));
        g.fillRect(0, 0, Engine.WIDTH, Engine.HEIGHT);

        g.setFont(new Font("Consolas", Font.BOLD, 24));
        g.setColor(new Color(255, 215, 60));
        g.drawString("✨ ZAFER SANDIĞI ÖDÜLLERİ ✨", Engine.WIDTH / 2 - 195, 115);

        if (lastChestResult != null) {
            g.setFont(new Font("Consolas", Font.BOLD, 17));
            g.setColor(new Color(120, 255, 150));
            g.drawString("+" + lastChestResult.goldReward + " Altın Kazanıldı!", Engine.WIDTH / 2 - 105, 155);

            int cardW = 220;
            int cardH = 270;
            int startX = (Engine.WIDTH - (3 * cardW + 40)) / 2;

            for (int i = 0; i < lastChestResult.cards.size(); i++) {
                ProfileData.CardReward cr = lastChestResult.cards.get(i);
                int cx = startX + i * (cardW + 20);
                int cy = 195;

                g.setColor(new Color(26, 30, 48));
                g.fillRoundRect(cx, cy, cardW, cardH, 14, 14);
                g.setColor(cr.rarityColor);
                g.drawRoundRect(cx, cy, cardW, cardH, 14, 14);

                g.fillRoundRect(cx + cardW / 2 - 28, cy + 35, 56, 56, 12, 12);

                g.setFont(new Font("Consolas", Font.BOLD, 16));
                g.setColor(cr.rarityColor);
                FontMetrics fm = g.getFontMetrics();
                g.drawString(cr.title, cx + (cardW - fm.stringWidth(cr.title)) / 2, cy + 135);

                g.setFont(new Font("Consolas", Font.PLAIN, 12));
                g.setColor(Color.WHITE);
                fm = g.getFontMetrics();
                g.drawString(cr.subtitle, cx + (cardW - fm.stringWidth(cr.subtitle)) / 2, cy + 170);

                g.setFont(new Font("Consolas", Font.BOLD, 20));
                g.setColor(new Color(255, 225, 80));
                String qty = "x" + cr.count + " KART";
                fm = g.getFontMetrics();
                g.drawString(qty, cx + (cardW - fm.stringWidth(qty)) / 2, cy + 225);
            }
        }

        g.setColor(new Color(50, 175, 100));
        g.fillRoundRect(Engine.WIDTH / 2 - 110, 520, 220, 46, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 16));
        g.drawString("HARİKA! (DEVAM ET)", Engine.WIDTH / 2 - 82, 548);
    }

    private void renderStageSummaryModal(Graphics2D g, CombatTracker tracker, boolean victory) {
        g.setColor(new Color(8, 10, 18, 215));
        g.fillRect(0, 0, Engine.WIDTH, Engine.HEIGHT);

        int boxW = 540;
        int boxH = 460;
        int bx = (Engine.WIDTH - boxW) / 2;
        int by = 95;

        g.setColor(new Color(20, 25, 42));
        g.fillRoundRect(bx, by, boxW, boxH, 16, 16);
        g.setColor(victory ? new Color(60, 235, 130) : new Color(235, 65, 65));
        g.drawRoundRect(bx, by, boxW, boxH, 16, 16);

        g.setFont(new Font("Consolas", Font.BOLD, 24));
        String header = victory
                ? "🏆 GÖREV BAŞARILI! (BÖLÜM " + profile.selectedStage + ")"
                : "💀 GÖREV BAŞARISIZ (BÖLÜM " + profile.selectedStage + ")";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(header, bx + (boxW - fm.stringWidth(header)) / 2, by + 48);

        g.setFont(new Font("Consolas", Font.PLAIN, 15));
        g.setColor(Color.WHITE);
        int sy = by + 100;
        g.drawString("⚔ Verilen Toplam Hasar : " + tracker.totalDamageDealt, bx + 65, sy);
        g.drawString("🛡 Alınan Toplam Hasar  : " + tracker.totalDamageTaken, bx + 65, sy + 34);
        g.drawString("💀 Öldürülen Düşman     : " + tracker.enemiesKilled, bx + 65, sy + 68);
        g.drawString("💥 Kritik Vuruş Sayısı  : " + tracker.criticalHits, bx + 65, sy + 102);
        g.drawString("⏱ Geçen Süre           : " + tracker.getFormattedTime(), bx + 65, sy + 136);
        g.setColor(new Color(255, 215, 60));
        g.drawString("💰 Kazanılan Altın      : +" + tracker.goldEarnedInStage + " Altın", bx + 65, sy + 172);

        if (victory) {
            g.setColor(new Color(255, 185, 50));
            g.setFont(new Font("Consolas", Font.BOLD, 16));
            g.drawString("🎁 BÖLÜM ÖDÜLÜ: +1 ZAFER SANDIĞI KAZANILDI!", bx + 65, sy + 212);
        }

        // Bottom Action Buttons
        g.setColor(new Color(60, 120, 210));
        g.fillRoundRect(Engine.WIDTH / 2 - 230, 495, 210, 48, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 14));
        g.drawString(victory ? "SANDIK AÇ & LOBİ" : "ANA MENÜYE DÖN", Engine.WIDTH / 2 - 195, 524);

        g.setColor(new Color(45, 180, 95));
        g.fillRoundRect(Engine.WIDTH / 2 + 20, 495, 210, 48, 10, 10);
        g.setColor(Color.WHITE);
        g.drawString(victory ? "SONRAKİ BÖLÜM ▶" : "TEKRAR DENE ↻", Engine.WIDTH / 2 + 60, 524);
    }
}

