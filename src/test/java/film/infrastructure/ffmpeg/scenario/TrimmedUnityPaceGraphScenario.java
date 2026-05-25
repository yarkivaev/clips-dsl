package film.infrastructure.ffmpeg.scenario;

import film.domain.model.MediaContract;
import film.domain.model.Pace;
import film.domain.model.Second;
import film.domain.model.SourceSpan;
import film.infrastructure.ffmpeg.ExcludeGraph;

import java.util.List;

/**
 * Unity-speed trimmed graph maps concat outputs without copy filter.
 */
public final class TrimmedUnityPaceGraphScenario {
    private final boolean matches;
    public TrimmedUnityPaceGraphScenario() {
        final String graph = ExcludeGraph.concat(
            List.of(
                new SourceSpan(new Second(4), new Second(15), Pace.one()),
                new SourceSpan(new Second(22), new Second(44), Pace.one())
            ),
            MediaContract.defaults(),
            4,
            Pace.one()
        );
        this.matches = graph.contains("[basev]") && graph.contains("[basea]") && !graph.contains("copy");
    }
    public boolean matches() {
        return matches;
    }
}
