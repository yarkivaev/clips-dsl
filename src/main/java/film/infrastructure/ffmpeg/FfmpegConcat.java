package film.infrastructure.ffmpeg;

import film.domain.model.ResolvedEnds;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.Timeline;
import film.domain.port.Concat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Joins clips with the concat filter (one ffmpeg input per clip).
 */
public final class FfmpegConcat implements Concat {
    private final FfmpegProcess ffmpeg;
    private final ConcatLabel labels;
    public FfmpegConcat(final Path logDir) {
        this.ffmpeg = new FfmpegProcess(logDir);
        this.labels = new ConcatLabel();
    }
    @Override
    public void join(
        final Timeline timeline,
        final ResolvedEnds ends,
        final Map<SegmentId, Path> clips,
        final Path output
    ) {
        final List<Path> inputs = new ArrayList<>();
        for (final SegmentSpec spec : timeline.segments()) {
            if (!clips.containsKey(spec.id())) {
                throw new IllegalStateException("missing clip path for segment " + spec.id().label());
            }
            inputs.add(clips.get(spec.id()));
        }
        joined(inputs, output, labels.logKey("film"), labels.film(inputs.size(), output));
    }
    @Override
    public void joined(final List<Path> inputs, final Path output, final String logKey, final String label) {
        if (inputs.isEmpty()) {
            throw new IllegalStateException("concat requires at least one input for " + output);
        }
        try {
            Files.createDirectories(output.getParent());
            final List<String> cmd = new ArrayList<>();
            cmd.add("ffmpeg");
            cmd.add("-y");
            cmd.add("-nostats");
            cmd.add("-loglevel");
            cmd.add("info");
            for (final Path input : inputs) {
                cmd.add("-i");
                cmd.add(input.toAbsolutePath().toString());
            }
            cmd.add("-filter_complex");
            cmd.add(filter(inputs.size()));
            cmd.add("-map");
            cmd.add("[outv]");
            cmd.add("-map");
            cmd.add("[outa]");
            cmd.add("-c:v");
            cmd.add("libx264");
            cmd.add("-preset");
            cmd.add("fast");
            cmd.add("-crf");
            cmd.add("22");
            cmd.add("-c:a");
            cmd.add("aac");
            cmd.add("-b:a");
            cmd.add("192k");
            cmd.add("-movflags");
            cmd.add("+faststart");
            cmd.add(output.toString());
            final ProcessBuilder builder = new ProcessBuilder(cmd);
            ffmpeg.run(builder, logKey, label);
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("ffmpeg concat cannot prepare output " + output, ex);
        }
    }
    private String filter(final int count) {
        final StringBuilder graph = new StringBuilder();
        for (int index = 0; index < count; index++) {
            graph.append("[");
            graph.append(index);
            graph.append(":v]fps=30,format=yuv420p,setpts=PTS-STARTPTS[v");
            graph.append(index);
            graph.append("];[");
            graph.append(index);
            graph.append(":a]aresample=48000,asetpts=PTS-STARTPTS[a");
            graph.append(index);
            graph.append("];");
        }
        for (int index = 0; index < count; index++) {
            graph.append("[v");
            graph.append(index);
            graph.append("][a");
            graph.append(index);
            graph.append("]");
        }
        graph.append("concat=n=");
        graph.append(count);
        graph.append(":v=1:a=1[outv][outa]");
        return graph.toString();
    }
}
