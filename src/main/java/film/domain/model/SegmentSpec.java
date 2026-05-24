package film.domain.model;

/**
 * One clip definition: source file, bounds, and optional pace.
 *
 * <p>Usage: {@code new SegmentSpec(id, source, new Cut(from, end, Pace.one()))}
 */
public final class SegmentSpec {
    private final SegmentId id;
    private final SourceRef source;
    private final Cut cut;
    public SegmentSpec(final SegmentId id, final SourceRef source, final Cut cut) {
        this.id = id;
        this.source = source;
        this.cut = cut;
    }
    public SegmentId id() {
        return id;
    }
    public SourceRef source() {
        return source;
    }
    public Second from() {
        return cut.from();
    }
    public CutEnd end() {
        return cut.end();
    }
    public Pace pace() {
        return cut.pace();
    }
    public Fingerprint fingerprint(final Second resolvedEnd) {
        return new Fingerprint(this, resolvedEnd);
    }
}
