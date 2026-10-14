# ⚔️ GrimDungeon 2D

> A lightweight, modular 2D Rogue-like Action & Strategy Game Engine featuring procedural dungeon generation and intelligent AI, built from scratch in Java.

This project was developed to test the architecture and systems engineering capabilities of **Gemini Argon 4**, building a clean 2D engine in pure Java (AWT & Swing) without relying on any external libraries or frameworks.

---

## 🎮 Gameplay & Controls

| Key | Action |
| :--- | :--- |
| **W, A, S, D** / Arrow Keys | Move Character (Smooth LERP Camera Viewport) |
| **Space** | Melee Attack: Wide Sword Sweep (High Knockback) |
| **Left Click** | Ranged Attack: Cast Magic Projectile toward Mouse Cursor |
| **E** | Interact: Open Treasure Chests (+Gold & +25 Health) |
| **F3** | Toggle Debug Overlay (Metrics, AI Paths & Full Map Visibility) |
| **F5** | Quick Save (Binary serialization to `savegame.dat`) |
| **F9** | Quick Load (Restore saved state from disk) |
| **R** | Regenerate Procedural Dungeon (Restart) |

---

## 🛠️ Engine Architecture & Features

1. **Core Engine Architecture:**
   - Fixed 60 UPS (Updates Per Second) Game Loop with Delta Time compensation.
   - Screen tearing prevention via Triple-Buffering (`BufferStrategy`).
   - Dynamic Screen Shake effects and smooth camera interpolation (LERP).

2. **World & Physics System:**
   - **Procedural Dungeon Generator:** Carves randomized rooms and connecting corridors.
   - 2D Matrix-based Tilemap architecture with distinct floor and wall definitions.
   - Axis-Aligned Bounding Box (**AABB Collision**) for precise movement and physics constraints.

3. **AI, Pathfinding & Vision:**
   - **A\* (A-Star) Pathfinding:** Enemies navigate corridors around obstacles to flank the player.
   - **Finite State Machine (FSM):** Dynamic transitions between `PATROL`, `CHASE`, and `FLEE` (at low HP).
   - **Bresenham Line of Sight & Fog of War:** Dynamic vision calculation hiding undiscovered tiles and occluded enemies in real-time.

4. **Combat, Effects & UI:**
   - 360-degree particle emitter with bouncing sparks and friction physics.
   - Real-time HUD and discovered tile minimap in the corner.
   - Binary serialization save/load system.

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

---


