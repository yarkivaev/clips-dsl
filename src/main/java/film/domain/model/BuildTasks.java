package film.domain.model;

import film.domain.port.Assembly;
import film.domain.port.AssemblyPlan;
import film.domain.port.Clip;
import film.domain.port.Concat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Executable plan: render changed clips then assemble when needed.
 */
public final class BuildTasks {
    private final Timeline timeline;
    private final ResolvedEnds ends;
    private final List<Task> steps;
    public BuildTasks(final Timeline timeline, final ResolvedEnds ends, final List<Task> steps) {
        this.timeline = timeline;
        this.ends = ends;
        this.steps = List.copyOf(steps);
    }
    public static BuildTasks withAssembly(
        final Timeline timeline,
        final ResolvedEnds ends,
        final List<Task> segmentSteps,
        final AssemblyPlan assemblyPlan
    ) {
        final List<Task> steps = new ArrayList<>(segmentSteps);
        if (!assemblyPlan.empty()) {
            steps.add(new AssembledTask(Assembled.INSTANCE, assemblyPlan));
        }
        return new BuildTasks(timeline, ends, steps);
    }
    public List<Task> steps() {
        return steps;
    }
    public List<Class<?>> types() {
        final List<Class<?>> names = new ArrayList<>();
        for (final Task step : steps) {
            names.add(step.getClass());
        }
        return names;
    }
    public boolean assembleScheduled() {
        for (final Task step : steps) {
            if (step.concat()) {
                return true;
            }
        }
        return false;
    }
    public BuildResult execute(
        final Clip clip,
        final Concat concat,
        final Assembly assembly,
        final Path workspace,
        final Path clipsDir,
        final Path partsDir,
        final Path output,
        final Manifest prior
    ) {
        final Map<SegmentId, Path> artifacts = new HashMap<>();
        for (final SegmentSpec spec : timeline.segments()) {
            if (prior.cached(spec.id(), spec.fingerprint(ends.end(spec)))) {
                artifacts.put(spec.id(), prior.path(spec.id()));
            }
        }
        final Execution execution = new Execution(
            clip,
            concat,
            assembly,
            workspace,
            clipsDir,
            partsDir,
            output,
            timeline,
            ends,
            artifacts,
            prior.assembly()
        );
        boolean anyWork = false;
        for (final Task step : steps) {
            if (step.render() || step.concat()) {
                anyWork = true;
            }
            step.apply(execution);
        }
        if (!anyWork) {
            System.out.println("no-op build");
        }
        return new BuildResult(artifacts, execution.assemblySnapshot());
    }
}
