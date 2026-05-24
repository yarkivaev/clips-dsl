package film.infrastructure.yaml.scenario;

import film.domain.model.SegmentSpec;
import film.infrastructure.yaml.YamlDsl;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * DSL speed keyframe at accepts M:SS time literals.
 */
public final class YamlDslKeyframeAtTimeScenario {
    private final double at;
    public YamlDslKeyframeAtTimeScenario(final Path workspace) {
        try {
            final Path dsl = workspace.resolve("keyframe-at.dsl.yaml");
            Files.writeString(
                dsl,
                """
                version: 1
                output: build/out.mp4
                clips:
                  - id: clip
                    source: 1
                    duration: 60
                    speed:
                      - at: 0
                        speed: 1
                      - at: 0:20
                        speed: 2
                """
            );
            final SegmentSpec spec = new YamlDsl().opened(dsl).timeline().segments().get(0);
            this.at = spec.pace().keyframes().points().get(1).at().amount();
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("cannot write DSL in " + workspace, ex);
        }
    }
    public double at() {
        return at;
    }
}
