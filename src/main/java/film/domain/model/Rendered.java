package film.domain.model;

/**
 * Render segment from source via ffmpeg.
 */
public final class Rendered {
    private final SegmentSpec spec;
    private final Second end;
    public Rendered(final SegmentSpec spec, final Second end) {
        this.spec = spec;
        this.end = end;
    }
    public SegmentSpec spec() {
        return spec;
    }
    public Second end() {
        return end;
    }
}
