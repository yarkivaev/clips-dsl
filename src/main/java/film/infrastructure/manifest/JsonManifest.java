package film.infrastructure.manifest;

import film.domain.model.CachedClip;
import film.domain.model.Fingerprint;
import film.domain.model.Manifest;
import film.domain.model.SegmentId;
import film.domain.port.ManifestFile;
import film.infrastructure.assembly.AssemblyCodec;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Persists build/manifest.json for incremental rebuild.
 */
public final class JsonManifest implements ManifestFile {
    private static final Pattern CLIP_LINE = Pattern.compile(
        "\\s*\"([^\"]+)\"\\s*:\\s*\\{\\s*\"digest\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"path\"\\s*:\\s*\"([^\"]+)\"\\s*\\}"
    );
    private final Path file;
    private final AssemblyCodec codec;
    public JsonManifest(final Path workspace) {
        this.file = workspace.resolve("build").resolve("manifest.json");
        this.codec = new AssemblyCodec();
    }
    @Override
    public Manifest loaded() {
        if (!Files.isRegularFile(file)) {
            return Manifest.empty();
        }
        try {
            final String text = Files.readString(file);
            final Map<SegmentId, CachedClip> clips = readClips(text);
            return new Manifest(codec.loaded(text), clips);
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("cannot read manifest " + file, ex);
        }
    }
    @Override
    public void saved(final Manifest manifest) {
        try {
            Files.createDirectories(file.getParent());
            final StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"clips\": {\n");
            boolean first = true;
            for (final Map.Entry<SegmentId, CachedClip> entry : manifest.clips().entrySet()) {
                if (!first) {
                    json.append(",\n");
                }
                first = false;
                final CachedClip clip = entry.getValue();
                json.append("    \"");
                json.append(clip.id().label());
                json.append("\": {\"digest\": \"");
                json.append(clip.fingerprint().digest());
                json.append("\", \"path\": \"");
                json.append(clip.path().toString().replace("\\", "\\\\").replace("\"", "\\\""));
                json.append("\"}");
            }
            json.append("\n  }");
            final String assembly = codec.saved(manifest.assembly());
            if (!assembly.isEmpty()) {
                json.append(",\n");
                json.append(assembly);
            }
            json.append("\n}\n");
            Files.writeString(file, json.toString());
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("cannot write manifest " + file, ex);
        }
    }
    private static Map<SegmentId, CachedClip> readClips(final String text) {
        final Map<SegmentId, CachedClip> clips = new HashMap<>();
        final Matcher matcher = CLIP_LINE.matcher(text);
        while (matcher.find()) {
            final SegmentId id = new SegmentId(matcher.group(1));
            final Fingerprint fingerprint = new Fingerprint(matcher.group(2));
            final Path path = Path.of(matcher.group(3).replace("\\\"", "\"").replace("\\\\", "\\"));
            clips.put(id, new CachedClip(id, fingerprint, path));
        }
        return clips;
    }
}
