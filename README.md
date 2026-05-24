# Sortavala low dump — film compiler

Numbered DJI `.LRF` sources (`make link`) and a YAML DSL describe a short highlight film. A Java compiler cuts clips, caches them, and concatenates with incremental rebuild when the DSL changes.

## Setup

```bash
make link
brew install rubberband ffmpeg
ffmpeg -filters 2>&1 | grep rubberband
```

`rubberband` in ffmpeg’s filter list gives better audio on speed keyframes. Without it, keyframe clips still build using stepped `atempo` (a warning is printed once).

## Build film

**Draft** (fast iteration):

```bash
make test
make film
```

**Release** (before publishing):

```bash
make film-release
```

Output: `build/output.mp4`. Clip cache: `build/clips/<id>.mp4`. State: `build/manifest.json` (includes `"profile": "draft"` or `"release"`).

Logs (written live during build): `build/logs/`

- `film.log` — high-level progress and “still running” heartbeats every 30s
- `cut-<id>.log` — ffmpeg output while rendering each clip
- `concat-leaf-<n>.log` — joining clips into tree leaf nodes (draft)
- `concat-node-<id>.log` — joining child nodes into internal tree nodes (draft)
- `concat-root.log` — joining the top tree node into the final film (draft)
- `concat-film.log` — direct clip-to-output join (release)
- `probe-<n>.log` — ffprobe when resolving end-of-file

While a step runs:

```bash
tail -f build/logs/cut-beavers.log
tail -f build/logs/film.log
```

## Draft vs release

Both profiles share one `build/` directory and the same normalized artifact contract. DJI timestamp quirks are fixed at **cut** only; nothing in `build/` is a raw source symlink.

| | `make film` (draft) | `make film-release` (release) |
|---|---|---|
| Cut preset / CRF | ultrafast / 28 | slow / 18 |
| Assembly | tree (fan-in 4) | flat (all clips → output) |
| Concat | stream copy (`-c copy`) | libx264/aac reencode |

Switching profile invalidates the cache via fingerprint and manifest `"profile"`.

**Build contract** (default): h264 1280×720 @ 30 fps, yuv420p, aac 48000 Hz stereo, mp4 + faststart. Every clip in `build/clips/` and `build/parts/` matches this shape so draft concat can copy streams safely.

Optional override in `film.dsl.yaml`:

```yaml
contract:
  width: 1280
  height: 720
  fps: 30
  audio_rate: 48000
```

## Edit DSL

Edit [`film.dsl.yaml`](film.dsl.yaml): each clip has stable `id`, `source` (symlink number), optional `from` (seconds on source), end bound as **`to`** (absolute second on source) **or** **`duration`** (length from `from`; do not use both).

**Exclude** — optional list of gaps to drop on the **source file** timeline (absolute seconds, same time forms as clip `from` / `to`). Each entry may set **`from`**, **`to`**, and/or **`duration`** (`to` and `duration` are mutually exclusive). Omit **`from`** to start at the clip window; omit **`to`** and **`duration`** to run through the clip window end. Gaps must lie inside the clip window. With `exclude` present, clip **`duration`** caps output on the **trimmed** timeline (after gaps are removed), and **`speed`** / keyframe **`at`** use that trimmed timeline. Outer bound is **`to`** or end-of-file; you may set both **`to`** and **`duration`** (source window plus trimmed cap).

```yaml
exclude:
  - from: 1:00
    to: 1:30
  - to: 2:00
  - from: 5:00
  - from: 3:00
    duration: 0:45
```

**Speed** — constant or keyframed (linear between points; `at` uses the same time forms as `from` / `to` / `duration`: seconds, `M:SS`, or `H:MM:SS`):

```yaml
speed: 20
```

```yaml
speed:
  - at: 0
    speed: 1
  - at: 120
    speed: 20
```

Constant output length is about `(to − from) / speed` or `duration / speed`. With keyframes, output length follows the integral of `1/speed(t)` (shorter when speed rises). Scene notes: [`explain.txt`](explain.txt).

Validate without ffmpeg:

```bash
make film-validate
```

## Incremental rebuild

Two cache levels:

- **Clips** — `build/clips/<id>.mp4` (per-segment cut). Changing bounds or speed re-renders only that `id`.
- **Assembly** — draft: `build/parts/n-*.mp4` (fan-in of 4: clips → leaves, leaves → internal nodes, top → output). Release: single reencode concat to `build/output.mp4`. A change invalidates only the affected branch (draft) or the full join (release).

Unchanged fingerprints are skipped at all levels. Reordering clips invalidates assembly but may skip re-rendering individual clips.

```bash
make film-clean
```

Removes `build/` only, not sources `1`…`67`.
