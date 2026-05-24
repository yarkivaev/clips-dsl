package film.infrastructure.yaml.scenario;

import film.infrastructure.yaml.YamlDsl;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * DSL rejects exclude entry with both to and duration.
 */
public final class YamlDslExcludeRejectsBothEndsScenario {
    private final boolean rejected;
    public YamlDslExcludeRejectsBothEndsScenario(final Path workspace) {
        boolean failed = false;
        try {
            final Path dsl = workspace.resolve("bad.dsl.yaml");
            Files.writeString(
                dsl,
                """
                version: 1
                output: build/out.mp4
                clips:
                  - id: clip
                    source: 1
                    from: 0
                    to: 10
                    exclude:
                      - from: 1
                        to: 2
                        duration: 1
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
