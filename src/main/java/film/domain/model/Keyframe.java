package film.domain.model;

/**
 * Speed factor at a point on the clip timeline from clip start.
 *
 * <p>Usage: {@code new Keyframe(new Second(0), 1.0)}
 */
public final class Keyframe {
    private final Second at;
    private final double factor;
    public Keyframe(final Second at, final double factor) {
        if (factor <= 0) {
            throw new IllegalStateException("keyframe factor must be positive, got " + factor);
        }
        this.at = at;
        this.factor = factor;
    }
    public Second at() {
        return at;
    }
    public double factor() {
        return factor;
    }
}
