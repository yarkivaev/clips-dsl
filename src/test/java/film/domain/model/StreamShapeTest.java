package film.domain.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests MediaContract compatibility checks against StreamShape.
 */
final class StreamShapeTest {
    @Test
    void defaultContractMatchesDefaultShape() {
        assertThat(
            "default contract should match default stream shape",
            MediaContract.defaults().matches(new StreamShape(1280, 720, 30, 48000)),
            is(true)
        );
    }
    @Test
    void defaultContractRejectsWrongHeight() {
        assertThat(
            "default contract should reject wrong height",
            MediaContract.defaults().matches(new StreamShape(1280, 1080, 30, 48000)),
            is(false)
        );
    }
}
