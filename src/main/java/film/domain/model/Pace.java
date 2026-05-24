package film.domain.model;

/**
 * Playback rate: 1.0 is normal, 2.0 is double speed, 0.5 is half speed.
 *
 * <p>Usage: {@code Pace.one()} or {@code new Pace(2.0)}
 */
public final class Pace {
    private final double factor;
    public Pace(final double factor) {
        if (factor <= 0) {
            throw new IllegalStateException("pace factor must be positive, got " + factor);
        }
        this.factor = factor;
    }
    public static Pace one() {
        return new Pace(1.0);
    }
    public double factor() {
        return factor;
    }
    public boolean unchanged() {
        return factor == 1.0;
    }
    public String videoSuffix() {
        if (unchanged()) {
            return "";
        }
        return ",setpts=PTS/" + factor;
    }
    public String audioChain() {
        if (unchanged()) {
            return "aresample=48000,asetpts=PTS-STARTPTS";
        }
        final StringBuilder chain = new StringBuilder("aresample=48000,asetpts=PTS-STARTPTS");
        double left = factor;
        while (left > 2.0) {
            chain.append(",atempo=2.0");
            left /= 2.0;
        }
        while (left < 0.5) {
            chain.append(",atempo=0.5");
            left /= 0.5;
        }
        if (Math.abs(left - 1.0) > 0.001) {
            chain.append(",atempo=").append(left);
        }
        return chain.toString();
    }
    public String label() {
        return Double.toString(factor);
    }
}
