package film.infrastructure.assembly;

import film.domain.model.VacantAssemblySnapshot;
import film.domain.model.AssemblySnapshot;
import film.domain.model.Manifest;
import film.domain.model.ResolvedEnds;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.Timeline;
import film.domain.model.MediaContract;
import film.domain.model.RenderProfile;
import film.domain.port.Assembly;
import film.domain.port.AssemblyPlan;
import film.domain.port.Concat;
import film.infrastructure.ffmpeg.ConcatLabel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tree assembly with configurable fan-in for partial concat on long films.
 *
 * <p>Usage: {@code new TreeAssembly(4, profile, contract).planned(prior, desired, ends, clips, workspace, output)}
 */
public final class TreeAssembly implements Assembly {
    private final int span;
    private final RenderProfile profile;
    private final MediaContract contract;
    private final ConcatLabel labels;
    public TreeAssembly(final int span, final RenderProfile profile, final MediaContract contract) {
        this.span = span;
        this.profile = profile;
        this.contract = contract;
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
        final TreeSnapshot wanted = TreeSnapshot.wanted(desired, ends, profile, contract, span, partsDir);
        final TreeSnapshot priorSnapshot = priorSnapshot(prior.assembly());
        if (!prior.profileMatches(profile)) {
            return fullPlan(wanted, desired, ends, clips, output);
        }
        final Map<String, Boolean> stale = staleMap(wanted, priorSnapshot);
        ensureChildArtifacts(stale, wanted, priorSnapshot);
        final List<TreePlan.Step> steps = new ArrayList<>();
        for (final String id : wanted.order()) {
            if (!Boolean.TRUE.equals(stale.get(id))) {
                continue;
            }
            final TreeSnapshot.Node node = wanted.node(id);
            steps.add(new TreePlan.Step(
                id,
                node.leaf(),
                node.path(),
                inputsFor(id, node, wanted, priorSnapshot, desired, clips, stale)
            ));
        }
        final boolean rootStale = priorSnapshot == null
            || !priorSnapshot.root().equals(wanted.root())
            || stale.values().stream().anyMatch(flag -> flag)
            || !Files.isRegularFile(output);
        if (steps.isEmpty() && !rootStale) {
            return TreePlan.idle();
        }
        final Path topPath = wanted.node(wanted.top()).path();
        return new TreePlan(false, steps, rootStale, topPath, output, wanted);
    }
    private AssemblyPlan fullPlan(
        final TreeSnapshot wanted,
        final Timeline desired,
        final ResolvedEnds ends,
        final Map<SegmentId, Path> clips,
        final Path output
    ) {
        final Map<String, Boolean> stale = new LinkedHashMap<>();
        for (final String id : wanted.order()) {
            stale.put(id, true);
        }
        final List<TreePlan.Step> steps = new ArrayList<>();
        for (final String id : wanted.order()) {
            final TreeSnapshot.Node node = wanted.node(id);
            steps.add(new TreePlan.Step(
                id,
                node.leaf(),
                node.path(),
                inputsFor(id, node, wanted, null, desired, clips, stale)
            ));
        }
        final Path topPath = wanted.node(wanted.top()).path();
        return new TreePlan(false, steps, true, topPath, output, wanted);
    }
    @Override
    public AssemblySnapshot executed(
        final AssemblyPlan plan,
        final Concat concat,
        final Path workspace,
        final Path partsDir,
        final Path output
    ) {
        if (!(plan instanceof TreePlan tree)) {
            throw new IllegalStateException("unexpected assembly plan type " + plan.getClass().getName());
        }
        if (tree.empty()) {
            throw new IllegalStateException("cannot execute vacant tree assembly plan");
        }
        try {
            Files.createDirectories(partsDir);
            Files.createDirectories(output.getParent());
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("cannot create assembly dirs under " + workspace, ex);
        }
        for (final TreePlan.Step step : tree.steps()) {
            if (step.leaf()) {
                concat.joined(
                    step.inputs(),
                    step.path(),
                    labels.logKey("leaf-" + step.id()),
                    labels.part(step.id(), step.inputs().size(), step.path())
                );
            } else {
                concat.joined(
                    step.inputs(),
                    step.path(),
                    labels.logKey("node-" + step.id().replace('.', '-')),
                    labels.node(step.id(), step.inputs().size(), step.path())
                );
            }
            System.out.println("assembled node " + step.id());
        }
        if (tree.root()) {
            concat.joined(
                List.of(tree.topPath()),
                output,
                labels.logKey("root"),
                labels.root(1, output)
            );
            System.out.println("assembled " + output);
        }
        return tree.snapshot();
    }
    private Map<String, Boolean> staleMap(final TreeSnapshot wanted, final TreeSnapshot prior) {
        final Map<String, Boolean> stale = new LinkedHashMap<>();
        for (final String id : wanted.order()) {
            stale.put(id, false);
        }
        for (final String id : wanted.order()) {
            markStale(id, wanted, prior, stale);
        }
        return stale;
    }
    private void ensureChildArtifacts(
        final Map<String, Boolean> stale,
        final TreeSnapshot wanted,
        final TreeSnapshot prior
    ) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (final String id : wanted.order()) {
                if (!Boolean.TRUE.equals(stale.get(id))) {
                    continue;
                }
                final TreeSnapshot.Node node = wanted.node(id);
                if (node.leaf()) {
                    continue;
                }
                for (final String childId : node.children()) {
                    if (Boolean.TRUE.equals(stale.get(childId))) {
                        continue;
                    }
                    if (!Files.isRegularFile(resolvedPath(childId, wanted, prior))) {
                        stale.put(childId, true);
                        markStale(childId, wanted, prior, stale);
                        changed = true;
                    }
                }
            }
        }
    }
    private boolean markStale(
        final String id,
        final TreeSnapshot wanted,
        final TreeSnapshot prior,
        final Map<String, Boolean> stale
    ) {
        if (Boolean.TRUE.equals(stale.get(id))) {
            return true;
        }
        final TreeSnapshot.Node node = wanted.node(id);
        for (final String childId : node.children()) {
            if (markStale(childId, wanted, prior, stale)) {
                stale.put(id, true);
            }
        }
        if (Boolean.TRUE.equals(stale.get(id))) {
            return true;
        }
        final TreeSnapshot.Node priorNode = prior == null ? null : prior.node(id);
        final boolean pathMoved = priorNode != null && !priorNode.path().equals(node.path());
        final boolean nodeStale = priorNode == null
            || !priorNode.digest().equals(node.digest())
            || pathMoved
            || !Files.isRegularFile(node.path());
        if (nodeStale) {
            stale.put(id, true);
        }
        return Boolean.TRUE.equals(stale.get(id));
    }
    private List<Path> inputsFor(
        final String id,
        final TreeSnapshot.Node node,
        final TreeSnapshot wanted,
        final TreeSnapshot prior,
        final Timeline timeline,
        final Map<SegmentId, Path> clips,
        final Map<String, Boolean> stale
    ) {
        if (node.leaf()) {
            return leafInputs(id, timeline, clips);
        }
        final List<Path> inputs = new ArrayList<>();
        for (final String childId : node.children()) {
            if (Boolean.TRUE.equals(stale.get(childId))) {
                inputs.add(wanted.node(childId).path());
            } else {
                inputs.add(resolvedPath(childId, wanted, prior));
            }
        }
        return inputs;
    }
    private List<Path> leafInputs(
        final String id,
        final Timeline timeline,
        final Map<SegmentId, Path> clips
    ) {
        final int leafIndex = Integer.parseInt(id);
        final int start = leafIndex * span;
        final int stop = Math.min(start + span, timeline.segments().size());
        final List<Path> inputs = new ArrayList<>();
        for (int offset = start; offset < stop; offset++) {
            final SegmentSpec spec = timeline.segments().get(offset);
            if (!clips.containsKey(spec.id())) {
                throw new IllegalStateException("missing clip path for segment " + spec.id().label());
            }
            inputs.add(clips.get(spec.id()));
        }
        return inputs;
    }
    private Path resolvedPath(final String id, final TreeSnapshot wanted, final TreeSnapshot prior) {
        final Path wantedPath = wanted.node(id).path();
        if (Files.isRegularFile(wantedPath)) {
            return wantedPath;
        }
        if (prior != null) {
            final TreeSnapshot.Node priorNode = prior.node(id);
            if (priorNode != null && Files.isRegularFile(priorNode.path())) {
                return priorNode.path();
            }
        }
        return wantedPath;
    }
    private static TreeSnapshot priorSnapshot(final AssemblySnapshot assembly) {
        if (assembly instanceof VacantAssemblySnapshot) {
            return null;
        }
        if (assembly instanceof TreeSnapshot snapshot) {
            return snapshot;
        }
        return null;
    }
}
