package film.infrastructure.assembly;

import film.domain.model.AssemblySnapshot;
import film.domain.model.SegmentSpec;
import film.domain.model.Timeline;
import film.domain.model.TimelineFingerprint;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cached tree assembly with leaf and internal nodes for partial concat.
 *
 * <p>Usage: {@code TreeSnapshot.wanted(timeline, ends, span, partsDir)}
 */
public final class TreeSnapshot implements AssemblySnapshot {
    private final String root;
    private final String top;
    private final Map<String, Node> nodes;
    public TreeSnapshot(final String root, final String top, final Map<String, Node> nodes) {
        this.root = root;
        this.top = top;
        this.nodes = Map.copyOf(nodes);
    }
    public List<String> order() {
        final List<String> ids = new ArrayList<>(nodes.keySet());
        ids.sort((left, right) -> {
            final int leftLayer = layer(left);
            final int rightLayer = layer(right);
            if (leftLayer != rightLayer) {
                return Integer.compare(leftLayer, rightLayer);
            }
            if (leftLayer == 0) {
                return Integer.compare(Integer.parseInt(left), Integer.parseInt(right));
            }
            return left.compareTo(right);
        });
        return List.copyOf(ids);
    }
    private static int layer(final String id) {
        int depth = 0;
        for (int index = 0; index < id.length(); index++) {
            if (id.charAt(index) == '.') {
                depth = depth + 1;
            }
        }
        return depth;
    }
    public String root() {
        return root;
    }
    public String top() {
        return top;
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
        public boolean leaf() {
            return children.isEmpty();
        }
    }
    public static TreeSnapshot wanted(
        final Timeline timeline,
        final film.domain.model.ResolvedEnds ends,
        final film.domain.model.RenderProfile profile,
        final film.domain.model.MediaContract contract,
        final int span,
        final Path partsDir
    ) {
        final Map<String, Node> nodes = new LinkedHashMap<>();
        final List<String> levelIds = new ArrayList<>();
        final List<SegmentSpec> segments = timeline.segments();
        for (int start = 0; start < segments.size(); start += span) {
            final String id = Integer.toString(start / span);
            final int stop = Math.min(start + span, segments.size());
            final List<SegmentSpec> slice = segments.subList(start, stop);
            final String digest = new TimelineFingerprint(new Timeline(slice), ends, profile, contract).digest();
            nodes.put(id, new Node(digest, nodePath(partsDir, id), List.of()));
            levelIds.add(id);
        }
        int depth = 1;
        while (levelIds.size() > 1) {
            final List<String> nextIds = new ArrayList<>();
            for (int start = 0; start < levelIds.size(); start += span) {
                final int stop = Math.min(start + span, levelIds.size());
                final List<String> childIds = levelIds.subList(start, stop);
                final String id = depth + "." + (start / span);
                final String digest = layerDigest(childIds, nodes);
                nodes.put(id, new Node(digest, nodePath(partsDir, id), List.copyOf(childIds)));
                nextIds.add(id);
            }
            levelIds.clear();
            levelIds.addAll(nextIds);
            depth = depth + 1;
        }
        final String top = levelIds.get(0);
        return new TreeSnapshot(nodes.get(top).digest(), top, nodes);
    }
    static Path nodePath(final Path partsDir, final String id) {
        return partsDir.resolve("n-" + id.replace('.', '-') + ".mp4");
    }
    private static String layerDigest(final List<String> childIds, final Map<String, Node> nodes) {
        final StringBuilder raw = new StringBuilder();
        for (final String childId : childIds) {
            raw.append(childId);
            raw.append('=');
            raw.append(nodes.get(childId).digest());
            raw.append(';');
        }
        return sha256(raw.toString());
    }
    private static String sha256(final String raw) {
        try {
            final java.security.MessageDigest hash = java.security.MessageDigest.getInstance("SHA-256");
            final byte[] bytes = hash.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(bytes);
        } catch (final java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 missing for tree node digest", ex);
        }
    }
}
