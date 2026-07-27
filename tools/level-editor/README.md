# Ocean Invaders Level Editor

Static visual editor for the scripted Ocean Invaders stages and the fixed boss room.
It edits terrain and timed spawn events without running or simulating the game.

Open `index.html` in a browser. There is no build step and no server.

## Workflow

1. Select Scene 1, Scene 2, or Boss Room.
2. Use Terrain to paint tiles from the real `tiles.png` atlas.
3. Use Enemies, Obstacles, and Powerups to place spawn events.
4. Select an event marker to edit its tick and vertical position.
5. Use Viewport Preview to inspect the exact 716 by 700 game viewport at a tick.
6. Set the file prefix, save the CSV files, and place them in `src/levels/`.

The editor downloads:

```text
scene1-terrain.csv
scene1-events.csv
```

The same names are used for Scene 2.

Boss Room downloads only:

```text
boss-terrain.csv
```

The boss room is a static 15-column viewport. It has no duration, horizontal scroll,
or scripted event file. The game creates the boss and random boss-fight powerups in
`BossScene`, so the editor exposes only terrain tools in this mode.

For Scene 1 and Scene 2, the Duration field controls the editor timeline and required
terrain width. Loading a terrain file infers its duration. A 60-second level uses 159
columns.

The file prefix is preserved when files are loaded. Loading
`scene1-test-terrain.csv` and `scene1-test-events.csv` therefore saves back to the same
test filenames instead of replacing the normal Scene 1 files.

## Views

World Layout shows the whole scrolling level as a horizontal timeline. The x axis is
world distance and time. The y axis is the game screen height. Event markers sit at
the time when they spawn. In Boss Room mode it instead shows the fixed viewport grid.

Viewport Preview shows the game background and terrain at one selected tick. Boss Room
always previews tick zero because its camera does not move. This is a static preview.
It does not estimate movement paths or run enemy behavior.

## Terrain

The logical grid uses 50 px cells and always has 14 rows. A tile can occupy more than
one cell. Click or drag to paint. Erasing any part of a multi-cell tile removes the
whole tile.

The default 735 columns cover five minutes, including the initial 716 px viewport.
Boss Room is locked to 15 columns, which covers its single 716 px viewport.

## Events

Normal enemies, obstacles, and powerups spawn at their configured right-edge X
position. Their Y position is selected in the World Layout and can be changed in the
event inspector.

Snakes are different. Their top or bottom side is part of the event type, and their
screen X position is editable. Select a Snake and drag it horizontally in Viewport
Preview, or enter its X position in the inspector.

In World Layout, a Snake marker represents the terrain position where the center of
the Snake will emerge. Its spawn tick is calculated from both that terrain position
and the Snake's screen X. Other enemy markers still represent the moment they reach
the right edge of the viewport.

Multiple events may use the same tick.

## Controls

| Input | Action |
|---|---|
| Left click or drag | Paint terrain |
| Right click | Erase terrain |
| Click in an event tab | Place or select an event |
| Erase, then click an event | Delete the event |
| `Delete` or `Backspace` | Delete the selected event |
| `Ctrl/Cmd+Z` | Undo |
| `Ctrl/Cmd+Shift+Z` | Redo |
| `Ctrl/Cmd+S` | Download the current stage files |

## Files

`config.js` contains tile atlas regions, tile footprints, event types, and image paths.
`index.html` contains the editor interface and logic. `LEVEL_FORMAT.md` defines the
file contract shared with the Java runtime.

Run the editor model tests from the project root:

```bash
node tools/level-editor/test-editor.mjs
```
