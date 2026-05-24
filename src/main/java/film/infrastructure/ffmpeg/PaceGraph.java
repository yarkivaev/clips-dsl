package film.infrastructure.ffmpeg;

import film.domain.model.Keyframe;
import film.domain.model.Keyframes;

/**
 * Builds filter_complex for keyframed speed on video and audio.
 *
 * <p>Usage: {@code new PaceGraph(true).complex(Keyframes.of(...), 300.0)}
 */
public final class PaceGraph {
    private static final int STEPS = 4;
    private final boolean rubberband;
    public PaceGraph(final boolean rubberband) {
        this.rubberband = rubberband;
    }
    public String complex(final Keyframes keyframes, final double span) {
        return complexOn(keyframes, span, "0:v", "0:a");
    }
    public String complexOn(
        final Keyframes keyframes,
        final double span,
        final String video,
        final String audio
    ) {
        final Keyframes curve = keyframes.normalized(span);
        final var points = curve.points();
        final int intervals = points.size() - 1;
        if (intervals < 1) {
            throw new IllegalStateException("normalized keyframes need at least two points");
        }
        final StringBuilder graph = new StringBuilder();
        int videoOut = 0;
        int audioOut = 0;
        for (int i = 0; i < intervals; i++) {
            final Keyframe start = points.get(i);
            final Keyframe end = points.get(i + 1);
            final double t0 = start.at().amount();
            final double t1 = end.at().amount();
            final double d = t1 - t0;
            final double v0 = start.factor();
            final double v1 = end.factor();
            graph.append(videoSegment(video, t0, t1, d, v0, v1, videoOut));
            graph.append(';');
            if (rubberband) {
                graph.append(audioRubberband(audio, t0, t1, v0, v1, audioOut));
            } else {
                graph.append(audioStepped(audio, t0, t1, d, v0, v1, audioOut));
            }
            if (i < intervals - 1) {
                graph.append(';');
            }
            videoOut++;
            audioOut += rubberband ? 1 : STEPS;
        }
        graph.append(';');
        appendVideoConcat(graph, videoOut);
        graph.append(';');
        appendAudioConcat(graph, audioOut);
        return graph.toString();
    }
    private static String videoSegment(
        final String input,
        final double t0,
        final double t1,
        final double d,
        final double v0,
        final double v1,
        final int label
    ) {
        final String ramp = videoRamp(v0, v1, d);
        return "[" + input + "]trim=start=" + t0 + ":end=" + t1
            + ",setpts=PTS-STARTPTS,fps=30,format=yuv420p,setpts='" + ramp + "'[v" + label + "]";
    }
    private static String videoRamp(final double v0, final double v1, final double d) {
        if (Math.abs(v0 - v1) < 0.0001) {
            return "PTS/" + v0;
        }
        return "if(eq(T,0),PTS/" + v0 + ",PTS*(" + d + "/(" + v1 + "-" + v0
            + "))*log((" + v0 + "+(" + v1 + "-" + v0 + ")*T/" + d + ")/" + v0 + ")/T)";
    }
    private static String audioRubberband(
        final String input,
        final double t0,
        final double t1,
        final double v0,
        final double v1,
        final int label
    ) {
        final double tempo = averageTempo(v0, v1);
        return "[" + input + "]atrim=start=" + t0 + ":end=" + t1
            + ",asetpts=PTS-STARTPTS,rubberband=tempo=" + tempo + "[a" + label + "]";
    }
    private static String audioStepped(
        final String input,
        final double t0,
        final double t1,
        final double d,
        final double v0,
        final double v1,
        final int base
    ) {
        final StringBuilder graph = new StringBuilder();
        final double step = d / STEPS;
        for (int s = 0; s < STEPS; s++) {
            final double s0 = t0 + s * step;
            final double s1 = t0 + (s + 1) * step;
            final double mid = (s0 + s1) / 2.0;
            final double tempo = v0 + (v1 - v0) * (mid - t0) / d;
            final int label = base + s;
            graph.append("[").append(input).append("]atrim=start=").append(s0)
                .append(":end=").append(s1).append(",asetpts=PTS-STARTPTS,")
                .append(tempoChain(tempo)).append("[a").append(label).append("]");
            if (s < STEPS - 1) {
                graph.append(';');
            }
        }
        return graph.toString();
    }
    private static void appendVideoConcat(final StringBuilder graph, final int count) {
        for (int i = 0; i < count; i++) {
            graph.append("[v").append(i).append(']');
        }
        graph.append("concat=n=").append(count).append(":v=1:a=0[outv]");
    }
    private static double averageTempo(final double v0, final double v1) {
        if (Math.abs(v0 - v1) < 0.0001) {
            return v0;
        }
        return (v1 - v0) / Math.log(v1 / v0);
    }
    private static String tempoChain(final double factor) {
        if (factor == 1.0) {
            return "aresample=48000";
        }
        final StringBuilder chain = new StringBuilder("aresample=48000");
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
    private static void appendAudioConcat(final StringBuilder graph, final int count) {
        for (int i = 0; i < count; i++) {
            graph.append("[a").append(i).append(']');
        }
        graph.append("concat=n=").append(count).append(":v=0:a=1,aresample=48000[outa]");
    }
}
