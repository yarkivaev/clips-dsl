package film.infrastructure.assembly;

import film.domain.port.AssemblyPlan;

import java.nio.file.Path;
import java.util.List;

/**
 * Executable flat assembly plan joining all clips into output.
 *
 * <p>Usage: {@code FlatPlan.idle()}
 */
public final class FlatPlan implements AssemblyPlan {
    private final boolean vacant;
    private final List<Path> inputs;
    private final Path output;
    private final FlatSnapshot snapshot;
    public FlatPlan(
        final boolean vacant,
        final List<Path> inputs,
        final Path output,
        final FlatSnapshot snapshot
    ) {
        this.vacant = vacant;
        this.inputs = List.copyOf(inputs);
        this.output = output;
        this.snapshot = snapshot;
    }
    @Override
    public boolean empty() {
        return vacant;
    }
    public List<Path> inputs() {
        return inputs;
    }
    public Path output() {
        return output;
    }
    public FlatSnapshot snapshot() {
        return snapshot;
    }
    public static FlatPlan idle() {
        return new FlatPlan(true, List.of(), Path.of("."), new FlatSnapshot(""));
    }
}
