package film.domain.model;

import film.domain.port.Duration;

import java.nio.file.Path;

/**
 * Segment ends at an explicit timestamp.
 *
 * <p>Usage: {@code new AtSecond(new Second(90))}
 */
public final class AtSecond implements CutEnd {
    private final Second second;
    public AtSecond(final Second second) {
        this.second = second;
    }
    public Second second() {
        return second;
    }
    @Override
    public Second resolved(final SegmentSpec spec, final Duration duration, final Path workspace) {
        return second;
    }
}
