# Ocean Invaders Level Format

Each scripted stage is stored in two CSV files under `src/levels/`:

```text
scene1-terrain.csv
scene1-events.csv
scene2-terrain.csv
scene2-events.csv
```

Blank lines and lines beginning with `#` are ignored.

## Timing And Coordinates

| Property | Value |
|---|---:|
| Viewport | 716 by 700 px |
| Game rate | 60 ticks per second |
| World scroll speed | 2 px per tick |
| Terrain cell | 50 by 50 px |
| Terrain rows | 14 |
| Five-minute stage | 18,000 ticks |
| Five-minute terrain width | 735 columns |

The terrain width includes enough world space for the initial viewport:

```text
columns = ceil((stageTicks * scrollSpeed + viewportWidth) / tileSize)
```

At tick `T`, the left side of the viewport is at world X `T * 2`.

## Terrain CSV

The terrain file is a rectangular 14-row integer grid. Every row must have the same
number of columns.

```text
# positive tile ID = anchor, -1 = covered cell
0,0,0,0,0
3,0,10,-1,0
-1,0,-1,-1,0
```

Cell meanings:

| Value | Meaning |
|---:|---|
| `0` | Empty water |
| `1` through `25` | Tile anchor and terrain tile ID |
| `-1` | Cell covered by a multi-cell tile anchored above or to the left |

A 1x1 tile is stored as its positive ID only. A 2x2 tile stores its positive ID in the
top-left cell and `-1` in the other three cells:

```text
10,-1
-1,-1
```

Only positive anchors are rendered because their image covers the full footprint.
The footprint cells reserve that placement area in the CSV, but runtime collision is
split into 10 by 10 pixel blocks and derived from the opaque parts of the tile image.
Transparent corners and gaps inside sloped or irregular tiles are therefore not solid.

### Tile Registry

| ID | Tile | Cells |
|---:|---|---:|
| 1 | Ceiling A | 2x1 |
| 2 | Ceiling B | 2x1 |
| 3 | Wall Left A | 1x2 |
| 4 | Wall Right A | 1x2 |
| 5 | Wall Left B | 1x2 |
| 6 | Wall Right B | 1x2 |
| 7 | Wide Ceiling | 4x1 |
| 8 | Ceiling Corner Right | 2x1 |
| 9 | Ceiling Corner Left | 2x1 |
| 10 | Large Slope Left | 2x2 |
| 11 | Large Slope Right | 2x2 |
| 12 | Inner Corner TL | 1x1 |
| 13 | Inner Corner TR | 1x1 |
| 14 | Inner Corner BL | 1x1 |
| 15 | Inner Corner BR | 1x1 |
| 16 | Large Ceiling Left | 2x2 |
| 17 | Large Ceiling Right | 2x2 |
| 18 | Small Rock TL | 1x1 |
| 19 | Small Rock TR | 1x1 |
| 20 | Hanging Island | 4x4 |
| 21 | Small Rock BL | 1x1 |
| 22 | Small Rock BR | 1x1 |
| 23 | Dark Fill | 1x1 |
| 24 | Wall B Up | 2x1 |
| 25 | Wall B Down | 2x1 |

Atlas source rectangles and standalone sprite paths are defined once in Java
`TileRegistry` and once in editor `config.js`. Their IDs, image sources, and footprints
must stay identical.

## Events CSV

Each line is one event:

```text
# tick,type,x,y
180,Jellyfish,756,180
180,Turtle,756,420
600,PowerUp-Speed,746,300
900,SnakeTop,420,0
```

| Column | Meaning |
|---|---|
| `tick` | Stage tick when the entity is created |
| `type` | Exact case-sensitive event type |
| `x` | Initial screen X |
| `y` | Initial screen Y |

Multiple events can use the same tick. The runtime stores a list for each tick and
creates every event in that list.

### Event Types

Scene 1 supports:

```text
Jellyfish
Turtle
Mine
PowerUp-Speed
PowerUp-Multi
PowerUp-Mega
PowerUp-Split
PowerUp-Heal
```

Scene 2 supports:

```text
Jellyfish
Turtle
Octopus
Swordfish
SnakeTop
SnakeBottom
Coral
PowerUp-Speed
PowerUp-Multi
PowerUp-Mega
PowerUp-Split
PowerUp-Heal
```

For normal spawns, X is the configured right-edge position and Y controls the vertical
spawn position. For a Snake, X controls where it enters from the top or bottom. The
Snake constructor determines Y from `SnakeTop` or `SnakeBottom`.

## Runtime Ownership

`LevelLoader` parses and validates both files. `TileMap` renders and collides with the
terrain. `SpawnManager` creates all timed events when scripted mode is active.

Random mode remains available for development and does not use terrain or events for
gameplay.
