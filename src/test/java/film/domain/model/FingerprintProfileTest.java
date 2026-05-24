package film.domain.model;

import film.domain.model.scenario.DifferentProfileFingerprintScenario;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests Fingerprint sensitivity to render profile.
 */
final class FingerprintProfileTest {
    @Test
    void differentProfileProducesDifferentFingerprint() {
        assertThat(
            "draft and release profiles should produce different fingerprints",
            new DifferentProfileFingerprintScenario().matches(),
            is(false)
        );
    }
}
