package film.infrastructure.ffmpeg;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests ConcatLabel progress message text.
 */
final class ConcatLabelTest {
    @Test
    void partLabelNamesPartClipCountAndOutput() {
        assertThat(
            "part label should name part id clip count and output file",
            new ConcatLabel().part("1", 8, Path.of("build/parts/part-001.mp4")),
            is("concat part 1 → part-001.mp4 (8 clips)")
        );
    }
}
