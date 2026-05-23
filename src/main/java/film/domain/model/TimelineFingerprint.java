package film.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hash of ordered segment fingerprints for concat invalidation.
 */
public final class TimelineFingerprint {
    private final String digest;
    public TimelineFingerprint(final Timeline timeline, final ResolvedEnds ends) {
        final StringBuilder raw = new StringBuilder();
        for (final SegmentSpec spec : timeline.segments()) {
            raw.append(spec.id().label());
            raw.append('=');
            raw.append(spec.fingerprint(ends.end(spec)).digest());
            raw.append(';');
        }
        this.digest = sha256(raw.toString());
    }
    public TimelineFingerprint(final String digest) {
        this.digest = digest;
    }
    public boolean matches(final TimelineFingerprint that) {
        return digest.equals(that.digest);
    }
    public String digest() {
        return digest;
    }
    private static String sha256(final String raw) {
        try {
            final MessageDigest hash = MessageDigest.getInstance("SHA-256");
            final byte[] bytes = hash.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 missing for timeline fingerprint", ex);
        }
    }
}
