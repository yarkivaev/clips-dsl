package film.domain.model;

import film.domain.port.Assembly;
import film.domain.port.AssemblyPlan;

/**
 * Task wrapper for incremental film assembly.
 */
public final class AssembledTask implements Task {
    private final Assembled assembled;
    private final AssemblyPlan plan;
    public AssembledTask(final Assembled assembled, final AssemblyPlan plan) {
        this.assembled = assembled;
        this.plan = plan;
    }
    public Assembled assembled() {
        return assembled;
    }
    public AssemblyPlan plan() {
        return plan;
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
        final AssemblySnapshot snapshot = execution.assembly().executed(
            plan,
            execution.concat(),
            execution.workspace(),
            execution.partsDir(),
            execution.output()
        );
        execution.assemblySnapshot(snapshot);
    }
}
