package film.domain.model;

/**
 * Render profile and media contract for one build run.
 *
 * <p>Usage: {@code new BuildSettings(RenderProfile.draft(), MediaContract.defaults())}
 */
public final class BuildSettings {
    private final RenderProfile profile;
    private final MediaContract contract;
    public BuildSettings(final RenderProfile profile, final MediaContract contract) {
        this.profile = profile;
        this.contract = contract;
    }
    public RenderProfile profile() {
        return profile;
    }
    public MediaContract contract() {
        return contract;
    }
}
