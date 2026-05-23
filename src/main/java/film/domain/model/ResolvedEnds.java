package film.domain.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolved end second for each segment in a timeline.
 */
public final class ResolvedEnds {
    private final Map<SegmentId, Second> ends;
    public ResolvedEnds(final Map<SegmentId, Second> ends) {
        this.ends = Map.copyOf(ends);
    }
    public Second end(final SegmentSpec spec) {
        final Second second = ends.get(spec.id());
        if (second == null) {
            throw new IllegalStateException("missing resolved end for segment " + spec.id().label());
        }
        return second;
    }
    public static ResolvedEnds of(final Timeline timeline, final EndResolver resolver) {
        final Map<SegmentId, Second> map = new HashMap<>();
        for (final SegmentSpec spec : timeline.segments()) {
            map.put(spec.id(), resolver.resolve(spec));
        }
        return new ResolvedEnds(map);
    }
    @FunctionalInterface
    public interface EndResolver {
        Second resolve(SegmentSpec spec);
    }
}
