package film.domain.model;

/**
 * Absolute source-time interval kept after excludes.
 */
public final class SourceSpan {
    private final Second start;
    private final Second stop;
    public SourceSpan(final Second start, final Second stop) {
        if (stop.amount() <= start.amount()) {
            throw new IllegalStateException(
                "source span stop must be after start " + start.amount() + " got " + stop.amount()
            );
        }
        this.start = start;
        this.stop = stop;
    }
    public Second start() {
        return start;
    }
    public Second stop() {
        return stop;
    }
    public double length() {
        return stop.amount() - start.amount();
    }
}
