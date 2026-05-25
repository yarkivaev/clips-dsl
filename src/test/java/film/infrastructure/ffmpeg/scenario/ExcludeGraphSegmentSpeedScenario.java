package film.infrastructure.ffmpeg.scenario;

import film.domain.model.MediaContract;
import film.domain.model.Pace;
import film.domain.model.Second;
import film.domain.model.SourceSpan;
import film.infrastructure.ffmpeg.ExcludeGraph;

import java.util.List;

/**
 * ExcludeGraph multiplies clip pace and segment pace in per-part filters.
 */
public final class ExcludeGraphSegmentSpeedScenario {
    private final boolean matches;
    public ExcludeGraphSegmentSpeedScenario() {
        final String graph = ExcludeGraph.concat(
            List.of(new SourceSpan(new Second(0), new Second(10), new Pace(2.0))),
            MediaContract.defaults(),
            0,
            new Pace(3.0)
        );
        this.matches = graph.contains("setpts=PTS/6.0") || graph.contains("setpts=PTS/6");
    }
    public boolean matches() {
        return matches;
    }
}
