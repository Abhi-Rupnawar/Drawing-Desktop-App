# 2D Drawing Desktop Application

## Overview

A small desktop drawing application inspired by Excalidraw. It provides a large pannable,
zoomable canvas on which you can draw rectangles, ellipses, lines and text, select and move
objects, and save your work to a local JSON file.

The goal of this project is not feature parity with Excalidraw. It is a deliberately small
application that demonstrates a clean layered architecture, a correct separation between world
and screen coordinates, defensive file handling, and a meaningful test suite.

## Features

- Infinite-feeling canvas with a reference grid
- Rectangle, ellipse/circle, line and text objects
- Drag creation in any direction, with negative sizes normalised
- Live preview of the shape while it is being drawn
- Click to select, dashed selection indicator, click empty canvas to deselect
- Drag a selected object to reposition it, correct at any zoom level
- Delete the selected object
- Zoom in / out / reset, plus cursor-anchored mouse-wheel zoom
- Panning with middle mouse drag or space + left drag
- New / Open / Save with an unsaved-changes prompt
- JSON persistence with validation and user-friendly error dialogs
- Status bar showing zoom level, object count, unsaved marker and last action
- Keyboard shortcuts

## Technology Stack

| Concern | Choice |
|---|---|
| Language | Java 17 |
| UI toolkit | JavaFX 21 |
| DI / application wiring | Spring Boot 3.2 (no web server) |
| Serialization | Jackson Databind |
| Build | Maven |
| Tests | JUnit 5 |
| Logging | SLF4J over Logback (Spring Boot default) |

### Why JavaFX

The challenge requires a desktop application. JavaFX was chosen over Swing because its
`Canvas` and `GraphicsContext` provide exactly the immediate-mode drawing model this application
needs, including a built-in affine transform on the graphics context. That transform is what
allows zoom and pan to be applied once per frame instead of being baked into object coordinates.
JavaFX also gives modern, well-behaved dialogs (`FileChooser`, `Alert`, `TextInputDialog`) out of
the box, which keeps the persistence and error-handling code short. Compared with a web UI, a
JavaFX desktop application also avoids needing any HTTP server or browser runtime.

### Why local JSON persistence

Drawings are small, self-contained documents that belong to the user, so a document file is the
right model, not a database. JSON was chosen because it is human-readable and diffable, needs no
schema migration infrastructure for a version-1 format, and Jackson can map it directly onto the
domain model with an explicit subtype whitelist. A binary format or Java serialization would be
smaller but opaque and, in the case of Java serialization, actively unsafe to read from files that
the application did not write.

## Architecture

The application is layered, and dependencies only point downwards:

```
        ui  (JavaFX: canvas, toolbar, status bar, dialogs, renderer)
         |
    controller  (gesture state machine, document commands)
         |
     service  (document state, selection, view transform, file rules)
         |
  repository  (Jackson read/write - the only file-system access)
         |
      model  (pure data + geometry, no framework dependencies)
```

Three decisions shape everything else:

**World coordinates versus screen coordinates.** Objects only ever store world coordinates. The
view transform (`zoom`, `panX`, `panY`) lives in `ViewTransformService` and is applied in two
places: the renderer applies it to the `GraphicsContext` before drawing, and the controller
converts incoming mouse coordinates back to world space before touching the model
(`world = (screen - pan) / zoom`). Zooming and panning therefore never modify a single object,
and dragging behaves identically at 10% and 800% zoom.

**The model is data, not a view.** No model class imports JavaFX. Colours are stored as validated
hex strings, and `CanvasRenderer` is the only class that knows how an object is painted. This is
also what makes the geometry unit testable without a JavaFX toolkit.

**All mutation goes through the service layer.** `DrawingService` is the only way to add, remove
or move an object, which is what keeps the dirty flag and the repaint notifications correct in one
place rather than scattered across UI handlers.

Two controllers exist rather than one: `DrawingController` owns canvas gestures and
`DocumentController` owns the new/open/save lifecycle. Splitting them avoids the "giant
controller" that a single class would quickly become, and it keeps both free of JavaFX types.

Spring Boot is used purely as a dependency injection container and lifecycle manager. No web
server is started (`spring.main.web-application-type=none`), nothing binds a port, and the
application has no dependency on localhost. `main()` hands control straight to JavaFX;
`JavaFxApplication.init()` then boots the Spring context, `start()` pulls `MainView` out of it,
and `stop()` closes it.

## Project Structure

```
src/main/java/com/example/drawingapp
├── DrawingApplication.java          Spring Boot root + main(), launches JavaFX
├── config
│   └── ApplicationConfig.java       Configured ObjectMapper bean
├── model
│   ├── DrawingObject.java           Abstract base: id, position, style, bounds, hit test
│   ├── RectangleObject.java
│   ├── EllipseObject.java
│   ├── LineObject.java
│   ├── TextObject.java
│   ├── DrawingDocument.java         Version + paint-ordered object list
│   ├── Bounds.java                  Immutable axis-aligned box (record)
│   ├── Point.java                   Immutable coordinate pair (record)
│   ├── StyleDefaults.java           Default and sanitised style values
│   └── enums
│       ├── DrawingTool.java
│       └── ObjectType.java
├── service
│   ├── DrawingService.java          Current document, dirty flag, change events
│   ├── SelectionService.java        Selection state and topmost hit testing
│   ├── ViewTransformService.java    Zoom/pan, world <-> screen conversion
│   └── FileService.java             Extension rules and document validation
├── repository
│   └── DrawingFileRepository.java   Jackson JSON read/write
├── controller
│   ├── DrawingController.java       Canvas gesture state machine
│   ├── DocumentController.java      New / open / save
│   └── TextInputProvider.java       Abstraction over "ask the user for text"
├── ui
│   ├── JavaFxApplication.java       JavaFX + Spring lifecycle bridge
│   ├── MainView.java                Layout, shortcuts, file command flow
│   ├── DrawingCanvas.java           Canvas node, mouse routing, repaint
│   ├── CanvasRenderer.java          All drawing code
│   ├── DrawingToolbar.java
│   ├── StatusBar.java
│   ├── DialogService.java           All modal dialogs and alerts
│   ├── ToolbarActions.java
│   └── UnsavedChangesChoice.java
├── exception
│   └── DrawingFileException.java    Checked, carries a user-facing message
└── util
    ├── BoundsUtil.java              Normalisation and segment distance
    └── ChangeSupport.java           Minimal listener registry
```

Two deviations from the suggested structure, both deliberate:

- `util/JsonUtil` was dropped. A single configured `ObjectMapper` bean injected into the
  repository does the same job without a static wrapper that would only forward calls.
- The suggested structure lists `DrawingApplication` twice. The root class keeps that name; the
  JavaFX `Application` subclass is `ui/JavaFxApplication` so the two are not confusable.

## Build Instructions

Requirements: JDK 17 or newer and Maven 3.8+.

```bash
mvn clean package
```

This compiles the application, runs the test suite and produces an executable jar in `target/`.

To run the tests alone:

```bash
mvn clean test
```

JavaFX is resolved as an ordinary Maven dependency; the correct native artifacts for your
operating system are selected automatically. No separate JavaFX SDK installation is required.

## Run Instructions

Either:

```bash
mvn javafx:run
```

or, after `mvn clean package`:

```bash
java -jar target/drawing-app-1.0.0.jar
```

Note that the packaged jar contains the JavaFX native libraries for the platform it was built on,
so build it on the machine you intend to run it on.

## How to Use

| Action | How |
|---|---|
| Draw a rectangle / ellipse / line | Pick the tool, then drag on the canvas in any direction |
| Add text | Pick the Text tool, click the canvas, type into the dialog |
| Select an object | Select tool, click the object |
| Deselect | Select tool, click empty canvas |
| Move an object | Select tool, drag the selected object |
| Delete an object | Select it, press `Delete` or `Backspace` |
| Zoom | Toolbar buttons, `+` / `-`, or the mouse wheel (zooms around the cursor) |
| Reset zoom | Toolbar button or `0` |
| Pan | **Middle mouse drag**, or hold **Space** and drag with the left button |
| New / Open / Save | Toolbar buttons or `Ctrl+N` / `Ctrl+O` / `Ctrl+S` (`Cmd` on macOS) |
| Switch tools | `V` select, `R` rectangle, `E` ellipse, `L` line, `T` text |

The selected object is marked with a blue dashed rectangle around its bounds. Resize handles are
not drawn, as the challenge states they are not required.

Save writes to the file the drawing was last opened from or saved to; the first save asks for a
location.

## File Format

Files use the extension `.drawing.json`, which is appended automatically if you omit it.

```json
{
  "version" : 1,
  "objects" : [ {
    "id" : "0e2a9b1c-8f4d-4a6e-9f2b-7c1d5e3a8b04",
    "type" : "RECTANGLE",
    "x" : 100.0,
    "y" : 100.0,
    "strokeColor" : "#1E1E1E",
    "strokeWidth" : 2.0,
    "width" : 200.0,
    "height" : 100.0
  } ]
}
```

`type` is the discriminator and must be one of `RECTANGLE`, `ELLIPSE`, `LINE`, `TEXT`. Rectangles
and ellipses carry `width`/`height`; lines carry `endX`/`endY`; text carries `text` and
`fontSize`. The array order is the paint order.

Reading is deliberately defensive:

- malformed JSON, an empty file or a missing file produce an error dialog, never a crash;
- an unknown `type` is reported by name and the file is rejected;
- unknown properties are ignored, so a file from a slightly different build still opens;
- missing properties fall back to safe defaults, and invalid style values are replaced;
- a file declaring a newer `version` is rejected with an explanation;
- files larger than 20 MB are rejected before being parsed.

Jackson's default typing is never enabled. Deserialization is restricted to the four subtypes
declared on `DrawingObject`, so a file cannot cause arbitrary Java classes to be instantiated.

## Design Decisions

**Transform on the graphics context, not on the data.** The renderer calls `translate` and
`scale` once per frame and then draws every object in world coordinates. The alternative,
transforming each object's coordinates before drawing, duplicates the transform in the renderer
and in hit testing and inevitably drifts out of sync.

**Full redraw on every change.** For the object counts this application targets, clearing and
repainting is fast and removes an entire class of stale-pixel bugs. Dirty-rectangle tracking would
be premature optimisation here.

**Hit testing lives in the model.** `containsPoint` is pure geometry with no rendering knowledge,
and each shape knows its own shape best: an ellipse rejects the corners of its bounding box, and a
line measures the distance to its segment rather than to its box. `SelectionService` only decides
*which* hit wins, by iterating in reverse paint order so the topmost object is picked.

**Hit tolerance is expressed in screen pixels.** The controller converts a 6-pixel tolerance into
world units using the current zoom, so a thin line stays clickable when zoomed out without
becoming a large target when zoomed in.

**Text bounds are estimated, not measured.** Measuring text needs JavaFX font metrics, which would
put a UI dependency into the model and make it untestable without a toolkit. The bounding box is
estimated from the font size and character count instead. The cost is a selection rectangle that
is slightly loose for some strings, which is acceptable when resize handles are out of scope.

**Checked exception for file errors.** `DrawingFileException` is checked so the compiler forces
the UI to handle it, and its message is written to be shown directly to the user. There are no
broad `catch (Exception)` blocks: the repository catches the specific Jackson and I/O exceptions
it expects and logs them through SLF4J.

**Single selection only.** Multi-select is not required by the challenge and would complicate
dragging, deletion and the selection indicator for no evaluated benefit.

**Two small controllers instead of one.** See Architecture above.

**No background threads.** Reading and writing a small local JSON file takes milliseconds; moving
it to a background thread would add complexity and a class of threading bugs for no benefit. All
UI work happens on the JavaFX application thread, which is also where the services are called
from.

## Assumptions

- Single-user, single-document application: one drawing is open at a time.
- Text objects are a single line. Multi-line text was not specified and would require wrapping
  rules and measured metrics.
- Style is fixed (dark stroke, 2px, 18px font). The model carries per-object style so a style
  picker could be added without touching persistence, but no style UI is provided.
- Shapes are hit-tested by their area rather than by their outline only, because clicking inside
  an unfilled rectangle is what users expect.
- `Save` overwrites the current file silently once a location is known; there is no separate
  "Save As". Choosing a new location is done via `New` then `Save`, or by saving an untitled
  drawing.
- Panning is offered through both middle drag and space + left drag, because many laptop
  trackpads have no middle button.
- Zoom is clamped to 10%–800%.
- Closing the window with unsaved changes shows the same prompt as New and Open.

## Limitations

- No undo/redo. It was not required, and doing it properly means a command stack that touches
  every mutation path.
- No rotation, resize handles, grouping, connectors, multi-select, styling UI, image or freehand
  objects. All are explicitly out of scope.
- Selection bounds for text are approximate, as described above.
- The packaged jar is platform-specific because of the bundled JavaFX natives.
- Very large drawings (tens of thousands of objects) would need a spatial index for hit testing
  and a dirty-region redraw strategy; neither is implemented.

## Testing

```bash
mvn clean test
```

The suite covers the layers where correctness is easy to get wrong, and deliberately does not
assert on rendered pixels:

| Area | Coverage |
|---|---|
| Geometry | Bounds for rectangle, ellipse, line and text; normalisation of all four drag directions; distance to a segment including the degenerate and beyond-the-end cases |
| Hit testing | Inside/outside for each shape, ellipse corners rejected, line measured against its segment, tolerance behaviour |
| Selection | Topmost object wins, empty canvas clears, `findAt` does not mutate state, listeners fire only on real changes |
| View transform | Identity start, screen/world round trip, zoom anchored on a point, zoom clamping, pan accumulation, screen-to-world distances |
| Document state | Dirty flag set on add/move/remove, cleared on save and on load, zero-move is not a change, listeners fire |
| Interaction | Shape creation from a drag, live preview, accidental clicks ignored, creation and dragging correct while zoomed and panned, select/deselect, text creation and cancellation, delete |
| Persistence | Round trip of all four types with ids and order preserved, extension handling, documented JSON structure, malformed JSON, unsupported type, missing fields, unknown fields, empty file, missing file, newer version |

Tests run as plain JUnit 5 without a Spring context or a JavaFX toolkit, which keeps them fast.
The persistence tests use the real `ObjectMapper` produced by `ApplicationConfig`, so they fail if
the production serialization configuration changes.
