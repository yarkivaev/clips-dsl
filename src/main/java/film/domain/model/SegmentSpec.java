package film.domain.model;

/**
 * One clip definition: source file and in/out bounds.
 *
 * <p>Usage: {@code new SegmentSpec(id, source, from, new AtSecond(to))}
 */
public final class SegmentSpec {
    private final SegmentId id;
    private final SourceRef source;
    private final Second from;
    private final CutEnd end;
    public SegmentSpec(
        final SegmentId id,
        final SourceRef source,
        final Second from,
        final CutEnd end
    ) {
        this.id = id;
        this.source = source;
        this.from = from;
        this.end = end;
    }
    public SegmentId id() {
        return id;
    }
    public SourceRef source() {
        return source;
    }
    public Second from() {
        return from;
    }
    public CutEnd end() {
        return end;
    }
    public Fingerprint fingerprint(final Second resolvedEnd) {
        return new Fingerprint(this, resolvedEnd);
    }
}
