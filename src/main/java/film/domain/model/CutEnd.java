package film.domain.model;

import film.domain.port.Duration;

import java.nio.file.Path;

/**
 * End bound of a segment cut (timestamp, span from {@code from}, or end-of-file).
 */
public sealed interface CutEnd permits AtSecond, AtSpan, AtEof {
    Second resolved(SegmentSpec spec, Duration duration, Path workspace);
}
