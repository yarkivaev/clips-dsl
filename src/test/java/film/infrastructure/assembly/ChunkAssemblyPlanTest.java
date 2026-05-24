package film.infrastructure.assembly;

import film.infrastructure.assembly.scenario.LegacyManifestScenario;
import film.infrastructure.assembly.scenario.MiddleChunkChangeScenario;
import film.infrastructure.assembly.scenario.UnchangedChunkAssemblyScenario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests ChunkAssembly incremental planning without ffmpeg.
 */
final class ChunkAssemblyPlanTest {
    @Test
    void unchangedAssemblyProducesEmptyPlan(@TempDir final Path workspace) {
        assertThat(
            "unchanged chunk assembly should not schedule work",
            new UnchangedChunkAssemblyScenario(workspace).empty(),
            is(true)
        );
    }
    @Test
    void middleChunkChangeStaleatesOneLeaf(@TempDir final Path workspace) {
        assertThat(
            "middle chunk change should stale only the affected leaf part",
            new MiddleChunkChangeScenario(workspace).staleNodes().equals(java.util.List.of("1")),
            is(true)
        );
    }
    @Test
    void middleChunkChangeSchedulesRootJoin(@TempDir final Path workspace) {
        assertThat(
            "middle chunk change should schedule root assembly join",
            new MiddleChunkChangeScenario(workspace).root(),
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
