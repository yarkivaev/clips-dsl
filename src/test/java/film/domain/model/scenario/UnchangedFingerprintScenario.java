package film.domain.model.scenario;

import film.domain.model.AtSecond;
import film.domain.model.BuildTasks;
import film.domain.model.Cut;
import film.domain.model.Edits;
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
import film.domain.model.VacantAssemblySnapshot;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Prior manifest matches desired timeline fingerprints.
 */
public final class UnchangedFingerprintScenario {
    private final BuildTasks tasks;
    private final Timeline timeline;
    private final ResolvedEnds ends;
    public UnchangedFingerprintScenario() {
        final SegmentSpec alpha = new SegmentSpec(
            new SegmentId("alpha"),
            new SourceRef(1),
            new Cut(new Second(0), new AtSecond(new Second(10)), Pace.one(), Edits.none())
        );
        final SegmentSpec beta = new SegmentSpec(
            new SegmentId("beta"),
            new SourceRef(2),
            new Cut(new Second(5), new AtSecond(new Second(15)), Pace.one(), Edits.none())
        );
        final Timeline timeline = new Timeline(List.of(alpha, beta));
        final ResolvedEnds ends = new ResolvedEnds(
            Map.of(
                alpha.id(), new Second(10),
                beta.id(), new Second(15)
            )
        );
        this.timeline = timeline;
        this.ends = ends;
        final Manifest prior = manifestFor(timeline, ends);
        final PlanDiff diff = prior.diff(
            timeline,
            ends,
            TestBuildSettings.profile(),
            TestBuildSettings.contract()
        );
        this.tasks = diff.tasks();
    }
    public Timeline timeline() {
        return timeline;
    }
    public ResolvedEnds ends() {
        return ends;
    }
    public BuildTasks tasks() {
        return tasks;
    }
    public List<Class<?>> taskTypes() {
        return tasks.types();
    }
    private static Manifest manifestFor(final Timeline timeline, final ResolvedEnds ends) {
        final SegmentSpec alpha = timeline.segments().get(0);
        final SegmentSpec beta = timeline.segments().get(1);
        final Fingerprint alphaPrint = alpha.fingerprint(
            ends.end(alpha), TestBuildSettings.profile(), TestBuildSettings.contract()
        );
        final Fingerprint betaPrint = beta.fingerprint(
            ends.end(beta), TestBuildSettings.profile(), TestBuildSettings.contract()
        );
        return new Manifest(
            TestBuildSettings.profile(),
            new VacantAssemblySnapshot(),
            Map.of(
                alpha.id(), new CachedClip(alpha.id(), alphaPrint, Path.of("build/clips/alpha.mp4")),
                beta.id(), new CachedClip(beta.id(), betaPrint, Path.of("build/clips/beta.mp4"))
            )
        );
    }
}
