package film.domain.model;

/**
 * Exclude gap ends at an absolute second on the source file.
 *
 * <p>Usage: {@code new GapAt(new Second(180))}
 */
public final class GapAt implements GapEnd {
    private final Second at;
    public GapAt(final Second at) {
        this.at = at;
    }
    @Override
    public double resolved(final double gapFrom, final double windowStop) {
        return at.amount();
    }
    @Override
    public String label() {
        return Double.toString(at.amount());
    }
}
