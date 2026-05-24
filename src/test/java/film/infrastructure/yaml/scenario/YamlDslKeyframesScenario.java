package film.infrastructure.yaml.scenario;

import film.domain.model.Pace;
import film.domain.model.SegmentSpec;
import film.infrastructure.yaml.YamlDsl;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * DSL speed keyframe list parses into pace curve.
 */
public final class YamlDslKeyframesScenario {
    private final String label;
    public YamlDslKeyframesScenario(final Path workspace) {
        try {
            final Path dsl = workspace.resolve("keyframes.dsl.yaml");
            Files.writeString(
                dsl,
                """
                version: 1
                output: build/out.mp4
                clips:
                  - id: clip
                    source: 1
                    duration: 300
                    speed:
                      - at: 0
                        speed: 1
                      - at: 120
                        speed: 10
                """
            );
            final SegmentSpec spec = new YamlDsl().opened(dsl).timeline().segments().get(0);
            final Pace pace = spec.pace();
            this.label = pace.label();
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("cannot write DSL in " + workspace, ex);
        }
    }
    public String label() {
        return label;
    }
}
