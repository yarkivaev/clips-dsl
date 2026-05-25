package film.infrastructure.ffmpeg;

import film.domain.model.MediaContract;
import film.domain.model.Pace;
import film.domain.model.SourceSpan;

import java.util.List;

/**
 * Builds filter_complex that trims kept source spans and concatenates them.
 *
 * <p>Usage: {@code ExcludeGraph.concat(parts, contract, offset, clipPace)}
 */
public final class ExcludeGraph {
    private ExcludeGraph() {
    }
    public static String concat(
        final List<SourceSpan> parts,
        final MediaContract contract,
        final double offset,
        final Pace clipPace
    ) {
        if (parts.isEmpty()) {
            throw new IllegalStateException("concat needs at least one kept span");
        }
        if (!clipPace.constant()) {
            throw new IllegalStateException("concat needs constant clip pace");
        }
        final double clipFactor = clipPace.constantFactor();
        final String base = ClipCommand.baseVideoFilter(contract);
        final StringBuilder graph = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            final SourceSpan part = parts.get(i);
            final double start = part.start().amount() - offset;
            final double stop = part.stop().amount() - offset;
            final double factor = clipFactor * part.pace().constantFactor();
            final String video = PaceChain.videoSuffix(factor);
            final String audio = PaceChain.audio(contract, factor);
            graph.append("[0:v]trim=start=").append(start).append(":end=").append(stop)
                .append(",setpts=PTS-STARTPTS,").append(base).append(video).append("[v").append(i).append("];");
            graph.append("[0:a]atrim=start=").append(start).append(":end=").append(stop)
                .append(',').append(audio).append("[a").append(i).append(']');
            if (i < parts.size() - 1) {
                graph.append(';');
            }
        }
        graph.append(';');
        for (int i = 0; i < parts.size(); i++) {
            graph.append("[v").append(i).append(']');
        }
        graph.append("concat=n=").append(parts.size()).append(":v=1:a=0[basev];");
        for (int i = 0; i < parts.size(); i++) {
            graph.append("[a").append(i).append(']');
        }
        graph.append("concat=n=").append(parts.size()).append(":v=0:a=1[basea]");
        return graph.toString();
    }
}
