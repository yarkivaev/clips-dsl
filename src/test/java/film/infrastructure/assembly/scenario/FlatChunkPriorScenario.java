package film.infrastructure.assembly.scenario;

import film.domain.model.CachedClip;
import film.domain.model.Manifest;
import film.domain.model.ResolvedEnds;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.Timeline;
import film.domain.port.AssemblyPlan;
import film.infrastructure.assembly.TreeAssembly;
import film.infrastructure.assembly.TreePlan;
import film.infrastructure.assembly.TreeSnapshot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flat chunk prior with legacy part paths forces rebuild of missing tree leaf files.
 */
public final class FlatChunkPriorScenario {
    private final List<String> staleLeaves;
    private final List<String> staleNodes;
    public FlatChunkPriorScenario(final Path workspace) {
        final int span = 4;
        final Timeline timeline = AssemblyTimelineFixture.timeline(9);
        final ResolvedEnds ends = AssemblyTimelineFixture.ends(timeline);
        final Path partsDir = workspace.resolve("build/parts");
        final TreeSnapshot wanted = TreeSnapshot.wanted(
            timeline, ends, AssemblyTimelineFixture.profile(), AssemblyTimelineFixture.contract(), span, partsDir
        );
        final TreeSnapshot flatPrior = flatPrior(wanted, partsDir);
        touchLegacyParts(flatPrior);
        touchOutput(workspace.resolve("build/output.mp4"));
        final Manifest prior = manifestFor(timeline, ends, flatPrior, workspace);
        final Map<SegmentId, Path> clips = clipPaths(timeline, workspace);
        final AssemblyPlan plan = new TreeAssembly(
            span, AssemblyTimelineFixture.profile(), AssemblyTimelineFixture.contract()
        ).planned(
            prior,
            timeline,
            ends,
            clips,
            workspace,
            workspace.resolve("build/output.mp4")
        );
        this.staleLeaves = ((TreePlan) plan).staleLeafIds();
        this.staleNodes = ((TreePlan) plan).staleNodeIds();
    }
    public List<String> staleLeaves() {
        return staleLeaves;
    }
    public List<String> staleNodes() {
        return staleNodes;
    }
    private static TreeSnapshot flatPrior(final TreeSnapshot wanted, final Path partsDir) {
        final Map<String, TreeSnapshot.Node> nodes = new HashMap<>();
        for (final Map.Entry<String, TreeSnapshot.Node> entry : wanted.nodes().entrySet()) {
            if (!entry.getValue().leaf()) {
                continue;
            }
            final String id = entry.getKey();
            final Path legacy = partsDir.resolve(String.format("part-%03d.mp4", Integer.parseInt(id)));
            nodes.put(id, new TreeSnapshot.Node(entry.getValue().digest(), legacy, List.of()));
        }
        return new TreeSnapshot(wanted.root(), "", nodes);
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
    private static void touchLegacyParts(final TreeSnapshot snapshot) {
        try {
            for (final TreeSnapshot.Node node : snapshot.nodes().values()) {
                Files.createDirectories(node.path().getParent());
                Files.createFile(node.path());
            }
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("cannot create legacy part files", ex);
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
