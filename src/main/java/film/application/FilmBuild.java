package film.application;

import film.domain.model.Manifest;
import film.domain.model.OpenedDsl;
import film.domain.model.ResolvedEnds;
import film.domain.port.Clip;
import film.domain.port.Concat;
import film.domain.port.Dsl;
import film.domain.port.Duration;
import film.domain.port.ManifestFile;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Use case: build film from DSL with incremental clip cache.
 *
 * <p>Usage: {@code new FilmBuild(dsl, duration, clip, concat, manifest, workspace, dslPath).run()}
 */
public final class FilmBuild {
    private final Dsl dsl;
    private final Duration duration;
    private final Clip clip;
    private final Concat concat;
    private final ManifestFile manifestFile;
    private final Path workspace;
    private final Path dslPath;
    public FilmBuild(
        final Dsl dsl,
        final Duration duration,
        final Clip clip,
        final Concat concat,
        final ManifestFile manifestFile,
        final Path workspace,
        final Path dslPath
    ) {
        this.dsl = dsl;
        this.duration = duration;
        this.clip = clip;
        this.concat = concat;
        this.manifestFile = manifestFile;
        this.workspace = workspace;
        this.dslPath = dslPath;
    }
    public void run() {
        final OpenedDsl opened = dsl.opened(dslPath);
        final var desired = opened.timeline();
        validateSources(desired);
        final Path output = workspace.resolve(opened.output().toString()).normalize();
        final ResolvedEnds ends = ResolvedEnds.of(desired, this::resolveEnd);
        final Manifest prior = manifestFile.loaded();
        final var tasks = prior.diff(desired, ends).tasks();
        final Path clipsDir = workspace.resolve("build").resolve("clips");
        try {
            Files.createDirectories(clipsDir);
            Files.createDirectories(output.getParent());
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("cannot create build dirs under " + workspace, ex);
        }
        final var artifacts = tasks.execute(clip, concat, workspace, clipsDir, output, prior);
        manifestFile.saved(prior.saved(desired, ends, artifacts));
    }
    private film.domain.model.Second resolveEnd(final film.domain.model.SegmentSpec spec) {
        return spec.end().resolved(spec, duration, workspace);
    }
    private void validateSources(final film.domain.model.Timeline desired) {
        for (final film.domain.model.SegmentSpec spec : desired.segments()) {
            final Path source = workspace.resolve(spec.source().filename());
            if (!Files.isRegularFile(source)) {
                throw new IllegalStateException(
                    "missing source file " + source + " for segment " + spec.id().label()
                );
            }
        }
    }
}
