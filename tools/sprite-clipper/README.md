# Ocean Invaders Sprite Clipper

Browser-based frame clipping tool for the animation system in `gdd.sprite.Sprite`.
It produces the `List<Rectangle>` declarations accepted by
`Sprite.setAnimationFrames()`.

Open `index.html` in a browser. There is no build step and no server.

## Workflow

1. Open or drag in a sprite sheet.
2. Generate an equal strip/grid, or use Frame to draw custom rectangles.
3. Move, resize, and reorder frames.
4. Check the animation in the fixed render-size preview.
5. Copy or download the generated Java declaration.

## Controls

| Input | Action |
|---|---|
| Mouse wheel | Zoom around the pointer |
| Middle mouse drag | Pan |
| Space + drag | Pan |
| Shift + click | Add or remove a frame from the selection |
| Arrow keys | Nudge selected frames by one pixel |
| Shift + arrow keys | Nudge selected frames by ten pixels |
| `Delete` or `Backspace` | Delete selected frames |
| `Ctrl/Cmd+Z` | Undo |
| `Ctrl/Cmd+Shift+Z` | Redo |
| `Ctrl/Cmd+S` | Save the editable JSON project |

The JSON project embeds the loaded image so it can be reopened without locating the
original PNG. Java export contains only the ordered source rectangles.

## Unequal Frames

The tool allows frames with different dimensions. The current game stretches every
cropped frame into the sprite's fixed render dimensions, so unequal clips can visibly
change the scale of the artwork between frames. The animation preview uses the same
fixed-size behavior to make that problem visible before exporting.
