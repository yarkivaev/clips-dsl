package film.infrastructure.ffmpeg;

import film.infrastructure.ffmpeg.scenario.ExcludeGraphConcatScenario;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests ExcludeGraph filter_complex for kept spans.
 */
final class ExcludeGraphTest {
    @Test
    void concatGraphTrimsKeptSpansAndJoinsThem() {
        assertThat(
            "concat graph should trim each kept span and concat",
            new ExcludeGraphConcatScenario().matches(),
            is(true)
        );
    }
}
