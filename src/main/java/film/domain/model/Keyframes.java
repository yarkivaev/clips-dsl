package film.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Sorted speed keyframes from clip start; linear interpolation between points.
 *
 * <p>Usage: {@code Keyframes.single(2.0)} or {@code Keyframes.of(list)}
 */
public final class Keyframes {
    private final List<Keyframe> points;
    public Keyframes(final List<Keyframe> points) {
        if (points.isEmpty()) {
            throw new IllegalStateException("keyframes must not be empty");
        }
        final List<Keyframe> sorted = new ArrayList<>(points);
        sorted.sort((left, right) -> Double.compare(left.at().amount(), right.at().amount()));
        if (sorted.get(0).at().amount() != 0) {
            throw new IllegalStateException("first keyframe at must be 0");
        }
        double prior = -1;
        for (final Keyframe point : sorted) {
            if (point.at().amount() <= prior) {
                throw new IllegalStateException("keyframe at values must strictly increase");
            }
            prior = point.at().amount();
        }
        this.points = List.copyOf(sorted);
    }
    public static Keyframes single(final double factor) {
        return new Keyframes(List.of(new Keyframe(new Second(0), factor)));
    }
    public static Keyframes of(final List<Keyframe> points) {
        return new Keyframes(points);
    }
    public List<Keyframe> points() {
        return points;
    }
    public boolean constant() {
        final double first = points.get(0).factor();
        for (final Keyframe point : points) {
            if (point.factor() != first) {
                return false;
            }
        }
        return true;
    }
    public double constantFactor() {
        if (!constant()) {
            throw new IllegalStateException("keyframes are not constant");
        }
        return points.get(0).factor();
    }
    public Keyframes normalized(final double span) {
        if (span <= 0) {
            throw new IllegalStateException("span must be positive, got " + span);
        }
        final Keyframe last = points.get(points.size() - 1);
        if (last.at().amount() >= span) {
            return this;
        }
        final List<Keyframe> extended = new ArrayList<>(points);
        extended.add(new Keyframe(new Second(span), last.factor()));
        return new Keyframes(extended);
    }
    public String label() {
        final StringBuilder text = new StringBuilder();
        for (int i = 0; i < points.size(); i++) {
            if (i > 0) {
                text.append(';');
            }
            final Keyframe point = points.get(i);
            text.append(point.at().amount()).append(':').append(point.factor());
        }
        return text.toString();
    }
}
