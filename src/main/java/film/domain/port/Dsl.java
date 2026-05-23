package film.domain.port;

import film.domain.model.OpenedDsl;

import java.nio.file.Path;

/**
 * Opens a DSL file into a timeline and output path.
 */
public interface Dsl {
    OpenedDsl opened(Path file);
}
