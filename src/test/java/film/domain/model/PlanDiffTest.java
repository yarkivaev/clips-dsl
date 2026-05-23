package film.domain.model;

import film.domain.model.scenario.ChangedSegmentBoundScenario;
import film.domain.model.scenario.UnchangedFingerprintScenario;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

/**
 * Tests PlanDiff task scheduling.
 */
final class PlanDiffTest {
    @Test
    void unchangedFingerprintProducesOnlySkippedTasks() {
        assertThat(
            "diff should not schedule render when fingerprints match",
            new UnchangedFingerprintScenario().taskTypes(),
            contains(SkippedTask.class, SkippedTask.class)
        );
    }
    @Test
    void unchangedFingerprintDoesNotScheduleJoin() {
        assertThat(
            "timeline fingerprint unchanged should not concat",
            new UnchangedFingerprintScenario().joinScheduled(),
            is(false)
        );
    }
    @Test
    void changedSegmentBoundSchedulesRender() {
        assertThat(
            "changed segment bound should schedule render",
            new ChangedSegmentBoundScenario().renderScheduled(),
            is(true)
        );
    }
    @Test
    void changedSegmentBoundSchedulesJoin() {
        assertThat(
            "changed segment bound should schedule concat",
            new ChangedSegmentBoundScenario().joinScheduled(),
            is(true)
        );
    }
}
