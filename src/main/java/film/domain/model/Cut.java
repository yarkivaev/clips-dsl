package film.domain.model;

/**
 * Source interval and playback rate for one clip.
 */
public final class Cut {
    private final Second from;
    private final CutEnd end;
    private final Pace pace;
    public Cut(final Second from, final CutEnd end, final Pace pace) {
        this.from = from;
        this.end = end;
        this.pace = pace;
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
}
