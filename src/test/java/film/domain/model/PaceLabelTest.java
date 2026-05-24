package film.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests Pace label serializes keyframes for fingerprint.
 */
final class PaceLabelTest {
    @Test
    void keyframeLabelListsAtAndFactorPairs() {
        final Pace pace = new Pace(Keyframes.of(List.of(
            new Keyframe(new Second(0), 1.0),
            new Keyframe(new Second(120), 10.0)
        )));
        assertThat(
            "label should list semicolon separated at factor pairs",
            pace.label(),
            is("0.0:1.0;120.0:10.0")
        );
    }
}
