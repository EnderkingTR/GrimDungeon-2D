# ⚔️ GrimDungeon 2D (v2.0)

> A modular 2D Action-RPG & Roguelite Game Engine featuring a linear campaign map, 5 distinct hero classes, card/equipment rarity progression, and Hive-Mind swarm AI — built from scratch in pure Java (AWT & Swing) with zero external libraries.

📄 **[Read the Full v2.0 Changelog (CHANGELOG.md)](CHANGELOG.md)**

---

## 🎮 Gameplay & Controls

| Key / Input | Action |
| :--- | :--- |
| **W, A, S, D** / Arrow Keys | Move Character (Smooth LERP Camera Viewport) |
| **Left Click** / **Space** | Primary Class Weapon Attack (Melee Arc or Ranged Projectile toward Mouse) |
| **Right Click** / **Q** / **Shift** | Activate Class Signature Skill (Shield Bash, Shadow Dash, Battle Cry, Multishot, Frost Nova) |
| **E** | Interact: Open Dungeon Treasure Chests (+Gold & +25 Health) |
| **ESC** | Return to 5-Tab Main Menu / Lobby |
| **F3** | Toggle Debug Overlay (Metrics, Individual A* & Hive-Mind Neural Paths, Full Map Visibility) |
| **F5 / F9** | Quick Save / Quick Load Persistent Profile (`savegame.dat`) |
| **R** | Restart Current Stage |

---

## 🌟 Key v2.0 Features & Architecture

1. **5-Tab Main Menu & Linear Campaign Progression (`MenuSystem.java`, `GameState.java`):**
   - **Sefer Haritası (Campaign Map):** Candy Crush-style 15-stage winding progression path; clearing all enemies in a stage unlocks the next node and scales procedural dungeon complexity.
   - **Karakterler, Ekipmanlar, Yetenekler, Market & Sandık:** Complete meta-progression lobby with Victory Chest opening (3-card draw) and Gold shop.

2. **5 Playable Hero Classes & Signature Skills (`HeroClass.java`, `Player.java`):**
   - **Savaşçı (Warrior):** Balanced melee fighter (*Sword* + *Kalkan Darbesi* AoE stun).
   - **Suikastçı (Assassin):** Ultra-fast high-crit melee class (*Daggers* + *Gölge Adımı* invulnerable dash).
   - **Viking (Berserker):** High-HP bruiser (*Battleaxe* + *Savaş Narası* ground slam & heal).
   - **Okçu (Archer):** Agile ranged marksman (*Bow* + *Yaylım Ateşi* 5-arrow piercing volley).
   - **Büyücü (Mage):** High-damage arcane caster (*Staff* + *Buz Novası* 8-way freezing burst).

3. **Card Collection, Equipment Loadout & Rarity Tiers (`EquipmentItem.java`, `ProfileData.java`):**
   - **4 Universal Armor Slots:** Helmet, Chestplate, Leggings, and Boots usable by all classes.
   - **5 Class-Specific Weapons:** Sword, Dagger, Battleaxe, Bow, and Staff.
   - **4 Rarity Tiers:** *Common*, *Rare*, *Epic*, and *Legendary* cards that can be collected and upgraded.

4. **Hive-Mind Swarm AI, Diverse Bestiary & Traps:**
   - **Collective Swarm Intelligence (`SwarmHiveMind.java`, `SwarmEnemy.java`):** Computes a single shared A* path from the swarm's centroid and combines it with local **Boids separation & cohesion** vectors for high-performance flocking.
   - **Enemy & Hazard Variety:** Melee Guards (`Enemy`), Kiting Ranged Cultists (`RangedEnemy`), Stationary Arcane Turrets (`TurretEnemy`), Spike Traps (`SpikeTrap`), Slowing Acid Pools (`AcidTrap`), and Pressure Dart Traps (`DartTrap`).

5. **Combat Telemetry & Stage Clear Summary (`CombatTracker.java`, `FloatingText.java`):**
   - Floating combat damage & critical hit numbers.
   - Automatic stage completion screen displaying total damage dealt/taken, enemies killed, critical hits, clear time, and Victory Chest rewards.

---

## 🚀 Getting Started

All you need is **Java (JDK 17 or later)** installed on your machine.

### Windows (Quick Launch):
Double-click `run.bat` in the root folder to compile and run the engine instantly.

### From Terminal:
```bash
# Compile all source files
javac -encoding UTF-8 -d bin *.java

# Launch the game engine
java -cp bin Main
```
