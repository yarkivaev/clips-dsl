package film.domain.model;

/**
 * One absolute source interval for clip edits with optional segment pace.
 *
 * <p>Usage: {@code new EditSpan(new Second(60), new GapAt(new Second(120)), new Pace(2.0))}
 */
public final class EditSpan {
    private final boolean hasFrom;
    private final Second from;
    private final GapEnd end;
    private final Pace pace;
    public EditSpan(final Second from, final GapEnd end) {
        this(from, end, Pace.one());
    }
    public EditSpan(final Second from, final GapEnd end, final Pace pace) {
        this(true, from, end, pace);
    }
    public EditSpan(final GapEnd end) {
        this(end, Pace.one());
    }
    public EditSpan(final GapEnd end, final Pace pace) {
        this(false, new Second(0), end, pace);
    }
    private EditSpan(final boolean hasFrom, final Second from, final GapEnd end, final Pace pace) {
        if (hasFrom && from.amount() < 0) {
            throw new IllegalStateException("edit from must be non-negative, got " + from.amount());
        }
        if (!pace.constant()) {
            throw new IllegalStateException("edit span speed must be a constant factor");
        }
        this.hasFrom = hasFrom;
        this.from = from;
        this.end = end;
        this.pace = pace;
    }
    public boolean hasFrom() {
        return hasFrom;
    }
    public Second from() {
        if (!hasFrom) {
            throw new IllegalStateException("edit from is open at clip window start");
        }
        return from;
    }
    public GapEnd end() {
        return end;
    }
    public Pace pace() {
        return pace;
    }
    public double startSource(final double windowStart) {
        return hasFrom ? from.amount() : windowStart;
    }
    public double stopSource(final double windowStart, final double windowStop) {
        final double start = startSource(windowStart);
        final double stop = end.resolved(start, windowStop);
        if (stop <= start) {
            throw new IllegalStateException(
                "edit stop must be after start " + start + " got " + stop
            );
        }
        return stop;
    }
    public String spanLabel() {
        final String start = hasFrom ? Double.toString(from.amount()) : "*";
        final String text = start + "-" + end.label();
        if (pace.unchanged()) {
            return text;
        }
        return text + "@" + pace.label();
    }
}
