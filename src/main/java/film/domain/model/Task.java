package film.domain.model;

/**
 * One build step: skip, render, or join.
 */
public sealed interface Task permits SkippedTask, RenderedTask, AssembledTask {
    void apply(Execution execution);
    boolean render();
    boolean concat();
}
