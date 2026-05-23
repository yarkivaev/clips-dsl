package film.domain.model;

import film.domain.model.scenario.FingerprintPairScenario;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests Fingerprint equality for segment specs.
 */
final class FingerprintTest {
    @Test
    void sameSpecProducesMatchingFingerprint() {
        assertThat(
            "identical segment bounds should produce matching fingerprints",
            new FingerprintPairScenario().matches(),
            is(true)
        );
    }
}
