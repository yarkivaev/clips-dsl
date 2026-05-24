package film;

import film.application.FilmBuild;
import film.domain.model.BuildSettings;
import film.domain.model.RenderProfile;
import film.domain.port.Assembly;
import film.domain.port.Dsl;
import film.infrastructure.assembly.FlatAssembly;
import film.infrastructure.assembly.TreeAssembly;
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
            throw new IllegalStateException("usage: film.jar <film.dsl.yaml> [--validate|--release]");
        }
        final Path workspace = Path.of("").toAbsolutePath().normalize();
        final Path dslPath = workspace.resolve(args[0]).normalize();
        final Dsl dsl = new YamlDsl();
        if (args.length > 1 && "--validate".equals(args[1])) {
            dsl.opened(dslPath);
            return;
        }
        final RenderProfile profile = releaseFlag() ? RenderProfile.release() : RenderProfile.draft();
        final Path logs = workspace.resolve("build/logs");
        final var opened = dsl.opened(dslPath);
        final BuildSettings settings = new BuildSettings(profile, opened.contract());
        final Assembly assembly = profile.copyConcat()
            ? new TreeAssembly(4, profile, opened.contract())
            : new FlatAssembly(profile, opened.contract());
        final FilmBuild build = new FilmBuild(
            dsl,
            new FfprobeDuration(logs),
            new FfmpegClip(logs, new FfmpegCapabilities(), profile, opened.contract()),
            new FfmpegConcat(logs, profile, opened.contract()),
            assembly,
            new JsonManifest(workspace),
            settings,
            workspace,
            dslPath
        );
        build.run();
    }
    private boolean releaseFlag() {
        for (final String arg : args) {
            if ("--release".equals(arg)) {
                return true;
            }
        }
        return false;
    }
}
