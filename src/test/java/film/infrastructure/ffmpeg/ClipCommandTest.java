package film.infrastructure.ffmpeg;

import film.domain.model.MediaContract;
import film.domain.model.RenderProfile;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * Tests ClipCommand filter and encode settings per render profile.
 */
final class ClipCommandTest {
    @Test
    void baseVideoFilterScalesToContractResolution() {
        assertThat(
            "base video filter should scale to contract resolution",
            ClipCommand.baseVideoFilter(MediaContract.defaults()),
            containsString("scale=1280:720")
        );
    }
    @Test
    void draftEncodeTailUsesUltrafastPreset() {
        final ArrayList<String> cmd = new ArrayList<>();
        ClipCommand.encodeTail(cmd, RenderProfile.draft(), Path.of("build/clips/x.mp4"));
        assertThat(
            "draft encode tail should use ultrafast preset",
            String.join(" ", cmd),
            containsString("ultrafast")
        );
    }
    @Test
    void releaseEncodeTailUsesSlowPreset() {
        final ArrayList<String> cmd = new ArrayList<>();
        ClipCommand.encodeTail(cmd, RenderProfile.release(), Path.of("build/clips/x.mp4"));
        assertThat(
            "release encode tail should use slow preset",
            String.join(" ", cmd),
            containsString("slow")
        );
    }
    @Test
    void draftEncodeTailUsesCrfTwentyEight() {
        final ArrayList<String> cmd = new ArrayList<>();
        ClipCommand.encodeTail(cmd, RenderProfile.draft(), Path.of("build/clips/x.mp4"));
        assertThat(
            "draft encode tail should use crf 28",
            cmd.get(cmd.indexOf("-crf") + 1),
            is("28")
        );
    }
}
