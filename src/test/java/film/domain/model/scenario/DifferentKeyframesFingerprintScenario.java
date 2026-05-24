package film.domain.model.scenario;

import film.domain.model.AtSecond;
import film.domain.model.Cut;
import film.domain.model.Fingerprint;
import film.domain.model.Keyframe;
import film.domain.model.Keyframes;
import film.domain.model.Pace;
import film.domain.model.Second;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.SourceRef;

import java.util.List;

/**
 * Fingerprints differ when speed keyframes differ.
 */
public final class DifferentKeyframesFingerprintScenario {
    private final boolean matches;
    public DifferentKeyframesFingerprintScenario() {
        final Pace slow = new Pace(Keyframes.of(List.of(
            new Keyframe(new Second(0), 1.0),
            new Keyframe(new Second(10), 2.0)
        )));
        final Pace fast = new Pace(Keyframes.of(List.of(
            new Keyframe(new Second(0), 1.0),
            new Keyframe(new Second(10), 4.0)
        )));
        final SegmentSpec leftSpec = new SegmentSpec(
            new SegmentId("clip"),
            new SourceRef(1),
            new Cut(new Second(0), new AtSecond(new Second(10)), slow)
        );
        final SegmentSpec rightSpec = new SegmentSpec(
            new SegmentId("clip"),
            new SourceRef(1),
            new Cut(new Second(0), new AtSecond(new Second(10)), fast)
        );
        final Second end = new Second(10);
        final Fingerprint left = leftSpec.fingerprint(end);
        final Fingerprint right = rightSpec.fingerprint(end);
        this.matches = left.matches(right);
    }
    public boolean matches() {
        return matches;
    }
}
