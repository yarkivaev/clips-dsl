package film.infrastructure.ffmpeg;

import film.domain.model.StreamShape;

import java.nio.file.Path;

/**
 * Reads normalized stream shape from mp4 files via ffprobe.
 *
 * <p>Usage: {@code new FfprobeStream(logDir).shape(path)}
 */
public final class FfprobeStream {
    private final FfmpegProcess ffmpeg;
    public FfprobeStream(final Path logDir) {
        this.ffmpeg = new FfmpegProcess(logDir);
    }
    public StreamShape shape(final Path file) {
        final int width = probeInt(file, "v:0", "stream=width", "width");
        final int height = probeInt(file, "v:0", "stream=height", "height");
        final int fps = probeFps(file);
        final int audioRate = probeInt(file, "a:0", "stream=sample_rate", "rate");
        return new StreamShape(width, height, fps, audioRate);
    }
    private int probeInt(
        final Path file,
        final String stream,
        final String entry,
        final String label
    ) {
        final ProcessBuilder builder = new ProcessBuilder(
            "ffprobe",
            "-v", "error",
            "-select_streams", stream,
            "-show_entries", entry,
            "-of", "default=noprint_wrappers=1:nokey=1",
            file.toString()
        );
        final String text = ffmpeg.output(builder, "probe-" + label + "-" + file.getFileName(), "probe " + label);
        if (text.isBlank()) {
            throw new IllegalStateException("ffprobe returned no " + label + " for " + file);
        }
        return Integer.parseInt(text.trim().split("\\.")[0]);
    }
    private int probeFps(final Path file) {
        final ProcessBuilder builder = new ProcessBuilder(
            "ffprobe",
            "-v", "error",
            "-select_streams", "v:0",
            "-show_entries", "stream=avg_frame_rate",
            "-of", "default=noprint_wrappers=1:nokey=1",
            file.toString()
        );
        final String text = ffmpeg.output(builder, "probe-fps-" + file.getFileName(), "probe fps");
        if (text.isBlank()) {
            throw new IllegalStateException("ffprobe returned no fps for " + file);
        }
        return ProbeFrameRate.parsed(text);
    }
}
