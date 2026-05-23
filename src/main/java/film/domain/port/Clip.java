package film.domain.port;

import film.domain.model.Second;
import film.domain.model.SegmentSpec;

import java.nio.file.Path;

/**
 * Cuts one segment from a numbered source into a clip file.
 */
public interface Clip {
    void cut(SegmentSpec spec, Second end, Path workspace, Path dest);
}
