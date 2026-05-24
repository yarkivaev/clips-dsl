package film.domain.port;

import film.domain.model.AssemblySnapshot;
import film.domain.model.Manifest;
import film.domain.model.ResolvedEnds;
import film.domain.model.SegmentId;
import film.domain.model.Timeline;

import java.nio.file.Path;
import java.util.Map;

/**
 * Plans and executes incremental assembly of cached clips into the final film.
 *
 * <p>Usage: {@code assembly.planned(prior, desired, ends, clips)}
 */
public interface Assembly {
    AssemblyPlan planned(
        Manifest prior,
        Timeline desired,
        ResolvedEnds ends,
        Map<SegmentId, Path> clips,
        Path workspace,
        Path output
    );
    AssemblySnapshot executed(
        AssemblyPlan plan,
        Concat concat,
        Path workspace,
        Path partsDir,
        Path output
    );
}
