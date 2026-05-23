package film.domain.model;

import java.nio.file.Path;

/**
 * Previously rendered clip artifact on disk.
 */
public final class CachedClip {
    private final SegmentId id;
    private final Fingerprint fingerprint;
    private final Path path;
    public CachedClip(final SegmentId id, final Fingerprint fingerprint, final Path path) {
        this.id = id;
        this.fingerprint = fingerprint;
        this.path = path;
    }
    public SegmentId id() {
        return id;
    }
    public Fingerprint fingerprint() {
        return fingerprint;
    }
    public Path path() {
        return path;
    }
}
