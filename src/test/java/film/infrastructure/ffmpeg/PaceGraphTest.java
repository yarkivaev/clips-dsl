package film.infrastructure.ffmpeg;

import film.infrastructure.ffmpeg.scenario.PaceGraphDecelerationRampScenario;
import film.infrastructure.ffmpeg.scenario.PaceGraphFilterScenario;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests PaceGraph builds expected audio filters.
 */
final class PaceGraphTest {
    @Test
    void rubberbandGraphContainsRubberbandFilter() {
        assertThat(
            "rubberband graph should contain rubberband filter",
            new PaceGraphFilterScenario(true).matches(),
            is(true)
        );
    }
    @Test
    void fallbackGraphContainsAtempoFilter() {
        assertThat(
            "fallback graph should contain atempo filter",
            new PaceGraphFilterScenario(false).matches(),
            is(true)
        );
    }
    @Test
    void decelerationRampUsesIntegratedSetptsExpression() {
        assertThat(
            "deceleration ramp should use log integrated setpts",
            new PaceGraphDecelerationRampScenario().matches(),
            is(true)
        );
    }
}
