package film.domain.model.scenario;

import film.domain.model.AtSecond;
import film.domain.model.Cut;
import film.domain.model.Edits;
import film.domain.model.ExcludeSpan;
import film.domain.model.Excludes;
import film.domain.model.Fingerprint;
import film.domain.model.GapSpan;
import film.domain.model.MediaContract;
import film.domain.model.Pace;
import film.domain.model.RenderProfile;
import film.domain.model.Second;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.SourceRef;
import film.domain.model.TrimCap;

import java.util.List;

/**
 * Fingerprints differ when exclude gaps differ.
 */
public final class DifferentExcludesFingerprintScenario {
    private final boolean differs;
    public DifferentExcludesFingerprintScenario() {
        final Second end = new Second(60);
        final RenderProfile profile = RenderProfile.draft();
        final MediaContract contract = MediaContract.defaults();
        final SegmentSpec left = new SegmentSpec(
            new SegmentId("a"),
            new SourceRef(1),
            new Cut(
                new Second(0),
                new AtSecond(end),
                Pace.one(),
                new Edits(
                    Excludes.of(List.of(new ExcludeSpan(new Second(5), new GapSpan(new Second(5))))),
                    TrimCap.none()
                )
            )
        );
        final SegmentSpec right = new SegmentSpec(
            new SegmentId("b"),
            new SourceRef(1),
            new Cut(new Second(0), new AtSecond(end), Pace.one(), Edits.none())
        );
        final Fingerprint leftPrint = left.fingerprint(end, profile, contract);
        final Fingerprint rightPrint = right.fingerprint(end, profile, contract);
        this.differs = !leftPrint.matches(rightPrint);
    }
    public boolean differs() {
        return differs;
    }
}
