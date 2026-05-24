package film.domain.model.scenario;

import film.domain.model.AtSecond;
import film.domain.model.Cut;
import film.domain.model.Edits;
import film.domain.model.Fingerprint;
import film.domain.model.Pace;
import film.domain.model.RenderProfile;
import film.domain.model.Second;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.SourceRef;

/**
 * Fingerprints differ when render profile differs.
 */
public final class DifferentProfileFingerprintScenario {
    private final boolean matches;
    public DifferentProfileFingerprintScenario() {
        final SegmentSpec spec = new SegmentSpec(
            new SegmentId("clip"),
            new SourceRef(1),
            new Cut(new Second(0), new AtSecond(new Second(10)), Pace.one(), Edits.none())
        );
        final Second end = new Second(10);
        final Fingerprint draft = spec.fingerprint(end, RenderProfile.draft(), TestBuildSettings.contract());
        final Fingerprint release = spec.fingerprint(end, RenderProfile.release(), TestBuildSettings.contract());
        this.matches = draft.matches(release);
    }
    public boolean matches() {
        return matches;
    }
}
