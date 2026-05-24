package film.domain.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Source spans kept after excludes and optional trimmed-timeline cap.
 *
 * <p>Usage: {@code KeptSpans.from(spec, outerEnd)}
 */
public final class KeptSpans {
    private final List<SourceSpan> parts;
    private final double play;
    public KeptSpans(final List<SourceSpan> parts, final double play) {
        if (parts.isEmpty()) {
            throw new IllegalStateException("kept spans must not be empty");
        }
        if (play <= 0) {
            throw new IllegalStateException("kept play span must be positive, got " + play);
        }
        this.parts = List.copyOf(parts);
        this.play = play;
    }
    public List<SourceSpan> parts() {
        return parts;
    }
    public double play() {
        return play;
    }
    public static KeptSpans from(final SegmentSpec spec, final Second outerEnd) {
        final double windowStart = spec.from().amount();
        final double windowStop = outerEnd.amount();
        if (windowStop <= windowStart) {
            throw new IllegalStateException(
                "clip window must be positive for " + spec.id().label()
            );
        }
        final List<SourceSpan> kept = complement(windowStart, windowStop, gaps(spec, windowStart, windowStop));
        final List<SourceSpan> capped = cap(kept, spec.edits().cap());
        final double play = capped.stream().mapToDouble(SourceSpan::length).sum();
        return new KeptSpans(capped, play);
    }
    private static List<double[]> gaps(
        final SegmentSpec spec,
        final double windowStart,
        final double windowStop
    ) {
        final List<double[]> absolute = new ArrayList<>();
        for (final ExcludeSpan gap : spec.edits().excludes().gaps()) {
            final double rawStart = gap.startSource(windowStart);
            final double rawStop = gap.stopSource(windowStart, windowStop);
            final double start = Math.max(windowStart, rawStart);
            final double stop = Math.min(windowStop, rawStop);
            if (start >= stop) {
                continue;
            }
            absolute.add(new double[] {start, stop});
        }
        absolute.sort(Comparator.comparingDouble(left -> left[0]));
        double priorStop = windowStart;
        for (final double[] gap : absolute) {
            if (gap[0] < priorStop) {
                throw new IllegalStateException("exclude gaps must not overlap");
            }
            priorStop = gap[1];
        }
        return absolute;
    }
    private static List<SourceSpan> complement(
        final double windowStart,
        final double windowStop,
        final List<double[]> gaps
    ) {
        double cursor = windowStart;
        final List<SourceSpan> kept = new ArrayList<>();
        for (final double[] gap : gaps) {
            if (gap[0] > windowStop) {
                throw new IllegalStateException("exclude starts past clip window");
            }
            if (gap[1] > windowStop) {
                throw new IllegalStateException("exclude ends past clip window");
            }
            if (gap[0] > cursor) {
                kept.add(new SourceSpan(new Second(cursor), new Second(gap[0])));
            }
            cursor = Math.max(cursor, gap[1]);
        }
        if (cursor < windowStop) {
            kept.add(new SourceSpan(new Second(cursor), new Second(windowStop)));
        }
        if (kept.isEmpty()) {
            throw new IllegalStateException("excludes remove entire clip window");
        }
        return kept;
    }
    private static List<SourceSpan> cap(final List<SourceSpan> kept, final TrimCap cap) {
        if (!cap.present()) {
            return kept;
        }
        double left = cap.span().amount();
        final List<SourceSpan> capped = new ArrayList<>();
        for (final SourceSpan part : kept) {
            if (left <= 0) {
                break;
            }
            final double len = part.length();
            if (len <= left) {
                capped.add(part);
                left -= len;
            } else {
                capped.add(new SourceSpan(part.start(), new Second(part.start().amount() + left)));
                left = 0;
            }
        }
        if (capped.isEmpty()) {
            throw new IllegalStateException("trim cap leaves no kept content");
        }
        return capped;
    }
}
