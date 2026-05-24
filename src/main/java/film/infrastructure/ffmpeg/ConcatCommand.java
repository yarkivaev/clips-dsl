package film.infrastructure.ffmpeg;

import film.domain.model.MediaContract;
import film.domain.model.RenderProfile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds ffmpeg argument lists for concat operations.
 *
 * <p>Usage: {@code ConcatCommand.copyCommand(listFile, output)}
 */
public final class ConcatCommand {
    private ConcatCommand() {
    }
    public static List<String> copyCommand(final Path listFile, final Path output) {
        final List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg");
        cmd.add("-y");
        cmd.add("-nostats");
        cmd.add("-loglevel");
        cmd.add("info");
        cmd.add("-f");
        cmd.add("concat");
        cmd.add("-safe");
        cmd.add("0");
        cmd.add("-i");
        cmd.add(listFile.toString());
        cmd.add("-c");
        cmd.add("copy");
        cmd.add("-movflags");
        cmd.add("+faststart");
        cmd.add(output.toString());
        return cmd;
    }
    public static List<String> reencodeCommand(
        final List<Path> inputs,
        final MediaContract contract,
        final RenderProfile profile,
        final Path output
    ) {
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
        cmd.add(filter(inputs.size(), contract));
        cmd.add("-map");
        cmd.add("[outv]");
        cmd.add("-map");
        cmd.add("[outa]");
        cmd.add("-c:v");
        cmd.add("libx264");
        cmd.add("-preset");
        cmd.add(profile.clipPreset());
        cmd.add("-crf");
        cmd.add(profile.clipCrf());
        cmd.add("-c:a");
        cmd.add("aac");
        cmd.add("-b:a");
        cmd.add("192k");
        cmd.add("-movflags");
        cmd.add("+faststart");
        cmd.add(output.toString());
        return cmd;
    }
    public static String filter(final int count, final MediaContract contract) {
        final StringBuilder graph = new StringBuilder();
        for (int index = 0; index < count; index++) {
            graph.append("[");
            graph.append(index);
            graph.append(":v]fps=");
            graph.append(contract.fps());
            graph.append(",format=yuv420p,setpts=PTS-STARTPTS[v");
            graph.append(index);
            graph.append("];[");
            graph.append(index);
            graph.append(":a]aresample=");
            graph.append(contract.audioRate());
            graph.append(",asetpts=PTS-STARTPTS[a");
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
