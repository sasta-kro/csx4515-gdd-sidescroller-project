# Ocean Invaders

Ocean Invaders is an underwater side-scrolling shooter made for the CSX4515
Game Design and Development pre-midterm project.

The player controls a fish, fires bubbles, collects temporary power-ups, and
survives two scrolling stages before fighting an Anglerfish boss.

## Team

- [Name]
- [Name]
- [Name]

## Controls

| Action | Keys |
| --- | --- |
| Move | WASD or Arrow Keys |
| Shoot | Space |
| Pause or resume | Escape |
| Restart from pause | R |
| Main menu from pause | M |
| Quit from a menu | Q |
| Confirm an end screen | Enter |

The title menu uses direct keys:

- `1`: Start from Stage 1
- `2`: Start from Stage 2 for development
- `Q`: Quit

## Run From The Command Line

Compile from the repository root:

```bash
javac -d out $(find src/gdd -name '*.java')
```

Run:

```bash
java -cp out gdd.Main
```

The project can also be opened and run from IntelliJ IDEA.

## Project Documents

- [Project specification](resources/PROJECT_SPECIFICATION.md)
- [Implementation plan](resources/IMPLEMENTATION_PLAN.md)
- [Original idea dump](resources/Side-Scroller%20Project%20Ideadump.md)

## Asset References

- Player fish sprites:
  <https://opengameart.org/content/cute-fish-sprites>
- Enemy and boss sprites:
  <https://craftpix.net/freebies/octopus-jellyfish-shark-and-turtle-free-sprite-pixel-art/>
- Bomber fish sprites:
  <https://craftpix.net/freebies/free-underwater-enemies-pixel-art-character-pack/>
- Explosion sprites:
  <https://opengameart.org/content/ring-explosion>
- Stage 1 background:
  <https://opengameart.org/content/underwater-mines-pixel-background>
- Stage 2 background:
  <https://opengameart.org/content/underwater-diving-pack>

The starter code is based on
[mchayapol/gdd-space-invaders-project](https://github.com/mchayapol/gdd-space-invaders-project),
which references the
[Java Space Invaders project](https://github.com/janbodnar/Java-Space-Invaders).
