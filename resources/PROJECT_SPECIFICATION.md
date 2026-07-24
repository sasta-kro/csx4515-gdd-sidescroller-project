# Ocean Invaders Project Specification

## 1. Document Purpose

This document defines the required gameplay and functional behavior of
Ocean Invaders. It is the shared reference for the project team.

This is a specification document. It does not define the final class
structure, detailed implementation design, task ownership, or test plan.

All features in this document are part of the intended project scope.
Features are not divided into required and optional tiers unless they are
explicitly marked as development-only configuration.

## 2. Course Project Constraints

- The project is a horizontal side-scrolling shooter.
- The project must extend the supplied
  `mchayapol/gdd-space-invaders-project` codebase.
- Existing project structure must be preserved.
- Code from previous class projects or unrelated codebases must not replace
  the supplied starter code.
- The title scene must use a project-specific image and show the names of all
  team members.
- The game must contain at least two scrolling stages.
- Each normal stage must support at least five minutes of gameplay.
- The final stage must contain a boss fight.
- The game must contain at least two enemy types.
- Major game sprites must be animated through sprite clipping or drawing.
- The game must provide two Speed Up levels and four Multi-shot levels.
- The dashboard must show score, speed, shot upgrade, and other relevant
  player status.

## 3. Product Overview

### 3.1 Title

**Ocean Invaders**

### 3.2 Concept

Ocean Invaders is a modern underwater interpretation of a retro
side-scrolling space shooter. The player controls a cute fish that fights
underwater enemies by firing bubbles.

The game keeps the readable movement, enemy waves, upgrades, and escalating
combat of a retro shooter while replacing the space setting with colorful
ocean environments, sea creatures, coral, mines, caves, and an Anglerfish
boss.

### 3.3 Technology

- Language: Java
- User interface and rendering: Swing/AWT
- Starting viewport: `716 x 700`
- Target update rate: approximately 60 updates per second
- The viewport dimensions and rendering scale must remain configurable in
  code.

## 4. Core Gameplay

### 4.1 Core Loop

1. The player enters a stage.
2. The world scrolls continuously while enemies, obstacles, and power-ups
   enter from outside the viewport.
3. The player moves freely within the viewport, avoids hazards, shoots
   enemies, earns score, and collects temporary power-ups.
4. The player survives until the stage timer ends.
5. The game clears the previous scene's entities and moves to the next stage.
6. Stage 2 continues into the boss encounter.
7. Defeating the boss ends the run with a victory screen.

### 4.2 Scrolling Model

- The player is not automatically pushed to the right.
- The player may move freely anywhere inside the viewport bounds.
- Background layers, enemies, obstacles, and power-ups move left.
- This leftward world movement creates the appearance that the player is
  progressing to the right.
- The base world-scroll speed is configurable.
- The player must remain inside the viewport.

### 4.3 Player Movement

- The player can move up, down, left, and right.
- Diagonal movement is supported through simultaneous horizontal and vertical
  input.
- Movement is limited by the viewport and solid level obstacles.
- The base movement speed is configurable.
- Speed Up may increase movement speed by up to two stack levels.
- Initial movement values should be tuned for responsive control at the
  starting resolution.

### 4.4 Controls

Gameplay controls:

| Action | Keys |
| --- | --- |
| Move up | `W` or Up Arrow |
| Move down | `S` or Down Arrow |
| Move left | `A` or Left Arrow |
| Move right | `D` or Right Arrow |
| Shoot | Space |
| Open pause menu | Escape |

Menus use direct keyboard commands. They do not use a highlighted cursor or
arrow-key menu navigation.

Title menu:

| Action | Key |
| --- | --- |
| Start from Stage 1 | `1` |
| Start from Stage 2 | `2` |
| Quit | `Q` |

Pause menu:

| Action | Key |
| --- | --- |
| Resume | Escape |
| Restart game from Stage 1 | `R` |
| Return to main menu | `M` |
| Quit | `Q` |

Game-over and victory screens:

| Action | Key |
| --- | --- |
| Return to main menu | Enter |

### 4.5 Pause Behavior

- Pressing Escape during gameplay pauses the game and opens the pause menu.
- While paused, gameplay timers, movement, spawning, animations, and combat
  updates stop.
- Pressing Escape again resumes the game.
- Restart Game begins a new run from Stage 1 with base player state.
- Main Menu abandons the current run and returns to the title menu.
- Quit closes the application.

## 5. Player State

### 5.1 Health

- The player starts with 5 HP.
- Maximum health is 5 HP.
- Starting and maximum health must be configurable.
- Heal cannot raise health above the maximum.
- Reaching 0 HP causes game over.

### 5.2 Damage

- Standard contact damage is 1 HP.
- Enemy projectiles deal 1 HP by default.
- Boss attack damage is configurable separately for each attack.
- After taking damage, the player receives a short period of invincibility.
- The invincibility duration is configurable.
- A starting duration near `0.75` seconds is recommended for initial tuning.
- Further collisions during invincibility do not remove health.
- Damage does not apply knockback.
- The player flashes red for several frames when damaged.

### 5.3 Scene Persistence

The following player state carries from Stage 1 to Stage 2 and from Stage 2
to the boss stage:

- Score
- Current health
- Player screen position

The following does not carry between stages:

- Active Speed Up
- Speed Up stacks
- Active weapon power-up
- Multi-shot stacks
- Power-up timers

Starting directly from Stage 2 through the development menu uses base state:

- Score: 0
- Health: maximum
- Base movement speed
- Base single-bubble weapon
- No active power-up
- Stage 2 starting position

## 6. Player Shooting

### 6.1 Base Weapon

- The player fires bubbles only toward the right.
- The player may tap Space repeatedly or hold Space for continuous fire.
- A configurable cooldown limits the firing rate.
- A base bubble deals 1 HP of damage.
- Bubble speed, size, cooldown, and maximum on-screen count are configurable.
- A bubble disappears when it:
  - Hits an enemy.
  - Hits a blocking obstacle.
  - Leaves the viewport.
- Player bubbles do not penetrate enemies.
- Player bubbles cannot cancel enemy rocks, boss bubbles, or other enemy
  projectiles.

Recommended initial tuning:

| Property | Initial value |
| --- | --- |
| Bubble damage | 1 HP |
| Bubble speed | 480 pixels per second |
| Base cooldown | 0.25 seconds |

Exact visual size and maximum on-screen count should be selected after the
final bubble sprite is imported.

### 6.2 Projectile Bounds

- Projectile collision uses the projectile's scaled image bounds.
- Render scaling and collision-bound scaling must be adjustable through
  shared wrapper behavior rather than repeated per-sprite calculations.

## 7. Power-Up System

### 7.1 General Rules

- Power-ups are temporary.
- Power-up durations are configurable.
- Power-ups do not drop from defeated enemies.
- In scripted spawning mode, their times and positions are defined in the
  spawn schedule.
- In random spawning mode, they spawn from the right using configurable
  probabilities, cooldowns, and vertical bounds.
- A power-up moves left with the world.
- It also moves slightly up and down using a sine-wave floating motion.
- Sine-wave amplitude and frequency are configurable.
- An uncollected power-up remains active until it leaves the left side of the
  viewport.
- Collecting another copy of a stackable power-up adds a stack up to its
  limit and resets that power-up's shared timer.
- All stacks of a power-up expire together when its shared timer ends.

### 7.2 Power-Up Colors

| State | Fish and bubble color |
| --- | --- |
| No active power-up | Orange |
| Speed Up only | Pink |
| Multi-shot | Purple |
| Mega-shot | Yellow |
| Split-shot | Light purple |
| Heal collection flash | Green |

Color priority:

1. The Heal collection flash temporarily overrides every other color.
2. The active weapon power-up color has priority over Speed Up.
3. Speed Up uses pink when no weapon power-up is active.
4. Orange is used when no timed power-up is active.

When the highest-priority effect expires, the color returns to the next
active effect rather than always returning directly to orange.

### 7.3 Speed Up

- Speed Up affects movement in every direction.
- Speed Up can stack twice.
- Each stack raises the player's movement speed.
- The two speed values are configurable.
- Recommended initial tuning:

| Level | Movement speed |
| --- | --- |
| Base | 240 pixels per second |
| Speed level 1 | 300 pixels per second |
| Speed level 2 | 360 pixels per second |

- Speed Up uses a shared timer for all stacks.
- Speed Up should have a longer duration than non-stackable weapon modes.
- Recommended initial duration: 15 seconds.
- Speed Up may remain active alongside one weapon power-up.

### 7.4 Multi-Shot

- Multi-shot is a timed weapon power-up.
- It supports four stack levels.
- Each firing action creates a rapid sequential burst.
- Burst bubbles are fired in the same rightward direction.
- The burst sequence is:

| State | Bubbles per burst |
| --- | --- |
| No Multi-shot | 1 |
| Multi-shot level 1 | 3 |
| Multi-shot level 2 | 4 |
| Multi-shot level 3 | 5 |
| Multi-shot level 4 | 6 |

- The interval between bubbles inside a burst is configurable.
- Recommended initial burst interval: 0.08 seconds.
- Multi-shot uses one shared timer for all stacks.
- Collecting another Multi-shot adds one stack and resets the shared timer.
- Multi-shot should have a longer duration than Mega-shot and Split-shot.
- Recommended initial duration: 15 seconds.

### 7.5 Mega-Shot

- Mega-shot is a timed, non-stackable weapon mode.
- It fires one larger bubble.
- The bubble uses a moderately longer cooldown than the base weapon.
- The cooldown increase must remain between 10% and 15% for initial tuning.
- Recommended initial damage: 2 HP.
- Recommended initial size: 1.75 times the base bubble.
- Recommended initial duration: 10 seconds.
- Collecting Mega-shot while it is already active resets its timer.

### 7.6 Split-Shot

- Split-shot is a timed, non-stackable weapon mode.
- Each firing action creates three bubbles.
- All three bubbles travel directly to the right.
- The bubbles use parallel vertical offsets with no firing angles.
- Vertical spacing is configurable.
- Recommended initial duration: 10 seconds.
- Collecting Split-shot while it is already active resets its timer.

### 7.7 Weapon Replacement

- The player may have only one weapon power-up active at a time.
- Collecting a different weapon immediately replaces the current weapon.
- The new weapon receives a fresh timer.
- Replacing Multi-shot removes all Multi-shot stacks.
- Expiration of Mega-shot or Split-shot returns the player to the base
  single-bubble weapon. Previous Multi-shot stacks are not restored.
- Active Speed Up and its timer are unaffected by weapon replacement.

### 7.8 Heal

- Heal restores 1 HP immediately.
- Heal cannot exceed maximum health.
- Heal does not occupy the weapon-power-up slot.
- Heal does not change Speed Up or weapon timers.
- Heal flashes the player green for several frames, then restores the color
  of the highest-priority active timed power-up.

## 8. Score

- Score is awarded only when the player directly defeats an enemy with player
  attacks.
- An enemy that leaves the screen awards no score.
- An enemy that collides with the player and continues off-screen awards no
  score.
- Mine explosions award no enemy score, even when the player triggered the
  mine.
- Destroying coral awards no score.
- Every enemy type has a separately configurable score value.

Recommended initial score values:

| Target | Score |
| --- | --- |
| Jellyfish | 100 |
| Snake | 150 |
| Octopus | 200 |
| Swordfish | 200 |
| Turtle | 250 |
| Anglerfish boss | 5,000 |

## 9. Spawning

### 9.1 Spawn Modes

The game supports two code-configurable spawning modes:

1. Random spawning
2. Scripted spawning

Random spawning is the default.

The selected mode is a development configuration. It is not exposed to the
player.

### 9.2 Scripted Spawning

- Spawn schedules are defined in readable Java code.
- Each scripted spawn entry identifies:
  - Spawn time.
  - Entity type.
  - Spawn position.
- Enemy and power-up schedules may be defined independently.
- A stage ends when its configured duration is reached, even when scripted
  spawning is active.

### 9.3 Random Spawning

- The spawn manager selects from the enemy types allowed in the current
  stage.
- Enemy type probabilities are configurable.
- Enemy spawn cooldowns are configurable.
- Power-up probabilities and cooldowns are configurable separately.
- Spawn positions are constrained to valid configurable bounds near the
  right side of the viewport.
- Spawn rates do not automatically increase over time.
- Difficulty is controlled directly through spawn-manager configuration.
- A stage ends when its configured duration is reached.

## 10. Stages

### 10.1 Stage Timing

- Stage 1 and Stage 2 each default to 5 minutes.
- Each duration is configurable in code.
- Short development durations may be used for testing and inspection.
- The boss stage is encounter-based and ends when the player or boss dies.

### 10.2 Stage Transition Rules

- Sudden scene replacement is the default transition.
- A configurable alternative may place the next stage's background adjacent
  to the current background and scroll it into view.
- On every transition, the game removes all:
  - Enemies.
  - Enemy projectiles.
  - Player projectiles.
  - Floating power-ups.
  - Obstacles.
- Persistent player state is then transferred according to Section 5.3.

### 10.3 Stage 1: Minefield

Environment:

- Underwater minefield.
- Scrolling water and mine-themed background layers.
- Mine hazards throughout the stage.
- Additional background details may use elements from the cave asset set.

Enemy set:

- Turtle
- Jellyfish

Stage completion:

- The player survives until the Stage 1 timer ends.
- The game transitions to Stage 2.

### 10.4 Stage 2: Deep Caves

Environment:

- Deep underwater cave.
- Cave ceiling and floor.
- Solid walls.
- Breakable coral on navigable boundaries.
- Background decorations remain independent from collision obstacles.

Enemy set:

- Turtle
- Jellyfish
- Swordfish
- Octopus
- Snake

Stage completion:

- All five enemy types are available immediately.
- The player survives until the Stage 2 timer ends.
- The game clears normal stage entities and enters the boss stage.

### 10.5 Stage 3: Anglerfish Boss

- Stage 3 is implemented as a separate scene.
- To the player, it appears to continue directly from Stage 2.
- The environment remains the deep cave.
- The cave has a floor, ceiling, and right wall.
- Normal background scrolling stops.
- The boss uses dedicated boss music.
- Normal enemies no longer spawn.
- Bomber fish created by the boss are still allowed.
- Defeating the boss ends the game with victory.

## 11. Maps And Backgrounds

### 11.1 Collision Map

- Stage collision maps load from external files.
- Collision-map data is independent from the background artwork.
- The map represents empty water, solid walls, breakable coral, mines, and
  any other collision-relevant stage objects.
- Tile size, file encoding, and final tile codes remain to be selected when
  the obstacle artwork is finalized.

### 11.2 Background Rendering

- Full backgrounds use PNG texture images.
- Each scrolling stage uses two or three parallax layers depending on the stage.
- Layer count and individual scroll multipliers are configurable.
- Background layers do not define collision.
- Stage 3 replaces scrolling layers with a static boss background.

## 12. Collision Rules

### 12.1 General Bounds

- Collision uses full scaled image bounds for simplicity.
- A shared collision-bound wrapper controls scaling.
- A shared rendering wrapper controls sprite scaling.
- Scaling behavior must remain adjustable without rewriting every collision
  check.

### 12.2 Player And Enemy Contact

- Contact removes 1 HP from the player by default.
- The player's invincibility period prevents repeated damage every frame.
- Contact does not automatically destroy the enemy.
- The enemy continues its movement and may leave through the left boundary.
- No score is awarded unless the player later defeats that enemy directly.

### 12.3 Projectile Rules

- A player bubble dies after its first enemy or obstacle collision.
- Player and enemy projectiles do not cancel one another.
- Enemy projectiles die when they hit the player, hit a blocking obstacle, or
  leave the viewport.

## 13. Obstacles

### 13.1 Mine

- Mines are stationary within the world and move left because of world
  scrolling.
- A mine triggers when it collides with any entity or projectile, including:
  - Player.
  - Enemy.
  - Player bubble.
  - Enemy projectile.
  - Another mine explosion.
- A triggered mine creates an explosion and removes itself.
- Explosion radius is configurable and visually based on the scaled
  explosion sprite.
- Recommended initial explosion damage: 1 HP.
- The explosion damages all nearby damageable entities.
- Mine explosions can trigger nearby mines and create chain reactions.
- Mine-caused enemy deaths never award score.

### 13.2 Coral

- Coral has 1 HP.
- Coral blocks player movement until destroyed.
- A player bubble can destroy coral.
- Direct player contact deals 1 HP to the player and destroys the coral.
- Player damage is subject to normal invincibility frames.
- Enemies and enemy projectiles do not destroy coral.
- Destroying coral awards no score.

### 13.3 Walls

- Walls are solid and block player movement.
- Walls cannot be destroyed.
- Contact damage is controlled by a configurable boolean.
- Contact damage is enabled by default.
- Recommended initial wall damage: 1 HP.
- Wall damage uses the same player invincibility period as other damage.

## 14. Enemies

### 14.1 Shared Enemy Rules

- Enemy health, speed, contact damage, and score are configurable per type.
- Standard enemy contact damage starts at 1 HP.
- An enemy is removed when:
  - Its health reaches zero.
  - It leaves its permitted off-screen boundary.
- An enemy that leaves without being killed gives no score.
- Enemy spawn frequency is controlled entirely by the active spawn mode.
- Enemy difficulty does not automatically scale during a stage.

### 14.2 Recommended Initial Values

| Enemy | Health | Base speed | Score |
| --- | ---: | ---: | ---: |
| Jellyfish | 1 HP | 90 px/s left | 100 |
| Snake | 2 HP | 150 px/s vertical | 150 |
| Octopus | 2 HP | 90 px/s left | 200 |
| Swordfish | 2 HP | 360 px/s while charging | 200 |
| Turtle | 4 HP | 75 px/s tracking | 250 |

These are starting values for playtesting and remain adjustable.

### 14.3 Jellyfish

- Jellyfish moves left with world flow.
- It floats aimlessly through configurable up-and-down movement.
- Vertical amplitude and speed are configurable.
- Its vertical movement stays within the viewport.
- It does not track the player.

### 14.4 Turtle

- Turtle continuously swims toward the player's current position.
- It uses a medium movement speed.
- Its distinguishing feature is higher health than normal enemies.
- It has no projectile or secondary attack.

### 14.5 Octopus

- Octopus moves left with world flow.
- It also moves up and down.
- At a configurable random frequency, it throws a rock toward the left.
- A rock travels in a straight line after being fired.
- Rocks do not track the player after firing.
- Recommended initial firing interval range: 2 to 4 seconds.
- Rock damage starts at 1 HP.

### 14.6 Swordfish

- Swordfish aims toward the player's position.
- It enters a visible charging state before attacking.
- After the charging delay, it rapidly moves toward the recorded target.
- Recommended initial charging delay: 0.75 seconds.
- It does not continuously correct its aim during the charge.
- If it misses and leaves the viewport, it is removed without awarding score.

### 14.7 Snake

- A Snake enters from either the top or bottom cave boundary.
- It travels vertically toward the opposite boundary.
- It also participates in the world's leftward scrolling.
- It is removed without score if it exits the viewport.
- It can be killed normally for score.

## 15. Anglerfish Boss

### 15.1 General Behavior

- The Anglerfish occupies approximately two-thirds of the viewport height.
- It remains primarily on the right side of the arena.
- Starting health is 50 HP.
- Boss health is configurable.
- The player may damage the boss continuously.
- The boss does not use separate vulnerable and invulnerable periods.
- Contact and attack damage values are configurable independently.
- The boss awards 5,000 score by default.

### 15.2 Phase Two

- Phase two begins at half health.
- At the default health value, the threshold is 25 HP.
- The boss changes to a slightly redder or pinker tint.
- Its attack cooldown decreases.
- Recommended initial cooldowns:

| Phase | Delay between attacks |
| --- | --- |
| Phase one | 3 seconds |
| Phase two | 2 seconds |

### 15.3 Attack Selection

- The boss randomly selects from attacks that are currently available.
- It cannot begin a new attack until the current attack and its recovery have
  completed.
- Attack-selection weights and cooldowns are configurable.

### 15.4 Bubble Laser

1. The boss opens its mouth and plays the laser charging animation.
2. It waits for a configurable charge duration.
3. It begins firing a continuous stream of bubbles directly to the left.
4. During the stream, it tracks the player's vertical position while
   remaining on the right side.
5. Each laser bubble deals configurable damage, starting at 1 HP.
6. The attack ends after its configured duration.

Recommended initial values:

| Property | Initial value |
| --- | --- |
| Charge duration | 1 second |
| Firing duration | 2 seconds |
| Bubble interval | 0.10 seconds |
| Bubble damage | 1 HP |

### 15.5 Bite

1. The boss plays a mouth-opening warning animation.
2. It rapidly dashes left toward the player.
3. It travels almost to the left edge of the viewport.
4. Contact deals configurable damage, starting at 1 HP.
5. It returns to its original position on the right.

Recommended initial warning duration: 0.75 seconds.

### 15.6 Summon

- The boss creates bomber fish.
- Each bomber fish continuously moves toward the player's current position.
- Each bomber receives a random explosion timer from a configurable range.
- A bomber explodes when its timer ends or it touches the player.
- Explosion radius is configurable and visually based on the scaled explosion
  sprite.
- Bomber contact and explosion damage start at 1 HP.
- Recommended initial timer range: 2 to 4 seconds.
- Recommended initial summon count: 3 bomber fish.

### 15.7 Boss Death

1. Boss health reaches zero.
2. Gameplay attacks and spawning stop.
3. The boss death animation plays.
4. The victory screen appears.
5. Input is ignored for the first 3 seconds.
6. After the delay, pressing Enter returns to the main menu.

## 16. Scene Flow

Normal run:

1. Title menu.
2. Player presses `1`.
3. Stage 1 loads.
4. The stage waits 2 seconds before spawning enemies.
5. Stage 1 runs until its timer ends.
6. Stage 2 loads using the configured transition.
7. Stage 2 begins with no active power-ups.
8. Stage 2 runs until its timer ends.
9. Stage 3 loads as a visual continuation of Stage 2.
10. Background scrolling stops.
11. The Anglerfish boss appears.
12. The player defeats the boss.
13. Victory screen appears.
14. After the 3-second input delay, Enter returns to the title menu.

Development Stage 2 start:

1. Title menu.
2. Player presses `2`.
3. Stage 2 loads with base player state.
4. The game continues through Stage 2 and the boss stage normally.

Failure flow:

1. Player health reaches zero.
2. Player death animation plays.
3. Game-over screen appears.
4. Pressing Enter returns to the title menu.
5. A new normal run always starts from Stage 1.

## 17. User Interface

### 17.1 Title Menu

The title menu displays:

- Ocean Invaders title.
- Project-specific title image.
- Team-member names using placeholders until final names are inserted.
- Direct key commands for Stage 1, Stage 2 development start, and Quit.

### 17.2 Dashboard

The gameplay dashboard displays:

- Score.
- Current HP and maximum HP.
- Speed Up stack level, from 0 to 2.
- Current weapon mode.
- Multi-shot stack level, from 0 to 4 when applicable.
- Speed Up remaining time when active.
- Weapon power-up remaining time when active.
- Stage identifier.
- Remaining stage time during Stage 1 and Stage 2.
- Boss health during Stage 3.

When Speed Up and a weapon are active together, both effects and both timers
are displayed.

### 17.3 Pause Menu

The pause overlay displays the direct keys for:

- Resume.
- Restart Game.
- Main Menu.
- Quit.

### 17.4 End Screens

- Game-over screen displays a prompt to press Enter for the main menu.
- Victory screen displays a win message.
- The victory prompt becomes active after 3 seconds.
- End screens do not use arrow-key selection.

## 18. Animation And Visual Feedback

### 18.1 Player

- The player continuously plays a swimming or moving animation, including
  while no movement key is held.
- Moving and shooting uses a shooting animation.
- Taking damage produces a red flash.
- Heal produces a temporary green flash.
- Death uses a dedicated death animation.
- Active power-ups recolor the fish and bubbles according to Section 7.2.

### 18.2 Enemies

- Every major enemy has at least a swimming or moving animation.
- Octopus includes a throwing animation.
- Swordfish includes a charging state.
- Enemies with no special attack may use one continuous movement animation.

### 18.3 Boss

The boss includes:

- Continuous idle or swimming animation.
- Bubble Laser charge and firing states.
- Bite warning and bite states.
- Summon state.
- Phase-two tint.
- Death animation.

### 18.4 Other Effects

- Mines use an explosion effect when triggered.
- Bomber fish use the same explosion system when detonating.
- Coral, walls, and background decorations are not required to have character
  animation.
- Final pixel-art interpolation and image-smoothing rules remain to be
  selected after the final assets are tested in the game.

## 19. Audio

- The game must retain a reusable audio playback or audio-manager capability.
- Required audio categories are:
  - Title music.
  - Stage 1 music.
  - Stage 2 music.
  - Boss music.
  - Shooting sound.
  - Player damage sound.
  - Enemy defeat sound.
  - Explosion sound.
  - Power-up collection sound.
  - Victory sound.
- Final audio assets and exact playback behavior remain to be selected.
- Scene transitions stop audio that belongs only to the previous scene.

## 20. Asset References

The project idea dump identifies these approved asset sources:

- Player fish sprites:
  <https://opengameart.org/content/cute-fish-sprites>
- Enemy and boss sprites:
  <https://craftpix.net/freebies/octopus-jellyfish-shark-and-turtle-free-sprite-pixel-art/>
- Boss bomber fish sprites:
  <https://craftpix.net/freebies/free-underwater-enemies-pixel-art-character-pack/>
- Explosion sprites:
  <https://opengameart.org/content/ring-explosion>
- Stage 1 background:
  <https://opengameart.org/content/underwater-mines-pixel-background>
- Stage 2 background:
  <https://opengameart.org/content/underwater-diving-pack>

Asset acquisition and license review have already been handled by the team.

## 21. Configurable Gameplay Values

The following values must be easy to tune in code (and not limited to these values):

- Viewport width and height.
- Global render scale.
- Global collision-bound scale.
- Target update rate.
- World-scroll speed.
- Stage durations.
- Development stage durations.
- Scene-transition mode.
- Player maximum health.
- Player base movement speed.
- Damage invincibility duration.
- Bubble speed, size, damage, cooldown, and on-screen limit.
- Speed Up levels and duration.
- Multi-shot duration, stack limit, burst size, and burst interval.
- Mega-shot duration, size, damage, and cooldown.
- Split-shot duration and vertical spacing.
- Power-up sine-wave amplitude and frequency.
- Enemy health, speed, damage, score, and attack frequency.
- Random spawn cooldowns, probabilities, types, and bounds.
- Mine damage and explosion radius.
- Wall contact-damage toggle and damage.
- Boss health, phase threshold, cooldowns, attack weights, and attack damage.
- Laser charge time, firing time, and bubble interval.
- Bite warning, movement speed, and recovery.
- Bomber count, movement speed, timer range, and explosion radius.
- Parallax layer count and scroll multipliers.

## 22. Open Content Decisions

These decisions remain intentionally open and do not change the required
gameplay:

- Final title image.
- Final team-member text.
- Collision-map file encoding.
- Tile dimensions and numeric tile codes.
- Final image interpolation and pixel-scaling style.
- Final music and sound-effect assets.
- Final sprite dimensions and collision-scale tuning.

These items must be resolved before the relevant content is considered final.
