package film.infrastructure.ffmpeg;

import film.domain.model.Keyframes;
import film.domain.model.Second;
import film.domain.model.SegmentSpec;
import film.domain.port.Clip;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Cuts a segment to 30 fps h264/aac with optional pace and zero-based pts.
 */
public final class FfmpegClip implements Clip {
    private final FfmpegProcess ffmpeg;
    private final PaceGraph graphs;
    private final boolean rubberband;
    private boolean warnedRubberband;
    public FfmpegClip(final Path logDir, final FfmpegCapabilities capabilities) {
        this.ffmpeg = new FfmpegProcess(logDir);
        this.rubberband = capabilities.rubberband();
        this.graphs = new PaceGraph(rubberband);
        this.warnedRubberband = false;
    }
    @Override
    public void cut(final SegmentSpec spec, final Second end, final Path workspace, final Path dest) {
        final Path input = workspace.resolve(spec.source().filename()).normalize();
        final String id = spec.id().label();
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
    private void cutConstant(
        final SegmentSpec spec,
        final Path input,
        final double span,
        final Path workspace,
        final Path dest,
        final String id
    ) {
        final double factor = spec.pace().constantFactor();
        final String video = "fps=30,format=yuv420p,setpts=PTS-STARTPTS" + constantVideoSuffix(factor);
        final String audio = constantAudioChain(factor);
        final ProcessBuilder builder = baseBuilder(spec, input, span, workspace);
        final List<String> command = builder.command();
        command.add("-vf");
        command.add(video);
        command.add("-af");
        command.add(audio);
        encodeTail(command, dest);
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
        final ProcessBuilder builder = baseBuilder(spec, input, span, workspace);
        final List<String> command = builder.command();
        command.add("-filter_complex");
        command.add(graph);
        command.add("-map");
        command.add("[outv]");
        command.add("-map");
        command.add("[outa]");
        encodeTail(command, dest);
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
    private static void encodeTail(final List<String> command, final Path dest) {
        command.add("-c:v");
        command.add("libx264");
        command.add("-preset");
        command.add("fast");
        command.add("-crf");
        command.add("22");
        command.add("-c:a");
        command.add("aac");
        command.add("-b:a");
        command.add("192k");
        command.add("-movflags");
        command.add("+faststart");
        command.add(dest.toString());
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
    private static String constantVideoSuffix(final double factor) {
        if (factor == 1.0) {
            return "";
        }
        return ",setpts=PTS/" + factor;
    }
    private static String constantAudioChain(final double factor) {
        if (factor == 1.0) {
            return "aresample=48000,asetpts=PTS-STARTPTS";
        }
        final StringBuilder chain = new StringBuilder("aresample=48000,asetpts=PTS-STARTPTS");
        double left = factor;
        while (left > 2.0) {
            chain.append(",atempo=2.0");
            left /= 2.0;
        }
        while (left < 0.5) {
            chain.append(",atempo=0.5");
            left /= 0.5;
        }
        if (Math.abs(left - 1.0) > 0.001) {
            chain.append(",atempo=").append(left);
        }
        return chain.toString();
    }
}
