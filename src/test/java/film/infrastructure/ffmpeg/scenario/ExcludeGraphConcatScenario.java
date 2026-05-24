package film.infrastructure.ffmpeg.scenario;

import film.domain.model.MediaContract;
import film.domain.model.Second;
import film.domain.model.SourceSpan;
import film.infrastructure.ffmpeg.ExcludeGraph;

import java.util.List;

/**
 * ExcludeGraph builds trim and concat for multiple kept spans.
 */
public final class ExcludeGraphConcatScenario {
    private final boolean matches;
    public ExcludeGraphConcatScenario() {
        final String graph = ExcludeGraph.concat(
            List.of(
                new SourceSpan(new Second(0), new Second(10)),
                new SourceSpan(new Second(20), new Second(30))
            ),
            MediaContract.defaults(),
            0
        );
        this.matches = graph.contains("trim=start=0.0:end=10.0")
            && graph.contains("trim=start=20.0:end=30.0")
            && graph.contains("concat=n=2");
    }
    public boolean matches() {
        return matches;
    }
}
