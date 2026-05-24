package film.infrastructure.yaml.scenario;

import film.infrastructure.yaml.YamlDsl;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * DSL rejects speed keyframes without at zero.
 */
public final class YamlDslRejectsBadKeyframesScenario {
    private final boolean rejected;
    public YamlDslRejectsBadKeyframesScenario(final Path workspace) {
        boolean failed = false;
        try {
            final Path dsl = workspace.resolve("bad-keyframes.dsl.yaml");
            Files.writeString(
                dsl,
                """
                version: 1
                output: build/out.mp4
                clips:
                  - id: clip
                    source: 1
                    duration: 10
                    speed:
                      - at: 5
                        speed: 2
                """
            );
            new YamlDsl().opened(dsl);
        } catch (final IllegalStateException ex) {
            failed = true;
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("cannot write DSL in " + workspace, ex);
        }
        this.rejected = failed;
    }
    public boolean rejected() {
        return rejected;
    }
}
