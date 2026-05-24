package film.domain.model;

import film.domain.model.scenario.DifferentExcludesFingerprintScenario;
import film.domain.model.scenario.KeptSpansAbsoluteExcludeScenario;
import film.domain.model.scenario.KeptSpansClippedExcludeScenario;
import film.domain.model.scenario.KeptSpansExcludeScenario;
import film.domain.model.scenario.KeptSpansOpenFromExcludeScenario;
import film.domain.model.scenario.KeptSpansOpenToExcludeScenario;
import film.domain.model.scenario.KeptSpansTrimCapScenario;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests KeptSpans after excludes and trim cap.
 */
final class KeptSpansTest {
    @Test
    void excludeStartingBeforeClipFromClipsToWindowStart() {
        assertThat(
            "exclude before clip from should clip to window start",
            new KeptSpansClippedExcludeScenario().play(),
            is(120.0)
        );
    }
    @Test
    void absoluteExcludeDoesNotShiftByClipFrom() {
        assertThat(
            "exclude at 60-70 on source should leave 145 seconds when clip starts at 45",
            new KeptSpansAbsoluteExcludeScenario().play(),
            is(145.0)
        );
    }
    @Test
    void openFromExcludeUsesClipWindowStart() {
        assertThat(
            "exclude without from should drop from clip window start to to",
            new KeptSpansOpenFromExcludeScenario().play(),
            is(40.0)
        );
    }
    @Test
    void openToExcludeUsesClipWindowEnd() {
        assertThat(
            "exclude without to should drop from absolute from through window end",
            new KeptSpansOpenToExcludeScenario().play(),
            is(20.0)
        );
    }
    @Test
    void excludesReducePlayLengthAlongTrimmedTimeline() {
        assertThat(
            "excludes should leave 80 seconds of play from a 100 second window",
            new KeptSpansExcludeScenario().play(),
            is(80.0)
        );
    }
    @Test
    void trimCapLimitsPlayLengthOnTrimmedTimeline() {
        assertThat(
            "trim cap should limit play to requested trimmed seconds",
            new KeptSpansTrimCapScenario().play(),
            is(25.0)
        );
    }
    @Test
    void differentExcludesYieldDifferentFingerprints() {
        assertThat(
            "different excludes should change fingerprint",
            new DifferentExcludesFingerprintScenario().differs(),
            is(true)
        );
    }
}
