package film.domain.model;

import java.nio.file.Path;

/**
 * Parsed DSL: timeline, output path, and media contract.
 */
public final class OpenedDsl {
    private final Timeline timeline;
    private final Path output;
    private final MediaContract contract;
    public OpenedDsl(final Timeline timeline, final Path output, final MediaContract contract) {
        this.timeline = timeline;
        this.output = output;
        this.contract = contract;
    }
    public Timeline timeline() {
        return timeline;
    }
    public Path output() {
        return output;
    }
    public MediaContract contract() {
        return contract;
    }
}
