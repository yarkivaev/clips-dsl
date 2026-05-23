package film.domain.model;

import java.nio.file.Path;

/**
 * Task wrapper for ffmpeg segment cut.
 */
public final class RenderedTask implements Task {
    private final Rendered rendered;
    public RenderedTask(final Rendered rendered) {
        this.rendered = rendered;
    }
    public Rendered rendered() {
        return rendered;
    }
    @Override
    public boolean render() {
        return true;
    }
    @Override
    public boolean concat() {
        return false;
    }
    @Override
    public void apply(final Execution execution) {
        final SegmentSpec spec = rendered.spec();
        final Path dest = execution.clipsDir().resolve(spec.id().label() + ".mp4");
        execution.clip().cut(spec, rendered.end(), execution.workspace(), dest);
        execution.artifacts().put(spec.id(), dest);
        System.out.println("rendered " + spec.id().label());
    }
}
