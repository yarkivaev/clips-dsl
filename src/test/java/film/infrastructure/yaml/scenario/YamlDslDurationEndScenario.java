package film.infrastructure.yaml.scenario;

import film.domain.model.ResolvedEnds;
import film.domain.model.SegmentSpec;
import film.domain.port.Duration;
import film.infrastructure.yaml.YamlDsl;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * DSL duration key resolves end as from plus duration.
 */
public final class YamlDslDurationEndScenario {
    private final double amount;
    public YamlDslDurationEndScenario(final Path workspace) {
        try {
            final Path dsl = workspace.resolve("duration.dsl.yaml");
            Files.writeString(
                dsl,
                """
                version: 1
                output: build/out.mp4
                clips:
                  - id: clip
                    source: 1
                    from: 10
                    duration: 20
                """
            );
            final SegmentSpec spec = new YamlDsl().opened(dsl).timeline().segments().get(0);
            final Duration duration = (source, root) -> new film.domain.model.Second(0);
            final ResolvedEnds ends = ResolvedEnds.of(
                new film.domain.model.Timeline(java.util.List.of(spec)),
                s -> s.end().resolved(s, duration, workspace)
            );
            this.amount = ends.end(spec).amount();
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("cannot write DSL in " + workspace, ex);
        }
    }
    public double amount() {
        return amount;
    }
}
