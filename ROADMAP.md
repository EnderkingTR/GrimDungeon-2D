# GrimDungeon 2D - Development Roadmap

## Phase 1: Core Architecture - [COMPLETED]
- [x] **Game Loop**: Fixed 60 UPS and Delta Time calculation
- [x] **Rendering**: Triple Buffering and 2D Camera Viewport (LERP smooth following)
- [x] **Input**: Keyboard event listener (W-A-S-D and F3 debug toggle)
- **Milestone Output**: Smooth camera movement without screen tearing and a controllable base entity.

---

## Phase 2: World, Map & Physics - [COMPLETED]
- [x] **Tilemap System**: 2D matrices for world map layers
- [x] **Procedural Generation**: Dungeon generation with rooms and corridors
- [x] **Collision**: AABB (Axis-Aligned Bounding Box) collision testing
- **Milestone Output**: An interactive, procedurally generated dungeon with solid walls.

---

## Phase 3: Entity Architecture - [COMPLETED]
- [x] **Architectural Design**: Component-based entity structure
- [x] **State Management**: Player stats, inventory/gold, interaction mechanics
- [x] **Entities**: Modular player, enemies, traps, and treasure chests
- **Milestone Output**: Modular entities integrated into the game world.

---

## Phase 4: Strategy & AI - [COMPLETED]
- [x] **Pathfinding**: A* (A-Star) algorithm navigating around obstacles
- [x] **Decision Making**: Finite State Machine (FSM) (Patrol, Chase, Flee)
- [x] **Field of View**: Bresenham raycasting algorithm with real-time Fog of War
- **Milestone Output**: Intelligent enemies that track the player through the shortest path and patrol when unaware.

---

## Phase 5: Combat, UI & Polish - [COMPLETED]
- [x] **Combat System**: Melee sword sweeps (Hitbox), magic projectiles, particles, and screen shake
- [x] **HUD & Debug UI**: Health bar, minimap, FPS/UPS/particle metrics, and status announcements
- [x] **Persistence**: Binary serialization save & load system (`savegame.dat` - [F5] Save / [F9] Load)
- **Milestone Output**: Fully playable, atmospheric prototype with combat depth and integrated analysis tools.
