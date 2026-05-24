package film.domain.model.scenario;

import film.domain.model.MediaContract;
import film.domain.model.RenderProfile;

/**
 * Default build settings for domain model test scenarios.
 */
public final class TestBuildSettings {
    private TestBuildSettings() {
    }
    public static RenderProfile profile() {
        return RenderProfile.draft();
    }
    public static MediaContract contract() {
        return MediaContract.defaults();
    }
}
