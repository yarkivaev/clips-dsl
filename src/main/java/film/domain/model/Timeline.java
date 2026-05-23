package film.domain.model;

import java.util.List;

/**
 * Ordered sequence of segment specs for the final film.
 *
 * <p>Usage: {@code new Timeline(List.of(specA, specB))}
 */
public final class Timeline {
    private final List<SegmentSpec> segments;
    public Timeline(final List<SegmentSpec> segments) {
        this.segments = List.copyOf(segments);
    }
    public List<SegmentSpec> segments() {
        return segments;
    }
    public TimelineFingerprint print(final ResolvedEnds ends) {
        return new TimelineFingerprint(this, ends);
    }
}
