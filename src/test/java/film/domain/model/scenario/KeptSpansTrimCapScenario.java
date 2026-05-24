package film.domain.model.scenario;

import film.domain.model.AtSecond;
import film.domain.model.Cut;
import film.domain.model.Edits;
import film.domain.model.Excludes;
import film.domain.model.KeptSpans;
import film.domain.model.Pace;
import film.domain.model.Second;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.SourceRef;
import film.domain.model.TrimCap;

/**
 * Trim cap limits play length on trimmed timeline when exclude is present.
 */
public final class KeptSpansTrimCapScenario {
    private final double play;
    public KeptSpansTrimCapScenario() {
        final SegmentSpec spec = new SegmentSpec(
            new SegmentId("clip"),
            new SourceRef(1),
            new Cut(
                new Second(0),
                new AtSecond(new Second(100)),
                Pace.one(),
                new Edits(Excludes.none(), TrimCap.of(new Second(25)))
            )
        );
        this.play = KeptSpans.from(spec, new Second(100)).play();
    }
    public double play() {
        return play;
    }
}
