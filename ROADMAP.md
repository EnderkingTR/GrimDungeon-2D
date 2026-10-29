# GrimDungeon 2D - Development Roadmap

## Phase 1: Core Architecture - [COMPLETED]
- [x] **Game Loop**: Fixed 60 UPS and Delta Time calculation
- [x] **Rendering**: Triple Buffering and 2D Camera Viewport (LERP smooth following)
- [x] **Input**: Keyboard & Mouse event listener

---

## Phase 2: World, Map & Physics - [COMPLETED]
- [x] **Tilemap System**: 2D matrices for world map layers
- [x] **Procedural Generation**: Stage-scaled dungeon generation with rooms and corridors
- [x] **Collision**: AABB (Axis-Aligned Bounding Box) collision testing

---

## Phase 3: Entity Architecture, Traps & Bestiary - [COMPLETED]
- [x] **Entities**: Modular player, melee enemies, ranged kiting enemies, arcane turrets, and chests
- [x] **Traps**: Spike traps, slowing toxic acid pools, and pressure dart traps
- [x] **Hive-Mind Swarm AI**: Shared centroid A* pathfinding + Boids separation/cohesion flocking for swarm packs

---

## Phase 4: Campaign, 5 Hero Classes & Card Progression (v2.0) - [COMPLETED]
- [x] **5-Tab Main Menu**: Linear 15-stage Campaign Map, Heroes, Equipment, Skills, and Chest/Card Shop
- [x] **5 Playable Hero Classes**: Warrior, Assassin, Viking, Archer, and Mage with unique weapons & active skills
- [x] **Equipment & Rarity System**: 4 universal armor slots + class-specific weapons across 4 rarity tiers (Common, Rare, Epic, Legendary)
- [x] **Combat Telemetry & Persistence**: Floating damage text, automatic stage clear summary screen, and full profile binary save (`savegame.dat`)
