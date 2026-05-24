package film.infrastructure.assembly.scenario;

import film.domain.model.MediaContract;
import film.domain.model.RenderProfile;
import film.domain.model.AtSecond;
import film.domain.model.Cut;
import film.domain.model.Edits;
import film.domain.model.Pace;
import film.domain.model.ResolvedEnds;
import film.domain.model.Second;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.SourceRef;
import film.domain.model.Timeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds synthetic timelines and resolved ends for assembly plan tests.
 */
public final class AssemblyTimelineFixture {
    private AssemblyTimelineFixture() {
    }
    public static RenderProfile profile() {
        return RenderProfile.draft();
    }
    public static MediaContract contract() {
        return MediaContract.defaults();
    }
    public static Timeline timeline(final int count) {
        final List<SegmentSpec> segments = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            segments.add(new SegmentSpec(
                new SegmentId("seg-" + index),
                new SourceRef(index + 1),
                new Cut(new Second(0), new AtSecond(new Second(10)), Pace.one(), Edits.none())
            ));
        }
        return new Timeline(segments);
    }
    public static ResolvedEnds ends(final Timeline timeline) {
        final Map<SegmentId, Second> map = new HashMap<>();
        for (final SegmentSpec spec : timeline.segments()) {
            map.put(spec.id(), new Second(10));
        }
        return new ResolvedEnds(map);
    }
}
