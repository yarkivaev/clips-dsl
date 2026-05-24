package film.infrastructure.ffmpeg;

import film.domain.model.MediaContract;

import java.nio.file.Path;
import java.util.List;

/**
 * Verifies all inputs match the build media contract before copy concat.
 *
 * <p>Usage: {@code new StreamMatch(probe).matched(inputs, contract)}
 */
public final class StreamMatch {
    private final FfprobeStream probe;
    public StreamMatch(final FfprobeStream probe) {
        this.probe = probe;
    }
    public void matched(final List<Path> inputs, final MediaContract contract) {
        for (final Path input : inputs) {
            if (!contract.matches(probe.shape(input))) {
                throw new IllegalStateException("stream shape mismatch for copy concat input " + input);
            }
        }
    }
}
