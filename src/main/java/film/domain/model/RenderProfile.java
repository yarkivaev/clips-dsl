package film.domain.model;

/**
 * Draft or release encoding settings for cut and concat stages.
 *
 * <p>Usage: {@code RenderProfile.draft()}
 */
public final class RenderProfile {
    private final String label;
    private final String clipPreset;
    private final String clipCrf;
    private final boolean copyConcat;
    private RenderProfile(
        final String label,
        final String clipPreset,
        final String clipCrf,
        final boolean copyConcat
    ) {
        this.label = label;
        this.clipPreset = clipPreset;
        this.clipCrf = clipCrf;
        this.copyConcat = copyConcat;
    }
    public static RenderProfile draft() {
        return new RenderProfile("draft", "ultrafast", "28", true);
    }
    public static RenderProfile release() {
        return new RenderProfile("release", "slow", "18", false);
    }
    public static RenderProfile parsed(final String label) {
        if ("release".equals(label)) {
            return release();
        }
        if ("draft".equals(label)) {
            return draft();
        }
        throw new IllegalStateException("unsupported render profile label " + label);
    }
    public String label() {
        return label;
    }
    public String clipPreset() {
        return clipPreset;
    }
    public String clipCrf() {
        return clipCrf;
    }
    public boolean copyConcat() {
        return copyConcat;
    }
}
