package film.domain.model.scenario;

import film.domain.model.AtSecond;
import film.domain.model.BuildTasks;
import film.domain.model.Cut;
import film.domain.model.Pace;
import film.domain.model.CachedClip;
import film.domain.model.Fingerprint;
import film.domain.model.Manifest;
import film.domain.model.PlanDiff;
import film.domain.model.ResolvedEnds;
import film.domain.model.Second;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.SourceRef;
import film.domain.model.Timeline;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Desired timeline changes one segment end bound versus prior manifest.
 */
public final class ChangedSegmentBoundScenario {
    private final BuildTasks tasks;
    public ChangedSegmentBoundScenario() {
        final SegmentSpec alpha = new SegmentSpec(
            new SegmentId("alpha"),
            new SourceRef(1),
            new Cut(new Second(0), new AtSecond(new Second(10)), Pace.one())
        );
        final SegmentSpec beta = new SegmentSpec(
            new SegmentId("beta"),
            new SourceRef(2),
            new Cut(new Second(5), new AtSecond(new Second(20)), Pace.one())
        );
        final ResolvedEnds desiredEnds = new ResolvedEnds(
            Map.of(
                alpha.id(), new Second(10),
                beta.id(), new Second(20)
            )
        );
        final ResolvedEnds priorEnds = new ResolvedEnds(
            Map.of(
                alpha.id(), new Second(10),
                beta.id(), new Second(15)
            )
        );
        final Timeline desired = new Timeline(List.of(alpha, beta));
        final Timeline priorTimeline = new Timeline(List.of(alpha, beta));
        final Manifest prior = manifestFor(priorTimeline, priorEnds);
        final PlanDiff diff = prior.diff(desired, desiredEnds);
        this.tasks = diff.tasks();
    }
    public boolean renderScheduled() {
        for (final var step : tasks.steps()) {
            if (step.render()) {
                return true;
            }
        }
        return false;
    }
    public boolean joinScheduled() {
        return tasks.joinScheduled();
    }
    private static Manifest manifestFor(final Timeline timeline, final ResolvedEnds ends) {
        final SegmentSpec alpha = timeline.segments().get(0);
        final SegmentSpec beta = timeline.segments().get(1);
        return new Manifest(
            timeline.print(ends),
            Map.of(
                alpha.id(), new CachedClip(
                    alpha.id(),
                    alpha.fingerprint(ends.end(alpha)),
                    Path.of("build/clips/alpha.mp4")
                ),
                beta.id(), new CachedClip(
                    beta.id(),
                    beta.fingerprint(ends.end(beta)),
                    Path.of("build/clips/beta.mp4")
                )
            )
        );
    }
}
