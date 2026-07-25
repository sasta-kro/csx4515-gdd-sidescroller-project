# Ocean Invaders Implementation Plan

## 1. Purpose

This plan translates `PROJECT_SPECIFICATION.md` into an ordered implementation
sequence for the existing Java/Swing starter project.

The user confirmed this plan on 24 July 2026. The first complete
placeholder-based implementation pass has been completed in the working tree.

The goal is to keep the professor's starter recognizable while extending it
into the full Ocean Invaders game. The plan does not replace the supplied
scene, sprite, power-up, audio, or package structure with a new framework.

## 2. Current Baseline

Repository:

- Branch: `main`
- Current local commit: `b11b007` (`added project specs`)
- Remote baseline: `origin/main` at `1a85ce8` (`Init`)
- Project language: Java
- UI and rendering: Swing/AWT
- Existing packages:
  - `gdd`
  - `gdd.scene`
  - `gdd.sprite`
  - `gdd.powerup`

Original starter pieces:

- `Game` owns the JFrame and scene replacement.
- `TitleScene` contains title rendering, input, and title audio.
- `Scene1` contains the game loop, scrolling map, spawning, collision,
  drawing, and gameplay audio.
- `Sprite` is the abstract sprite superclass.
- `Player`, `Enemy`, `Alien1`, `Shot`, and `Explosion` provided the starter
  sprite behavior.
- `PowerUp` and `SpeedUp` provide the starter power-up pattern.
- `AudioPlayer` provides audio playback.

`Alien1` and `Shot` were Space Invaders-specific placeholders. They were
removed after the Ocean Invaders replacements were implemented.

Known issue in the original baseline:

- The untouched starter does not compile because `Sprite` requires
  `act()`, while `Enemy`, `Explosion`, `Shot`, and `Alien1.Bomb` do not
  implement the exact no-argument method.

## 3. Implementation Rules

### 3.1 Preserve The Starter

- Keep the existing package layout.
- Keep `Game`, `TitleScene`, `Scene1`, `Sprite`, `Player`, `Enemy`,
  `PowerUp`, and `AudioPlayer` as recognizable parts of the final project.
- Add new classes inside the existing packages unless a new package is
  clearly necessary.
- Do not copy code from the earlier class projects.
- Do not replace the starter with a new engine or broad architecture.
- Avoid unrelated cleanup and formatting changes.

### 3.2 Configuration

- Put game-wide adjustable values in `Global.java`.
- Keep stage-specific spawn values near the relevant scene or spawn manager.
- Store durations as seconds in configuration and convert them consistently
  to game ticks.
- Use one target update rate for movement, cooldowns, animations, stage
  timers, and invincibility counters.
- Do not scatter unexplained numeric values through scene code.

### 3.3 Scene Lifecycle

Every scene must support a clear lifecycle:

1. Construct or reset state.
2. Start input, timer, and scene audio.
3. Update and draw only while active.
4. Pause without losing the current run.
5. Stop timer and audio before scene replacement.
6. Remove scene-owned entities during transition.

### 3.4 Incremental Verification

After every phase:

- Compile all Java sources.
- Run focused behavior checks for logic added in that phase.
- Launch the game for a short manual playtest when rendering or input changes.
- Check `git diff --check`.
- Confirm unrelated user changes remain untouched.

## 4. User-Owned Open Work

The specification marks the following items as open. The user will implement
or finalize them manually:

- Final title image.
- Final team-member names.
- Collision-map file encoding.
- Tile dimensions.
- Numeric tile codes.
- Final Stage 1 and Stage 2 map data.
- Final choice of two or three parallax layers.
- Final background and parallax artwork integration.
- Final image interpolation and pixel-scaling style.
- Final music and sound-effect assets.
- Final sprite dimensions.
- Final collision-scale tuning.

Codex implementation may provide hooks required by the finished gameplay,
but it must not make final decisions for these items.

Integration checkpoints are included in the phases below so the user's work
can be added without redesigning completed gameplay.

## 5. Planned Runtime Shape

This section defines the intended additions without restructuring the
starter.

### 5.1 Existing Files To Extend

`src/gdd/Global.java`

- Central gameplay constants and initial tuning values.
- Viewport, update rate, stage durations, scroll speed, damage, cooldowns,
  power-up durations, enemy values, and boss values.

`src/gdd/Game.java`

- Own current run state.
- Load title, Stage 1, Stage 2, and boss scenes.
- Stop the outgoing scene before replacement.
- Start a normal run or Stage 2 development run.
- Return to the title after game over or victory.

`src/gdd/scene/TitleScene.java`

- Direct keyboard commands for `1`, `2`, and `Q`.
- Team-name placeholders and project title.
- Correct title lifecycle and focus handling.

`src/gdd/scene/Scene1.java`

- Stage 1 game loop.
- Player, projectiles, Turtle, Jellyfish, mines, power-ups, timer, HUD, pause,
  and Stage 2 transition.

`src/gdd/sprite/Sprite.java`

- Keep abstract.
- Provide shared scaled bounds and visibility behavior.
- Preserve a simple per-tick `act()` contract.

`src/gdd/sprite/Player.java`

- Four-direction movement.
- Held-key state.
- Health, invincibility, damage flash, shooting cooldown, active weapon,
  Speed Up stacks, weapon stacks, and effect timers.

`src/gdd/sprite/Enemy.java`

- Keep as the common enemy superclass.
- Shared health, damage, score value, movement, death, and hit handling.

`src/gdd/powerup/PowerUp.java`

- Shared floating movement, duration metadata, collection, and off-screen
  removal.

`src/gdd/AudioPlayer.java`

- Retain as the playback mechanism.
- Add only the lifecycle behavior needed to stop scene-specific sound.

### 5.2 Planned New Files

Core state:

- `src/gdd/RunState.java`
  - Score, health, and carried player position.
  - Reset for Stage 1 and base reset for Stage 2 development start.

- `src/gdd/SpawnManager.java`
  - Random and scripted modes.
  - Stage-specific enemy pools.
  - Enemy and power-up timing.
  - Readable Java scripted-spawn entries.

- `src/gdd/SpawnMode.java`
  - `RANDOM` and `SCRIPTED`.

Scenes:

- `src/gdd/scene/Scene2.java`
- `src/gdd/scene/BossScene.java`

Player projectiles and effects:

- `src/gdd/sprite/Bubble.java`
- `src/gdd/sprite/EnemyRock.java`
- `src/gdd/sprite/Explosion.java` remains the shared explosion effect.

Enemies:

- `src/gdd/sprite/Jellyfish.java`
- `src/gdd/sprite/Turtle.java`
- `src/gdd/sprite/Octopus.java`
- `src/gdd/sprite/Swordfish.java`
- `src/gdd/sprite/Snake.java`
- `src/gdd/sprite/BomberFish.java`
- `src/gdd/sprite/Anglerfish.java`

Obstacles:

- `src/gdd/sprite/Mine.java`
- `src/gdd/sprite/Coral.java`
- Solid walls remain map tiles rather than individual sprite objects.

Power-ups:

- `src/gdd/powerup/MultiShot.java`
- `src/gdd/powerup/MegaShot.java`
- `src/gdd/powerup/SplitShot.java`
- `src/gdd/powerup/Heal.java`

Small shared types:

- `src/gdd/powerup/WeaponType.java`
  - `BASE`, `MULTI_SHOT`, `MEGA_SHOT`, and `SPLIT_SHOT`.

Map integration:

- Map-loading class and filenames remain a user-owned integration point until
  the user selects the encoding, tile size, and tile codes.

### 5.3 Deliberately Avoided Structure

The first implementation will not introduce:

- A custom game engine.
- Entity-component-system architecture.
- Dependency-injection framework.
- Scene graph framework.
- New build system.
- Large hierarchy of managers.
- A broad rewrite of `Scene1`.

Small helper methods may be extracted when they remove repeated collision,
drawing, timer, or spawning logic from multiple scenes.

## 6. Phase 0: Restore A Compiling Baseline

### Goal

Make the professor's baseline compile before adding features.

### Work

1. Implement the required no-argument `act()` contract in starter sprite
   classes that remain concrete.
2. Keep `Enemy` abstract if it no longer represents a directly spawnable
   enemy.
3. Remove or isolate the unused starter bomb implementation if it blocks the
   abstract contract.
4. Compile all files under `src/gdd`.
5. Run the current scene to confirm the starter still opens.

### Verification

- `javac` succeeds with zero errors.
- Existing Stage 1 window opens.
- Existing player movement and starter shooting still function.
- No gameplay redesign occurs in this phase.

## 7. Phase 1: Configuration, Run State, And Scene Flow

### Goal

Create a complete but mostly empty route through the game:

Title -> Stage 1 -> Stage 2 -> Boss -> Victory -> Title

### Work

1. Organize `Global.java` into clearly named configuration groups without
   changing package structure.
2. Add `RunState` for score, health, and carried player position.
3. Update `Game` with:
   - `loadTitle()`
   - `startNewGame()`
   - `startFromScene2()`
   - `loadScene1(RunState)`
   - `loadScene2(RunState)`
   - `loadBossScene(RunState)`
   - `showGameOver()`
   - `showVictory()`
4. Make title loading the normal startup behavior.
5. Implement direct title keys:
   - `1`: new run from Stage 1
   - `2`: base-state Stage 2 development run
   - `Q`: quit
6. Add placeholder `Scene2` and `BossScene` with working timers and
   transitions.
7. Ensure outgoing timers and audio stop before loading another scene.
8. Add simple temporary game-over and victory screens.
9. Enforce the 3-second victory input delay.

### Verification

- Every scene can be reached without restarting the application.
- Scene transitions do not leave duplicate timers running.
- Stage 2 development start has base state.
- Enter returns from game-over and victory screens to the title.
- Victory ignores Enter for the first 3 seconds.

## 8. Phase 2: Input, Pause, Player State, And HUD Foundation

### Goal

Finish the shared player-control and status foundation before adding content.

### Work

1. Change Player input from one-axis `dx` movement to held-key state for:
   - W and Up
   - S and Down
   - A and Left
   - D and Right
2. Support diagonal movement.
3. Clamp scaled player bounds to the viewport.
4. Add player health with a configurable maximum starting at 5.
5. Add a shared damage method.
6. Add configurable invincibility ticks after damage.
7. Add red damage flashing without changing permanent sprite data.
8. Add pause state to each gameplay scene.
9. Implement direct pause keys:
   - Escape: resume
   - R: restart from Stage 1
   - M: main menu
   - Q: quit
10. Ensure pause freezes gameplay timers, movement, spawning, animation, and
    power-up countdowns.
11. Add an initial text HUD showing:
    - Score
    - HP
    - Stage
    - Remaining stage time
    - Speed level
    - Weapon and weapon level
    - Active timers

### Verification

- WASD and arrow keys work independently and together.
- Player never leaves the viewport.
- One collision cannot remove all HP during adjacent frames.
- Pause freezes and resumes the exact current state.
- Restart, main menu, and quit commands work without menu navigation.

## 9. Phase 3: Bubble Combat Vertical Slice

### Goal

Complete the base shooting loop with one temporary enemy before adding all
enemy types.

### Work

1. Introduce `Bubble` as the player's right-moving projectile.
2. Support tap and held-Space firing.
3. Add a configurable cooldown.
4. Add scaled full-bounds collision.
5. Add a shared sprite-bounds wrapper.
6. Add shared render-scale behavior.
7. Make bubbles die after:
   - First enemy hit
   - Blocking obstacle hit
   - Leaving the viewport
8. Add enemy health and `takeDamage`.
9. Add score only for direct player kills.
10. Keep mine-caused and off-screen deaths scoreless.
11. Use the existing explosion effect for defeated enemies.

### Verification

- Holding Space fires at the configured rate.
- A bubble damages only its first target.
- A killed enemy adds score once.
- An off-screen enemy adds no score.
- Rendering and collision scales can be changed centrally.

## 10. Phase 4: Spawn Manager And Stage 1 Enemies

### Goal

Make Stage 1 playable with both supported spawn modes.

### Work

1. Add `SpawnMode`.
2. Add `SpawnManager`.
3. Make random spawning the default.
4. Add readable Java scripted-spawn entries using type, time, and position.
5. Keep enemy and power-up schedules independent.
6. Add configurable random:
   - Cooldowns
   - Type weights
   - Vertical bounds
   - Power-up probabilities
7. Add a 2-second initial spawn delay.
8. Implement Jellyfish:
   - Leftward world movement
   - Configurable vertical floating
   - No tracking
9. Implement Turtle:
   - Medium player tracking
   - Higher health
   - No projectile attack
10. Restrict the Stage 1 random pool to Jellyfish and Turtle.
11. End Stage 1 when its configured timer reaches zero.

### Verification

- Random mode produces only Stage 1 enemy types.
- Scripted mode spawns exact configured entries.
- No enemies spawn during the first 2 seconds.
- Spawn rate does not increase automatically.
- Stage 1 defaults to 5 minutes and supports a shorter development value.

## 11. Phase 5: Complete Power-Up System

### Goal

Implement all five power-ups, their movement, timers, stacking, replacement,
color priority, and HUD display.

### Work

1. Extend `PowerUp` with:
   - Leftward world movement
   - Sine-wave vertical movement
   - Off-screen removal
2. Keep Heal instant and all other effects timed.
3. Implement two Speed Up stacks with one shared timer.
4. Implement four Multi-shot stacks with bursts of:
   - Level 1: 3 bubbles
   - Level 2: 4 bubbles
   - Level 3: 5 bubbles
   - Level 4: 6 bubbles
5. Implement an internal burst counter so bubbles fire sequentially rather
   than simultaneously.
6. Implement Mega-shot:
   - One larger bubble
   - Higher damage
   - 10% to 15% longer cooldown
7. Implement Split-shot:
   - Three parallel right-moving bubbles
   - Configurable vertical offsets
8. Implement Heal:
   - Restore 1 HP
   - Never exceed maximum
   - Green flash
9. Implement replacement rules:
   - One weapon mode at a time
   - New weapon replaces old weapon
   - Replacing Multi-shot removes its stacks
   - Expired replacement returns to base weapon
10. Allow Speed Up and one weapon to coexist.
11. Apply color priority:
    - Heal flash
    - Weapon
    - Speed
    - Orange base
12. Display Speed and weapon timers separately.

### Verification

- Stack limits cannot be exceeded.
- Recollecting a stackable power-up resets its shared timer.
- Speed remains active when a weapon changes.
- Weapon replacement clears Multi-shot stacks.
- Heal restores correct color after flashing.
- Scene transition clears all power-up effects.

## 12. Phase 6: Stage 2 Enemy Set

### Goal

Implement all Stage 2 enemy behaviors and enemy projectiles.

### Work

1. Add Octopus:
   - Vertical movement
   - Configurable random rock interval
   - Straight left-moving rocks
   - Throw animation hook
2. Add Swordfish:
   - Record player target
   - Visible charge delay
   - Fast committed dash
   - Off-screen removal after a miss
3. Add Snake:
   - Spawn from top or bottom
   - Travel toward opposite vertical boundary
   - Participate in world scrolling
4. Make Stage 2 enemy pool include all five enemy types immediately.
5. Add enemy-projectile collision with player and blocking obstacles.
6. Prevent player bubbles and enemy projectiles from cancelling one another.
7. Apply shared damage and invincibility behavior to all enemy attacks.
8. Give every enemy configurable health, speed, damage, and score.

### Verification

- Every enemy behaves distinctly.
- Swordfish does not retarget after beginning its dash.
- Octopus rocks fly straight.
- Snake can be killed for score or leave without score.
- Enemy projectiles cannot be destroyed by player bubbles.

## 13. Phase 7: Obstacles And User Map Integration

### Goal

Complete mine, coral, and wall gameplay around the user's map decisions.

### Codex Work

1. Implement Mine:
   - Trigger on player, enemy, player bubble, enemy projectile, or explosion
   - Configurable explosion radius
   - Damage all nearby damageable entities
   - Chain reactions
   - No score from mine kills
2. Implement Coral:
   - 1 HP
   - Block player while present
   - Break from player bubble
   - Break and damage player on direct contact
   - Ignore enemies and enemy projectiles
3. Implement wall collision behavior:
   - Solid
   - Indestructible
   - Configurable contact-damage toggle
   - Shared invincibility handling
4. Keep obstacle rendering independent from background images.
5. Provide scene integration methods that consume the user's completed map
   representation.

### User Integration Checkpoint

Before final map integration, the user supplies or implements:

- External file format.
- Tile size.
- Tile codes.
- Stage 1 map.
- Stage 2 map.

### Verification

- Mine chain reactions terminate cleanly.
- One mine explosion cannot award score.
- Coral removal opens its occupied space.
- Walls clamp movement without trapping the player inside a tile.
- Scene code reads obstacle state independently from background rendering.

## 14. Phase 8: Stage Transitions And Parallax Hooks

### Goal

Finish state transfer and both transition modes without selecting the user's
final artwork.

### Work

1. Make sudden scene replacement the default.
2. Add a configurable transition-mode setting.
3. Provide a hook for adjacent-background scrolling.
4. Preserve score, health, player position, and active power-up state.
5. Clear:
   - Enemies
   - Enemy projectiles
   - Player bubbles
   - Floating power-ups
   - Obstacles
6. Provide configurable parallax-layer update and draw hooks.
7. Stop Stage 2 scrolling when entering BossScene.

### User Integration Checkpoint

The user selects and integrates:

- Two or three final parallax layers.
- Final layer images.
- Final scroll multipliers.
- Final seamless transition artwork.

### Verification

- Sudden transition works without user artwork.
- No outgoing entities survive scene replacement.
- Score, health, player position, and active power-ups persist.
- Background behavior never moves the player automatically.

## 15. Phase 9: Anglerfish Boss

### Goal

Implement the complete boss encounter and victory flow.

### Work

1. Add Anglerfish with:
   - 50 HP
   - Scaled bounds
   - Continuous damage reception
   - Configurable score
2. Add random attack selection with cooldown and recovery.
3. Add phase two at 25 HP:
   - Redder or pinker tint
   - Shorter attack cooldown
4. Implement Bubble Laser:
   - Charge state
   - Straight left-moving bubble stream
   - Vertical tracking during firing
   - Remain on right side
5. Implement Bite:
   - Mouth-opening warning
   - Fast leftward dash
   - Configurable damage
   - Return to original position
6. Implement Summon:
   - Bomber fish track player
   - Random timer per fish
   - Contact or timer explosion
   - Configurable radius and damage
7. Apply player invincibility to all boss damage.
8. Add phase and attack animation hooks.
9. Stop attacks when boss health reaches zero.
10. Play boss death sequence.
11. Show victory screen and enforce 3-second input lock.
12. Return to title on Enter.

### Verification

- Boss changes phase exactly once.
- Boss cannot start overlapping attacks.
- Laser tracks vertically but fires left.
- Bite returns to its starting side.
- Bomber timers vary inside configured range.
- Boss remains damageable throughout the fight.
- Victory flow cannot be skipped during the first 3 seconds.

## 16. Phase 10: Audio, Animation Hooks, And Presentation

### Goal

Finish all code-side presentation support while leaving open content choices
to the user.

### Codex Work

1. Add scene-specific audio lifecycle calls.
2. Add sound-trigger hooks for:
   - Shooting
   - Player damage
   - Enemy defeat
   - Explosion
   - Power-up collection
   - Victory
3. Add simple sprite-animation state support for:
   - Player swimming
   - Player shooting
   - Player death
   - Octopus throwing
   - Swordfish charging
   - Boss idle
   - Boss laser
   - Boss bite
   - Boss summon
   - Boss death
4. Keep static walls, coral, and background decorations outside the character
   animation requirement.
5. Apply power-up and phase tinting without destroying animation state.

### User Integration Checkpoint

The user adds or finalizes:

- Final sprite sheets and frame dimensions.
- Final title image.
- Team names.
- Final scaling and interpolation choice.
- Final music and sound effects.
- Final animation frame rectangles and timing.

### Verification

- Missing optional audio does not break scene flow.
- Scene changes stop outgoing music.
- Animation state changes do not alter collision position.
- Damage, heal, weapon, Speed Up, and boss-phase colors follow priority rules.

## 17. Phase 11: Full Integration And Balancing

### Goal

Join every completed system and tune the game against the specification.

### Work

1. Run a complete Stage 1 start.
2. Run a direct Stage 2 development start.
3. Run the full Stage 1 -> Stage 2 -> Boss path.
4. Verify both random and scripted spawning.
5. Verify both transition settings.
6. Test every enemy, obstacle, and power-up.
7. Test game over from each stage.
8. Test pause commands from each stage.
9. Test score, health, and active power-up persistence.
10. Test that scene-owned entities do not persist.
11. Tune recommended values without hardcoding scene-specific magic numbers.
12. Confirm five-minute default stage timers.
13. Update README with:
    - Ocean Invaders project description
    - Controls
    - Team-name placeholders until replaced
    - Run instructions
    - Asset credits
14. Compile from a clean temporary build directory.
15. Check repository status and exclude IDE/build output.

### Final Verification Checklist

- Project compiles with no errors.
- Title starts by default.
- Direct keyboard menus work.
- Player moves freely in four directions and never auto-scrolls.
- World moves left.
- Player shoots only right.
- Health, invincibility, score, and HUD work.
- Every power-up follows stacking and replacement rules.
- Stage 1 contains only Turtle and Jellyfish.
- Stage 2 allows all five enemies immediately.
- Random mode is default.
- Scripted mode is selectable in code.
- Stage timers and transitions work.
- Mine, coral, and wall behavior matches the specification.
- Boss has all attacks and phase two.
- Game-over and victory flows return to title.
- No old scene timer or audio continues after transition.
- Open user-owned content is either integrated or clearly identified.

## 18. Suggested Commit Boundaries

Each phase should be reviewable and reversible:

1. `fix baseline sprite contract`
2. `add run state and scene flow`
3. `add player movement pause and hud`
4. `add bubble combat`
5. `add spawn manager and stage one enemies`
6. `add complete powerup system`
7. `add stage two enemies`
8. `add obstacles and map integration`
9. `add stage transitions and parallax hooks`
10. `add anglerfish boss`
11. `add animation and audio hooks`
12. `finish integration and balancing`

Commits should not include IDE metadata, compiled classes, or unrelated user
files.

## 19. Team Parallelization

After Phase 2 establishes shared state and contracts, work can proceed in
parallel:

### Gameplay Lane

- Player shooting
- Power-ups
- HUD
- Damage and pause

### Enemy Lane

- Spawn manager
- Five normal enemies
- Enemy projectiles
- Boss and bomber fish

### World And Content Lane

- External maps
- Obstacles
- Background layers
- Sprites and animation rectangles
- Audio
- Title and team information

The lanes rejoin after each major phase through compilation and a short
manual game run. Changes to `Sprite`, `Player`, `Global`, and scene lifecycle
must be coordinated because those files are shared.

## 20. Confirmation Gate

No gameplay source changes are authorized by this plan alone.

After the user confirms:

1. Begin with Phase 0.
2. Complete and verify one phase at a time.
3. Report material implementation decisions as they arise.
4. Preserve user-created assets, maps, and concurrent changes.
5. Stop for user input only when an unresolved user-owned item blocks further
   implementation.
