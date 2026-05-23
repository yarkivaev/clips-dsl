package film.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Difference between prior manifest and desired timeline.
 */
public final class PlanDiff {
    private final Manifest prior;
    private final Timeline desired;
    private final ResolvedEnds ends;
    public PlanDiff(final Manifest prior, final Timeline desired, final ResolvedEnds ends) {
        this.prior = prior;
        this.desired = desired;
        this.ends = ends;
    }
    public BuildTasks tasks() {
        final List<Task> steps = new ArrayList<>();
        for (final SegmentSpec spec : desired.segments()) {
            final Second end = ends.end(spec);
            final Fingerprint wanted = spec.fingerprint(end);
            if (prior.cached(spec.id(), wanted)) {
                steps.add(new SkippedTask(new Skipped(spec.id())));
            } else {
                steps.add(new RenderedTask(new Rendered(spec, end)));
            }
        }
        final TimelineFingerprint wantedPrint = desired.print(ends);
        if (!prior.timeline().matches(wantedPrint)) {
            steps.add(new JoinedTask(Joined.INSTANCE));
        }
        return new BuildTasks(desired, ends, steps);
    }
}
