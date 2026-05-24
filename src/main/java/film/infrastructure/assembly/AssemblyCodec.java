package film.infrastructure.assembly;

import film.domain.model.AssemblySnapshot;
import film.domain.model.VacantAssemblySnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads and saves assembly snapshots from manifest JSON by kind field.
 *
 * <p>Usage: {@code new AssemblyCodec().loaded(text)}
 */
public final class AssemblyCodec {
    private static final Pattern KIND = Pattern.compile("\"kind\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ROOT = Pattern.compile("\"root\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern TOP = Pattern.compile("\"top\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern NODE = Pattern.compile(
        "\"([^\"]+)\"\\s*:\\s*\\{\\s*\"digest\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"path\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"children\"\\s*:\\s*\\[(.*?)\\]\\s*\\}"
    );
    private static final Pattern CHILD = Pattern.compile("\"([^\"]+)\"");
    public AssemblySnapshot loaded(final String text) {
        if (!text.contains("\"assembly\"")) {
            return new VacantAssemblySnapshot();
        }
        final Matcher kind = KIND.matcher(text);
        if (!kind.find()) {
            return new VacantAssemblySnapshot();
        }
        if ("chunk".equals(kind.group(1))) {
            return loadFlatLeaves(text);
        }
        if ("flat".equals(kind.group(1))) {
            return loadFlat(text);
        }
        if (!"tree".equals(kind.group(1))) {
            throw new IllegalStateException("unsupported assembly kind " + kind.group(1));
        }
        return loadTree(text);
    }
    public String saved(final AssemblySnapshot assembly) {
        if (assembly instanceof VacantAssemblySnapshot) {
            return "";
        }
        if (assembly instanceof FlatSnapshot snapshot) {
            final StringBuilder json = new StringBuilder();
            json.append("  \"assembly\": {\n");
            json.append("    \"kind\": \"flat\",\n");
            json.append("    \"root\": \"");
            json.append(snapshot.root());
            json.append("\"\n");
            json.append("  }");
            return json.toString();
        }
        if (!(assembly instanceof TreeSnapshot snapshot)) {
            throw new IllegalStateException("unsupported assembly snapshot type " + assembly.getClass().getName());
        }
        final StringBuilder json = new StringBuilder();
        json.append("  \"assembly\": {\n");
        json.append("    \"kind\": \"tree\",\n");
        json.append("    \"root\": \"");
        json.append(snapshot.root());
        json.append("\",\n");
        json.append("    \"top\": \"");
        json.append(snapshot.top());
        json.append("\",\n");
        json.append("    \"nodes\": {\n");
        boolean first = true;
        for (final Map.Entry<String, TreeSnapshot.Node> entry : snapshot.nodes().entrySet()) {
            if (!first) {
                json.append(",\n");
            }
            first = false;
            final TreeSnapshot.Node node = entry.getValue();
            json.append("      \"");
            json.append(entry.getKey());
            json.append("\": {\"digest\": \"");
            json.append(node.digest());
            json.append("\", \"path\": \"");
            json.append(escape(node.path().toString()));
            json.append("\", \"children\": [");
            appendChildren(json, node.children());
            json.append("]}");
        }
        json.append("\n    }\n");
        json.append("  }");
        return json.toString();
    }
    private static FlatSnapshot loadFlat(final String text) {
        final Matcher root = ROOT.matcher(text);
        if (!root.find()) {
            throw new IllegalStateException("assembly missing root digest");
        }
        return new FlatSnapshot(root.group(1));
    }
    private static TreeSnapshot loadTree(final String text) {
        final Matcher root = ROOT.matcher(text);
        if (!root.find()) {
            throw new IllegalStateException("assembly missing root digest");
        }
        final Matcher top = TOP.matcher(text);
        if (!top.find()) {
            throw new IllegalStateException("assembly missing top node id");
        }
        return new TreeSnapshot(root.group(1), top.group(1), readNodes(text));
    }
    private static TreeSnapshot loadFlatLeaves(final String text) {
        final Matcher root = ROOT.matcher(text);
        if (!root.find()) {
            throw new IllegalStateException("assembly missing root digest");
        }
        final Map<String, TreeSnapshot.Node> nodes = readNodes(text);
        return new TreeSnapshot(root.group(1), "", nodes);
    }
    private static Map<String, TreeSnapshot.Node> readNodes(final String text) {
        final Map<String, TreeSnapshot.Node> nodes = new LinkedHashMap<>();
        final Matcher node = NODE.matcher(text);
        while (node.find()) {
            final String id = node.group(1);
            final String digest = node.group(2);
            final String path = node.group(3).replace("\\\"", "\"").replace("\\\\", "\\");
            final List<String> children = readChildren(node.group(4));
            nodes.put(id, new TreeSnapshot.Node(digest, java.nio.file.Path.of(path), children));
        }
        return nodes;
    }
    private static List<String> readChildren(final String raw) {
        final List<String> children = new ArrayList<>();
        final Matcher child = CHILD.matcher(raw);
        while (child.find()) {
            children.add(child.group(1));
        }
        return children;
    }
    private static void appendChildren(final StringBuilder json, final List<String> children) {
        boolean first = true;
        for (final String child : children) {
            if (!first) {
                json.append(", ");
            }
            first = false;
            json.append("\"");
            json.append(child);
            json.append("\"");
        }
    }
    private static String escape(final String raw) {
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
