package film.domain.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Source spans kept after excludes or includes and optional trimmed-timeline cap.
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
        final List<SourceSpan> kept = spec.edits().includes().present()
            ? fromIncludes(spec, windowStart, windowStop)
            : fromExcludes(spec, windowStart, windowStop);
        final List<SourceSpan> capped = cap(kept, spec.edits().cap());
        final double play = capped.stream().mapToDouble(SourceSpan::play).sum();
        return new KeptSpans(capped, play);
    }
    private static List<SourceSpan> fromIncludes(
        final SegmentSpec spec,
        final double windowStart,
        final double windowStop
    ) {
        final List<SourceSpan> raw = new ArrayList<>();
        for (final EditSpan span : spec.edits().includes().spans()) {
            final double start = Math.max(windowStart, span.startSource(windowStart));
            final double stop = Math.min(windowStop, span.stopSource(windowStart, windowStop));
            if (start >= stop) {
                continue;
            }
            raw.add(new SourceSpan(new Second(start), new Second(stop), span.pace()));
        }
        if (raw.isEmpty()) {
            throw new IllegalStateException(
                "include leaves no content in clip window for " + spec.id().label()
            );
        }
        raw.sort(Comparator.comparingDouble(part -> part.start().amount()));
        return merge(raw);
    }
    private static List<SourceSpan> merge(final List<SourceSpan> parts) {
        final List<SourceSpan> out = new ArrayList<>();
        SourceSpan current = parts.get(0);
        for (int i = 1; i < parts.size(); i++) {
            final SourceSpan next = parts.get(i);
            if (next.start().amount() <= current.stop().amount()) {
                if (!current.pace().label().equals(next.pace().label())) {
                    throw new IllegalStateException("overlapping includes must use the same speed");
                }
                current = new SourceSpan(
                    current.start(),
                    new Second(Math.max(current.stop().amount(), next.stop().amount())),
                    current.pace()
                );
            } else {
                out.add(current);
                current = next;
            }
        }
        out.add(current);
        return out;
    }
    private static List<SourceSpan> fromExcludes(
        final SegmentSpec spec,
        final double windowStart,
        final double windowStop
    ) {
        final List<EditSpan> gaps = new ArrayList<>(spec.edits().excludes().gaps());
        gaps.sort(Comparator.comparingDouble(span -> span.startSource(windowStart)));
        double priorStop = windowStart;
        final List<SourceSpan> kept = new ArrayList<>();
        for (final EditSpan gap : gaps) {
            final double rawStart = gap.startSource(windowStart);
            final double rawStop = gap.stopSource(windowStart, windowStop);
            final double start = Math.max(windowStart, rawStart);
            final double stop = Math.min(windowStop, rawStop);
            if (start >= stop) {
                continue;
            }
            if (start < priorStop) {
                throw new IllegalStateException("exclude gaps must not overlap");
            }
            if (start > priorStop) {
                kept.add(new SourceSpan(new Second(priorStop), new Second(start), gap.pace()));
            }
            priorStop = stop;
        }
        if (priorStop < windowStop) {
            kept.add(new SourceSpan(new Second(priorStop), new Second(windowStop), Pace.one()));
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
            final double out = part.play();
            if (out <= left) {
                capped.add(part);
                left -= out;
            } else {
                final double source = left * part.pace().constantFactor();
                capped.add(new SourceSpan(part.start(), new Second(part.start().amount() + source), part.pace()));
                left = 0;
            }
        }
        if (capped.isEmpty()) {
            throw new IllegalStateException("trim cap leaves no kept content");
        }
        return capped;
    }
}
