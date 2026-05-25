package film.infrastructure.yaml;

import film.infrastructure.yaml.scenario.YamlDslDurationEndScenario;
import film.infrastructure.yaml.scenario.YamlDslEditSpanSpeedScenario;
import film.infrastructure.yaml.scenario.YamlDslExcludePlayScenario;
import film.infrastructure.yaml.scenario.YamlDslIncludePlayScenario;
import film.infrastructure.yaml.scenario.YamlDslOpenExcludeScenario;
import film.infrastructure.yaml.scenario.YamlDslRejectsExcludeAndIncludeScenario;
import film.infrastructure.yaml.scenario.YamlDslExcludeRejectsBothEndsScenario;
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
    @Test
    void editSpanSpeedParsesFromIncludeEntry(@TempDir final Path workspace) {
        assertThat(
            "include entry speed 2 should halve trimmed play for 20 source seconds",
            new YamlDslEditSpanSpeedScenario(workspace).play(),
            is(10.0)
        );
    }
    @Test
    void includeKeepsOnlyListedSpans(@TempDir final Path workspace) {
        assertThat(
            "include should parse and keep only listed source spans",
            new YamlDslIncludePlayScenario(workspace).play(),
            is(25.0)
        );
    }
    @Test
    void clipCannotSetExcludeAndIncludeTogether(@TempDir final Path workspace) {
        assertThat(
            "clip must not accept both exclude and include",
            new YamlDslRejectsExcludeAndIncludeScenario(workspace).rejected(),
            is(true)
        );
    }
    @Test
    void excludeMayOmitFromForWindowStart(@TempDir final Path workspace) {
        assertThat(
            "exclude without from should parse and keep tail of clip window",
            new YamlDslOpenExcludeScenario(workspace).play(),
            is(40.0)
        );
    }
    @Test
    void excludeDurationCapsTrimmedPlayLength(@TempDir final Path workspace) {
        assertThat(
            "exclude with duration should cap play on trimmed timeline",
            new YamlDslExcludePlayScenario(workspace).play(),
            is(30.0)
        );
    }
    @Test
    void excludeCannotSetToAndDurationTogether(@TempDir final Path workspace) {
        assertThat(
            "exclude must not accept both to and duration",
            new YamlDslExcludeRejectsBothEndsScenario(workspace).rejected(),
            is(true)
        );
    }
}
