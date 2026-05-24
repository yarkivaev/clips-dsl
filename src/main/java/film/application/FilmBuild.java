package film.application;

import film.domain.model.BuildResult;
import film.domain.model.BuildTasks;
import film.domain.model.Manifest;
import film.domain.model.OpenedDsl;
import film.domain.model.ResolvedEnds;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.port.Assembly;
import film.domain.port.Clip;
import film.domain.port.Concat;
import film.domain.port.Dsl;
import film.domain.port.Duration;
import film.domain.port.ManifestFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Use case: build film from DSL with incremental clip cache.
 *
 * <p>Usage: {@code new FilmBuild(dsl, duration, clip, concat, assembly, manifest, workspace, dslPath).run()}
 */
public final class FilmBuild {
    private final Dsl dsl;
    private final Duration duration;
    private final Clip clip;
    private final Concat concat;
    private final Assembly assembly;
    private final ManifestFile manifestFile;
    private final Path workspace;
    private final Path dslPath;
    public FilmBuild(
        final Dsl dsl,
        final Duration duration,
        final Clip clip,
        final Concat concat,
        final Assembly assembly,
        final ManifestFile manifestFile,
        final Path workspace,
        final Path dslPath
    ) {
        this.dsl = dsl;
        this.duration = duration;
        this.clip = clip;
        this.concat = concat;
        this.assembly = assembly;
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
        final var segmentTasks = prior.diff(desired, ends).tasks();
        final Path clipsDir = workspace.resolve("build/clips");
        final Path partsDir = workspace.resolve("build/parts");
        try {
            Files.createDirectories(clipsDir);
            Files.createDirectories(partsDir);
            Files.createDirectories(output.getParent());
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("cannot create build dirs under " + workspace, ex);
        }
        final Map<SegmentId, Path> clips = preseed(prior, desired, ends, clipsDir);
        final var assemblyPlan = assembly.planned(prior, desired, ends, clips, workspace, output);
        final BuildTasks tasks = BuildTasks.withAssembly(desired, ends, segmentTasks.steps(), assemblyPlan);
        final BuildResult result = tasks.execute(
            clip, concat, assembly, workspace, clipsDir, partsDir, output, prior
        );
        manifestFile.saved(prior.saved(desired, ends, result.artifacts(), result.assembly()));
    }
    private Map<SegmentId, Path> preseed(
        final Manifest prior,
        final film.domain.model.Timeline desired,
        final ResolvedEnds ends,
        final Path clipsDir
    ) {
        final Map<SegmentId, Path> clips = new HashMap<>();
        for (final SegmentSpec spec : desired.segments()) {
            if (prior.cached(spec.id(), spec.fingerprint(ends.end(spec)))) {
                clips.put(spec.id(), prior.path(spec.id()));
            } else {
                clips.put(spec.id(), clipsDir.resolve(spec.id().label() + ".mp4"));
            }
        }
        return clips;
    }
    private film.domain.model.Second resolveEnd(final film.domain.model.SegmentSpec spec) {
        return spec.end().resolved(spec, duration, workspace);
    }
    private void validateSources(final film.domain.model.Timeline desired) {
        for (final SegmentSpec spec : desired.segments()) {
            final Path source = workspace.resolve(spec.source().filename());
            if (!Files.isRegularFile(source)) {
                throw new IllegalStateException(
                    "missing source file " + source + " for segment " + spec.id().label()
                );
            }
        }
    }
}
