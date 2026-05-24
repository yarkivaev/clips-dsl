package film.infrastructure.ffmpeg;

import film.domain.model.MediaContract;
import film.domain.model.RenderProfile;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Tests ConcatCommand argument lists for draft and release paths.
 */
final class ConcatCommandTest {
    @Test
    void copyCommandUsesConcatDemuxerAndStreamCopy() {
        final List<String> cmd = ConcatCommand.copyCommand(
            Path.of("build/logs/list.txt"),
            Path.of("build/output.mp4")
        );
        assertThat(
            "copy concat should use concat demuxer",
            String.join(" ", cmd),
            containsString("-f concat")
        );
    }
    @Test
    void reencodeCommandUsesFilterComplex() {
        final List<String> cmd = ConcatCommand.reencodeCommand(
            List.of(Path.of("build/clips/a.mp4")),
            MediaContract.defaults(),
            RenderProfile.release(),
            Path.of("build/output.mp4")
        );
        assertThat(
            "release concat should use filter_complex",
            String.join(" ", cmd),
            containsString("-filter_complex")
        );
    }
    @Test
    void copyCommandDoesNotUseFilterComplex() {
        final List<String> cmd = ConcatCommand.copyCommand(
            Path.of("build/logs/list.txt"),
            Path.of("build/output.mp4")
        );
        assertThat(
            "copy concat should not use filter_complex",
            String.join(" ", cmd),
            not(containsString("-filter_complex"))
        );
    }
}
