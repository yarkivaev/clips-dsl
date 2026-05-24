package film.domain.model.scenario;

import film.domain.model.AtSecond;
import film.domain.model.Cut;
import film.domain.model.Edits;
import film.domain.model.Fingerprint;
import film.domain.model.Pace;
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
            new Cut(new Second(1), new AtSecond(new Second(9)), Pace.one(), Edits.none())
        );
        final Second end = new Second(9);
        final Fingerprint left = spec.fingerprint(end, TestBuildSettings.profile(), TestBuildSettings.contract());
        final Fingerprint right = spec.fingerprint(end, TestBuildSettings.profile(), TestBuildSettings.contract());
        this.matches = left.matches(right);
    }
    public boolean matches() {
        return matches;
    }
}
