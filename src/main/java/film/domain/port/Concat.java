package film.domain.port;

import film.domain.model.ResolvedEnds;
import film.domain.model.SegmentId;
import film.domain.model.Timeline;

import java.nio.file.Path;
import java.util.Map;

/**
 * Joins clip files into the final film.
 */
public interface Concat {
    void join(Timeline timeline, ResolvedEnds ends, Map<SegmentId, Path> clips, Path output);
}
