package film.domain.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests RenderProfile draft and release settings.
 */
final class RenderProfileTest {
    @Test
    void draftProfileUsesCopyConcat() {
        assertThat(
            "draft profile should enable copy concat",
            RenderProfile.draft().copyConcat(),
            is(true)
        );
    }
    @Test
    void releaseProfileUsesReencodeConcat() {
        assertThat(
            "release profile should disable copy concat",
            RenderProfile.release().copyConcat(),
            is(false)
        );
    }
}
