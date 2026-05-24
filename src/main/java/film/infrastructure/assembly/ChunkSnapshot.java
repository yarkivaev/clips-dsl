package film.infrastructure.assembly;

import film.domain.model.AssemblySnapshot;
import film.domain.model.SegmentSpec;
import film.domain.model.Timeline;
import film.domain.model.TimelineFingerprint;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Cached chunk assembly with flat leaf nodes for incremental partial concat.
 *
 * <p>Usage: {@code new ChunkSnapshot(root, nodes)}
 */
public final class ChunkSnapshot implements AssemblySnapshot {
    private final String root;
    private final Map<String, Node> nodes;
    public ChunkSnapshot(final String root, final Map<String, Node> nodes) {
        this.root = root;
        this.nodes = Map.copyOf(nodes);
    }
    public String root() {
        return root;
    }
    public Map<String, Node> nodes() {
        return nodes;
    }
    public Node node(final String id) {
        return nodes.get(id);
    }
    /**
     * One cached assembly node with digest, path, and child ids.
     *
     * <p>Usage: {@code new Node(digest, path, children)}
     */
    public static final class Node {
        private final String digest;
        private final Path path;
        private final List<String> children;
        public Node(final String digest, final Path path, final List<String> children) {
            this.digest = digest;
            this.path = path;
            this.children = List.copyOf(children);
        }
        public String digest() {
            return digest;
        }
        public Path path() {
            return path;
        }
        public List<String> children() {
            return children;
        }
    }
    /**
     * Computes wanted leaf digests for a timeline split into chunks.
     *
     * <p>Usage: {@code ChunkSnapshot.leafDigests(timeline, ends, span)}
     */
    public static List<String> leafDigests(
        final Timeline timeline,
        final film.domain.model.ResolvedEnds ends,
        final int span
    ) {
        final List<SegmentSpec> segments = timeline.segments();
        final List<String> digests = new java.util.ArrayList<>();
        for (int start = 0; start < segments.size(); start += span) {
            final int stop = Math.min(start + span, segments.size());
            final List<SegmentSpec> slice = segments.subList(start, stop);
            digests.add(new TimelineFingerprint(new Timeline(slice), ends).digest());
        }
        return digests;
    }
    public static String rootDigest(final List<String> leafDigests) {
        final StringBuilder raw = new StringBuilder();
        for (int index = 0; index < leafDigests.size(); index++) {
            raw.append(index);
            raw.append('=');
            raw.append(leafDigests.get(index));
            raw.append(';');
        }
        return sha256(raw.toString());
    }
    public static ChunkSnapshot wanted(
        final Timeline timeline,
        final film.domain.model.ResolvedEnds ends,
        final int span,
        final Path partsDir
    ) {
        final List<String> digests = leafDigests(timeline, ends, span);
        final String root = rootDigest(digests);
        final Map<String, Node> nodes = new java.util.HashMap<>();
        for (int index = 0; index < digests.size(); index++) {
            final Path path = partsDir.resolve(partName(index));
            nodes.put(Integer.toString(index), new Node(digests.get(index), path, Collections.emptyList()));
        }
        return new ChunkSnapshot(root, nodes);
    }
    static String partName(final int index) {
        return String.format("part-%03d.mp4", index);
    }
    private static String sha256(final String raw) {
        try {
            final java.security.MessageDigest hash = java.security.MessageDigest.getInstance("SHA-256");
            final byte[] bytes = hash.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(bytes);
        } catch (final java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 missing for chunk root digest", ex);
        }
    }
}
