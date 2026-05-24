package film.infrastructure.assembly;

import film.domain.model.Manifest;
import film.domain.model.ResolvedEnds;
import film.domain.model.SegmentId;
import film.domain.model.Timeline;
import film.domain.model.VacantAssemblySnapshot;
import film.infrastructure.assembly.scenario.AssemblyTimelineFixture;
import film.infrastructure.assembly.scenario.FlatChunkPriorScenario;
import film.infrastructure.assembly.scenario.MatchedTreeMissingFilesScenario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

/**
 * Tests TreeAssembly stale planning for nine clip timelines.
 */
final class TreeAssemblyNineClipsTest {
    @Test
    void vacantPriorStaleatesAllLeaves(@TempDir final Path workspace) {
        final int span = 4;
        final Timeline timeline = AssemblyTimelineFixture.timeline(9);
        final ResolvedEnds ends = AssemblyTimelineFixture.ends(timeline);
        final Manifest prior = new Manifest(AssemblyTimelineFixture.profile(), new VacantAssemblySnapshot(), new HashMap<>());
        final Map<SegmentId, Path> clips = clipPaths(timeline, workspace);
        final TreePlan tree = (TreePlan) new TreeAssembly(span, AssemblyTimelineFixture.profile(), AssemblyTimelineFixture.contract()).planned(
            prior,
            timeline,
            ends,
            clips,
            workspace,
            workspace.resolve("build/output.mp4")
        );
        assertThat(
            "vacant prior should stale all three leaves for nine clips",
            tree.staleLeafIds(),
            containsInAnyOrder("0", "1", "2")
        );
    }
    @Test
    void matchedTreeMissingFilesRunsLeavesBeforeInternalNode(@TempDir final Path workspace) {
        assertThat(
            "missing node files should run leaves before internal node",
            new MatchedTreeMissingFilesScenario(workspace).staleNodes(),
            is(List.of("0", "1", "2", "1.0"))
        );
    }
    @Test
    void flatChunkPriorRunsLeavesBeforeInternalNode(@TempDir final Path workspace) {
        assertThat(
            "legacy chunk paths should run leaves before internal node",
            new FlatChunkPriorScenario(workspace).staleNodes(),
            is(List.of("0", "1", "2", "1.0"))
        );
    }
    @Test
    void flatChunkPriorStaleatesLeavesWhenTreePathsMissing(@TempDir final Path workspace) {
        assertThat(
            "flat chunk prior with legacy part paths should stale all leaves when tree paths are missing",
            new FlatChunkPriorScenario(workspace).staleLeaves(),
            containsInAnyOrder("0", "1", "2")
        );
    }
    private static Map<SegmentId, Path> clipPaths(final Timeline timeline, final Path workspace) {
        final Map<SegmentId, Path> clips = new HashMap<>();
        for (final film.domain.model.SegmentSpec spec : timeline.segments()) {
            clips.put(spec.id(), workspace.resolve("build/clips/" + spec.id().label() + ".mp4"));
        }
        return clips;
    }
}
