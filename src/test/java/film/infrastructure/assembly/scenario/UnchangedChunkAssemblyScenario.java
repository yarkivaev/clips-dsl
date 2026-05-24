package film.infrastructure.assembly.scenario;

import film.domain.model.CachedClip;
import film.domain.model.Fingerprint;
import film.domain.model.Manifest;
import film.domain.model.ResolvedEnds;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.Timeline;
import film.domain.port.AssemblyPlan;
import film.infrastructure.assembly.ChunkAssembly;
import film.infrastructure.assembly.ChunkSnapshot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Prior chunk assembly matches desired timeline with all part files present.
 */
public final class UnchangedChunkAssemblyScenario {
    private final boolean empty;
    public UnchangedChunkAssemblyScenario(final Path workspace) {
        final int span = 8;
        final Timeline timeline = AssemblyTimelineFixture.timeline(10);
        final ResolvedEnds ends = AssemblyTimelineFixture.ends(timeline);
        final Path partsDir = workspace.resolve("build/parts");
        final Path output = workspace.resolve("build/output.mp4");
        final ChunkSnapshot wanted = ChunkSnapshot.wanted(timeline, ends, span, partsDir);
        touchParts(workspace, wanted);
        touchOutput(output);
        final Manifest prior = manifestFor(timeline, ends, wanted, workspace);
        final Map<SegmentId, Path> clips = clipPaths(timeline, workspace);
        final AssemblyPlan plan = new ChunkAssembly(span).planned(prior, timeline, ends, clips, workspace, output);
        this.empty = plan.empty();
    }
    public boolean empty() {
        return empty;
    }
    private static Manifest manifestFor(
        final Timeline timeline,
        final ResolvedEnds ends,
        final ChunkSnapshot assembly,
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
        return new Manifest(assembly, clips);
    }
    private static Map<SegmentId, Path> clipPaths(final Timeline timeline, final Path workspace) {
        final Map<SegmentId, Path> clips = new HashMap<>();
        for (final SegmentSpec spec : timeline.segments()) {
            clips.put(spec.id(), workspace.resolve("build/clips/" + spec.id().label() + ".mp4"));
        }
        return clips;
    }
    private static void touchParts(final Path workspace, final ChunkSnapshot snapshot) {
        try {
            Files.createDirectories(workspace.resolve("build/parts"));
            for (final ChunkSnapshot.Node node : snapshot.nodes().values()) {
                Files.createFile(node.path());
            }
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("cannot create part files under " + workspace, ex);
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
