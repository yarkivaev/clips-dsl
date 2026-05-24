package film.infrastructure.yaml.scenario;

import film.infrastructure.yaml.YamlDsl;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * DSL clip with both to and duration is rejected.
 */
public final class YamlDslRejectsToAndDurationScenario {
    private final boolean rejected;
    public YamlDslRejectsToAndDurationScenario(final Path workspace) {
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
                    to: 30
                    duration: 20
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
