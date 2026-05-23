package film.domain.model;

import java.nio.file.Path;

/**
 * Parsed DSL: timeline and output path.
 */
public final class OpenedDsl {
    private final Timeline timeline;
    private final Path output;
    public OpenedDsl(final Timeline timeline, final Path output) {
        this.timeline = timeline;
        this.output = output;
    }
    public Timeline timeline() {
        return timeline;
    }
    public Path output() {
        return output;
    }
}
