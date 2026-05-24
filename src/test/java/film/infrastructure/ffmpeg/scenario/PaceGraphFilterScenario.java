package film.infrastructure.ffmpeg.scenario;

import film.domain.model.Keyframe;
import film.domain.model.Keyframes;
import film.infrastructure.ffmpeg.PaceGraph;

import java.util.List;

/**
 * PaceGraph filter_complex contains rubberband or atempo for keyframed span.
 */
public final class PaceGraphFilterScenario {
    private final boolean matches;
    public PaceGraphFilterScenario(final boolean rubberband) {
        final Keyframes curve = Keyframes.of(List.of(
            new Keyframe(new film.domain.model.Second(0), 1.0),
            new Keyframe(new film.domain.model.Second(60), 4.0)
        ));
        final String graph = new PaceGraph(rubberband).complex(curve, 120.0);
        if (rubberband) {
            this.matches = graph.contains("rubberband=tempo=");
        } else {
            this.matches = graph.contains("atempo=");
        }
    }
    public boolean matches() {
        return matches;
    }
}
