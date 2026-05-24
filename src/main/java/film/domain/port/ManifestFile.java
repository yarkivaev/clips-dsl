package film.domain.port;

import film.domain.model.Manifest;
import film.domain.model.RenderProfile;

/**
 * Loads and saves build manifest on disk.
 */
public interface ManifestFile {
    Manifest loaded(RenderProfile profile);
    void saved(Manifest manifest);
}
