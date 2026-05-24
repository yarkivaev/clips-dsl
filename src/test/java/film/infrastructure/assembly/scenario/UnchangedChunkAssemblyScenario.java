package film.infrastructure.assembly.scenario;

import film.domain.model.CachedClip;
import film.domain.model.Manifest;
import film.domain.model.ResolvedEnds;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.Timeline;
import film.domain.port.AssemblyPlan;
import film.infrastructure.assembly.TreeAssembly;
import film.infrastructure.assembly.TreeSnapshot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Prior tree assembly matches desired timeline with all node files present.
 */
public final class UnchangedChunkAssemblyScenario {
    private final boolean empty;
    public UnchangedChunkAssemblyScenario(final Path workspace) {
        final int span = 4;
        final Timeline timeline = AssemblyTimelineFixture.timeline(10);
        final ResolvedEnds ends = AssemblyTimelineFixture.ends(timeline);
        final Path partsDir = workspace.resolve("build/parts");
        final Path output = workspace.resolve("build/output.mp4");
        final TreeSnapshot priorAssembly = TreeSnapshot.wanted(
            timeline, ends, AssemblyTimelineFixture.profile(), AssemblyTimelineFixture.contract(), span, partsDir
        );
        touchNodes(workspace, priorAssembly);
        touchOutput(output);
        final Manifest prior = manifestFor(timeline, ends, priorAssembly, workspace);
        final Map<SegmentId, Path> clips = clipPaths(timeline, workspace);
        final AssemblyPlan plan = new TreeAssembly(
            span, AssemblyTimelineFixture.profile(), AssemblyTimelineFixture.contract()
        ).planned(prior, timeline, ends, clips, workspace, output);
        this.empty = plan.empty();
    }
    public boolean empty() {
        return empty;
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
    private static void touchNodes(final Path workspace, final TreeSnapshot snapshot) {
        try {
            Files.createDirectories(workspace.resolve("build/parts"));
            for (final TreeSnapshot.Node node : snapshot.nodes().values()) {
                Files.createFile(node.path());
            }
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("cannot create node files under " + workspace, ex);
        }
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
