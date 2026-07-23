
**Topic** [[CSX4515 Game Design And Development]]
**Tags** #project #java 
**Date** 23-07-2026
___
## Theme

Underwater scenes. Main character is a fish, and it's fighting different enemies by shooting out bubbles at them.

##### Player & Powerups
Cute fish, attacking with bubbles. Default colour is **orange**, change colour based on the powerup it picks up:

- **Speed up** (pink) - increase movement speed in every direction
- **Multi-shot** (purple) - shoot 4 smaller bubbles in a row instead of just 1 normal
- **Mega-shot** (yellow) - slightly higher cooldown (10-15% more), larger bubble
- **Split-shot** (light-purple) - shoot 3 smaller bubbles (off-set) in a split pattern
- **Heal** (flash green) - heal 1 hp

Instead of going in a straight line, powerups will move in a sin curve (up and down slightly) as if floating in the water.

Flash **red** for a few frames when taking damage.

Player can move in 4 directions anywhere on the sceen, but the screen will scroll to the right at a constant speed. Player can shoot bubbles in only 1 direction (to the right). Player has 5 hp (adjustable variable)

##### Obstacles
- **Mine** - stationary, explodes on contact with anything (any projectiles or sprites)
- **Coral** - breakable, does damage only to player
- **Walls** - do damage to player when touched

##### Enemies
- **Octopus** - moves up & down and randomly throws rocks at the player.
- **Swordfish** - aims for the player and comes zooming at it.
- **Jellyfish** - floats around aimlessly, doesn't track the player.
- **Turtle** - swims towards the player at a medium speed. It's tanky.
- **Snake** - comes from a wall, moves up/down instead of of sideways.

##### Boss
Anglerfish, scaled up to take up 2/3 of the screen. Has 3 main attacks:

1. **Bubble lazer** - Open its mouth and shoot a bubble "lazer" at the player's location, track player's location while the lazer is on (move up & down tgt with the lazer tracking the player).
2. **Bite** - Try to bite the player by moving forward to the player's location and opening its mouth.
3. **Summon** - Spawn bomber "kamikaze" fish that track the player and explode once a timer runs out or they touch the player.

##### Sources
- [Player fish sprites](https://opengameart.org/content/cute-fish-sprites)
- [Enemies + boss sprites](https://craftpix.net/freebies/octopus-jellyfish-shark-and-turtle-free-sprite-pixel-art/)
- [Boss summon bomber fish sprites](https://craftpix.net/freebies/free-underwater-enemies-pixel-art-character-pack/)
- [Explosion sprites](https://opengameart.org/content/ring-explosion)
- [Stage 1 background tiles](https://opengameart.org/content/underwater-mines-pixel-background)
- [Stage 2 background tiles](https://opengameart.org/content/underwater-diving-pack)

___
## Stages
##### Stage 1 - Minefield
>Mine background with the ground & some objects from the other file

- Mines all over the place
- Only 2 enemies - turtle and jellyfish

##### Stage 2 - Deep Caves
>Cave background with ceiling, floor and background objects

- All 5 enemies
- Corals on the ground

##### Stage 2.1 or 3 - Boss Fight
>Deep caves with floor, ceiling & right wall (no longer scrolling)

- Boss spawns
- Boss music
- No more enemies

___
## Classes Needed

- **Sprite** - superclass for all sprites, acts like an abstract class but isn't
- **Player** - subclass of sprite, player fish
- **Enemy** - subclass of sprite, superclass for all enemies. Handles player hurt behaviour
- **Turtle** - subclass of enemy
- **Jellyfish** - subclass of enemy
- **Swordfish** - subclass of enemy
- **Octopus** - subclass of enemy
- **Snake** - subclass of enemy
- **Anglerfish (boss)** - subclass of enemy
- **Bubble** - subclass of sprite, shots in this game, used by player & boss
- **Explosion** - subclass of sprite, explosions caused by mines & bomber fish
- **Tile** - 

___