package film.domain.model.scenario;

import film.domain.model.AtSecond;
import film.domain.model.Cut;
import film.domain.model.Edits;
import film.domain.model.ExcludeSpan;
import film.domain.model.Excludes;
import film.domain.model.KeptSpans;
import film.domain.model.Pace;
import film.domain.model.Second;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.SourceRef;
import film.domain.model.TrimCap;
import film.domain.model.WindowStop;

import java.util.List;

/**
 * Exclude without to or duration drops from absolute from through clip window end.
 */
public final class KeptSpansOpenToExcludeScenario {
    private final double play;
    public KeptSpansOpenToExcludeScenario() {
        final SegmentSpec spec = new SegmentSpec(
            new SegmentId("clip"),
            new SourceRef(1),
            new Cut(
                new Second(40),
                new AtSecond(new Second(100)),
                Pace.one(),
                new Edits(
                    Excludes.of(List.of(new ExcludeSpan(new Second(60), WindowStop.INSTANCE))),
                    TrimCap.none()
                )
            )
        );
        this.play = KeptSpans.from(spec, new Second(100)).play();
    }
    public double play() {
        return play;
    }
}
