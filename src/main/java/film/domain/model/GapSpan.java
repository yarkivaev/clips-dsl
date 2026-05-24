package film.domain.model;

/**
 * Exclude gap ends after a length from absolute source {@code from}.
 *
 * <p>Usage: {@code new GapSpan(new Second(60))} with {@code from: 120} ends at 180 on source
 */
public final class GapSpan implements GapEnd {
    private final Second span;
    public GapSpan(final Second span) {
        if (span.amount() <= 0) {
            throw new IllegalStateException("gap span must be positive, got " + span.amount());
        }
        this.span = span;
    }
    @Override
    public double resolved(final double gapFrom, final double windowStop) {
        return gapFrom + span.amount();
    }
    @Override
    public String label() {
        return "+" + span.amount();
    }
}
