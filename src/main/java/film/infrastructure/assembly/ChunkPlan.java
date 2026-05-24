package film.infrastructure.assembly;

import film.domain.port.AssemblyPlan;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Executable chunk assembly plan with stale leaf nodes and optional root join.
 *
 * <p>Usage: {@code plan.empty()}
 */
public final class ChunkPlan implements AssemblyPlan {
    private final boolean vacant;
    private final List<Leaf> leaves;
    private final boolean root;
    private final List<Path> parts;
    private final Path output;
    private final ChunkSnapshot snapshot;
    public ChunkPlan(
        final boolean vacant,
        final List<Leaf> leaves,
        final boolean root,
        final List<Path> parts,
        final Path output,
        final ChunkSnapshot snapshot
    ) {
        this.vacant = vacant;
        this.leaves = List.copyOf(leaves);
        this.root = root;
        this.parts = List.copyOf(parts);
        this.output = output;
        this.snapshot = snapshot;
    }
    @Override
    public boolean empty() {
        return vacant;
    }
    public List<Leaf> leaves() {
        return leaves;
    }
    public boolean root() {
        return root;
    }
    public List<Path> parts() {
        return parts;
    }
    public Path output() {
        return output;
    }
    public ChunkSnapshot snapshot() {
        return snapshot;
    }
    public List<String> staleNodeIds() {
        final List<String> ids = new java.util.ArrayList<>();
        for (final Leaf leaf : leaves) {
            if (leaf.stale()) {
                ids.add(leaf.id());
            }
        }
        return ids;
    }
    /**
     * One leaf node to reuse or rebuild from clip paths.
     *
     * <p>Usage: {@code new Leaf(id, stale, path, inputs)}
     */
    public static final class Leaf {
        private final String id;
        private final boolean stale;
        private final Path path;
        private final List<Path> inputs;
        public Leaf(final String id, final boolean stale, final Path path, final List<Path> inputs) {
            this.id = id;
            this.stale = stale;
            this.path = path;
            this.inputs = List.copyOf(inputs);
        }
        public String id() {
            return id;
        }
        public boolean stale() {
            return stale;
        }
        public Path path() {
            return path;
        }
        public List<Path> inputs() {
            return inputs;
        }
    }
    public static ChunkPlan idle() {
        return new ChunkPlan(true, List.of(), false, List.of(), Path.of("."), new ChunkSnapshot("", Map.of()));
    }
}
