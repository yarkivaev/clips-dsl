package film.infrastructure.yaml;

import film.infrastructure.yaml.scenario.YamlDslDurationEndScenario;
import film.infrastructure.yaml.scenario.YamlDslKeyframeAtTimeScenario;
import film.infrastructure.yaml.scenario.YamlDslKeyframesScenario;
import film.infrastructure.yaml.scenario.YamlDslRejectsBadKeyframesScenario;
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
    @Test
    void speedKeyframeListParsesIntoPaceLabel(@TempDir final Path workspace) {
        assertThat(
            "speed keyframes should parse into pace label",
            new YamlDslKeyframesScenario(workspace).label(),
            is("0.0:1.0;120.0:10.0")
        );
    }
    @Test
    void speedKeyframesRequireAtZero(@TempDir final Path workspace) {
        assertThat(
            "speed keyframes must start at zero",
            new YamlDslRejectsBadKeyframesScenario(workspace).rejected(),
            is(true)
        );
    }
    @Test
    void speedKeyframeAtAcceptsMinuteSecondLiteral(@TempDir final Path workspace) {
        assertThat(
            "keyframe at should parse M:SS time literal as seconds",
            new YamlDslKeyframeAtTimeScenario(workspace).at(),
            is(20.0)
        );
    }
}
