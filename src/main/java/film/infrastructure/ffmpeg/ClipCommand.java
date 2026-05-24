package film.infrastructure.ffmpeg;

import film.domain.model.MediaContract;
import film.domain.model.RenderProfile;

import java.nio.file.Path;
import java.util.List;

/**
 * Builds ffmpeg argument lists for clip cuts.
 *
 * <p>Usage: {@code ClipCommand.baseVideoFilter(contract)}
 */
public final class ClipCommand {
    private ClipCommand() {
    }
    public static String baseVideoFilter(final MediaContract contract) {
        return "fps="
            + contract.fps()
            + ",format=yuv420p,setpts=PTS-STARTPTS,scale="
            + contract.width()
            + ":"
            + contract.height()
            + ":force_original_aspect_ratio=decrease,pad="
            + contract.width()
            + ":"
            + contract.height()
            + ":(ow-iw)/2:(oh-ih)/2";
    }
    public static String audioResample(final MediaContract contract) {
        return "aresample=" + contract.audioRate() + ",asetpts=PTS-STARTPTS";
    }
    public static void encodeTail(
        final List<String> command,
        final RenderProfile profile,
        final Path dest
    ) {
        command.add("-c:v");
        command.add("libx264");
        command.add("-preset");
        command.add(profile.clipPreset());
        command.add("-crf");
        command.add(profile.clipCrf());
        command.add("-c:a");
        command.add("aac");
        command.add("-b:a");
        command.add("192k");
        command.add("-movflags");
        command.add("+faststart");
        command.add(dest.toString());
    }
}
