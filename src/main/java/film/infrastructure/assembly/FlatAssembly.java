package film.infrastructure.assembly;

import film.domain.model.AssemblySnapshot;
import film.domain.model.Manifest;
import film.domain.model.MediaContract;
import film.domain.model.RenderProfile;
import film.domain.model.ResolvedEnds;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.Timeline;
import film.domain.model.TimelineFingerprint;
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
 * Flat assembly joining all clips directly into the final output.
 *
 * <p>Usage: {@code new FlatAssembly(profile, contract).planned(prior, desired, ends, clips, workspace, output)}
 */
public final class FlatAssembly implements Assembly {
    private final RenderProfile profile;
    private final MediaContract contract;
    private final ConcatLabel labels;
    public FlatAssembly(final RenderProfile profile, final MediaContract contract) {
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
        final FlatSnapshot wanted = new FlatSnapshot(
            new TimelineFingerprint(desired, ends, profile, contract).digest()
        );
        if (!prior.profileMatches(profile)) {
            return activePlan(desired, clips, output, wanted);
        }
        final FlatSnapshot priorSnapshot = priorSnapshot(prior.assembly());
        final boolean stale = priorSnapshot == null
            || !priorSnapshot.root().equals(wanted.root())
            || !Files.isRegularFile(output);
        if (!stale) {
            return FlatPlan.idle();
        }
        return activePlan(desired, clips, output, wanted);
    }
    @Override
    public AssemblySnapshot executed(
        final AssemblyPlan plan,
        final Concat concat,
        final Path workspace,
        final Path partsDir,
        final Path output
    ) {
        if (!(plan instanceof FlatPlan flat)) {
            throw new IllegalStateException("unexpected assembly plan type " + plan.getClass().getName());
        }
        if (flat.empty()) {
            throw new IllegalStateException("cannot execute vacant flat assembly plan");
        }
        concat.joined(
            flat.inputs(),
            flat.output(),
            labels.logKey("film"),
            labels.film(flat.inputs().size(), flat.output())
        );
        System.out.println("assembled " + flat.output());
        return flat.snapshot();
    }
    private static FlatPlan activePlan(
        final Timeline desired,
        final Map<SegmentId, Path> clips,
        final Path output,
        final FlatSnapshot snapshot
    ) {
        final List<Path> inputs = new ArrayList<>();
        for (final SegmentSpec spec : desired.segments()) {
            if (!clips.containsKey(spec.id())) {
                throw new IllegalStateException("missing clip path for segment " + spec.id().label());
            }
            inputs.add(clips.get(spec.id()));
        }
        return new FlatPlan(false, inputs, output, snapshot);
    }
    private static FlatSnapshot priorSnapshot(final AssemblySnapshot assembly) {
        if (assembly instanceof VacantAssemblySnapshot) {
            return null;
        }
        if (assembly instanceof FlatSnapshot snapshot) {
            return snapshot;
        }
        return null;
    }
}
