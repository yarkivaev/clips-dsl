package film.domain.port;

import film.domain.model.Second;
import film.domain.model.SourceRef;

/**
 * Reads source file length in seconds.
 */
public interface Duration {
    Second length(SourceRef source, java.nio.file.Path workspace);
}
