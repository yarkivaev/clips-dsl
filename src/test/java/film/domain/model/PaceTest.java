package film.domain.model;

import film.domain.model.scenario.DifferentPaceFingerprintScenario;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests Pace affects segment fingerprint.
 */
final class PaceTest {
    @Test
    void differentPaceProducesDifferentFingerprint() {
        assertThat(
            "different pace factor should change fingerprint",
            new DifferentPaceFingerprintScenario().matches(),
            is(false)
        );
    }
}
