package film.domain.port;

/**
 * Opaque plan for incremental film assembly; topology lives in infrastructure.
 *
 * <p>Usage: {@code plan.empty()}
 */
public interface AssemblyPlan {
    boolean empty();
}
