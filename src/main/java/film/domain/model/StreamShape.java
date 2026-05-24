package film.domain.model;

/**
 * Probed stream parameters from a normalized build mp4 file.
 *
 * <p>Usage: {@code new StreamShape(1280, 720, 30, 48000)}
 */
public final class StreamShape {
    private final int width;
    private final int height;
    private final int fps;
    private final int audioRate;
    public StreamShape(final int width, final int height, final int fps, final int audioRate) {
        this.width = width;
        this.height = height;
        this.fps = fps;
        this.audioRate = audioRate;
    }
    public int width() {
        return width;
    }
    public int height() {
        return height;
    }
    public int fps() {
        return fps;
    }
    public int audioRate() {
        return audioRate;
    }
}
