package film.domain.model.scenario;

import film.domain.model.AtSecond;
import film.domain.model.Cut;
import film.domain.model.Edits;
import film.domain.model.EditSpan;
import film.domain.model.Excludes;
import film.domain.model.GapSpan;
import film.domain.model.Includes;
import film.domain.model.KeptSpans;
import film.domain.model.Pace;
import film.domain.model.Second;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.SourceRef;
import film.domain.model.TrimCap;

import java.util.List;

/**
 * Include segment speed shortens trimmed play length before clip speed applies.
 */
public final class KeptSpansIncludeSpeedScenario {
    private final double play;
    public KeptSpansIncludeSpeedScenario() {
        final SegmentSpec spec = new SegmentSpec(
            new SegmentId("clip"),
            new SourceRef(1),
            new Cut(
                new Second(0),
                new AtSecond(new Second(100)),
                Pace.one(),
                new Edits(
                    Excludes.none(),
                    Includes.of(List.of(
                        new EditSpan(new Second(0), new GapSpan(new Second(20)), new Pace(2.0))
                    )),
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
