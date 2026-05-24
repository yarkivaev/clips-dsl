package film.infrastructure.assembly.scenario;

import film.domain.model.CachedClip;
import film.domain.model.Manifest;
import film.domain.model.ResolvedEnds;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.Timeline;
import film.infrastructure.assembly.TreeAssembly;
import film.infrastructure.assembly.TreePlan;
import film.infrastructure.assembly.TreeSnapshot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prior tree digests match desired timeline but node files are absent on disk.
 */
public final class MatchedTreeMissingFilesScenario {
    private final List<String> staleNodes;
    public MatchedTreeMissingFilesScenario(final Path workspace) {
        final int span = 4;
        final Timeline timeline = AssemblyTimelineFixture.timeline(9);
        final ResolvedEnds ends = AssemblyTimelineFixture.ends(timeline);
        final Path partsDir = workspace.resolve("build/parts");
        final Path output = workspace.resolve("build/output.mp4");
        touchOutput(output);
        final TreeSnapshot priorAssembly = TreeSnapshot.wanted(
            timeline, ends, AssemblyTimelineFixture.profile(), AssemblyTimelineFixture.contract(), span, partsDir
        );
        final Manifest prior = manifestFor(timeline, ends, priorAssembly, workspace);
        final Map<SegmentId, Path> clips = clipPaths(timeline, workspace);
        final TreePlan plan = (TreePlan) new TreeAssembly(
            span, AssemblyTimelineFixture.profile(), AssemblyTimelineFixture.contract()
        ).planned(
            prior, timeline, ends, clips, workspace, output
        );
        this.staleNodes = plan.staleNodeIds();
    }
    public List<String> staleNodes() {
        return staleNodes;
    }
    private static Manifest manifestFor(
        final Timeline timeline,
        final ResolvedEnds ends,
        final TreeSnapshot assembly,
        final Path workspace
    ) {
        final Map<SegmentId, CachedClip> clips = new HashMap<>();
        for (final SegmentSpec spec : timeline.segments()) {
            final Path path = workspace.resolve("build/clips/" + spec.id().label() + ".mp4");
            clips.put(
                spec.id(),
                new CachedClip(spec.id(), spec.fingerprint(ends.end(spec), AssemblyTimelineFixture.profile(), AssemblyTimelineFixture.contract()), path)
            );
        }
        return new Manifest(AssemblyTimelineFixture.profile(), assembly, clips);
    }
    private static Map<SegmentId, Path> clipPaths(final Timeline timeline, final Path workspace) {
        final Map<SegmentId, Path> clips = new HashMap<>();
        for (final SegmentSpec spec : timeline.segments()) {
            clips.put(spec.id(), workspace.resolve("build/clips/" + spec.id().label() + ".mp4"));
        }
        return clips;
    }
    private static void touchOutput(final Path output) {
        try {
            Files.createDirectories(output.getParent());
            Files.createFile(output);
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("cannot create output file " + output, ex);
        }
    }
}
