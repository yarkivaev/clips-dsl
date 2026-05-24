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

```bash
make test
make film
```

Output: `build/output.mp4`. Clip cache: `build/clips/<id>.mp4`. State: `build/manifest.json`.

Logs (written live during build): `build/logs/`

- `film.log` — high-level progress and “still running” heartbeats every 30s
- `cut-<id>.log` — ffmpeg output while rendering each clip
- `concat.log` — final join
- `probe-<n>.log` — ffprobe when resolving end-of-file

While a step runs:

```bash
tail -f build/logs/cut-beavers.log
tail -f build/logs/film.log
```

## Edit DSL

Edit [`film.dsl.yaml`](film.dsl.yaml): each clip has stable `id`, `source` (symlink number), optional `from` (seconds on source), end bound as **`to`** (absolute second on source) **or** **`duration`** (length from `from`; do not use both).

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

Changing one clip’s bounds or speed curve re-renders only that `id` and re-concats. Unchanged fingerprints are skipped. Reordering clips re-concats only.

Clips and the final join are encoded as **h264/aac** (not stream copy) so VLC does not freeze when timestamps differ between DJI segments.

```bash
make film-clean
```

Removes `build/` only, not sources `1`…`67`.
