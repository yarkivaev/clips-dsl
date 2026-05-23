package film.domain.model;

/**
 * Stable clip identifier from DSL.
 *
 * <p>Usage: {@code new SegmentId("beavers")}
 */
public final class SegmentId {
    private final String label;
    public SegmentId(final String label) {
        this.label = label;
    }
    public String label() {
        return label;
    }
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SegmentId)) {
            return false;
        }
        final SegmentId that = (SegmentId) other;
        return label.equals(that.label);
    }
    @Override
    public int hashCode() {
        return label.hashCode();
    }
}
