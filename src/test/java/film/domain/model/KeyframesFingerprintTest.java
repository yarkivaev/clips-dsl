package film.domain.model;

import film.domain.model.scenario.DifferentKeyframesFingerprintScenario;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests keyframed pace affects segment fingerprint.
 */
final class KeyframesFingerprintTest {
    @Test
    void differentKeyframesProduceDifferentFingerprint() {
        assertThat(
            "different keyframes should change fingerprint",
            new DifferentKeyframesFingerprintScenario().matches(),
            is(false)
        );
    }
}
