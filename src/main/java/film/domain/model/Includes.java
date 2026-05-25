package film.domain.model;

import java.util.List;

/**
 * Source spans to keep inside a clip window.
 *
 * <p>Usage: {@code Includes.of(list)} or {@code Includes.none()}
 */
public final class Includes {
    private final List<EditSpan> spans;
    public Includes(final List<EditSpan> spans) {
        this.spans = List.copyOf(spans);
    }
    public static Includes none() {
        return new Includes(List.of());
    }
    public static Includes of(final List<EditSpan> spans) {
        return new Includes(spans);
    }
    public boolean present() {
        return !spans.isEmpty();
    }
    public List<EditSpan> spans() {
        return spans;
    }
    public String label() {
        if (spans.isEmpty()) {
            return "";
        }
        final StringBuilder text = new StringBuilder();
        for (int i = 0; i < spans.size(); i++) {
            if (i > 0) {
                text.append(';');
            }
            text.append(spans.get(i).spanLabel());
        }
        return text.toString();
    }
}
