package film.domain.model;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Snapshot of the last successful build for incremental diff.
 */
public final class Manifest {
    private final RenderProfile profile;
    private final AssemblySnapshot assembly;
    private final Map<SegmentId, CachedClip> clips;
    public Manifest(
        final RenderProfile profile,
        final AssemblySnapshot assembly,
        final Map<SegmentId, CachedClip> clips
    ) {
        this.profile = profile;
        this.assembly = assembly;
        this.clips = Map.copyOf(clips);
    }
    public static Manifest empty(final RenderProfile profile) {
        return new Manifest(profile, new VacantAssemblySnapshot(), Collections.emptyMap());
    }
    public PlanDiff diff(
        final Timeline desired,
        final ResolvedEnds ends,
        final RenderProfile profile,
        final MediaContract contract
    ) {
        return new PlanDiff(this, desired, ends, profile, contract);
    }
    public boolean cached(
        final SegmentId id,
        final Fingerprint wanted
    ) {
        if (!profileMatches(profile)) {
            return false;
        }
        if (!clips.containsKey(id)) {
            return false;
        }
        return clips.get(id).fingerprint().matches(wanted);
    }
    public boolean profileMatches(final RenderProfile wanted) {
        return profile.label().equals(wanted.label());
    }
    public Path path(final SegmentId id) {
        if (!clips.containsKey(id)) {
            throw new IllegalStateException("missing cached clip for segment " + id.label());
        }
        return clips.get(id).path();
    }
    public RenderProfile profile() {
        return profile;
    }
    public AssemblySnapshot assembly() {
        return assembly;
    }
    public Map<SegmentId, CachedClip> clips() {
        return clips;
    }
    public Manifest saved(
        final Timeline desired,
        final ResolvedEnds ends,
        final RenderProfile profile,
        final MediaContract contract,
        final Map<SegmentId, Path> artifacts,
        final AssemblySnapshot assemblySnapshot
    ) {
        final Map<SegmentId, CachedClip> next = new HashMap<>();
        for (final SegmentSpec spec : desired.segments()) {
            final Path path = artifacts.get(spec.id());
            if (path == null) {
                throw new IllegalStateException("missing artifact path for segment " + spec.id().label());
            }
            next.put(
                spec.id(),
                new CachedClip(
                    spec.id(),
                    spec.fingerprint(ends.end(spec), profile, contract),
                    path
                )
            );
        }
        return new Manifest(profile, assemblySnapshot, next);
    }
}
