# 📜 Changelog — GrimDungeon 2D

All notable changes and architectural milestones for **GrimDungeon 2D** are documented in this file.

---

## [v2.0.0] - 2026-10-29 — The Campaign, Heroes & Hive-Mind Update

### 🏆 Campaign & 5-Tab Main Menu System (`MenuSystem.java`, `GameState.java`)
- **State Machine Architecture:** Added `MAIN_MENU`, `PLAYING`, `STAGE_CLEAR`, `GAME_OVER`, and `CHEST_OPENING` states with seamless transitions and `[ESC]` quick-return to lobby.
- **Candy Crush-Style Linear Campaign Map (`SEFER HARİTASI`):**
  - Added a 15-stage winding progression path where clearing a stage automatically unlocks the next stage node.
  - Each stage dynamically scales procedural dungeon room count, enemy composition, and stats.
- **5 Interactive Lobby Tabs:**
  1. **Sefer Haritası (Campaign):** Stage selection, enemy/hazard briefing, and hero stat preview.
  2. **Karakterler (Heroes):** Class selection and card-based hero leveling.
  3. **Ekipmanlar (Equipment):** 5-slot loadout panel + 27-item collection grid with equip & card upgrade actions.
  4. **Yetenekler (Skills):** Active class skill upgrades increasing skill power and reducing cooldowns.
  5. **Market & Sandık (Shop & Chests):** Interactive Victory Chest opener (3-card draw + gold) and direct gold card shop.

### 🦸 5 Playable Hero Classes & Active Skills (`HeroClass.java`, `Player.java`)
- **Savaşçı (Warrior):** Balanced melee fighter with *Sword* sweeps and **Kalkan Darbesi** (AoE shockwave + stun).
- **Suikastçı (Assassin):** High-speed, high-crit melee class with dual *Daggers* and **Gölge Adımı** (invulnerable dash attack).
- **Viking (Berserker):** High-HP heavy bruiser with wide *Battleaxe* cleaves and **Savaş Narası** (360° ground slam + self-heal).
- **Okçu (Archer):** Agile ranged marksman firing high-velocity *Bow* arrows and **Yaylım Ateşi** (5-arrow piercing volley).
- **Büyücü (Mage):** High-damage arcane caster firing *Staff* energy orbs and **Buz Novası** (8-way freezing projectiles).

### 🛡️ Card, Equipment & Rarity System (`EquipmentItem.java`, `ProfileData.java`)
- **4 Universal Armor Slots:** `Kask (Helmet)`, `Göğüslük (Chestplate)`, `Pantolon (Leggings)`, and `Çizme (Boots)` usable across all classes.
- **Class-Specific Weapons:** `Kılıç`, `Hançer`, `Balta`, `Yay`, and `Asa` tailored to each hero class.
- **4 Rarity Tiers:** `Sıradan (Common)`, `Nadir (Rare)`, `Destansı (Epic)`, and `Efsanevi (Legendary)` with scaling stat multipliers.
- **Persistent Binary Profile (`savegame.dat` v2):** Saves unlocked stages, gold, unopened victory chests, hero levels/cards, skill levels, and equipment inventory/loadouts.

### 🧠 Hive-Mind Swarm AI, New Enemies & Traps
- **Collective Swarm Intelligence (`SwarmHiveMind.java`, `SwarmEnemy.java`):**
  - Instead of computing individual A* paths per swarm unit, a shared `SwarmHiveMind` computes a single A* path from the pack's centroid once per tick and combines it with local **Boids separation & cohesion** steering vectors for high-performance organic flocking.
- **Ranged Cultist/Archer (`RangedEnemy.java`):** Kites away when the player gets close and fires dark magic bolts from afar.
- **Arcane Turret (`TurretEnemy.java`):** Stationary crystal obelisk that periodically fires 4-way cardinal/diagonal projectile bursts.
- **New Dungeon Hazards (`AcidTrap.java`, `DartTrap.java`):**
  - *Acid Pool:* Slows movement speed and deals periodic poison damage.
  - *Dart Trap:* Pressure-triggered trap firing high-speed poisoned darts.

### 📊 Stage Clear Telemetry & Floating Combat Text (`CombatTracker.java`, `FloatingText.java`)
- **Automatic Stage Completion:** Defeating the last hostile enemy in a stage automatically triggers the **Görev Başarılı (Stage Clear)** summary screen and awards **+1 Victory Chest**.
- **Detailed Combat Telemetry:** Tracks total damage dealt, damage taken, enemies killed, critical hits, clear time, and gold earned.
- **Floating Combat Text:** Real-time popups for normal damage, critical strikes (`KRİTİK!`), status effects, and gold pickups.

---

## [v1.0.0] - Initial Prototype
- Pure Java (AWT/Swing) 60 UPS game loop with Triple-Buffering (`BufferStrategy`) and smooth LERP camera.
- Procedural room & corridor dungeon generation with AABB collision detection.
- Individual A* pathfinding, FSM (`PATROL`, `CHASE`, `FLEE`), and Bresenham Line-of-Sight Fog of War.
- Basic melee sweep, projectile casting, particle emitter, and minimap HUD.
