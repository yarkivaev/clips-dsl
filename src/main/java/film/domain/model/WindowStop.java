package film.domain.model;

/**
 * Exclude gap runs through the clip window end on the source file.
 *
 * <p>Usage: {@code WindowStop.INSTANCE}
 */
public enum WindowStop implements GapEnd {
    INSTANCE;
    @Override
    public double resolved(final double gapFrom, final double windowStop) {
        return windowStop;
    }
    @Override
    public String label() {
        return "*";
    }
}
