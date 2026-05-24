package film.domain.model;

import java.nio.file.Path;
import java.util.Map;

/**
 * Result of a build run with clip artifacts and assembly snapshot.
 *
 * <p>Usage: {@code new BuildResult(artifacts, assembly)}
 */
public final class BuildResult {
    private final Map<SegmentId, Path> artifacts;
    private final AssemblySnapshot assembly;
    public BuildResult(final Map<SegmentId, Path> artifacts, final AssemblySnapshot assembly) {
        this.artifacts = Map.copyOf(artifacts);
        this.assembly = assembly;
    }
    public Map<SegmentId, Path> artifacts() {
        return artifacts;
    }
    public AssemblySnapshot assembly() {
        return assembly;
    }
}
