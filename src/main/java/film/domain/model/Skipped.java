package film.domain.model;

/**
 * Reuse cached clip; no ffmpeg cut needed.
 */
public final class Skipped {
    private final SegmentId id;
    public Skipped(final SegmentId id) {
        this.id = id;
    }
    public SegmentId id() {
        return id;
    }
}
