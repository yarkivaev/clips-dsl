package film.domain.model;

import film.domain.port.Assembly;
import film.domain.port.Clip;
import film.domain.port.Concat;

import java.nio.file.Path;
import java.util.Map;

/**
 * Context passed to each build task during execute.
 */
public final class Execution {
    private final Clip clip;
    private final Concat concat;
    private final Assembly assembly;
    private final Path workspace;
    private final Path clipsDir;
    private final Path partsDir;
    private final Path output;
    private final Timeline timeline;
    private final ResolvedEnds ends;
    private final Map<SegmentId, Path> artifacts;
    private AssemblySnapshot assemblySnapshot;
    public Execution(
        final Clip clip,
        final Concat concat,
        final Assembly assembly,
        final Path workspace,
        final Path clipsDir,
        final Path partsDir,
        final Path output,
        final Timeline timeline,
        final ResolvedEnds ends,
        final Map<SegmentId, Path> artifacts,
        final AssemblySnapshot assemblySnapshot
    ) {
        this.clip = clip;
        this.concat = concat;
        this.assembly = assembly;
        this.workspace = workspace;
        this.clipsDir = clipsDir;
        this.partsDir = partsDir;
        this.output = output;
        this.timeline = timeline;
        this.ends = ends;
        this.artifacts = artifacts;
        this.assemblySnapshot = assemblySnapshot;
    }
    public Clip clip() {
        return clip;
    }
    public Concat concat() {
        return concat;
    }
    public Assembly assembly() {
        return assembly;
    }
    public Path workspace() {
        return workspace;
    }
    public Path clipsDir() {
        return clipsDir;
    }
    public Path partsDir() {
        return partsDir;
    }
    public Path output() {
        return output;
    }
    public Timeline timeline() {
        return timeline;
    }
    public ResolvedEnds ends() {
        return ends;
    }
    public Map<SegmentId, Path> artifacts() {
        return artifacts;
    }
    public AssemblySnapshot assemblySnapshot() {
        return assemblySnapshot;
    }
    public void assemblySnapshot(final AssemblySnapshot snapshot) {
        this.assemblySnapshot = snapshot;
    }
}
