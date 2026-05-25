package film.domain.model.scenario;

import film.domain.model.AtSecond;
import film.domain.model.Cut;
import film.domain.model.Edits;
import film.domain.model.EditSpan;
import film.domain.model.Excludes;
import film.domain.model.GapAt;
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
 * Overlapping include spans merge into one kept interval.
 */
public final class KeptSpansIncludeMergeScenario {
    private final double play;
    public KeptSpansIncludeMergeScenario() {
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
                        new EditSpan(new Second(10), new GapAt(new Second(25))),
                        new EditSpan(new Second(20), new GapAt(new Second(30)))
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
