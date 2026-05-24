package film.domain.model.scenario;

import film.domain.model.AtSpan;
import film.domain.model.Cut;
import film.domain.model.Edits;
import film.domain.model.Pace;
import film.domain.model.Second;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.SourceRef;
import film.domain.port.Duration;

import java.nio.file.Path;

/**
 * Resolving AtSpan adds span to from on the source timeline.
 */
public final class DurationEndScenario {
    private final double amount;
    public DurationEndScenario() {
        final SegmentSpec spec = new SegmentSpec(
            new SegmentId("clip"),
            new SourceRef(3),
            new Cut(new Second(10), new AtSpan(new Second(20)), Pace.one(), Edits.none())
        );
        final Duration duration = (source, workspace) -> new Second(999);
        final Second end = spec.end().resolved(spec, duration, Path.of("."));
        this.amount = end.amount();
    }
    public double amount() {
        return amount;
    }
}
