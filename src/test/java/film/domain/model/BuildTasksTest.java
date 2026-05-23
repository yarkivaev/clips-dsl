package film.domain.model;

import film.domain.model.scenario.UnchangedFingerprintScenario;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests BuildTasks join scheduling flag.
 */
final class BuildTasksTest {
    @Test
    void unchangedTimelineDoesNotScheduleJoin() {
        assertThat(
            "unchanged timeline should not schedule join task",
            new UnchangedFingerprintScenario().joinScheduled(),
            is(false)
        );
    }
}
