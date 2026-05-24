package film.infrastructure.ffmpeg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * FFmpeg features detected once at startup.
 *
 * <p>Usage: {@code new FfmpegCapabilities().rubberband()}
 */
public final class FfmpegCapabilities {
    private final boolean rubberband;
    public FfmpegCapabilities() {
        this.rubberband = probeRubberband();
    }
    public FfmpegCapabilities(final boolean rubberband) {
        this.rubberband = rubberband;
    }
    public boolean rubberband() {
        return rubberband;
    }
    private static boolean probeRubberband() {
        try {
            final Process process = new ProcessBuilder("ffmpeg", "-filters").start();
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("rubberband")) {
                        process.waitFor();
                        return true;
                    }
                }
            }
            process.waitFor();
            return false;
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        } catch (final java.io.IOException ex) {
            return false;
        }
    }
}
