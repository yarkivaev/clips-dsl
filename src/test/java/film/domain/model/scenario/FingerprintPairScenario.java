package film.domain.model.scenario;

import film.domain.model.AtSecond;
import film.domain.model.Fingerprint;
import film.domain.model.Second;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.SourceRef;

/**
 * Two fingerprints from the same segment bounds.
 */
public final class FingerprintPairScenario {
    private final boolean matches;
    public FingerprintPairScenario() {
        final SegmentSpec spec = new SegmentSpec(
            new SegmentId("clip"),
            new SourceRef(3),
            new Second(1),
            new AtSecond(new Second(9))
        );
        final Second end = new Second(9);
        final Fingerprint left = spec.fingerprint(end);
        final Fingerprint right = spec.fingerprint(end);
        this.matches = left.matches(right);
    }
    public boolean matches() {
        return matches;
    }
}
