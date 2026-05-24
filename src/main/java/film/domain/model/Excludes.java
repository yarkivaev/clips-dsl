package film.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Sorted exclude gaps on the source file timeline.
 *
 * <p>Usage: {@code Excludes.of(list)} or {@code Excludes.none()}
 */
public final class Excludes {
    private final List<ExcludeSpan> gaps;
    public Excludes(final List<ExcludeSpan> gaps) {
        this.gaps = List.copyOf(gaps);
    }
    public static Excludes none() {
        return new Excludes(List.of());
    }
    public static Excludes of(final List<ExcludeSpan> gaps) {
        return new Excludes(gaps);
    }
    public boolean present() {
        return !gaps.isEmpty();
    }
    public List<ExcludeSpan> gaps() {
        return gaps;
    }
    public String label() {
        if (gaps.isEmpty()) {
            return "";
        }
        final StringBuilder text = new StringBuilder();
        for (int i = 0; i < gaps.size(); i++) {
            if (i > 0) {
                text.append(';');
            }
            text.append(gaps.get(i).gapLabel());
        }
        return text.toString();
    }
}
