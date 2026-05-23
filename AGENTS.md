# Agent notes

## Film compiler

Java 21 + Maven under `src/main/java/film/`. **Elegant Objects** and **DDD** are required.

### Layers

- `film.domain.model` — immutable domain; no ffmpeg/YAML imports
- `film.domain.port` — interfaces (`Dsl`, `Clip`, `Concat`, …)
- `film.application` — `FilmBuild` use case
- `film.infrastructure.*` — adapters only
- `film.Application` — composition root (`main`)

Dependencies point inward: infrastructure → application → domain. Domain must not import infrastructure.

### Style

- No class names ending in `-er`
- No `null`; no static helpers in domain
- Tests: one `assertThat` per `@Test`; Hamcrest matchers; state in `src/test/java/film/domain/model/scenario/`

### Commands

```bash
make test
make film
make film-validate
make film-clean
```

ffmpeg/ffprobe stream to `build/logs/` via `FfmpegProcess` (flush per line; heartbeats in `film.log`).
