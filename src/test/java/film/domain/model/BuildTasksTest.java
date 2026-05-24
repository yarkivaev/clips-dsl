package film.domain.model;

import film.domain.model.scenario.UnchangedFingerprintScenario;
import film.domain.port.AssemblyPlan;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests BuildTasks assembly scheduling flag.
 */
final class BuildTasksTest {
    @Test
    void emptyAssemblyPlanDoesNotScheduleAssembly() {
        final UnchangedFingerprintScenario scenario = new UnchangedFingerprintScenario();
        final AssemblyPlan empty = () -> true;
        final BuildTasks tasks = BuildTasks.withAssembly(
            scenario.timeline(),
            scenario.ends(),
            scenario.tasks().steps(),
            empty
        );
        assertThat(
            "empty assembly plan should not schedule assembly task",
            tasks.assembleScheduled(),
            is(false)
        );
    }
}
