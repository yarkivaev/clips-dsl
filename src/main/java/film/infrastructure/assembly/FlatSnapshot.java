package film.infrastructure.assembly;

import film.domain.model.AssemblySnapshot;

/**
 * Cached flat assembly with timeline root digest only.
 *
 * <p>Usage: {@code new FlatSnapshot(digest)}
 */
public final class FlatSnapshot implements AssemblySnapshot {
    private final String root;
    public FlatSnapshot(final String root) {
        this.root = root;
    }
    public String root() {
        return root;
    }
}
