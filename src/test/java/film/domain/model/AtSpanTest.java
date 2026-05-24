package film.domain.model;

import film.domain.model.scenario.DurationEndScenario;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests AtSpan resolves end as from plus span.
 */
final class AtSpanTest {
    @Test
    void spanEndEqualsFromPlusSpan() {
        assertThat(
            "span end should equal from plus span on source timeline",
            new DurationEndScenario().amount(),
            is(30.0)
        );
    }
}
