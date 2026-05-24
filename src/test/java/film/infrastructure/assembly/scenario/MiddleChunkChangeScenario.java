package film.infrastructure.assembly.scenario;

import film.domain.model.AtSecond;
import film.domain.model.CachedClip;
import film.domain.model.Cut;
import film.domain.model.Fingerprint;
import film.domain.model.Manifest;
import film.domain.model.Pace;
import film.domain.model.ResolvedEnds;
import film.domain.model.Second;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.SourceRef;
import film.domain.model.Timeline;
import film.domain.port.AssemblyPlan;
import film.infrastructure.assembly.ChunkAssembly;
import film.infrastructure.assembly.ChunkPlan;
import film.infrastructure.assembly.ChunkSnapshot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One segment change in the second chunk invalidates only that leaf part.
 */
public final class MiddleChunkChangeScenario {
    private final List<String> staleNodes;
    private final boolean root;
    public MiddleChunkChangeScenario(final Path workspace) {
        final int span = 8;
        final Timeline priorTimeline = AssemblyTimelineFixture.timeline(16);
        final ResolvedEnds priorEnds = AssemblyTimelineFixture.ends(priorTimeline);
        final Path partsDir = workspace.resolve("build/parts");
        final ChunkSnapshot priorAssembly = ChunkSnapshot.wanted(priorTimeline, priorEnds, span, partsDir);
        touchParts(workspace, priorAssembly);
        touchOutput(workspace.resolve("build/output.mp4"));
        final Manifest prior = manifestFor(priorTimeline, priorEnds, priorAssembly, workspace);
        final List<SegmentSpec> desiredSegments = new ArrayList<>(priorTimeline.segments());
        final SegmentSpec changed = desiredSegments.get(9);
        desiredSegments.set(
            9,
            new SegmentSpec(
                changed.id(),
                changed.source(),
                new Cut(changed.from(), new AtSecond(new Second(20)), Pace.one())
            )
        );
        final Timeline desired = new Timeline(desiredSegments);
        final Map<SegmentId, Second> endMap = new HashMap<>();
        for (final SegmentSpec spec : desired.segments()) {
            endMap.put(spec.id(), spec.id().label().equals("seg-9") ? new Second(20) : new Second(10));
        }
        final ResolvedEnds desiredEnds = new ResolvedEnds(endMap);
        final Map<SegmentId, Path> clips = clipPaths(desired, workspace);
        final AssemblyPlan plan = new ChunkAssembly(span).planned(
            prior, desired, desiredEnds, clips, workspace, workspace.resolve("build/output.mp4")
        );
        final ChunkPlan chunk = (ChunkPlan) plan;
        this.staleNodes = chunk.staleNodeIds();
        this.root = chunk.root();
    }
    public List<String> staleNodes() {
        return staleNodes;
    }
    public boolean root() {
        return root;
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
