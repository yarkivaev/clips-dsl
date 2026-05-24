package film.domain.model;

/**
 * One absolute source interval to drop inside a clip window.
 *
 * <p>Usage: {@code new ExcludeSpan(new Second(60), new GapAt(new Second(120)))}
 */
public final class ExcludeSpan {
    private final boolean hasFrom;
    private final Second from;
    private final GapEnd end;
    public ExcludeSpan(final Second from, final GapEnd end) {
        this(true, from, end);
    }
    public ExcludeSpan(final GapEnd end) {
        this(false, new Second(0), end);
    }
    private ExcludeSpan(final boolean hasFrom, final Second from, final GapEnd end) {
        if (hasFrom && from.amount() < 0) {
            throw new IllegalStateException("exclude from must be non-negative, got " + from.amount());
        }
        this.hasFrom = hasFrom;
        this.from = from;
        this.end = end;
    }
    public boolean hasFrom() {
        return hasFrom;
    }
    public Second from() {
        if (!hasFrom) {
            throw new IllegalStateException("exclude from is open at clip window start");
        }
        return from;
    }
    public GapEnd end() {
        return end;
    }
    public double startSource(final double windowStart) {
        return hasFrom ? from.amount() : windowStart;
    }
    public double stopSource(final double windowStart, final double windowStop) {
        final double start = startSource(windowStart);
        final double stop = end.resolved(start, windowStop);
        if (stop <= start) {
            throw new IllegalStateException(
                "exclude stop must be after start " + start + " got " + stop
            );
        }
        return stop;
    }
    public String gapLabel() {
        final String start = hasFrom ? Double.toString(from.amount()) : "*";
        return start + "-" + end.label();
    }
}
