package film.infrastructure.ffmpeg;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests ProbeFrameRate parsing for ffprobe output.
 */
final class ProbeFrameRateTest {
    @Test
    void concatPartAverageRateRoundsToThirty() {
        assertThat(
            "concat part average frame rate should round to 30 fps",
            ProbeFrameRate.parsed("8770560/292417"),
            is(30)
        );
    }
    @Test
    void wholeRateParsesToThirty() {
        assertThat(
            "whole frame rate should parse to 30 fps",
            ProbeFrameRate.parsed("30/1"),
            is(30)
        );
    }
}
