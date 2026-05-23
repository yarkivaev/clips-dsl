package film.domain.model;

/**
 * Time position in seconds on a source file.
 *
 * <p>Usage: {@code new Second(12.5)}
 */
public final class Second {
    private final double amount;
    public Second(final double amount) {
        this.amount = amount;
    }
    public double amount() {
        return amount;
    }
    public String ffmpeg() {
        return Double.toString(amount);
    }
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Second)) {
            return false;
        }
        final Second that = (Second) other;
        return Double.compare(amount, that.amount) == 0;
    }
    @Override
    public int hashCode() {
        return Double.hashCode(amount);
    }
}
