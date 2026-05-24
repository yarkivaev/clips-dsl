package film.domain.model.scenario;

import film.domain.model.AtSecond;
import film.domain.model.Cut;
import film.domain.model.Fingerprint;
import film.domain.model.Pace;
import film.domain.model.Second;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.SourceRef;

/**
 * Fingerprints differ when pace factor differs.
 */
public final class DifferentPaceFingerprintScenario {
    private final boolean matches;
    public DifferentPaceFingerprintScenario() {
        final SegmentSpec normal = new SegmentSpec(
            new SegmentId("clip"),
            new SourceRef(1),
            new Cut(new Second(0), new AtSecond(new Second(10)), Pace.one())
        );
        final SegmentSpec fast = new SegmentSpec(
            new SegmentId("clip"),
            new SourceRef(1),
            new Cut(new Second(0), new AtSecond(new Second(10)), new Pace(2.0))
        );
        final Second end = new Second(10);
        final Fingerprint left = normal.fingerprint(end);
        final Fingerprint right = fast.fingerprint(end);
        this.matches = left.matches(right);
    }
    public boolean matches() {
        return matches;
    }
}
