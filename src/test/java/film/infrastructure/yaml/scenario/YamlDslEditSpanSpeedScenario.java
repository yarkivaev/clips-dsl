package film.infrastructure.yaml.scenario;

import film.domain.model.KeptSpans;
import film.domain.model.ResolvedEnds;
import film.domain.model.SegmentSpec;
import film.domain.port.Duration;
import film.infrastructure.yaml.YamlDsl;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * DSL include entry speed parses into segment pace for play length.
 */
public final class YamlDslEditSpanSpeedScenario {
    private final double play;
    public YamlDslEditSpanSpeedScenario(final Path workspace) {
        try {
            final Path dsl = workspace.resolve("speed.dsl.yaml");
            Files.writeString(
                dsl,
                """
                version: 1
                output: build/out.mp4
                clips:
                  - id: clip
                    source: 1
                    from: 0
                    to: 100
                    include:
                      - from: 0
                        duration: 20
                        speed: 2
                """
            );
            final SegmentSpec spec = new YamlDsl().opened(dsl).timeline().segments().get(0);
            final Duration probe = (source, root) -> new film.domain.model.Second(0);
            final ResolvedEnds ends = ResolvedEnds.of(
                new film.domain.model.Timeline(java.util.List.of(spec)),
                s -> s.end().resolved(s, probe, workspace)
            );
            this.play = KeptSpans.from(spec, ends.end(spec)).play();
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("cannot write DSL in " + workspace, ex);
        }
    }
    public double play() {
        return play;
    }
}
