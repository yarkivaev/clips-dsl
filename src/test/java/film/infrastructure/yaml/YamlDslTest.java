package film.infrastructure.yaml;

import film.infrastructure.yaml.scenario.YamlDslDurationEndScenario;
import film.infrastructure.yaml.scenario.YamlDslRejectsToAndDurationScenario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests YamlDsl clip end bounds from duration and to.
 */
final class YamlDslTest {
    @Test
    void durationResolvesEndFromPlusFrom(@TempDir final Path workspace) {
        assertThat(
            "duration should resolve end as from plus duration",
            new YamlDslDurationEndScenario(workspace).amount(),
            is(30.0)
        );
    }
    @Test
    void clipCannotSetToAndDurationTogether(@TempDir final Path workspace) {
        assertThat(
            "clip must not accept both to and duration",
            new YamlDslRejectsToAndDurationScenario(workspace).rejected(),
            is(true)
        );
    }
}
