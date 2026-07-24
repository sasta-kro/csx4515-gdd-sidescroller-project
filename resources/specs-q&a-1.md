
Reply using the IDs below. Short answers are fine. Use `TBD` where the team has not decided, and I will preserve it as an open decision.

**Document And Team**

- `D1` What should the game be called?
- `D2` Where should the finished document be stored, and what filename should it use?
- `D3` Should this be one combined game design and technical specification, or separate design and implementation documents?
- `D4` Who are the team members, and what roles or responsibilities does each person have?
- `D5` Should the document distinguish required features, planned features, and optional stretch goals?
- `D6` Which idea-dump features are definitely required for your submission, and which can be cut if time runs short?

**Game Fundamentals**

- `G1` Give me the intended game in one or two sentences. What should the player feel while playing?
- `G2` What resolution and frame rate should the game target?
- `G3` What are the exact controls for movement, shooting, pausing, and restarting?
- `G4` Can the fish move freely in all four directions and diagonally?
- `G5` Does the world scroll automatically from right to left, or does scrolling depend on player movement?
- `G6` Is the player locked to a section of the screen, or free to move across the entire viewport?
- `G7` How many hit points and lives does the player start with?
- `G8` After taking damage, does the player receive temporary invincibility, knockback, or both? For how long?
- `G9` What causes game over, and can the player retry the current stage or only restart the whole game?
- `G10` What is the complete scene flow from launching the game through winning or losing?

**Stages And Maps**

- `S1` Is the boss fight a separate third stage, or the final non-scrolling section of Stage 2?
- `S2` Must Stage 1 and Stage 2 each provide five minutes of play? How long should the boss fight last?
- `S3` Should stages have fixed lengths, fixed timers, or finish when the player reaches a particular map position?
- `S4` What marks the end of Stage 1, and how does the game transition into Stage 2?
- `S5` Should Stage 2 contain all five enemy types simultaneously or introduce them gradually?
- `S6` Should maps and spawn schedules remain Java arrays, load from external files, or use a mixture?
- `S7` What tile size and tile types are needed for caves, walls, coral, mines, decorations, and empty water?
- `S8` Do you want parallax background layers, or one scrolling background per stage?

**Combat And Scoring**

- `C1` What are the base bubble’s damage, size, speed, maximum on-screen count, and firing cooldown?
- `C2` Can the player hold the fire key for automatic shooting, or must each shot use a separate press?
- `C3` What happens when a projectile leaves the screen or hits an obstacle?
- `C4` How is score earned, and how many points should each enemy, obstacle, and boss award?
- `C5` Does touching every enemy damage the player? Does it also damage or destroy the enemy?
- `C6` Should collisions use each sprite’s full image bounds or smaller custom hitboxes?

**Power-Ups**

- `P1` Are power-ups permanent for the current run, temporary, or lost when the player takes damage or dies?
- `P2` Can speed and weapon upgrades exist together, or can the player carry only one power-up?
- `P3` What are the exact values for the required two speed-up levels?
- `P4` What are the exact four multi-shot levels required by the assignment?
- `P5` Does “four smaller bubbles in a row” mean four simultaneous parallel bubbles, a rapid four-shot burst, or something else?
- `P6` Are Mega-shot and Split-shot separate weapon modes, stages of the four-level shot upgrade, or additional optional upgrades?
- `P7` If weapon modes are separate, what happens when the player collects a different weapon?
- `P8` What are the exact Mega-shot size, damage, and cooldown values?
- `P9` What angles or vertical offsets should Split-shot use?
- `P10` What is the player’s maximum health, and can Heal exceed it?
- `P11` How do power-ups enter the stage: fixed map positions, timed spawns, enemy drops, or random spawns?
- `P12` How long do uncollected power-ups remain, and how large should their sine-wave movement be?
- `P13` Does the fish remain recolored while an upgrade is active? What color appears when multiple upgrades are active?

**Enemies And Obstacles**

- `E1` For each enemy, what are its health, movement speed, contact damage, projectile damage, and score value?
- `E2` How frequently does the octopus throw rocks, and do rocks aim at the player or travel straight?
- `E3` Does the swordfish telegraph its charge before accelerating? What happens after it misses?
- `E4` How should the jellyfish’s aimless movement be constrained so it stays on-screen and remains fair?
- `E5` Does the turtle simply move toward the player, or does it have another attack?
- `E6` Where does the snake attach, how far does it move vertically, and can it be killed?
- `E7` How should enemy quantities and spawn difficulty increase during each stage?
- `O1` What triggers a mine, what is its explosion radius and damage, and can mines cause chain reactions?
- `O2` How much health does coral have, and does breaking it award score or reveal anything?
- `O3` Do walls physically block the player, damage on contact, or both? Can they be destroyed?

**Boss**

- `B1` How much health does the Anglerfish have, and does it use health-based phases?
- `B2` How are its three attacks selected, and what cooldown separates them?
- `B3` How long does Bubble Laser charge, track, and fire? Can the player cross through it or only avoid it vertically?
- `B4` How is the Bite attack telegraphed, and where does the boss return afterward?
- `B5` How many bomber fish can Summon create, how long is their timer, and what is their explosion radius?
- `B6` Can the player damage the boss continuously, or only during vulnerable periods?
- `B7` What happens visually and mechanically when the boss dies?

**Presentation And Technical Scope**

- `V1` Which animation states are required for the player, each enemy, the boss, projectiles, damage, and explosions?
- `V2` Should pixel art use nearest-neighbor scaling with no smoothing?
- `V3` What music and sound effects are required for the title, both stages, boss fight, shooting, damage, pickups, and victory?
- `V4` What exactly must the dashboard display besides score, speed level, shot level, and health?
- `V5` What title image, team names, instructions, and credits should appear in the title scene?
- `T1` Should `Sprite` remain abstract as supplied, or should it become concrete as proposed in the idea dump?
- `T2` How much class renaming is acceptable given the professor’s “do not restructure” rule?
- `T3` Do you want separate `Scene1`, `Scene2`, and boss-scene classes, or the boss contained in Scene 2?
- `T4` Should the specification include class responsibilities, data formats, state diagrams, acceptance tests, and a team task breakdown?
- `T5` Have the listed assets already been downloaded and license-checked, or should asset acquisition and attribution remain required tasks?


---



D1 - Ocean Invaders
D2 - in the same resources dir, call it a project specificatin doc
d3 - just specifications not design/implementation doc yet.
d4 - no need to mention
d5 - no, everything in equal weight. evey will be implemented
d6 - everything will/must be included for our project

g1 - It's like those cool retro side scroller games (like space shooters) but a modern remake take on it and this time it is actually a whole different vibe like oceans and fishes.
g2 - what does the base say?
g3 - usual wsad and arrow keys and pause is esc which will show the pause menu.
g4 - yes. Player can move in 4 directions anywhere on the sceen, but the screen will scroll to the right at a constant speed. Player can shoot bubbles in only 1 direction (to the right). Player has 5 hp (adjustable variable)

g5 - refer to g4
g6 - entire viewport within bounds.
g7 - starts with 5 (max). but this kind of question should be a clear adjustable variable so it doesn't really matter. we can tweak and change anyways.
g8 - no invincibility nor knockback
g9 - game over caused by death. player can only retry from the starting stage. game also ends when boss is defeated at the last stage. (but in the start menu, there is an option to start from stage 2 for dev and inspection purposes)

g10 - start with title/start menu (options: start game, start from 2nd stage, quit game) -> game scene starts (stage1) -> 2 second delay before enemies spawn after getting into game scene -> when stage 1 is over (survive for a certain amount of time--dev adjustable variable) -> transition to scene2 (should be adjustable like sudden teleport to new scene or new scene tile/background spawns adjacent to the old tile/background and scrolls in the same way) -> scene2 starts -> after certain amout of time (adjustable) boss spawns -> when boss spawns, bg changes to a static bg -> boss fight (boss has adjustable hit points variable starts with 5hp) -> after boss dies -> game win screen -> goes back to main menu

s1 - implemented as 3rd stage but looks to the player as a continuation of stage 2.

s2 - duration of each stage must be a time variable. so there are 2 enemy spawn modes of the game (dev adjustable by code, not adjustable by player). first is scripted mode where the game's spawner manager has a template of scripted enemy spawns with spawn locations & time. second is where the game's spawner manager uses a random enemy spawn (implementation adjustable such as cool down, types, bounds, etc)

s3 - fixed timer/length in scripted mode. and for random mode, it is also fixed with a time variable.

s4 - timer. easy transition is sudden teleport.

s5 - immediately.

s6 - map is load from external file, spawn schedules should be java code (so its more readable like what class spawns what time, location, etc).

s7 - to be decided. as for the full background, there are texture image pngs. the bg will be independent from the obstacles.

s8 - there will be parallax as the bg has 2 or 3 layers.

c1 - base dmg is 1 hit point. other stats variable adjustable. make them sane values for the start.
c2 - player can spam or hold. there will be shot cooldowns

c3 - disappears/dies
c4 - every enemy has a certain score that is dev adjustable. use sane values
c5 - yes it costs a hp for player, enemy just scrolls past the player to the left and player doesn't get the point for that enemy cuz plyaer didn't kill it

c6 - full bounds for simplicity sake. the bounds should be wrapped with a wrapper function so they can be adjustable or scalable like a scaling factor. this goes for texture rendering as well. should be wrapped so that the scaling is adjustable in the wrapper function.

p1 - timed temp. time is dev adjustable.
p2 - only 1 one weapon power up at the same time. except hp powerup cuz it just heals and flashes a heal animation for a few frames. each powerup will change the player fish's colour and shot/bubble color (cuz player fish will shoot bubbles)

p3 - variables, sane.

p4 - only speed and multi shot can be stackable, speed 2 stacks max and multishot 4 stacks max (each stack adding 1 extra bubble).

p5 - rapid 4 shot bursts

p6 - separate weapon upgrades, non stackable, also timed.

p7 - new one overrides old one

p8 - adjustable variables, use sane values at the start

p9 - parallel vertical offsets. no angles.

p10 - heal cannot excced.

p11 - depends on whether scripted or random spawn mode. no enemy drops. random spawns from the right and moves along with the map flow to the left while moving up and down a lil bit like its bouyancing in the water.

p12 - sine wave movement is variable adjustable. use sane values. uncollected powerups stay and disappears/dies when goes offscreen.

p13 - colour is overridden with latest powerup color. if expires, change back to default color. but when heal powerup is collected, it flashes green and goes back to the colour of whatever powerup it is currently holding.

e1 - contatct damage all the same as 1 hp dmg, projectile is only shot by octopus and boss and each projectile has 1 dmg. health and speed are adjustable varibles for each enemy type.

e2 - variable frequency, flies straight.

e3 - swordfish has a "charging" state where it delays before charging. when it misses and goes offscreen, it dies/disappears and removed.

e4 - just a variable adjustable up and down movement (also moves to the left with the map ofc) but it doesn't go off screen unless it is the left bound where it just treated as any other enemy where it just disappears without giving score to the player.

e5 - just moves towards. the gimmck of the turtle is it is tanky

e6 - it can be killed. it appears from the bottom/top and the moves towards the opposite direction until it goes offscreen, if player kills it, player gets score, but if not, it just disappears offscreen and no score.

e7 - they dont increased. either scripted or adjustable random spawns in the spawn manager

o1  - mine triggers when it collides with any entity such as player, enemies, shot/bubble and enemy shots as well. explosion radius adjustable and will be based on explosion sprite size. yes to chain reaction.

o2 - coral has 1 health. there is no score. it just makes it so that player is not obstructed and can move in those tiles now.

o3 - walls block, canont be destoryed. dmg on contact boolean variable adjustable. obstacles such as corals can be destoryed, dmg on contact and the coral gets destroyed after damaging the player.

b1 - variable adjustable. start with 10. at half health, it changes phase where the colour changes to a slightly redder/pinker tint and attack cooldown decreases.

b2 - varibles and random attacks.

b3 - bubble lazer charge time is variable adjustable. use sane values. it doesn't track, it just shoots straight. since it is like shooting multiple bubbles (stream) while moving, after the boss starts shooting, it will moves toward the player for the duration of the lazer. player gets damanged when gets hit by the bubble.

b4 - when performing the bite attack, it start a mouth opening animation and then moves fast towards the left almost until the left edge then goes back to the original spot before the dash-bite.

b5 - so bomber fish movement tracking the player where it goes toward the player's location. timer is random withtin a range (varibles) for each bomber fish. explosion radius wil be based on sprite but adjustable. use sane values.

b6 - can damage continuosly. in the actual game, boss will have like 50+ hp or something.

b7 - boss die animation and then winning screen shows.

v1 - player and boss are like constantly playing moving animation even while idle (cuz they are fish in water) player states are moving, moving & shooting, depends on the enemy cuz most have only 1 state but like octopus can shoot. boss has moving/idle, and a state for each attack. dying will just plays when player or boss dies.

v2 - to be decided.

v3 - to be decided. just have the audio player/manager ready.

v4 - +current power up, current power up's timer countdown.

v5 - to be decided/changed

t1 - sprite remain abstract as supplied

t2 - theme specific things renameable.  can make new classes as well.

t3 - separate scene1, scene2 and boss scene classes.

t4 - nah

t5 - dont worry about assets. already checkd and stuff.

process these. any other questions?

---
Processed. Your answers establish nearly everything needed. The starter currently targets `716 × 700` and updates through a roughly 60 FPS Swing timer. I need these final clarifications before writing the specification.

**Document**

- `F1` Should the filename be `PROJECT_SPECIFICATION.md` inside `resources/`?
- `F2` The professor requires team names on the title scene. Should the specification state this requirement with placeholders but omit actual names?

**Controls And Menus**

- `F3` Which key shoots bubbles?
- `F4` Are menus controlled by keyboard, mouse, or both?
- `F5` What options appear in the pause menu? For example: Resume, Restart Stage, Main Menu, Quit.
- `F6` What options appear on the game-over and victory screens?
- `F7` Does the victory screen return to the main menu automatically, after a timer, or after player input?

**Game State**

- `F8` When moving between stages, do score, health, active power-up, stack levels, and remaining power-up time carry over?
- `F9` When using “Start from Stage 2,” what health, score, speed level, and weapon level does the player receive?
- `F10` When a scene ends, are all remaining enemies, projectiles, power-ups, and obstacles immediately removed?
- `F11` Should scripted spawning or random spawning be the default mode?
- `F12` Should the normal stage duration default to the required five minutes, with a shorter developer value available in code?

**Scrolling**

- `F13` To confirm the wording: the player progresses rightward, while backgrounds, obstacles, enemies, and power-ups visually move left across the screen. Correct?
- `F14` Should the base resolution remain `716 × 700`, or should we specify a different target?

**Damage And Collision**

- `F15` With no invincibility, should an enemy contact remove exactly one HP and then mark that enemy as having already damaged the player? Otherwise one overlap could remove all five HP across several frames.
- `F16` Should walls similarly use a contact-damage cooldown, damage once per continuous contact, or damage every frame?
- `F17` Do player bubbles disappear after hitting the first enemy, or can some bubble types penetrate multiple enemies?
- `F18` Can player bubbles destroy enemy rocks and boss bubbles?
- `F19` Does a mine explosion damage every nearby damageable object, including player, enemies, coral, other mines, and boss summons?
- `F20` If a mine kills an enemy after being triggered by the player’s bubble, does the player receive that enemy’s score?
- `F21` Can enemies and enemy projectiles destroy coral, or only the player and player bubbles?

**Power-Ups**

- `F22` For four multi-shot stacks, is the progression `1 → 2 → 3 → 4 → 5` bubbles, or does level four mean a maximum four-bubble burst?
- `F23` Can a speed upgrade remain active alongside one weapon upgrade, or is only one timed upgrade of any kind allowed?
- `F24` When another copy of the current power-up is collected, does it add a stack and reset the timer?
- `F25` Do all stacks expire together when one shared timer reaches zero?
- `F26` If Mega-shot or Split-shot replaces a stacked Multi-shot, are the Multi-shot stacks discarded or restored after the replacement expires?

**Boss And Presentation**

- `F27` During Bubble Laser, does the boss track only the player’s vertical position while remaining on the right side?
- `F28` Do all boss attacks deal one HP per successful attack, including Bite, laser bubbles, bomber contact, and bomber explosions?
- `F29` Should the initial boss-health constant be `10` for development or `50` for the intended game?
- `F30` The assignment requires all sprites to be animated. Should mines and coral also animate, such as mine pulsing and coral swaying, while walls remain background tiles?

---


f1 - yes
f2 - yes placeholders
f3 - space
f4 - only keyboard
f5 - Resume, Restart Game, Main Menu, Quit.
f6 - press enter to go to main menu
f7 - for the victory screen, there is a 3 second delay before the player can press anything. afterwhcih when player press enter, it goes to main menu

f8 - score, health, and player position carry over. powerups dont carry over.
f9 - just base.
f10 - no enemies, projectiles, floating powerups, nor obstacles persist.
f11 - random default
f12 - yes.
f13 - yes. and player can move freely anywhere. The player isn't actively being moved to the right. The entire background and everything is moving to the left, making it look like the player is moving rightward. The player can move freely and the game never tries to move to the right.

f14 - remain for now but there should be room for change.
f15 - we can add invincibility frames for a counter after taking any damage.
f16 - use the same short invincibility frames.
f17 - no peneration
f18 - cannot.
f19 - yes
f20 - no
f21 - player only.
f22 - 1 → 3 → 4 → 5 -> 6
f23 - speed can remain alongside weapon upgrade.
f24 - yes.
f25 - yes, cuz in the final game, multi shot and speed will get special treatment and have longer duration.
f26 - it resets.
f27 - yes
f28 - yes. but adjustable variable damange for each attack.
f29 - 50
f30 - dont worry about it. not all sprites have to be animated. only the main ones.


---F31 Which keys navigate menus: W/S, Up/Down, or both? Is Enter always the selection key?
F32 When Speed and a weapon are active together, should the HUD show both power-ups with separate timers?
F33 When Speed and a weapon are active together, which one determines the fish and bubble color? If the latest one expires while the other remains active, should the color return to the remaining power-up’s color?
F34 Should sudden scene replacement be the default transition, with seamless adjacent-background scrolling available through a code setting?


f31 - sigh, just make the menus map to different keys, no arrow selection/navigation on the menus.
f32 - yes
f33 - speed is secondary so weapon powerup color take precedence. when the latest one expires while the other remains active, the color should return to the remaining one.
f34 - yes.


---