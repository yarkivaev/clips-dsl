package film.domain.model;

/**
 * Optional excludes, includes, and trimmed-timeline output cap for one clip.
 *
 * <p>Usage: {@code Edits.none()} or {@code new Edits(Excludes.none(), Includes.of(spans), TrimCap.none())}
 */
public final class Edits {
    private final Excludes excludes;
    private final Includes includes;
    private final TrimCap cap;
    public Edits(final Excludes excludes, final Includes includes, final TrimCap cap) {
        this.excludes = excludes;
        this.includes = includes;
        this.cap = cap;
    }
    public static Edits none() {
        return new Edits(Excludes.none(), Includes.none(), TrimCap.none());
    }
    public boolean trimmed() {
        return excludes.present() || includes.present() || cap.present();
    }
    public Excludes excludes() {
        return excludes;
    }
    public Includes includes() {
        return includes;
    }
    public TrimCap cap() {
        return cap;
    }
    public String label() {
        if (!trimmed()) {
            return "";
        }
        final StringBuilder text = new StringBuilder();
        if (includes.present()) {
            text.append("keep:").append(includes.label());
        } else if (excludes.present()) {
            text.append("trim:").append(excludes.label());
        } else {
            text.append("trim");
        }
        if (cap.present()) {
            text.append('|').append(cap.label());
        }
        return text.toString();
    }
}
