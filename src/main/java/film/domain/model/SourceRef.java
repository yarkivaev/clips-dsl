package film.domain.model;

/**
 * Numbered symlink to a source recording (1..67).
 *
 * <p>Usage: {@code new SourceRef(6)}
 */
public final class SourceRef {
    private final int number;
    public SourceRef(final int number) {
        this.number = number;
    }
    public int number() {
        return number;
    }
    public String filename() {
        return Integer.toString(number);
    }
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SourceRef)) {
            return false;
        }
        final SourceRef that = (SourceRef) other;
        return number == that.number;
    }
    @Override
    public int hashCode() {
        return Integer.hashCode(number);
    }
}
