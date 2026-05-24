package film.domain.model;

/**
 * Resolves one exclude gap end on the source file timeline.
 */
public interface GapEnd {
    double resolved(double gapFrom, double windowStop);
    String label();
}
