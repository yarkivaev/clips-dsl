package film.infrastructure.ffmpeg;

import film.domain.model.MediaContract;
import film.domain.model.RenderProfile;
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
 * Joins clips with stream copy in draft or filter concat in release.
 */
public final class FfmpegConcat implements Concat {
    private final FfmpegProcess ffmpeg;
    private final ConcatLabel labels;
    private final RenderProfile profile;
    private final MediaContract contract;
    private final StreamMatch match;
    public FfmpegConcat(
        final Path logDir,
        final RenderProfile profile,
        final MediaContract contract
    ) {
        this.ffmpeg = new FfmpegProcess(logDir);
        this.labels = new ConcatLabel();
        this.profile = profile;
        this.contract = contract;
        this.match = new StreamMatch(new FfprobeStream(logDir));
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
            if (profile.copyConcat()) {
                copyJoined(inputs, output, logKey, label);
                return;
            }
            reencodeJoined(inputs, output, logKey, label);
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("ffmpeg concat cannot prepare output " + output, ex);
        }
    }
    private void copyJoined(
        final List<Path> inputs,
        final Path output,
        final String logKey,
        final String label
    ) throws java.io.IOException {
        match.matched(inputs, contract);
        final Path listFile = output.getParent().resolve(logKey + "-list.txt");
        writeList(inputs, listFile);
        final ProcessBuilder builder = new ProcessBuilder(
            ConcatCommand.copyCommand(listFile, output)
        );
        ffmpeg.run(builder, logKey, label);
    }
    private void reencodeJoined(
        final List<Path> inputs,
        final Path output,
        final String logKey,
        final String label
    ) {
        final ProcessBuilder builder = new ProcessBuilder(
            ConcatCommand.reencodeCommand(inputs, contract, profile, output)
        );
        ffmpeg.run(builder, logKey, label);
    }
    private static void writeList(final List<Path> inputs, final Path listFile) throws java.io.IOException {
        final StringBuilder text = new StringBuilder();
        for (final Path input : inputs) {
            text.append("file '");
            text.append(input.toAbsolutePath().toString().replace("'", "'\\''"));
            text.append("'\n");
        }
        Files.writeString(listFile, text.toString());
    }
}
