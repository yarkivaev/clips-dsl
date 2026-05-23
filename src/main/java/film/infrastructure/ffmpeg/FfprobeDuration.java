package film.infrastructure.ffmpeg;

import film.domain.model.Second;
import film.domain.model.SourceRef;
import film.domain.port.Duration;

import java.nio.file.Path;

/**
 * Reads media duration via ffprobe.
 */
public final class FfprobeDuration implements Duration {
    private final FfmpegProcess ffmpeg;
    public FfprobeDuration(final Path logDir) {
        this.ffmpeg = new FfmpegProcess(logDir);
    }
    @Override
    public Second length(final SourceRef source, final Path workspace) {
        final Path input = workspace.resolve(source.filename()).normalize();
        final ProcessBuilder builder = new ProcessBuilder(
            "ffprobe",
            "-v", "error",
            "-show_entries", "format=duration",
            "-of", "default=noprint_wrappers=1:nokey=1",
            input.toString()
        );
        builder.directory(workspace.toFile());
        final String text = ffmpeg.output(builder, "probe-" + source.number(), "probe " + source.number());
        if (text.isBlank()) {
            throw new IllegalStateException("ffprobe returned no duration for source " + source.number());
        }
        return new Second(Double.parseDouble(text.trim()));
    }
}
