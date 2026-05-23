package film.domain.model;

/**
 * Task wrapper for skipped clip reuse.
 */
public final class SkippedTask implements Task {
    private final Skipped skipped;
    public SkippedTask(final Skipped skipped) {
        this.skipped = skipped;
    }
    public Skipped skipped() {
        return skipped;
    }
    @Override
    public void apply(final Execution execution) {
        System.out.println("skipped " + skipped.id().label());
    }
    @Override
    public boolean render() {
        return false;
    }
    @Override
    public boolean concat() {
        return false;
    }
}
