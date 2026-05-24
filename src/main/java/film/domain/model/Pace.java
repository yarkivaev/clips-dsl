package film.domain.model;

/**
 * Playback rate as constant factor or keyframed curve on the clip.
 *
 * <p>Usage: {@code Pace.one()} or {@code new Pace(Keyframes.single(2.0))}
 */
public final class Pace {
    private final Keyframes keyframes;
    public Pace(final Keyframes keyframes) {
        this.keyframes = keyframes;
    }
    public Pace(final double factor) {
        this(Keyframes.single(factor));
    }
    public static Pace one() {
        return new Pace(Keyframes.single(1.0));
    }
    public Keyframes keyframes() {
        return keyframes;
    }
    public boolean constant() {
        return keyframes.constant();
    }
    public double constantFactor() {
        return keyframes.constantFactor();
    }
    public boolean unchanged() {
        return constant() && constantFactor() == 1.0;
    }
    public String label() {
        return keyframes.label();
    }
}
