package film.domain.model.scenario;

import film.domain.model.AtSecond;
import film.domain.model.Cut;
import film.domain.model.Edits;
import film.domain.model.ExcludeSpan;
import film.domain.model.Excludes;
import film.domain.model.GapAt;
import film.domain.model.KeptSpans;
import film.domain.model.Pace;
import film.domain.model.Second;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.SourceRef;
import film.domain.model.TrimCap;

import java.util.List;

/**
 * Exclude intervals clip to the clip window instead of failing when they stick out.
 */
public final class KeptSpansClippedExcludeScenario {
    private final double play;
    public KeptSpansClippedExcludeScenario() {
        final SegmentSpec spec = new SegmentSpec(
            new SegmentId("clip"),
            new SourceRef(1),
            new Cut(
                new Second(45),
                new AtSecond(new Second(200)),
                Pace.one(),
                new Edits(
                    Excludes.of(List.of(new ExcludeSpan(new Second(30), new GapAt(new Second(80))))),
                    TrimCap.none()
                )
            )
        );
        this.play = KeptSpans.from(spec, new Second(200)).play();
    }
    public double play() {
        return play;
    }
}
