package film.domain.model;

import film.domain.port.Duration;

import java.nio.file.Path;

/**
 * End bound of a segment cut (explicit second or end-of-file).
 */
public sealed interface CutEnd permits AtSecond, AtEof {
    Second resolved(SegmentSpec spec, Duration duration, Path workspace);
}
