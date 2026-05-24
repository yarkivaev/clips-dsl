package film.infrastructure.ffmpeg.scenario;

import film.domain.model.Keyframe;
import film.domain.model.Keyframes;
import film.domain.model.Second;
import film.infrastructure.ffmpeg.PaceGraph;

import java.util.List;

/**
 * Deceleration ramp uses integrated setpts log expression not linear divisor.
 */
public final class PaceGraphDecelerationRampScenario {
    private final boolean matches;
    public PaceGraphDecelerationRampScenario() {
        final Keyframes curve = Keyframes.of(List.of(
            new Keyframe(new Second(0), 3.0),
            new Keyframe(new Second(20), 1.0)
        ));
        final String graph = new PaceGraph(true).complex(curve, 30.0);
        this.matches = graph.contains("log((");
    }
    public boolean matches() {
        return matches;
    }
}
