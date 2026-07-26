import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import vm from "node:vm";

const html = readFileSync(new URL("index.html", import.meta.url), "utf8");
const configSource = readFileSync(new URL("config.js", import.meta.url), "utf8");
const scene1Terrain = readFileSync(
  new URL("../../src/levels/scene1-terrain.csv", import.meta.url),
  "utf8",
);
const scene1Events = readFileSync(
  new URL("../../src/levels/scene1-events.csv", import.meta.url),
  "utf8",
);
const tileRegistry = readFileSync(
  new URL("../../src/gdd/TileRegistry.java", import.meta.url),
  "utf8",
);
const scriptMatch = html.match(
  /<script src="config\.js"><\/script>\s*<script>([\s\S]*)<\/script>\s*<\/body>/,
);

assert.ok(scriptMatch, "editor script was not found");

function canvasContext() {
  return new Proxy(
    { imageSmoothingEnabled: false },
    {
      get(target, property) {
        if (!(property in target)) target[property] = () => {};
        return target[property];
      },
    },
  );
}

function element(id = "") {
  const listeners = new Map();
  return {
    id,
    value: id === "zoom" ? "0.3" : id === "stage" ? "scene1" : "0",
    style: {},
    className: "",
    innerHTML: "",
    textContent: "",
    disabled: false,
    width: 716,
    height: 700,
    classList: {
      add() {},
      remove() {},
      toggle() {},
    },
    append() {},
    appendChild() {},
    click() {},
    setPointerCapture() {},
    addEventListener(type, listener) {
      listeners.set(type, listener);
    },
    getBoundingClientRect() {
      return { left: 0, top: 0, width: this.width, height: this.height };
    },
    getContext() {
      return canvasContext();
    },
  };
}

const elements = new Map();
const document = {
  getElementById(id) {
    if (!elements.has(id)) elements.set(id, element(id));
    return elements.get(id);
  },
  createElement(tag) {
    return element(tag);
  },
  createTextNode(text) {
    return { textContent: text };
  },
  querySelectorAll() {
    return [];
  },
};

class FakeImage {
  complete = true;
  width = 100;
  height = 100;
  set src(value) {
    this.path = value;
  }
}

const context = vm.createContext({
  console,
  document,
  window: {
    addEventListener() {},
  },
  Image: FakeImage,
  Int16Array,
  Uint8Array,
  Math,
  Number,
  Object,
  String,
  Array,
  Map,
  Set,
  Blob,
  URL,
  setTimeout,
  clearTimeout,
  alert() {},
});

const tests = `
assert.ok(columns === 735);

placeTile(2, 3, 10);
assert.equal(tileAt(2, 3), 10);
assert.equal(tileAt(2, 4), -1);
assert.equal(tileAt(3, 3), -1);
assert.equal(tileAt(3, 4), -1);
eraseTile(3, 4);
assert.equal(tileAt(2, 3), 0);
assert.equal(tileAt(3, 4), 0);

selectedEventType = "Jellyfish";
placeEvent(tickToWorldX(120), 200);
placeEvent(tickToWorldX(120), 300);
assert.equal(events.length, 2);
assert.equal(events[0].tick, 120);
assert.equal(events[1].tick, 120);
assert.match(eventsCsv(), /120,Jellyfish,756,152/);

document.getElementById("stage").value = "scene2";
selectedEventType = "SnakeTop";
placeEvent(tickToWorldX(480), 100);
assert.equal(events[2].type, "SnakeTop");
assert.equal(events[2].x, 420);
assert.equal(events[2].y, 0);

placeTile(4, 8, 20);
const roundTrip = parseTerrain(terrainCsv());
assert.equal(roundTrip.width, 735);
assert.equal(roundTrip.values[4 * 735 + 8], 20);
assert.equal(roundTrip.values[7 * 735 + 11], -1);

placeTile(0, 731, 7);
document.getElementById("columns").value = "733";
resizeLevel();
assert.equal(columns, 733);
assert.equal(tileAt(0, 731), 0);

assert.throws(
  () => parseTerrain(Array.from({ length: 14 }, (_, row) =>
    row === 0 ? "10,0" : "0,0").join("\\n")),
  /incomplete footprint/,
);
assert.throws(() => parseEvents("10,Unknown,0,0"), /Unknown event type/);
assert.equal(previewEventY(
  { type: "SnakeBottom", y: 700 },
  TYPE_BY_NAME.SnakeBottom,
), 604);

const fixtureTerrain = parseTerrain(scene1Terrain);
const fixtureEvents = parseEvents(scene1Events);
assert.equal(fixtureTerrain.width, 735);
assert.equal(fixtureEvents.length, 6);

for (const match of tileRegistry.matchAll(
  /add\\((\\d+), "[^"]+", (\\d+), (\\d+), (\\d+), (\\d+), (\\d+), (\\d+)\\);/g,
)) {
  const [, id, sx, sy, sw, sh, wide, high] = match.map(Number);
  const editorTile = TILES[id];
  assert.ok(editorTile, \`Java tile \${id} is missing from the editor\`);
  assert.deepEqual(editorTile.source, [sx, sy, sw, sh]);
  assert.deepEqual(editorTile.footprint, [wide, high]);
}
assert.equal(Object.keys(TILES).length, 22);
`;

context.assert = assert;
context.scene1Terrain = scene1Terrain;
context.scene1Events = scene1Events;
context.tileRegistry = tileRegistry;
vm.runInContext(
  `${configSource}\n${scriptMatch[1]}\n${tests}`,
  context,
  { filename: "level-editor.js" },
);

console.log("Level editor tests passed");
