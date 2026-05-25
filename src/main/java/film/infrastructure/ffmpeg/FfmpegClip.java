package film.infrastructure.ffmpeg;

import film.domain.model.Keyframes;
import film.domain.model.KeptSpans;
import film.domain.model.MediaContract;
import film.domain.model.Pace;
import film.domain.model.RenderProfile;
import film.domain.model.Second;
import film.domain.model.SegmentSpec;
import film.domain.port.Clip;

import java.nio.file.Path;
import java.util.List;

/**
 * Cuts a segment to normalized h264/aac with optional pace and zero-based pts.
 */
public final class FfmpegClip implements Clip {
    private final FfmpegProcess ffmpeg;
    private final PaceGraph graphs;
    private final RenderProfile profile;
    private final MediaContract contract;
    private final boolean rubberband;
    private boolean warnedRubberband;
    public FfmpegClip(
        final Path logDir,
        final FfmpegCapabilities capabilities,
        final RenderProfile profile,
        final MediaContract contract
    ) {
        this.ffmpeg = new FfmpegProcess(logDir);
        this.rubberband = capabilities.rubberband();
        this.graphs = new PaceGraph(rubberband);
        this.profile = profile;
        this.contract = contract;
        this.warnedRubberband = false;
    }
    @Override
    public void cut(final SegmentSpec spec, final Second end, final Path workspace, final Path dest) {
        final Path input = workspace.resolve(spec.source().filename()).normalize();
        final String id = spec.id().label();
        if (spec.edits().trimmed()) {
            cutTrimmed(spec, end, workspace, dest, id, input);
            return;
        }
        final double start = spec.from().amount();
        final double stop = end.amount();
        final double span = stop - start;
        if (span <= 0) {
            throw new IllegalStateException(
                "segment span must be positive for " + id + " from " + start + " to " + stop
            );
        }
        if (spec.pace().constant()) {
            cutConstant(spec, input, span, workspace, dest, id);
            return;
        }
        warnRubberbandFallback(id);
        cutKeyframes(spec, span, workspace, dest, id, input);
    }
    private void cutTrimmed(
        final SegmentSpec spec,
        final Second end,
        final Path workspace,
        final Path dest,
        final String id,
        final Path input
    ) {
        final KeptSpans kept = KeptSpans.from(spec, end);
        final double window = end.amount() - spec.from().amount();
        if (spec.pace().constant()) {
            cutTrimmedConstant(spec, kept, window, workspace, dest, id, input);
            return;
        }
        warnRubberbandFallback(id);
        cutTrimmedKeyframes(spec, kept, window, workspace, dest, id, input);
    }
    private void cutTrimmedConstant(
        final SegmentSpec spec,
        final KeptSpans kept,
        final double window,
        final Path workspace,
        final Path dest,
        final String id,
        final Path input
    ) {
        final double offset = spec.from().amount();
        final String concat = ExcludeGraph.concat(kept.parts(), contract, offset, spec.pace());
        runTrimmedFilter(spec, input, window, workspace, dest, id, concat, "[basev]", "[basea]");
    }
    private void cutTrimmedKeyframes(
        final SegmentSpec spec,
        final KeptSpans kept,
        final double window,
        final Path workspace,
        final Path dest,
        final String id,
        final Path input
    ) {
        final Keyframes curve = spec.pace().keyframes();
        final double offset = spec.from().amount();
        final String concat = ExcludeGraph.concat(kept.parts(), contract, offset, Pace.one());
        final String pace = graphs.complexOn(curve, kept.play(), "basev", "basea");
        runTrimmedFilter(spec, input, window, workspace, dest, id, concat + ";" + pace, "[outv]", "[outa]");
    }
    private void cutConstant(
        final SegmentSpec spec,
        final Path input,
        final double span,
        final Path workspace,
        final Path dest,
        final String id
    ) {
        final double factor = spec.pace().constantFactor();
        final String video = ClipCommand.baseVideoFilter(contract) + PaceChain.videoSuffix(factor);
        final String audio = PaceChain.audio(contract, factor);
        final ProcessBuilder builder = baseBuilder(spec, input, span, workspace);
        final List<String> command = builder.command();
        command.add("-vf");
        command.add(video);
        command.add("-af");
        command.add(audio);
        ClipCommand.encodeTail(command, profile, dest);
        ffmpeg.run(builder, "cut-" + id, "cut " + id);
    }
    private void cutKeyframes(
        final SegmentSpec spec,
        final double span,
        final Path workspace,
        final Path dest,
        final String id,
        final Path input
    ) {
        final Keyframes curve = spec.pace().keyframes();
        final String graph = graphs.complex(curve, span);
        runTrimmedFilter(spec, input, span, workspace, dest, id, graph, "[outv]", "[outa]");
    }
    private void runTrimmedFilter(
        final SegmentSpec spec,
        final Path input,
        final double window,
        final Path workspace,
        final Path dest,
        final String id,
        final String graph,
        final String video,
        final String audio
    ) {
        final ProcessBuilder builder = new ProcessBuilder(
            "ffmpeg",
            "-y",
            "-nostats",
            "-loglevel", "info",
            "-ss", spec.from().ffmpeg(),
            "-t", Double.toString(window),
            "-i", input.toString()
        );
        builder.directory(workspace.toFile());
        final List<String> command = builder.command();
        command.add("-filter_complex");
        command.add(graph);
        command.add("-map");
        command.add(video);
        command.add("-map");
        command.add(audio);
        ClipCommand.encodeTail(command, profile, dest);
        ffmpeg.run(builder, "cut-" + id, "cut " + id);
    }
    private static ProcessBuilder baseBuilder(
        final SegmentSpec spec,
        final Path input,
        final double span,
        final Path workspace
    ) {
        final ProcessBuilder builder = new ProcessBuilder(
            "ffmpeg",
            "-y",
            "-nostats",
            "-loglevel", "info",
            "-ss", spec.from().ffmpeg(),
            "-t", Double.toString(span),
            "-i", input.toString()
        );
        builder.directory(workspace.toFile());
        return builder;
    }
    private void warnRubberbandFallback(final String id) {
        if (rubberband || warnedRubberband) {
            return;
        }
        warnedRubberband = true;
        System.out.println(
            "warning: ffmpeg has no rubberband filter, keyframe clip " + id + " uses stepped atempo audio"
        );
    }
}
