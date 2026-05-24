package film.domain.model;

import film.domain.port.Duration;

import java.nio.file.Path;

/**
 * Segment ends after a length on the source timeline from {@code from}.
 *
 * <p>Usage: {@code new AtSpan(new Second(90))} with {@code from: 10} resolves to second 100
 */
public final class AtSpan implements CutEnd {
    private final Second span;
    public AtSpan(final Second span) {
        if (span.amount() <= 0) {
            throw new IllegalStateException("span must be positive, got " + span.amount());
        }
        this.span = span;
    }
    public Second span() {
        return span;
    }
    @Override
    public Second resolved(final SegmentSpec spec, final Duration duration, final Path workspace) {
        return new Second(spec.from().amount() + span.amount());
    }
}
