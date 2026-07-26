(() => {
    "use strict";

    const $ = (selector) => document.querySelector(selector);
    const $$ = (selector) => [...document.querySelectorAll(selector)];

    const editorCanvas = $("#editor-canvas");
    const editorContext = editorCanvas.getContext("2d");
    const previewCanvas = $("#preview-canvas");
    const previewContext = previewCanvas.getContext("2d");
    const canvasShell = $("#canvas-shell");

    const state = {
        image: null,
        imageName: "",
        imageDataUrl: "",
        frames: [],
        nextFrameId: 1,
        selectedIds: new Set(),
        guides: { vertical: [], horizontal: [] },
        tool: "select",
        zoom: 1,
        offsetX: 0,
        offsetY: 0,
        drag: null,
        spacePressed: false,
        history: [],
        future: [],
        previewPlaying: true,
        previewFrame: 0,
        previewLastTime: 0,
        toastTimer: null
    };

    const HANDLE_NAMES = [
        "nw", "n", "ne", "e", "se", "s", "sw", "w"
    ];

    function cloneFrames(frames = state.frames) {
        return frames.map((frame) => ({ ...frame }));
    }

    function snapshot() {
        return {
            frames: cloneFrames(),
            guides: {
                vertical: [...state.guides.vertical],
                horizontal: [...state.guides.horizontal]
            },
            selectedIds: [...state.selectedIds],
            nextFrameId: state.nextFrameId
        };
    }

    function restore(saved) {
        state.frames = cloneFrames(saved.frames);
        state.guides = {
            vertical: [...saved.guides.vertical],
            horizontal: [...saved.guides.horizontal]
        };
        state.selectedIds = new Set(saved.selectedIds);
        state.nextFrameId = saved.nextFrameId;
        state.previewFrame = Math.min(
                state.previewFrame,
                Math.max(0, state.frames.length - 1)
        );
        refreshAll();
    }

    function edit(change) {
        state.history.push(snapshot());
        state.future = [];
        change();
        refreshAll();
    }

    function undo() {
        if (!state.history.length) {
            return;
        }
        state.future.push(snapshot());
        restore(state.history.pop());
    }

    function redo() {
        if (!state.future.length) {
            return;
        }
        state.history.push(snapshot());
        restore(state.future.pop());
    }

    function showToast(message) {
        const toast = $("#toast");
        toast.textContent = message;
        toast.classList.add("visible");
        clearTimeout(state.toastTimer);
        state.toastTimer = setTimeout(() => {
            toast.classList.remove("visible");
        }, 1800);
    }

    function resizeEditorCanvas() {
        const rect = canvasShell.getBoundingClientRect();
        const ratio = window.devicePixelRatio || 1;
        editorCanvas.width = Math.max(1, Math.round(rect.width * ratio));
        editorCanvas.height = Math.max(1, Math.round(rect.height * ratio));
        editorCanvas.style.width = `${rect.width}px`;
        editorCanvas.style.height = `${rect.height}px`;
        renderEditor();
    }

    function screenSize() {
        const ratio = window.devicePixelRatio || 1;
        return {
            width: editorCanvas.width / ratio,
            height: editorCanvas.height / ratio,
            ratio
        };
    }

    function imageToScreen(x, y) {
        return {
            x: state.offsetX + x * state.zoom,
            y: state.offsetY + y * state.zoom
        };
    }

    function screenToImage(x, y) {
        return {
            x: (x - state.offsetX) / state.zoom,
            y: (y - state.offsetY) / state.zoom
        };
    }

    function pointerPosition(event) {
        const bounds = editorCanvas.getBoundingClientRect();
        return {
            x: event.clientX - bounds.left,
            y: event.clientY - bounds.top
        };
    }

    function snapped(value) {
        return $("#snap-pixels").checked ? Math.round(value) : value;
    }

    function clampImagePoint(point) {
        if (!state.image) {
            return { x: 0, y: 0 };
        }
        return {
            x: Math.max(0, Math.min(state.image.naturalWidth, point.x)),
            y: Math.max(0, Math.min(state.image.naturalHeight, point.y))
        };
    }

    function selectedFrames() {
        return state.frames.filter((frame) => state.selectedIds.has(frame.id));
    }

    function frameBounds(frames) {
        if (!frames.length) {
            return null;
        }
        const left = Math.min(...frames.map((frame) => frame.x));
        const top = Math.min(...frames.map((frame) => frame.y));
        const right = Math.max(...frames.map((frame) => frame.x + frame.width));
        const bottom = Math.max(...frames.map((frame) => frame.y + frame.height));
        return {
            x: left,
            y: top,
            width: right - left,
            height: bottom - top
        };
    }

    function handlePoints(bounds) {
        const left = bounds.x;
        const centerX = bounds.x + bounds.width / 2;
        const right = bounds.x + bounds.width;
        const top = bounds.y;
        const centerY = bounds.y + bounds.height / 2;
        const bottom = bounds.y + bounds.height;
        return {
            nw: { x: left, y: top },
            n: { x: centerX, y: top },
            ne: { x: right, y: top },
            e: { x: right, y: centerY },
            se: { x: right, y: bottom },
            s: { x: centerX, y: bottom },
            sw: { x: left, y: bottom },
            w: { x: left, y: centerY }
        };
    }

    function hitHandle(imagePoint) {
        const bounds = frameBounds(selectedFrames());
        if (!bounds) {
            return null;
        }
        const radius = 8 / state.zoom;
        const points = handlePoints(bounds);
        return HANDLE_NAMES.find((name) => {
            const point = points[name];
            return Math.abs(point.x - imagePoint.x) <= radius
                    && Math.abs(point.y - imagePoint.y) <= radius;
        }) || null;
    }

    function hitFrame(imagePoint) {
        for (let index = state.frames.length - 1; index >= 0; index--) {
            const frame = state.frames[index];
            if (imagePoint.x >= frame.x
                    && imagePoint.x <= frame.x + frame.width
                    && imagePoint.y >= frame.y
                    && imagePoint.y <= frame.y + frame.height) {
                return frame;
            }
        }
        return null;
    }

    function fitImage() {
        if (!state.image) {
            return;
        }
        const size = screenSize();
        const margin = 54;
        state.zoom = Math.min(
                (size.width - margin * 2) / state.image.naturalWidth,
                (size.height - margin * 2) / state.image.naturalHeight
        );
        state.zoom = Math.max(0.05, Math.min(32, state.zoom));
        state.offsetX = (size.width - state.image.naturalWidth * state.zoom) / 2;
        state.offsetY = (size.height - state.image.naturalHeight * state.zoom) / 2;
        renderEditor();
        updateZoomLabel();
    }

    function drawImagePixels(context) {
        const imageOrigin = imageToScreen(0, 0);
        const imageWidth = state.image.naturalWidth * state.zoom;
        const imageHeight = state.image.naturalHeight * state.zoom;

        context.imageSmoothingEnabled = false;
        context.drawImage(
                state.image,
                imageOrigin.x,
                imageOrigin.y,
                imageWidth,
                imageHeight
        );

        if ($("#dim-outside").checked && state.frames.length) {
            context.fillStyle = "rgba(4, 8, 11, 0.62)";
            context.fillRect(imageOrigin.x, imageOrigin.y, imageWidth, imageHeight);

            for (const frame of state.frames) {
                const destination = imageToScreen(frame.x, frame.y);
                context.drawImage(
                        state.image,
                        frame.x,
                        frame.y,
                        frame.width,
                        frame.height,
                        destination.x,
                        destination.y,
                        frame.width * state.zoom,
                        frame.height * state.zoom
                );
            }
        }
    }

    function drawPixelGrid(context) {
        if (!$("#pixel-grid").checked || state.zoom < 6) {
            return;
        }

        const origin = imageToScreen(0, 0);
        const width = state.image.naturalWidth;
        const height = state.image.naturalHeight;
        context.beginPath();
        context.strokeStyle = "rgba(255, 255, 255, 0.12)";
        context.lineWidth = 1;

        for (let x = 0; x <= width; x++) {
            const screenX = Math.round(origin.x + x * state.zoom) + 0.5;
            context.moveTo(screenX, origin.y);
            context.lineTo(screenX, origin.y + height * state.zoom);
        }
        for (let y = 0; y <= height; y++) {
            const screenY = Math.round(origin.y + y * state.zoom) + 0.5;
            context.moveTo(origin.x, screenY);
            context.lineTo(origin.x + width * state.zoom, screenY);
        }
        context.stroke();
    }

    function drawGuides(context, size) {
        context.save();
        context.strokeStyle = "#f0c75e";
        context.lineWidth = 1;
        context.setLineDash([4, 4]);

        for (const x of state.guides.vertical) {
            const screen = imageToScreen(x, 0);
            context.beginPath();
            context.moveTo(Math.round(screen.x) + 0.5, 0);
            context.lineTo(Math.round(screen.x) + 0.5, size.height);
            context.stroke();
        }
        for (const y of state.guides.horizontal) {
            const screen = imageToScreen(0, y);
            context.beginPath();
            context.moveTo(0, Math.round(screen.y) + 0.5);
            context.lineTo(size.width, Math.round(screen.y) + 0.5);
            context.stroke();
        }
        context.restore();
    }

    function drawFrames(context) {
        context.save();
        context.font = "11px ui-monospace, SFMono-Regular, Consolas, monospace";
        context.textBaseline = "top";

        state.frames.forEach((frame, index) => {
            const point = imageToScreen(frame.x, frame.y);
            const width = frame.width * state.zoom;
            const height = frame.height * state.zoom;
            const selected = state.selectedIds.has(frame.id);

            context.strokeStyle = selected ? "#37d5c0" : "#ecf4f7";
            context.lineWidth = selected ? 2 : 1;
            context.setLineDash(selected ? [] : [5, 3]);
            context.strokeRect(
                    Math.round(point.x) + 0.5,
                    Math.round(point.y) + 0.5,
                    Math.round(width),
                    Math.round(height)
            );

            const label = `${index + 1}`;
            const labelWidth = context.measureText(label).width + 8;
            context.fillStyle = selected ? "#20b8a5" : "rgba(10, 15, 19, 0.86)";
            context.fillRect(point.x, point.y, labelWidth, 18);
            context.fillStyle = selected ? "#071714" : "#eef3f6";
            context.fillText(label, point.x + 4, point.y + 3);
        });

        context.restore();
    }

    function drawSelectionHandles(context) {
        const bounds = frameBounds(selectedFrames());
        if (!bounds) {
            return;
        }
        const topLeft = imageToScreen(bounds.x, bounds.y);
        context.save();
        context.strokeStyle = "#37d5c0";
        context.lineWidth = 1;
        context.setLineDash([]);
        context.strokeRect(
                topLeft.x,
                topLeft.y,
                bounds.width * state.zoom,
                bounds.height * state.zoom
        );

        const points = handlePoints(bounds);
        for (const name of HANDLE_NAMES) {
            const point = imageToScreen(points[name].x, points[name].y);
            context.fillStyle = "#eef3f6";
            context.strokeStyle = "#087f72";
            context.lineWidth = 1;
            context.fillRect(point.x - 4, point.y - 4, 8, 8);
            context.strokeRect(point.x - 4, point.y - 4, 8, 8);
        }
        context.restore();
    }

    function drawDraft(context) {
        if (!state.drag || state.drag.type !== "create") {
            return;
        }
        const left = Math.min(state.drag.start.x, state.drag.current.x);
        const top = Math.min(state.drag.start.y, state.drag.current.y);
        const right = Math.max(state.drag.start.x, state.drag.current.x);
        const bottom = Math.max(state.drag.start.y, state.drag.current.y);
        const point = imageToScreen(left, top);

        context.save();
        context.fillStyle = "rgba(32, 184, 165, 0.14)";
        context.strokeStyle = "#37d5c0";
        context.lineWidth = 2;
        context.fillRect(
                point.x,
                point.y,
                (right - left) * state.zoom,
                (bottom - top) * state.zoom
        );
        context.strokeRect(
                point.x,
                point.y,
                (right - left) * state.zoom,
                (bottom - top) * state.zoom
        );
        context.restore();
    }

    function renderEditor() {
        const size = screenSize();
        editorContext.setTransform(size.ratio, 0, 0, size.ratio, 0, 0);
        editorContext.clearRect(0, 0, size.width, size.height);

        if (!state.image) {
            return;
        }

        drawImagePixels(editorContext);
        drawPixelGrid(editorContext);
        drawGuides(editorContext, size);
        drawFrames(editorContext);
        if (state.tool === "select") {
            drawSelectionHandles(editorContext);
        }
        drawDraft(editorContext);
    }

    function frameAtPreviewIndex() {
        if (!state.frames.length) {
            return null;
        }
        state.previewFrame %= state.frames.length;
        return state.frames[state.previewFrame];
    }

    function renderPreview() {
        const width = previewCanvas.width;
        const height = previewCanvas.height;
        previewContext.clearRect(0, 0, width, height);

        const frame = frameAtPreviewIndex();
        if (!state.image || !frame) {
            previewContext.fillStyle = "#758792";
            previewContext.font = "12px system-ui, sans-serif";
            previewContext.textAlign = "center";
            previewContext.fillText("No frames", width / 2, height / 2);
            return;
        }

        const requestedWidth = positiveNumber($("#render-width").value, 144);
        const requestedHeight = positiveNumber($("#render-height").value, 144);
        const fitScale = Math.min(
                1,
                (width - 26) / requestedWidth,
                (height - 26) / requestedHeight
        );
        const drawWidth = requestedWidth * fitScale;
        const drawHeight = requestedHeight * fitScale;
        const x = (width - drawWidth) / 2;
        const y = (height - drawHeight) / 2;

        previewContext.imageSmoothingEnabled = false;
        previewContext.drawImage(
                state.image,
                frame.x,
                frame.y,
                frame.width,
                frame.height,
                x,
                y,
                drawWidth,
                drawHeight
        );
        previewContext.strokeStyle = "rgba(255, 255, 255, 0.38)";
        previewContext.strokeRect(
                Math.round(x) + 0.5,
                Math.round(y) + 0.5,
                Math.round(drawWidth),
                Math.round(drawHeight)
        );
        previewContext.fillStyle = "rgba(9, 14, 18, 0.82)";
        previewContext.fillRect(8, 8, 64, 21);
        previewContext.fillStyle = "#eef3f6";
        previewContext.font = "11px ui-monospace, SFMono-Regular, Consolas, monospace";
        previewContext.textAlign = "left";
        previewContext.fillText(
                `${state.previewFrame + 1} / ${state.frames.length}`,
                15,
                22
        );
    }

    function animatePreview(time) {
        const frameDuration = 1000 / positiveNumber($("#preview-fps").value, 8);
        if (state.previewPlaying
                && state.frames.length > 1
                && time - state.previewLastTime >= frameDuration) {
            state.previewFrame = (state.previewFrame + 1) % state.frames.length;
            state.previewLastTime = time;
            renderPreview();
        }
        requestAnimationFrame(animatePreview);
    }

    function renderFrameList() {
        const list = $("#frame-list");
        list.replaceChildren();

        state.frames.forEach((frame, index) => {
            const card = document.createElement("button");
            card.type = "button";
            card.className = "frame-card";
            card.dataset.frameId = frame.id;
            if (state.selectedIds.has(frame.id)) {
                card.classList.add("selected");
            }

            const canvas = document.createElement("canvas");
            canvas.width = 148;
            canvas.height = 124;
            const label = document.createElement("span");
            label.textContent = `${index + 1} · ${frame.width}×${frame.height}`;
            card.append(canvas, label);
            list.append(card);

            if (state.image) {
                const context = canvas.getContext("2d");
                const scale = Math.min(
                        1,
                        (canvas.width - 10) / frame.width,
                        (canvas.height - 10) / frame.height
                );
                const width = frame.width * scale;
                const height = frame.height * scale;
                context.imageSmoothingEnabled = false;
                context.drawImage(
                        state.image,
                        frame.x,
                        frame.y,
                        frame.width,
                        frame.height,
                        (canvas.width - width) / 2,
                        (canvas.height - height) / 2,
                        width,
                        height
                );
            }
        });
    }

    function updateInspector() {
        const selected = selectedFrames();
        const fields = $("#frame-fields");
        const multiNote = $("#multi-selection-note");
        const badge = $("#selected-frame-number");

        fields.classList.toggle("disabled", selected.length !== 1);
        multiNote.hidden = selected.length < 2;

        if (selected.length === 1) {
            const frame = selected[0];
            const index = state.frames.findIndex((candidate) => candidate.id === frame.id);
            badge.textContent = `Frame ${index + 1}`;
            $("#frame-x").value = frame.x;
            $("#frame-y").value = frame.y;
            $("#frame-width").value = frame.width;
            $("#frame-height").value = frame.height;
        } else {
            badge.textContent = selected.length ? `${selected.length} selected` : "None";
            for (const input of $$("#frame-fields input")) {
                input.value = "";
            }
        }

        const count = state.frames.length;
        $("#selection-summary").textContent = `${count} frame${count === 1 ? "" : "s"}`
                + (selected.length ? ` · ${selected.length} selected` : "");

        const exactlyOne = selected.length === 1;
        const hasSelection = selected.length > 0;
        $("#duplicate-frame").disabled = !hasSelection;
        $("#delete-frame").disabled = !hasSelection;
        $("#move-frame-left").disabled = !exactlyOne;
        $("#move-frame-right").disabled = !exactlyOne;
    }

    function validVariableName(value) {
        const trimmed = value.trim();
        return /^[A-Za-z_$][A-Za-z0-9_$]*$/.test(trimmed)
                ? trimmed
                : "animationClips";
    }

    function javaCode() {
        const name = validVariableName($("#variable-name").value);
        if (!state.frames.length) {
            return `private static final List<Rectangle> ${name} = List.of();`;
        }

        const rectangles = state.frames.map((frame, index) => {
            const ending = index === state.frames.length - 1 ? "" : ",";
            return `        new Rectangle(${frame.x}, ${frame.y}, ${frame.width}, ${frame.height})${ending}`;
        });

        return [
            `private static final List<Rectangle> ${name} = List.of(`,
            ...rectangles,
            ");"
        ].join("\n");
    }

    function updateJavaOutput() {
        $("#java-output").value = javaCode();
    }

    function updatePreviewWarning() {
        const warning = $("#preview-warning");
        if (state.frames.length < 2) {
            warning.hidden = true;
            return;
        }

        const first = state.frames[0];
        const unequal = state.frames.some((frame) =>
            frame.width !== first.width || frame.height !== first.height
        );
        warning.hidden = !unequal;
        warning.textContent = unequal
                ? "Frame sizes differ. The game stretches every crop into the same render size."
                : "";
    }

    function updateZoomLabel() {
        $("#zoom-label").textContent = `${Math.round(state.zoom * 100)}%`;
    }

    function updateHistoryButtons() {
        $("#undo").disabled = !state.history.length;
        $("#redo").disabled = !state.future.length;
    }

    function updateImageSummary() {
        const summary = $("#image-summary");
        if (!state.image) {
            summary.textContent = "No image loaded";
            return;
        }
        summary.textContent = `${state.imageName} · ${state.image.naturalWidth}×${state.image.naturalHeight}`;
    }

    function refreshAll() {
        renderEditor();
        renderFrameList();
        renderPreview();
        updateInspector();
        updateJavaOutput();
        updatePreviewWarning();
        updateZoomLabel();
        updateHistoryButtons();
        updateImageSummary();
    }

    function positiveNumber(value, fallback) {
        const number = Number(value);
        return Number.isFinite(number) && number > 0 ? number : fallback;
    }

    function integerInput(selector, fallback = 0) {
        const value = Number($(selector).value);
        return Number.isFinite(value) ? Math.round(value) : fallback;
    }

    function setTool(tool) {
        state.tool = tool;
        $$(".tool[data-tool]").forEach((button) => {
            button.classList.toggle("active", button.dataset.tool === tool);
        });
        editorCanvas.style.cursor = cursorForTool(tool);
    }

    function cursorForTool(tool) {
        const cursors = {
            select: "default",
            frame: "crosshair",
            pan: "grab",
            "guide-v": "col-resize",
            "guide-h": "row-resize"
        };
        return cursors[tool];
    }

    function updatePresetLayout() {
        const layout = $("#preset-layout").value;
        const columns = $("#preset-columns");
        const rows = $("#preset-rows");
        if (layout === "horizontal") {
            rows.value = 1;
            rows.disabled = true;
            columns.disabled = false;
        } else if (layout === "vertical") {
            columns.value = 1;
            columns.disabled = true;
            rows.disabled = false;
        } else {
            columns.disabled = false;
            rows.disabled = false;
        }
    }

    function fitPresetToImage() {
        if (!state.image) {
            showToast("Open an image first");
            return;
        }
        updatePresetLayout();
        const columns = Math.max(1, integerInput("#preset-columns", 1));
        const rows = Math.max(1, integerInput("#preset-rows", 1));
        const x = Math.max(0, integerInput("#preset-x"));
        const y = Math.max(0, integerInput("#preset-y"));
        const gapX = Math.max(0, integerInput("#preset-gap-x"));
        const gapY = Math.max(0, integerInput("#preset-gap-y"));
        const usableWidth = state.image.naturalWidth - x - gapX * (columns - 1);
        const usableHeight = state.image.naturalHeight - y - gapY * (rows - 1);
        $("#preset-width").value = Math.max(1, Math.floor(usableWidth / columns));
        $("#preset-height").value = Math.max(1, Math.floor(usableHeight / rows));
    }

    function presetFrames(firstId = state.nextFrameId) {
        const columns = Math.max(1, integerInput("#preset-columns", 1));
        const rows = Math.max(1, integerInput("#preset-rows", 1));
        const startX = Math.max(0, integerInput("#preset-x"));
        const startY = Math.max(0, integerInput("#preset-y"));
        const width = Math.max(1, integerInput("#preset-width", 1));
        const height = Math.max(1, integerInput("#preset-height", 1));
        const gapX = Math.max(0, integerInput("#preset-gap-x"));
        const gapY = Math.max(0, integerInput("#preset-gap-y"));
        const frames = [];

        for (let row = 0; row < rows; row++) {
            for (let column = 0; column < columns; column++) {
                frames.push({
                    id: firstId++,
                    x: startX + column * (width + gapX),
                    y: startY + row * (height + gapY),
                    width,
                    height
                });
            }
        }
        return frames;
    }

    function generatePreset() {
        if (!state.image) {
            showToast("Open an image first");
            return;
        }
        const nextFrames = presetFrames();
        const outside = nextFrames.some((frame) =>
            frame.x < 0
            || frame.y < 0
            || frame.x + frame.width > state.image.naturalWidth
            || frame.y + frame.height > state.image.naturalHeight
        );
        if (outside) {
            showToast("Preset extends outside the image");
            return;
        }

        edit(() => {
            state.frames = nextFrames;
            state.nextFrameId += nextFrames.length;
            state.selectedIds = new Set(nextFrames.map((frame) => frame.id));
            state.previewFrame = 0;
        });
    }

    function suggestPreset() {
        const width = state.image.naturalWidth;
        const height = state.image.naturalHeight;
        let columns = 1;
        let rows = 1;
        let frameWidth = width;
        let frameHeight = height;
        let layout = "horizontal";

        if (width > height && width % height === 0) {
            columns = width / height;
            frameWidth = height;
        } else if (height > width && height % width === 0) {
            layout = "vertical";
            rows = height / width;
            frameHeight = width;
        }

        $("#preset-layout").value = layout;
        $("#preset-columns").value = columns;
        $("#preset-rows").value = rows;
        $("#preset-x").value = 0;
        $("#preset-y").value = 0;
        $("#preset-width").value = frameWidth;
        $("#preset-height").value = frameHeight;
        $("#preset-gap-x").value = 0;
        $("#preset-gap-y").value = 0;
        updatePresetLayout();
    }

    function loadImageElement(dataUrl) {
        return new Promise((resolve, reject) => {
            const image = new Image();
            image.onload = () => resolve(image);
            image.onerror = () => reject(new Error("The image could not be loaded."));
            image.src = dataUrl;
        });
    }

    function readFileAsDataUrl(file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = () => resolve(reader.result);
            reader.onerror = () => reject(reader.error);
            reader.readAsDataURL(file);
        });
    }

    async function openImageFile(file) {
        if (!file) {
            return;
        }
        try {
            const dataUrl = await readFileAsDataUrl(file);
            const image = await loadImageElement(dataUrl);
            state.image = image;
            state.imageName = file.name;
            state.imageDataUrl = dataUrl;
            state.frames = [];
            state.nextFrameId = 1;
            state.selectedIds.clear();
            state.guides = { vertical: [], horizontal: [] };
            state.history = [];
            state.future = [];
            state.previewFrame = 0;
            suggestPreset();
            state.frames = presetFrames();
            state.nextFrameId += state.frames.length;
            state.selectedIds = new Set(state.frames.map((frame) => frame.id));
            $("#empty-state").hidden = true;
            fitImage();
            refreshAll();
        } catch (error) {
            showToast(error.message);
        }
    }

    function normalizeFrame(frame, id) {
        return {
            id,
            x: Math.round(Number(frame.x)),
            y: Math.round(Number(frame.y)),
            width: Math.max(1, Math.round(Number(frame.width))),
            height: Math.max(1, Math.round(Number(frame.height)))
        };
    }

    async function openProjectFile(file) {
        if (!file) {
            return;
        }
        try {
            const project = JSON.parse(await file.text());
            if (project.version !== 1 || !project.image?.dataUrl) {
                throw new Error("This is not a Sprite Clipper project.");
            }
            const image = await loadImageElement(project.image.dataUrl);
            state.image = image;
            state.imageName = project.image.name || "sprite-sheet.png";
            state.imageDataUrl = project.image.dataUrl;
            state.frames = (project.frames || []).map((frame, index) =>
                normalizeFrame(frame, index + 1)
            );
            state.nextFrameId = state.frames.length + 1;
            state.selectedIds.clear();
            state.guides = {
                vertical: [...(project.guides?.vertical || [])],
                horizontal: [...(project.guides?.horizontal || [])]
            };
            state.history = [];
            state.future = [];
            state.previewFrame = 0;
            $("#variable-name").value = project.export?.variableName
                    || "idleAnimationClips";
            $("#render-width").value = project.preview?.renderWidth || 144;
            $("#render-height").value = project.preview?.renderHeight || 144;
            $("#preview-fps").value = project.preview?.fps || 8;
            $("#preview-fps-value").value = `${$("#preview-fps").value} fps`;
            $("#empty-state").hidden = true;
            fitImage();
            refreshAll();
        } catch (error) {
            showToast(error.message);
        }
    }

    function projectData() {
        return {
            version: 1,
            image: {
                name: state.imageName,
                width: state.image?.naturalWidth || 0,
                height: state.image?.naturalHeight || 0,
                dataUrl: state.imageDataUrl
            },
            frames: state.frames.map(({ x, y, width, height }) => ({
                x, y, width, height
            })),
            guides: {
                vertical: [...state.guides.vertical],
                horizontal: [...state.guides.horizontal]
            },
            preview: {
                renderWidth: positiveNumber($("#render-width").value, 144),
                renderHeight: positiveNumber($("#render-height").value, 144),
                fps: positiveNumber($("#preview-fps").value, 8)
            },
            export: {
                variableName: validVariableName($("#variable-name").value)
            }
        };
    }

    function baseFileName() {
        return (state.imageName || "sprite-sheet")
                .replace(/\.[^.]+$/, "")
                .replace(/[^A-Za-z0-9_-]+/g, "-");
    }

    function download(name, content, type) {
        const blob = new Blob([content], { type });
        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        link.download = name;
        document.body.append(link);
        link.click();
        link.remove();
        setTimeout(() => URL.revokeObjectURL(link.href), 1000);
    }

    function saveProject() {
        if (!state.image) {
            showToast("Open an image first");
            return;
        }
        download(
                `${baseFileName()}.sprite-clips.json`,
                JSON.stringify(projectData(), null, 2),
                "application/json"
        );
        showToast("Project saved");
    }

    function exportJava() {
        download(
                `${validVariableName($("#variable-name").value)}.java.txt`,
                `${javaCode()}\n`,
                "text/plain"
        );
        showToast("Java clip list downloaded");
    }

    async function copyJava() {
        const code = javaCode();
        try {
            await navigator.clipboard.writeText(code);
        } catch {
            const output = $("#java-output");
            output.focus();
            output.select();
            document.execCommand("copy");
        }
        showToast("Java copied");
    }

    function selectOnly(frame) {
        state.selectedIds = frame ? new Set([frame.id]) : new Set();
    }

    function beginSelectionDrag(imagePoint, frame, event) {
        const handle = hitHandle(imagePoint);
        if (handle) {
            state.drag = {
                type: "resize",
                handle,
                start: imagePoint,
                originalFrames: cloneFrames(selectedFrames()),
                originalBounds: frameBounds(selectedFrames()),
                before: snapshot(),
                changed: false
            };
            return;
        }

        if (!frame) {
            if (!event.shiftKey) {
                state.selectedIds.clear();
                refreshAll();
            }
            return;
        }

        if (event.shiftKey) {
            if (state.selectedIds.has(frame.id)) {
                state.selectedIds.delete(frame.id);
            } else {
                state.selectedIds.add(frame.id);
            }
            refreshAll();
            return;
        }

        if (!state.selectedIds.has(frame.id)) {
            selectOnly(frame);
        }
        state.drag = {
            type: "move",
            start: imagePoint,
            originalFrames: cloneFrames(selectedFrames()),
            originalBounds: frameBounds(selectedFrames()),
            before: snapshot(),
            changed: false
        };
        refreshAll();
    }

    function beginPointerAction(event) {
        if (!state.image) {
            return;
        }
        canvasShell.focus();
        editorCanvas.setPointerCapture(event.pointerId);
        const screen = pointerPosition(event);
        const rawImagePoint = screenToImage(screen.x, screen.y);
        const imagePoint = clampImagePoint({
            x: snapped(rawImagePoint.x),
            y: snapped(rawImagePoint.y)
        });

        if (event.button === 1 || state.spacePressed || state.tool === "pan") {
            state.drag = {
                type: "pan",
                startScreen: screen,
                originalOffsetX: state.offsetX,
                originalOffsetY: state.offsetY
            };
            editorCanvas.style.cursor = "grabbing";
            event.preventDefault();
            return;
        }

        if (event.button !== 0) {
            return;
        }

        if (state.tool === "frame") {
            state.drag = {
                type: "create",
                start: imagePoint,
                current: imagePoint
            };
        } else if (state.tool === "guide-v") {
            edit(() => state.guides.vertical.push(Math.round(imagePoint.x)));
        } else if (state.tool === "guide-h") {
            edit(() => state.guides.horizontal.push(Math.round(imagePoint.y)));
        } else {
            beginSelectionDrag(imagePoint, hitFrame(imagePoint), event);
        }
        renderEditor();
    }

    function moveSelectedFrames(drag, imagePoint) {
        let dx = snapped(imagePoint.x - drag.start.x);
        let dy = snapped(imagePoint.y - drag.start.y);
        const bounds = drag.originalBounds;

        dx = Math.max(-bounds.x, Math.min(
                state.image.naturalWidth - bounds.x - bounds.width,
                dx
        ));
        dy = Math.max(-bounds.y, Math.min(
                state.image.naturalHeight - bounds.y - bounds.height,
                dy
        ));

        for (const original of drag.originalFrames) {
            const frame = state.frames.find((candidate) => candidate.id === original.id);
            frame.x = Math.round(original.x + dx);
            frame.y = Math.round(original.y + dy);
        }
        drag.changed = dx !== 0 || dy !== 0;
    }

    function resizedBounds(drag, imagePoint) {
        const original = drag.originalBounds;
        const deltaX = snapped(imagePoint.x - drag.start.x);
        const deltaY = snapped(imagePoint.y - drag.start.y);
        let left = original.x;
        let top = original.y;
        let right = original.x + original.width;
        let bottom = original.y + original.height;

        if (drag.handle.includes("w")) {
            left = Math.min(right - 1, Math.max(0, original.x + deltaX));
        }
        if (drag.handle.includes("e")) {
            right = Math.max(left + 1, Math.min(
                    state.image.naturalWidth,
                    original.x + original.width + deltaX
            ));
        }
        if (drag.handle.includes("n")) {
            top = Math.min(bottom - 1, Math.max(0, original.y + deltaY));
        }
        if (drag.handle.includes("s")) {
            bottom = Math.max(top + 1, Math.min(
                    state.image.naturalHeight,
                    original.y + original.height + deltaY
            ));
        }

        return {
            x: left,
            y: top,
            width: right - left,
            height: bottom - top
        };
    }

    function resizeSelectedFrames(drag, imagePoint) {
        const nextBounds = resizedBounds(drag, imagePoint);
        const oldBounds = drag.originalBounds;
        const scaleX = nextBounds.width / oldBounds.width;
        const scaleY = nextBounds.height / oldBounds.height;

        for (const original of drag.originalFrames) {
            const frame = state.frames.find((candidate) => candidate.id === original.id);
            frame.x = Math.round(nextBounds.x
                    + (original.x - oldBounds.x) * scaleX);
            frame.y = Math.round(nextBounds.y
                    + (original.y - oldBounds.y) * scaleY);
            frame.width = Math.max(1, Math.round(original.width * scaleX));
            frame.height = Math.max(1, Math.round(original.height * scaleY));
        }
        drag.changed = nextBounds.x !== oldBounds.x
                || nextBounds.y !== oldBounds.y
                || nextBounds.width !== oldBounds.width
                || nextBounds.height !== oldBounds.height;
    }

    function continuePointerAction(event) {
        const screen = pointerPosition(event);
        const rawImagePoint = screenToImage(screen.x, screen.y);
        $("#cursor-position").textContent =
                `x ${Math.round(rawImagePoint.x)}, y ${Math.round(rawImagePoint.y)}`;

        if (!state.drag) {
            if (state.tool === "select" && state.image) {
                const handle = hitHandle(rawImagePoint);
                editorCanvas.style.cursor = handle
                        ? resizeCursor(handle)
                        : (hitFrame(rawImagePoint) ? "move" : "default");
            }
            return;
        }

        if (state.drag.type === "pan") {
            state.offsetX = state.drag.originalOffsetX
                    + screen.x - state.drag.startScreen.x;
            state.offsetY = state.drag.originalOffsetY
                    + screen.y - state.drag.startScreen.y;
            renderEditor();
            return;
        }

        const imagePoint = clampImagePoint({
            x: snapped(rawImagePoint.x),
            y: snapped(rawImagePoint.y)
        });
        if (state.drag.type === "create") {
            state.drag.current = imagePoint;
        } else if (state.drag.type === "move") {
            moveSelectedFrames(state.drag, imagePoint);
        } else if (state.drag.type === "resize") {
            resizeSelectedFrames(state.drag, imagePoint);
        }
        renderEditor();
        updateInspector();
        updateJavaOutput();
        renderPreview();
    }

    function finishPointerAction() {
        const drag = state.drag;
        state.drag = null;
        if (!drag) {
            return;
        }

        if (drag.type === "create") {
            const left = Math.round(Math.min(drag.start.x, drag.current.x));
            const top = Math.round(Math.min(drag.start.y, drag.current.y));
            const right = Math.round(Math.max(drag.start.x, drag.current.x));
            const bottom = Math.round(Math.max(drag.start.y, drag.current.y));
            if (right > left && bottom > top) {
                edit(() => {
                    const frame = {
                        id: state.nextFrameId++,
                        x: left,
                        y: top,
                        width: right - left,
                        height: bottom - top
                    };
                    state.frames.push(frame);
                    selectOnly(frame);
                    state.previewFrame = state.frames.length - 1;
                });
            } else {
                renderEditor();
            }
        } else if ((drag.type === "move" || drag.type === "resize") && drag.changed) {
            state.history.push(drag.before);
            state.future = [];
            refreshAll();
        } else {
            refreshAll();
        }
        editorCanvas.style.cursor = cursorForTool(state.tool);
    }

    function resizeCursor(handle) {
        const cursors = {
            n: "ns-resize",
            s: "ns-resize",
            e: "ew-resize",
            w: "ew-resize",
            ne: "nesw-resize",
            sw: "nesw-resize",
            nw: "nwse-resize",
            se: "nwse-resize"
        };
        return cursors[handle];
    }

    function zoomAt(event) {
        if (!state.image) {
            return;
        }
        event.preventDefault();
        const screen = pointerPosition(event);
        const before = screenToImage(screen.x, screen.y);
        const factor = event.deltaY < 0 ? 1.15 : 1 / 1.15;
        state.zoom = Math.max(0.05, Math.min(32, state.zoom * factor));
        state.offsetX = screen.x - before.x * state.zoom;
        state.offsetY = screen.y - before.y * state.zoom;
        updateZoomLabel();
        renderEditor();
    }

    function deleteSelected() {
        if (!state.selectedIds.size) {
            return;
        }
        edit(() => {
            state.frames = state.frames.filter(
                    (frame) => !state.selectedIds.has(frame.id)
            );
            state.selectedIds.clear();
            state.previewFrame = Math.min(
                    state.previewFrame,
                    Math.max(0, state.frames.length - 1)
            );
        });
    }

    function duplicateSelected() {
        const selected = selectedFrames();
        if (!selected.length) {
            return;
        }
        edit(() => {
            const copies = selected.map((frame) => ({
                ...frame,
                id: state.nextFrameId++,
                x: Math.min(
                        state.image.naturalWidth - frame.width,
                        frame.x + 4
                ),
                y: Math.min(
                        state.image.naturalHeight - frame.height,
                        frame.y + 4
                )
            }));
            state.frames.push(...copies);
            state.selectedIds = new Set(copies.map((frame) => frame.id));
        });
    }

    function reorderSelected(direction) {
        if (state.selectedIds.size !== 1) {
            return;
        }
        const id = [...state.selectedIds][0];
        const index = state.frames.findIndex((frame) => frame.id === id);
        const destination = index + direction;
        if (destination < 0 || destination >= state.frames.length) {
            return;
        }
        edit(() => {
            const [frame] = state.frames.splice(index, 1);
            state.frames.splice(destination, 0, frame);
            state.previewFrame = destination;
        });
    }

    function nudgeSelected(dx, dy) {
        const selected = selectedFrames();
        if (!selected.length || !state.image) {
            return;
        }
        const bounds = frameBounds(selected);
        dx = Math.max(-bounds.x, Math.min(
                state.image.naturalWidth - bounds.x - bounds.width,
                dx
        ));
        dy = Math.max(-bounds.y, Math.min(
                state.image.naturalHeight - bounds.y - bounds.height,
                dy
        ));
        if (!dx && !dy) {
            return;
        }
        edit(() => {
            for (const frame of selectedFrames()) {
                frame.x += dx;
                frame.y += dy;
            }
        });
    }

    function changeSelectedFrame() {
        const selected = selectedFrames();
        if (selected.length !== 1 || !state.image) {
            return;
        }
        const next = {
            x: Math.max(0, Math.min(
                    state.image.naturalWidth - 1,
                    integerInput("#frame-x")
            )),
            y: Math.max(0, Math.min(
                    state.image.naturalHeight - 1,
                    integerInput("#frame-y")
            )),
            width: Math.max(1, integerInput("#frame-width", 1)),
            height: Math.max(1, integerInput("#frame-height", 1))
        };
        next.width = Math.min(next.width, state.image.naturalWidth - next.x);
        next.height = Math.min(next.height, state.image.naturalHeight - next.y);

        const frame = selected[0];
        if (frame.x === next.x
                && frame.y === next.y
                && frame.width === next.width
                && frame.height === next.height) {
            return;
        }
        state.history.push(snapshot());
        state.future = [];
        Object.assign(
                state.frames.find((candidate) => candidate.id === frame.id),
                next
        );
        renderEditor();
        renderFrameList();
        renderPreview();
        updateJavaOutput();
        updatePreviewWarning();
        updateHistoryButtons();
    }

    function selectFrameCard(event) {
        const card = event.target.closest(".frame-card");
        if (!card) {
            return;
        }
        const id = Number(card.dataset.frameId);
        const frame = state.frames.find((candidate) => candidate.id === id);
        if (!frame) {
            return;
        }
        if (event.shiftKey) {
            if (state.selectedIds.has(id)) {
                state.selectedIds.delete(id);
            } else {
                state.selectedIds.add(id);
            }
        } else {
            selectOnly(frame);
        }
        state.previewFrame = state.frames.indexOf(frame);
        refreshAll();
    }

    function keyboardShortcut(event) {
        const target = event.target;
        const editingText = target.matches("input, textarea, select");
        const command = event.ctrlKey || event.metaKey;

        if (command && event.key.toLowerCase() === "z") {
            event.preventDefault();
            event.shiftKey ? redo() : undo();
            return;
        }
        if (command && event.key.toLowerCase() === "s") {
            event.preventDefault();
            saveProject();
            return;
        }
        if (editingText) {
            return;
        }

        if (event.code === "Space") {
            state.spacePressed = true;
            editorCanvas.style.cursor = "grab";
            event.preventDefault();
        } else if (event.key === "Delete" || event.key === "Backspace") {
            event.preventDefault();
            deleteSelected();
        } else if (event.key === "ArrowLeft") {
            event.preventDefault();
            nudgeSelected(event.shiftKey ? -10 : -1, 0);
        } else if (event.key === "ArrowRight") {
            event.preventDefault();
            nudgeSelected(event.shiftKey ? 10 : 1, 0);
        } else if (event.key === "ArrowUp") {
            event.preventDefault();
            nudgeSelected(0, event.shiftKey ? -10 : -1);
        } else if (event.key === "ArrowDown") {
            event.preventDefault();
            nudgeSelected(0, event.shiftKey ? 10 : 1);
        } else if (event.key.toLowerCase() === "v") {
            setTool("select");
        } else if (event.key.toLowerCase() === "f") {
            setTool("frame");
        } else if (event.key.toLowerCase() === "h") {
            setTool("pan");
        }
    }

    function keyboardRelease(event) {
        if (event.code === "Space") {
            state.spacePressed = false;
            editorCanvas.style.cursor = cursorForTool(state.tool);
        }
    }

    function loadDroppedFile(event) {
        event.preventDefault();
        canvasShell.classList.remove("dragging");
        const file = event.dataTransfer.files[0];
        if (!file) {
            return;
        }
        if (file.name.toLowerCase().endsWith(".json")) {
            openProjectFile(file);
        } else if (file.type.startsWith("image/")) {
            openImageFile(file);
        } else {
            showToast("Drop an image or Sprite Clipper JSON project");
        }
    }

    $$(".tool[data-tool]").forEach((button) => {
        button.addEventListener("click", () => setTool(button.dataset.tool));
    });
    $("#clear-guides").addEventListener("click", () => {
        if (state.guides.vertical.length || state.guides.horizontal.length) {
            edit(() => {
                state.guides.vertical = [];
                state.guides.horizontal = [];
            });
        }
    });
    $("#image-input").addEventListener("change", (event) => {
        openImageFile(event.target.files[0]);
        event.target.value = "";
    });
    $("#project-input").addEventListener("change", (event) => {
        openProjectFile(event.target.files[0]);
        event.target.value = "";
    });
    $("#save-project").addEventListener("click", saveProject);
    $("#export-java").addEventListener("click", exportJava);
    $("#copy-java").addEventListener("click", copyJava);
    $("#download-java").addEventListener("click", exportJava);
    $("#undo").addEventListener("click", undo);
    $("#redo").addEventListener("click", redo);
    $("#fit-image").addEventListener("click", fitImage);
    $("#fit-preset").addEventListener("click", fitPresetToImage);
    $("#generate-preset").addEventListener("click", generatePreset);
    $("#preset-layout").addEventListener("change", updatePresetLayout);
    $("#frame-list").addEventListener("click", selectFrameCard);
    $("#delete-frame").addEventListener("click", deleteSelected);
    $("#duplicate-frame").addEventListener("click", duplicateSelected);
    $("#move-frame-left").addEventListener("click", () => reorderSelected(-1));
    $("#move-frame-right").addEventListener("click", () => reorderSelected(1));
    $("#frame-fields").addEventListener("input", changeSelectedFrame);
    $("#variable-name").addEventListener("input", updateJavaOutput);
    $("#render-width").addEventListener("input", renderPreview);
    $("#render-height").addEventListener("input", renderPreview);
    $("#preview-fps").addEventListener("input", () => {
        $("#preview-fps-value").value = `${$("#preview-fps").value} fps`;
    });
    $("#play-preview").addEventListener("click", () => {
        state.previewPlaying = !state.previewPlaying;
        $("#play-preview").textContent = state.previewPlaying ? "Ⅱ" : "▶";
        $("#play-preview").title = state.previewPlaying ? "Pause preview" : "Play preview";
        $("#play-preview").setAttribute(
                "aria-label",
                state.previewPlaying ? "Pause preview" : "Play preview"
        );
        $("#play-preview").classList.toggle("active", state.previewPlaying);
        state.previewLastTime = performance.now();
    });
    $("#pixel-grid").addEventListener("change", renderEditor);
    $("#dim-outside").addEventListener("change", renderEditor);

    editorCanvas.addEventListener("pointerdown", beginPointerAction);
    editorCanvas.addEventListener("pointermove", continuePointerAction);
    editorCanvas.addEventListener("pointerup", finishPointerAction);
    editorCanvas.addEventListener("pointercancel", finishPointerAction);
    editorCanvas.addEventListener("wheel", zoomAt, { passive: false });
    editorCanvas.addEventListener("contextmenu", (event) => event.preventDefault());

    canvasShell.addEventListener("dragover", (event) => {
        event.preventDefault();
        canvasShell.classList.add("dragging");
    });
    canvasShell.addEventListener("dragleave", () => {
        canvasShell.classList.remove("dragging");
    });
    canvasShell.addEventListener("drop", loadDroppedFile);

    window.addEventListener("keydown", keyboardShortcut);
    window.addEventListener("keyup", keyboardRelease);
    window.addEventListener("blur", () => {
        state.spacePressed = false;
    });

    new ResizeObserver(resizeEditorCanvas).observe(canvasShell);
    updatePresetLayout();
    refreshAll();
    requestAnimationFrame(animatePreview);
})();
