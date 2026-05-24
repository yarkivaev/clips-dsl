package film.domain.model;

/**
 * Normalized build artifact format for cut output and copy-concat checks.
 *
 * <p>Usage: {@code MediaContract.defaults()}
 */
public final class MediaContract {
    private final int width;
    private final int height;
    private final int fps;
    private final int audioRate;
    public MediaContract(final int width, final int height, final int fps, final int audioRate) {
        this.width = width;
        this.height = height;
        this.fps = fps;
        this.audioRate = audioRate;
    }
    public static MediaContract defaults() {
        return new MediaContract(1280, 720, 30, 48000);
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
    public String label() {
        return width + "x" + height + "@" + fps + "-h264-aac" + audioRate;
    }
    public boolean matches(final StreamShape shape) {
        return width == shape.width()
            && height == shape.height()
            && fps == shape.fps()
            && audioRate == shape.audioRate();
    }
}
