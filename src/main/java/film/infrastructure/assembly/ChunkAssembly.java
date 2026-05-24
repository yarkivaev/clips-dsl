package film.infrastructure.assembly;

import film.domain.model.AssemblySnapshot;
import film.domain.model.Manifest;
import film.domain.model.ResolvedEnds;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.Timeline;
import film.domain.model.VacantAssemblySnapshot;
import film.domain.port.Assembly;
import film.domain.port.AssemblyPlan;
import film.domain.port.Concat;
import film.infrastructure.ffmpeg.ConcatLabel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Flat chunk assembly with configurable leaf span for partial concat.
 *
 * <p>Usage: {@code new ChunkAssembly(8).planned(prior, desired, ends, clips)}
 */
public final class ChunkAssembly implements Assembly {
    private final int span;
    private final ConcatLabel labels;
    public ChunkAssembly(final int span) {
        this.span = span;
        this.labels = new ConcatLabel();
    }
    @Override
    public AssemblyPlan planned(
        final Manifest prior,
        final Timeline desired,
        final ResolvedEnds ends,
        final Map<SegmentId, Path> clips,
        final Path workspace,
        final Path output
    ) {
        final Path partsDir = workspace.resolve("build/parts");
        final ChunkSnapshot wanted = ChunkSnapshot.wanted(desired, ends, span, partsDir);
        final ChunkSnapshot priorSnapshot = priorSnapshot(prior.assembly());
        final List<SegmentSpec> segments = desired.segments();
        final List<ChunkPlan.Leaf> leaves = new ArrayList<>();
        final List<Path> partPaths = new ArrayList<>();
        boolean anyStale = priorSnapshot == null;
        for (int start = 0; start < segments.size(); start += span) {
            final int index = start / span;
            final String id = Integer.toString(index);
            final int stop = Math.min(start + span, segments.size());
            final List<SegmentSpec> slice = segments.subList(start, stop);
            final String digest = wanted.node(id).digest();
            final Path path = wanted.node(id).path();
            final ChunkSnapshot.Node priorNode = priorSnapshot == null ? null : priorSnapshot.node(id);
            final boolean stale = priorNode == null
                || !priorNode.digest().equals(digest)
                || !Files.isRegularFile(path);
            if (stale) {
                anyStale = true;
            }
            final List<Path> inputs = new ArrayList<>();
            for (final SegmentSpec spec : slice) {
                if (!clips.containsKey(spec.id())) {
                    throw new IllegalStateException("missing clip path for segment " + spec.id().label());
                }
                inputs.add(clips.get(spec.id()));
            }
            leaves.add(new ChunkPlan.Leaf(id, stale, path, inputs));
            partPaths.add(path);
        }
        final boolean rootStale = priorSnapshot == null
            || !priorSnapshot.root().equals(wanted.root())
            || anyStale
            || !Files.isRegularFile(output);
        if (!anyStale && !rootStale) {
            return ChunkPlan.idle();
        }
        return new ChunkPlan(false, leaves, rootStale, partPaths, output, wanted);
    }
    @Override
    public AssemblySnapshot executed(
        final AssemblyPlan plan,
        final Concat concat,
        final Path workspace,
        final Path partsDir,
        final Path output
    ) {
        if (!(plan instanceof ChunkPlan chunk)) {
            throw new IllegalStateException("unexpected assembly plan type " + plan.getClass().getName());
        }
        if (chunk.empty()) {
            throw new IllegalStateException("cannot execute vacant chunk assembly plan");
        }
        try {
            Files.createDirectories(partsDir);
            Files.createDirectories(output.getParent());
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("cannot create assembly dirs under " + workspace, ex);
        }
        for (final ChunkPlan.Leaf leaf : chunk.leaves()) {
            if (leaf.stale()) {
                concat.joined(
                    leaf.inputs(),
                    leaf.path(),
                    labels.logKey("part-" + leaf.id()),
                    labels.part(leaf.id(), leaf.inputs().size(), leaf.path())
                );
                System.out.println("assembled part " + leaf.id());
            }
        }
        if (chunk.root()) {
            concat.joined(
                chunk.parts(),
                output,
                labels.logKey("root"),
                labels.root(chunk.parts().size(), output)
            );
            System.out.println("assembled " + output);
        }
        return chunk.snapshot();
    }
    private static ChunkSnapshot priorSnapshot(final AssemblySnapshot assembly) {
        if (assembly instanceof VacantAssemblySnapshot) {
            return null;
        }
        if (assembly instanceof ChunkSnapshot snapshot) {
            return snapshot;
        }
        throw new IllegalStateException("unsupported assembly snapshot type " + assembly.getClass().getName());
    }
}
