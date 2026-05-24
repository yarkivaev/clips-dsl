package film.infrastructure.ffmpeg;

/**
 * Parses ffprobe frame rate strings into integer fps.
 *
 * <p>Usage: {@code ProbeFrameRate.parsed("8770560/292417")}
 */
public final class ProbeFrameRate {
    private ProbeFrameRate() {
    }
    public static int parsed(final String raw) {
        final String text = raw.trim();
        if (!text.contains("/")) {
            return (int) Math.round(Double.parseDouble(text));
        }
        final String[] parts = text.split("/");
        final double num = Double.parseDouble(parts[0]);
        final double den = Double.parseDouble(parts[1]);
        return (int) Math.round(num / den);
    }
}
