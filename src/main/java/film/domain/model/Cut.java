package film.domain.model;

/**
 * Source interval, optional excludes, and playback rate for one clip.
 *
 * <p>Usage: {@code new Cut(from, end, pace, Edits.none())}
 */
public final class Cut {
    private final Second from;
    private final CutEnd end;
    private final Pace pace;
    private final Edits edits;
    public Cut(final Second from, final CutEnd end, final Pace pace, final Edits edits) {
        this.from = from;
        this.end = end;
        this.pace = pace;
        this.edits = edits;
    }
    public Second from() {
        return from;
    }
    public CutEnd end() {
        return end;
    }
    public Pace pace() {
        return pace;
    }
    public Edits edits() {
        return edits;
    }
}
