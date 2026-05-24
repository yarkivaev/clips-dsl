package film.domain.model;

/**
 * Optional excludes and trimmed-timeline output cap for one clip.
 *
 * <p>Usage: {@code Edits.none()} or {@code new Edits(Excludes.of(gaps), TrimCap.of(span))}
 */
public final class Edits {
    private final Excludes excludes;
    private final TrimCap cap;
    public Edits(final Excludes excludes, final TrimCap cap) {
        this.excludes = excludes;
        this.cap = cap;
    }
    public static Edits none() {
        return new Edits(Excludes.none(), TrimCap.none());
    }
    public boolean trimmed() {
        return excludes.present() || cap.present();
    }
    public Excludes excludes() {
        return excludes;
    }
    public TrimCap cap() {
        return cap;
    }
    public String label() {
        if (!trimmed()) {
            return "";
        }
        final StringBuilder text = new StringBuilder("trim");
        if (excludes.present()) {
            text.append(':').append(excludes.label());
        }
        if (cap.present()) {
            text.append('|').append(cap.label());
        }
        return text.toString();
    }
}
