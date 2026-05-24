package film.infrastructure.assembly.scenario;

import film.domain.model.CachedClip;
import film.domain.model.Manifest;
import film.domain.model.ResolvedEnds;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.Timeline;
import film.domain.model.VacantAssemblySnapshot;
import film.domain.port.AssemblyPlan;
import film.infrastructure.assembly.ChunkAssembly;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Legacy manifest without assembly section yields a non-empty assembly plan.
 */
public final class LegacyManifestScenario {
    private final boolean empty;
    public LegacyManifestScenario(final Path workspace) {
        final Timeline timeline = AssemblyTimelineFixture.timeline(4);
        final ResolvedEnds ends = AssemblyTimelineFixture.ends(timeline);
        final Manifest prior = manifestFor(timeline, ends, workspace);
        final Map<SegmentId, Path> clips = clipPaths(timeline, workspace);
        final AssemblyPlan plan = new ChunkAssembly(8).planned(
            prior,
            timeline,
            ends,
            clips,
            workspace,
            workspace.resolve("build/output.mp4")
        );
        this.empty = plan.empty();
    }
    public boolean empty() {
        return empty;
    }
    private static Manifest manifestFor(
        final Timeline timeline,
        final ResolvedEnds ends,
        final Path workspace
    ) {
        final Map<SegmentId, CachedClip> clips = new HashMap<>();
        for (final SegmentSpec spec : timeline.segments()) {
            final Path path = workspace.resolve("build/clips/" + spec.id().label() + ".mp4");
            clips.put(
                spec.id(),
                new CachedClip(spec.id(), spec.fingerprint(ends.end(spec)), path)
            );
        }
        return new Manifest(new VacantAssemblySnapshot(), clips);
    }
    private static Map<SegmentId, Path> clipPaths(final Timeline timeline, final Path workspace) {
        final Map<SegmentId, Path> clips = new HashMap<>();
        for (final SegmentSpec spec : timeline.segments()) {
            clips.put(spec.id(), workspace.resolve("build/clips/" + spec.id().label() + ".mp4"));
        }
        return clips;
    }
}
