package film.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Content hash of a segment specification for cache and diff.
 *
 * <p>Usage: {@code spec.fingerprint(resolvedEnd)}
 */
public final class Fingerprint {
    private final String digest;
    public Fingerprint(final SegmentSpec spec, final Second resolvedEnd) {
        final String raw = spec.source().number()
            + "|" + spec.from().amount()
            + "|" + resolvedEnd.amount()
            + "|x264-fps30";
        this.digest = sha256(raw);
    }
    public Fingerprint(final String digest) {
        this.digest = digest;
    }
    public boolean matches(final Fingerprint that) {
        return digest.equals(that.digest);
    }
    public String digest() {
        return digest;
    }
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Fingerprint)) {
            return false;
        }
        return digest.equals(((Fingerprint) other).digest);
    }
    @Override
    public int hashCode() {
        return digest.hashCode();
    }
    private static String sha256(final String raw) {
        try {
            final MessageDigest hash = MessageDigest.getInstance("SHA-256");
            final byte[] bytes = hash.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 missing for fingerprint " + raw, ex);
        }
    }
}
