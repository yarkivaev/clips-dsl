package film.infrastructure.assembly;

import film.domain.port.AssemblyPlan;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Executable tree assembly plan with stale nodes in bottom-up order.
 *
 * <p>Usage: {@code plan.empty()}
 */
public final class TreePlan implements AssemblyPlan {
    private final boolean vacant;
    private final List<Step> steps;
    private final boolean root;
    private final Path topPath;
    private final Path output;
    private final TreeSnapshot snapshot;
    public TreePlan(
        final boolean vacant,
        final List<Step> steps,
        final boolean root,
        final Path topPath,
        final Path output,
        final TreeSnapshot snapshot
    ) {
        this.vacant = vacant;
        this.steps = List.copyOf(steps);
        this.root = root;
        this.topPath = topPath;
        this.output = output;
        this.snapshot = snapshot;
    }
    @Override
    public boolean empty() {
        return vacant;
    }
    public List<Step> steps() {
        return steps;
    }
    public boolean root() {
        return root;
    }
    public Path topPath() {
        return topPath;
    }
    public Path output() {
        return output;
    }
    public TreeSnapshot snapshot() {
        return snapshot;
    }
    public List<String> staleNodeIds() {
        final List<String> ids = new java.util.ArrayList<>();
        for (final Step step : steps) {
            ids.add(step.id());
        }
        return ids;
    }
    public List<String> staleLeafIds() {
        final List<String> ids = new java.util.ArrayList<>();
        for (final Step step : steps) {
            if (step.leaf()) {
                ids.add(step.id());
            }
        }
        return ids;
    }
    /**
     * One tree node to reuse or rebuild from clip or child paths.
     *
     * <p>Usage: {@code new Step(id, leaf, path, inputs)}
     */
    public static final class Step {
        private final String id;
        private final boolean leaf;
        private final Path path;
        private final List<Path> inputs;
        public Step(final String id, final boolean leaf, final Path path, final List<Path> inputs) {
            this.id = id;
            this.leaf = leaf;
            this.path = path;
            this.inputs = List.copyOf(inputs);
        }
        public String id() {
            return id;
        }
        public boolean leaf() {
            return leaf;
        }
        public Path path() {
            return path;
        }
        public List<Path> inputs() {
            return inputs;
        }
    }
    public static TreePlan idle() {
        return new TreePlan(true, List.of(), false, Path.of("."), Path.of("."), new TreeSnapshot("", "0", Map.of()));
    }
}
