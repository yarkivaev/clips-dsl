package film.domain.model;

/**
 * Absolute source-time interval kept after edits with optional segment pace.
 *
 * <p>Usage: {@code new SourceSpan(new Second(0), new Second(10), Pace.one())}
 */
public final class SourceSpan {
    private final Second start;
    private final Second stop;
    private final Pace pace;
    public SourceSpan(final Second start, final Second stop, final Pace pace) {
        if (stop.amount() <= start.amount()) {
            throw new IllegalStateException(
                "source span stop must be after start " + start.amount() + " got " + stop.amount()
            );
        }
        this.start = start;
        this.stop = stop;
        this.pace = pace;
    }
    public Second start() {
        return start;
    }
    public Second stop() {
        return stop;
    }
    public Pace pace() {
        return pace;
    }
    public double length() {
        return stop.amount() - start.amount();
    }
    public double play() {
        return length() / pace.constantFactor();
    }
}
