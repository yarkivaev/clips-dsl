package film.domain.model;

/**
 * Optional limit on trimmed timeline length after excludes.
 *
 * <p>Usage: {@code TrimCap.of(new Second(90))} or {@code TrimCap.none()}
 */
public final class TrimCap {
    private final boolean present;
    private final Second span;
    private TrimCap(final boolean present, final Second span) {
        this.present = present;
        this.span = span;
    }
    public static TrimCap none() {
        return new TrimCap(false, new Second(0));
    }
    public static TrimCap of(final Second span) {
        if (span.amount() <= 0) {
            throw new IllegalStateException("trim cap must be positive, got " + span.amount());
        }
        return new TrimCap(true, span);
    }
    public boolean present() {
        return present;
    }
    public Second span() {
        if (!present()) {
            throw new IllegalStateException("trim cap is absent");
        }
        return span;
    }
    public String label() {
        if (!present()) {
            return "";
        }
        return Double.toString(span.amount());
    }
}
