package film.domain.model;

/**
 * Task wrapper for final concat.
 */
public final class JoinedTask implements Task {
    private final Joined joined;
    public JoinedTask(final Joined joined) {
        this.joined = joined;
    }
    public Joined joined() {
        return joined;
    }
    @Override
    public boolean render() {
        return false;
    }
    @Override
    public boolean concat() {
        return true;
    }
    @Override
    public void apply(final Execution execution) {
        execution.concat().join(
            execution.timeline(),
            execution.ends(),
            execution.artifacts(),
            execution.output()
        );
        System.out.println("joined " + execution.output());
    }
}
