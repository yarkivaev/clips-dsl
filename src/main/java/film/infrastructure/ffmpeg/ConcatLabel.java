package film.infrastructure.ffmpeg;

import java.nio.file.Path;

/**
 * Human-readable labels for concat progress logs.
 *
 * <p>Usage: {@code new ConcatLabel().part("0", 8, path)}
 */
public final class ConcatLabel {
    public String node(final String id, final int inputs, final Path output) {
        return "concat node " + id + " → " + output.getFileName() + " (" + inputs + " inputs)";
    }
    public String part(final String id, final int clips, final Path output) {
        return "concat part " + id + " → " + output.getFileName() + " (" + clips + " clips)";
    }
    public String root(final int parts, final Path output) {
        return "concat root → " + output.getFileName() + " (" + parts + " parts)";
    }
    public String film(final int clips, final Path output) {
        return "concat film → " + output.getFileName() + " (" + clips + " clips)";
    }
    public String logKey(final String step) {
        return "concat-" + step;
    }
}
