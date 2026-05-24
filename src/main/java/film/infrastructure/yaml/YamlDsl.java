package film.infrastructure.yaml;

import film.domain.model.AtEof;
import film.domain.model.AtSecond;
import film.domain.model.AtSpan;
import film.domain.model.Cut;
import film.domain.model.CutEnd;
import film.domain.model.Pace;
import film.domain.model.OpenedDsl;
import film.domain.model.Second;
import film.domain.model.SegmentId;
import film.domain.model.SegmentSpec;
import film.domain.model.SourceRef;
import film.domain.model.Timeline;
import film.domain.port.Dsl;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reads film.dsl.yaml into domain timeline.
 */
public final class YamlDsl implements Dsl {
    @Override
    public OpenedDsl opened(final Path file) {
        final Map<String, Object> root = load(file);
        final Path output = Path.of(string(root, "output"));
        final List<SegmentSpec> segments = clips(root);
        validateUniqueIds(segments);
        return new OpenedDsl(new Timeline(segments), output);
    }
    @SuppressWarnings("unchecked")
    private static List<SegmentSpec> clips(final Map<String, Object> root) {
        final Object raw = root.get("clips");
        if (!(raw instanceof List<?> list)) {
            throw new IllegalStateException("DSL clips must be a list in " + root);
        }
        final List<SegmentSpec> segments = new ArrayList<>();
        for (final Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                throw new IllegalStateException("each clip must be a mapping");
            }
            segments.add(clip((Map<String, Object>) map));
        }
        return segments;
    }
    private static SegmentSpec clip(final Map<String, Object> map) {
        final String id = string(map, "id");
        final int source = asInt(map, "source");
        final double from = map.containsKey("from") ? asDouble(map, "from") : 0;
        final CutEnd end = end(map);
        final Pace pace;
        if (map.containsKey("speed")) {
            pace = new Pace(asDouble(map, "speed"));
        } else {
            pace = Pace.one();
        }
        return new SegmentSpec(
            new SegmentId(id),
            new SourceRef(source),
            new Cut(new Second(from), end, pace)
        );
    }
    private static CutEnd end(final Map<String, Object> map) {
        final boolean hasTo = map.containsKey("to");
        final boolean hasDuration = map.containsKey("duration");
        if (hasTo && hasDuration) {
            throw new IllegalStateException(
                "clip " + map.get("id") + " cannot set both to and duration"
            );
        }
        if (hasTo) {
            return new AtSecond(new Second(asDouble(map, "to")));
        }
        if (hasDuration) {
            return new AtSpan(new Second(asDouble(map, "duration")));
        }
        return AtEof.INSTANCE;
    }
    private static void validateUniqueIds(final List<SegmentSpec> segments) {
        final Set<String> seen = new LinkedHashSet<>();
        for (final SegmentSpec spec : segments) {
            final String label = spec.id().label();
            if (!seen.add(label)) {
                throw new IllegalStateException("duplicate clip id in DSL " + label);
            }
        }
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> load(final Path file) {
        try {
            final String text = Files.readString(file);
            final Object loaded = new Yaml().load(text);
            if (!(loaded instanceof Map<?, ?> map)) {
                throw new IllegalStateException("DSL root must be a mapping in " + file);
            }
            return (Map<String, Object>) map;
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("cannot read DSL file " + file, ex);
        }
    }
    private static String string(final Map<String, Object> map, final String key) {
        final Object value = map.get(key);
        if (value == null) {
            throw new IllegalStateException("missing DSL key " + key);
        }
        return value.toString();
    }
    private static int asInt(final Map<String, Object> map, final String key) {
        final Object value = map.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new IllegalStateException("DSL key " + key + " must be a number");
    }
    private static double asDouble(final Map<String, Object> map, final String key) {
        final Object value = map.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        throw new IllegalStateException("DSL key " + key + " must be a number");
    }
}
