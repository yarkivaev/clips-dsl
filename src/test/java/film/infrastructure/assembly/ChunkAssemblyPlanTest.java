package film.infrastructure.assembly;

import film.infrastructure.assembly.scenario.DeepTreeChangeScenario;
import film.infrastructure.assembly.scenario.FlatChunkPriorScenario;
import film.infrastructure.assembly.scenario.LegacyManifestScenario;
import film.infrastructure.assembly.scenario.MiddleChunkChangeScenario;
import film.infrastructure.assembly.scenario.UnchangedChunkAssemblyScenario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests TreeAssembly incremental planning without ffmpeg.
 */
final class ChunkAssemblyPlanTest {
    @Test
    void unchangedAssemblyProducesEmptyPlan(@TempDir final Path workspace) {
        assertThat(
            "unchanged tree assembly should not schedule work",
            new UnchangedChunkAssemblyScenario(workspace).empty(),
            is(true)
        );
    }
    @Test
    void middleChunkChangeStaleatesOneLeaf(@TempDir final Path workspace) {
        assertThat(
            "middle leaf change should stale only the affected leaf part",
            new MiddleChunkChangeScenario(workspace).staleLeaves().equals(java.util.List.of("2")),
            is(true)
        );
    }
    @Test
    void middleChunkChangeStaleatesAncestors(@TempDir final Path workspace) {
        assertThat(
            "middle leaf change should stale ancestor internal nodes",
            new MiddleChunkChangeScenario(workspace).staleNodes().contains("1.0"),
            is(true)
        );
    }
    @Test
    void middleChunkChangeSchedulesRootJoin(@TempDir final Path workspace) {
        assertThat(
            "middle leaf change should schedule root assembly join",
            new MiddleChunkChangeScenario(workspace).root(),
            is(true)
        );
    }
    @Test
    void deepTreeChangeStaleatesPathToRoot(@TempDir final Path workspace) {
        assertThat(
            "first leaf change in deep tree should stale path to root",
            new DeepTreeChangeScenario(workspace).staleNodes().contains("2.0"),
            is(true)
        );
    }
    @Test
    void flatChunkPriorStaleatesMissingTreeLeaves(@TempDir final Path workspace) {
        assertThat(
            "flat chunk prior should stale leaves when tree node paths are absent",
            new FlatChunkPriorScenario(workspace).staleLeaves().contains("1"),
            is(true)
        );
    }
    @Test
    void legacyManifestSchedulesAssembly(@TempDir final Path workspace) {
        assertThat(
            "legacy manifest without assembly should schedule assembly work",
            new LegacyManifestScenario(workspace).empty(),
            is(false)
        );
    }
}
