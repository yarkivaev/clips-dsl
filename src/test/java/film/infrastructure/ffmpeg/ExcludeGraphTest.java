package film.infrastructure.ffmpeg;

import film.infrastructure.ffmpeg.scenario.ExcludeGraphConcatScenario;
import film.infrastructure.ffmpeg.scenario.ExcludeGraphSegmentSpeedScenario;
import film.infrastructure.ffmpeg.scenario.TrimmedUnityPaceGraphScenario;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests ExcludeGraph filter_complex for kept spans.
 */
final class ExcludeGraphTest {
    @Test
    void concatGraphMultipliesClipAndSegmentSpeed() {
        assertThat(
            "concat graph should apply clip and segment speed product per part",
            new ExcludeGraphSegmentSpeedScenario().matches(),
            is(true)
        );
    }
    @Test
    void unitySpeedTrimmedGraphAvoidsCopyFilter() {
        assertThat(
            "unity speed trimmed graph should not use copy filter",
            new TrimmedUnityPaceGraphScenario().matches(),
            is(true)
        );
    }
    @Test
    void concatGraphTrimsKeptSpansAndJoinsThem() {
        assertThat(
            "concat graph should trim each kept span and concat",
            new ExcludeGraphConcatScenario().matches(),
            is(true)
        );
    }
}
