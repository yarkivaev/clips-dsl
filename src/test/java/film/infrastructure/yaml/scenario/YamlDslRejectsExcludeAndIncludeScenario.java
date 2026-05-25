package film.infrastructure.yaml.scenario;

import film.infrastructure.yaml.YamlDsl;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * DSL rejects clip with both exclude and include.
 */
public final class YamlDslRejectsExcludeAndIncludeScenario {
    private final boolean rejected;
    public YamlDslRejectsExcludeAndIncludeScenario(final Path workspace) {
        boolean failed = false;
        try {
            final Path dsl = workspace.resolve("both.dsl.yaml");
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
                      - from: 2
                        to: 3
                    include:
                      - from: 5
                        to: 6
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
