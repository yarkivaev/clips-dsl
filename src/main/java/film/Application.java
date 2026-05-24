package film;

import film.application.FilmBuild;
import film.domain.port.Dsl;
import film.infrastructure.assembly.ChunkAssembly;
import film.infrastructure.ffmpeg.FfmpegCapabilities;
import film.infrastructure.ffmpeg.FfmpegClip;
import film.infrastructure.ffmpeg.FfmpegConcat;
import film.infrastructure.ffmpeg.FfprobeDuration;
import film.infrastructure.manifest.JsonManifest;
import film.infrastructure.yaml.YamlDsl;

import java.nio.file.Path;

/**
 * Composition root for the film compiler CLI.
 *
 * <p>Usage: {@code java -jar film.jar film.dsl.yaml}
 */
public final class Application {
    private final String[] args;
    public Application(final String[] args) {
        this.args = args;
    }
    public static void main(final String[] args) {
        new Application(args).run();
    }
    public void run() {
        if (args.length < 1) {
            throw new IllegalStateException("usage: film.jar <film.dsl.yaml> [--validate]");
        }
        final Path workspace = Path.of("").toAbsolutePath().normalize();
        final Path dslPath = workspace.resolve(args[0]).normalize();
        final Dsl dsl = new YamlDsl();
        if (args.length > 1 && "--validate".equals(args[1])) {
            dsl.opened(dslPath);
            return;
        }
        final Path logs = workspace.resolve("build/logs");
        final FfmpegCapabilities capabilities = new FfmpegCapabilities();
        final FilmBuild build = new FilmBuild(
            dsl,
            new FfprobeDuration(logs),
            new FfmpegClip(logs, capabilities),
            new FfmpegConcat(logs),
            new ChunkAssembly(4),
            new JsonManifest(workspace),
            workspace,
            dslPath
        );
        build.run();
    }
}
