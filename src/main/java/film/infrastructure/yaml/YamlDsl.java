package film.infrastructure.yaml;

import film.domain.model.MediaContract;
import film.domain.model.AtEof;
import film.domain.model.AtSecond;
import film.domain.model.AtSpan;
import film.domain.model.Cut;
import film.domain.model.CutEnd;
import film.domain.model.Edits;
import film.domain.model.ExcludeSpan;
import film.domain.model.Excludes;
import film.domain.model.GapAt;
import film.domain.model.GapEnd;
import film.domain.model.GapSpan;
import film.domain.model.TrimCap;
import film.domain.model.WindowStop;
import film.domain.model.Keyframe;
import film.domain.model.Keyframes;
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
        return new OpenedDsl(new Timeline(segments), output, contract(root));
    }
    @SuppressWarnings("unchecked")
    private static MediaContract contract(final Map<String, Object> root) {
        if (!root.containsKey("contract")) {
            return MediaContract.defaults();
        }
        final Object raw = root.get("contract");
        if (!(raw instanceof Map<?, ?> map)) {
            throw new IllegalStateException("DSL contract must be a mapping");
        }
        final Map<String, Object> fields = (Map<String, Object>) map;
        final int width = contractInt(fields, "width", 1280);
        final int height = contractInt(fields, "height", 720);
        final int fps = contractInt(fields, "fps", 30);
        final int audioRate = contractInt(fields, "audio_rate", 48000);
        return new MediaContract(width, height, fps, audioRate);
    }
    private static int contractInt(
        final Map<String, Object> map,
        final String key,
        final int fallback
    ) {
        if (!map.containsKey(key)) {
            return fallback;
        }
        final Object value = map.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new IllegalStateException("DSL contract key " + key + " must be a number");
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
        final Edits edits = edits(map);
        final CutEnd end = end(map, edits);
        final Pace pace = pace(map);
        return new SegmentSpec(
            new SegmentId(id),
            new SourceRef(source),
            new Cut(new Second(from), end, pace, edits)
        );
    }
    @SuppressWarnings("unchecked")
    private static Edits edits(final Map<String, Object> map) {
        if (!map.containsKey("exclude")) {
            return Edits.none();
        }
        final Object raw = map.get("exclude");
        if (!(raw instanceof List<?> list)) {
            throw new IllegalStateException("DSL exclude must be a list for clip " + map.get("id"));
        }
        final List<ExcludeSpan> gaps = new ArrayList<>();
        for (final Object item : list) {
            if (!(item instanceof Map<?, ?> gapMap)) {
                throw new IllegalStateException(
                    "exclude entry must be a mapping for clip " + map.get("id")
                );
            }
            gaps.add(exclude((Map<String, Object>) gapMap, map.get("id")));
        }
        final TrimCap cap = map.containsKey("duration")
            ? TrimCap.of(new Second(asDouble(map, "duration")))
            : TrimCap.none();
        return new Edits(Excludes.of(gaps), cap);
    }
    private static ExcludeSpan exclude(final Map<String, Object> map, final Object clipId) {
        final boolean hasFrom = map.containsKey("from");
        final boolean hasTo = map.containsKey("to");
        final boolean hasDuration = map.containsKey("duration");
        if (hasTo && hasDuration) {
            throw new IllegalStateException(
                "exclude cannot set both to and duration for clip " + clipId
            );
        }
        final GapEnd end = hasTo
            ? new GapAt(new Second(seconds(map.get("to"), "to", clipId)))
            : hasDuration
                ? new GapSpan(new Second(seconds(map.get("duration"), "duration", clipId)))
                : WindowStop.INSTANCE;
        if (hasFrom) {
            return new ExcludeSpan(new Second(seconds(map.get("from"), "from", clipId)), end);
        }
        return new ExcludeSpan(end);
    }
    private static Pace pace(final Map<String, Object> map) {
        if (!map.containsKey("speed")) {
            return Pace.one();
        }
        final Object raw = map.get("speed");
        if (raw instanceof Number number) {
            return new Pace(number.doubleValue());
        }
        if (raw instanceof List<?> list) {
            return new Pace(keyframesFrom(list, map));
        }
        throw new IllegalStateException(
            "DSL key speed must be a number or keyframe list for clip " + map.get("id")
        );
    }
    @SuppressWarnings("unchecked")
    private static Keyframes keyframesFrom(final List<?> list, final Map<String, Object> clip) {
        final List<Keyframe> points = new ArrayList<>();
        for (final Object item : list) {
            if (!(item instanceof Map<?, ?> raw)) {
                throw new IllegalStateException(
                    "speed keyframe entry must be a mapping for clip " + clip.get("id")
                );
            }
            final Map<String, Object> point = (Map<String, Object>) raw;
            final double at = seconds(point.get("at"), "at", clip.get("id"));
            final double factor = number(point.get("speed"), "speed", clip.get("id"));
            points.add(new Keyframe(new Second(at), factor));
        }
        return Keyframes.of(points);
    }
    private static double number(final Object value, final String key, final Object clipId) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        throw new IllegalStateException(
            "DSL key " + key + " must be a number for clip " + clipId
        );
    }
    private static double seconds(final Object value, final String key, final Object clipId) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text) {
            return parseTime(text.trim(), key, clipId);
        }
        throw new IllegalStateException(
            "DSL key " + key + " must be a number or time for clip " + clipId
        );
    }
    private static double parseTime(final String text, final String key, final Object clipId) {
        if (!text.contains(":")) {
            try {
                return Double.parseDouble(text);
            } catch (final NumberFormatException ex) {
                throw new IllegalStateException(
                    "DSL key " + key + " is not a valid time for clip " + clipId + " value " + text
                );
            }
        }
        final String[] parts = text.split(":");
        if (parts.length == 2) {
            return parseTimePart(parts[0], key, clipId) * 60 + parseTimePart(parts[1], key, clipId);
        }
        if (parts.length == 3) {
            return parseTimePart(parts[0], key, clipId) * 3600
                + parseTimePart(parts[1], key, clipId) * 60
                + parseTimePart(parts[2], key, clipId);
        }
        throw new IllegalStateException(
            "DSL key " + key + " time must be M:SS or H:MM:SS for clip " + clipId + " value " + text
        );
    }
    private static double parseTimePart(final String part, final String key, final Object clipId) {
        try {
            return Double.parseDouble(part);
        } catch (final NumberFormatException ex) {
            throw new IllegalStateException(
                "DSL key " + key + " time part is not numeric for clip " + clipId + " part " + part
            );
        }
    }
    private static CutEnd end(final Map<String, Object> map, final Edits edits) {
        final boolean hasTo = map.containsKey("to");
        final boolean hasDuration = map.containsKey("duration");
        if (edits.trimmed()) {
            if (hasTo && hasDuration) {
                return new AtSecond(new Second(asDouble(map, "to")));
            }
            if (hasTo) {
                return new AtSecond(new Second(asDouble(map, "to")));
            }
            return AtEof.INSTANCE;
        }
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
        return seconds(map.get(key), key, map.get("id"));
    }
}
