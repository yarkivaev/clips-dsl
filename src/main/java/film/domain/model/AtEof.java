package film.domain.model;

import film.domain.port.Duration;

import java.nio.file.Path;

/**
 * Segment runs until the source file ends.
 *
 * <p>Usage: {@code AtEof.INSTANCE}
 */
public enum AtEof implements CutEnd {
    INSTANCE;
    @Override
    public Second resolved(final SegmentSpec spec, final Duration duration, final Path workspace) {
        return duration.length(spec.source(), workspace);
    }
}
