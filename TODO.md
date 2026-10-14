# ⚔️ GrimDungeon 2D - Master Development To-Do List
> **Target:** Commercial-grade Action Roguelite (inspired by *Archero, Soul Knight, Hades*) built with pure Java Procedural Low-Poly graphics.

---

## 📌 Phase 1: Game State Machine & Stage Progression (Step A)
*Goal: Transform the single endless dungeon into an episodic, stage-based roguelite loop with menus and perk drafting.*

- [ ] **1.1. Core State Architecture (`GameState.java`)**
  - [ ] Implement `GameState` enum: `MAIN_MENU`, `CLASS_SELECT`, `UPGRADE_SHOP`, `IN_GAME`, `PERK_SELECT`, `BOSS_FIGHT`, `GAME_OVER`, `VICTORY`.
  - [ ] Refactor `Engine.java` to delegate update and render logic based on the active `GameState`.

- [ ] **1.2. Main Menu & Meta-Progression UI**
  - [ ] Title banner & navigation buttons: `Start Run`, `Classes`, `Upgrades`, `Settings`.
  - [ ] **Class Selection Screen:** Toggle between Warrior, Mage, and Ranger with preview models and stat breakdowns.
  - [ ] **Sanctuary Upgrade Shop:** Spend persistent gold earned across runs to permanently upgrade:
    - Max HP (+10 per rank)
    - Base Damage (+5% per rank)
    - Movement Speed (+4% per rank)
    - Starting Gold / Critical Chance

- [ ] **1.3. Compact Stage-Based Progression Engine**
  - [ ] Replace the 60x60 mega-map with compact single-room arenas (e.g. 25x20 tiles) designed for focused combat.
  - [ ] Add `StageManager` tracking: Current World (1 to 3) and Current Stage (1 to 15).
  - [ ] Room clearing condition: Monitor alive enemy count in current room.
  - [ ] **Exit Portal Entity:** Spawns at room center when all enemies are defeated; stepping on it triggers transition.

- [ ] **1.4. 3-Card Perk Selection System (Drafting)**
  - [ ] Create `Perk.java` data structure (ID, Name, Description, Rarity, Stat Modifier lambda).
  - [ ] Build initial Perk Pool (~15 perks):
    - *Offense:* Attack Speed (+25%), Critical Chance (+15%), Projectile Bouncing, Multishot (+1 Arrow), Piercing Shots.
    - *Defense:* Shield Barrier, Max HP Boost (+30), Armor (Flat -5 damage taken), Vampirism (Heal 3 HP on kill).
    - *Utility:* Move Speed (+20%), Magnet (Auto-loot coins), Thorn Damage.
  - [ ] Interactive `PERK_SELECT` overlay: 3 distinct cards rendered with mouse hover and click selection.

---

## 🎨 Phase 2: Retro Low-Poly Vector Rendering Engine (Step B)
*Goal: Replace 2D primitive rectangles with flat-shaded 3D-styled polygon geometries and mathematical procedural animations.*

- [ ] **2.1. Procedural Polygon Renderer (`PolyModel.java`)**
  - [ ] Model definition consisting of 2D polygon face arrays, depth ordering, and ambient lighting color palettes.
  - [ ] Matrix rotation and scaling transforms for dynamic character facing and weapon swinging.

- [ ] **2.2. Procedural Animation Controller**
  - [ ] Trigonometric oscillation (`Math.sin(tick * speed)`) for breathing/idle bobbing.
  - [ ] Running gait: Tilting body angle and swinging limbs without external sprite frames.
  - [ ] Attack animations: High-speed arc rotation for swords, staff thrusts, and bow draws.

- [ ] **2.3. Low-Poly Entity Models**
  - [ ] **Warrior:** Angular plate helmet, horned pauldrons, low-poly broadsword & kite shield.
  - [ ] **Mage:** Pointed hood/cowl, glowing floating crystal orb, wooden gnarled staff.
  - [ ] **Ranger:** Agile feathered hood, dual daggers on hip, recurve composite bow.

---

## 🏹 Phase 3: Classes, Weapons & Combat Builds (Step B+)
*Goal: Deliver distinct tactical identities for each selectable class.*

- [ ] **3.1. Class Mechanics & Weapon Archetypes**
  - [ ] **Warrior (Ironclad):**
    - High HP (150 base), heavy armor.
    - Weapon: Heavy Broadsword (180-degree sweep, high knockback).
    - Special: Shield Bash (Stun enemies for 1.2 seconds).
  - [ ] **Mage (Arcanist):**
    - Glass cannon HP (75 base).
    - Weapon: Crystal Staff (Fires piercing homing energy orbs).
    - Special: Frost Nova (Area of Effect freeze around player).
  - [ ] **Ranger (Shadowstalker):**
    - High agility (5.5 speed).
    - Weapon: Wind Bow (Rapid-fire arrows with critical hit multipliers).
    - Special: Dash / Tumble (Invulnerability frames through enemy projectiles).

---

## 🏰 Phase 4: Biomes, Enemies & Boss Hierarchies (Step C)
*Goal: Create thematic progression across 3 worlds with multi-stage boss encounters.*

- [ ] **4.1. Biome Theme System (`BiomeTheme.java`)**
  - [ ] **World 1: The Crypts (Stages 1–5):**
    - Palette: Slate grey, moss green, obsidian.
    - Hazards: Spike traps, poison dart walls.
  - [ ] **World 2: Inferno Pits (Stages 6–10):**
    - Palette: Basalt black, molten lava pools (damage over time), glowing embers.
  - [ ] **World 3: Glacial Sanctum (Stages 11–15):**
    - Palette: Frozen crystal, ice tiles (momentum friction), falling icicles.

- [ ] **4.2. Enemy Variety & Behaviors**
  - [ ] *Skeletons / Grunts:* Fast melee rushers.
  - [ ] *Archers / Cultists:* Ranged kiting enemies that stay at maximum range.
  - [ ] *Ghosts / Phantoms:* Phase through solid walls with teleportation.

- [ ] **4.3. Boss Hierarchy Engine**
  - [ ] **Mini-Bosses (Stages 1–4):** Elite Guardians (Enhanced HP, dual attack patterns).
  - [ ] **World 1 Main Boss (Stage 5 - The Necromancer):**
    - Phase 1: Radial skull projectiles & minion summoning.
    - Phase 2: Teleportation, dark beam raycasting across the room, enraged attack speed.
  - [ ] **World 2 Main Boss (Stage 10 - Magma Colossus):**
    - Ground slam shockwaves & falling meteors.

---

## 💾 Phase 5: Profile Persistence & Meta-Progression
*Goal: Ensure player progress, unlocked classes, and high scores are saved safely on disk.*

- [ ] **5.1. Profile Serialization (`ProfileManager.java`)**
  - [ ] Save/Load JSON or binary `profile.dat`:
    - Persistent Gold & Gems
    - Unlocked Classes & Achievements
    - Upgrade Tiers (HP, Damage, Speed levels)
    - Run Statistics (Highest stage reached, total kills, play time)

---

## 🚀 Phase 6: Steam & Distribution Polish
*Goal: Prepare the project for commercial Steam release.*

- [ ] **6.1. Settings & Audio System**
  - [ ] Master / Music / SFX volume sliders.
  - [ ] Fullscreen toggle and resolution scaling (960x640 upscaled with crisp aspect ratio).
  - [ ] Procedural sound synthesis or lightweight OGG audio player.

- [ ] **6.2. Gamepad & Controller Support**
  - [ ] Dual-stick aiming (Left stick move, Right stick aim/attack).

- [ ] **6.3. Standalone Packaging (.exe)**
  - [ ] Bundle standalone embedded JRE via `jlink` / `jpackage` so players do not need Java installed.
