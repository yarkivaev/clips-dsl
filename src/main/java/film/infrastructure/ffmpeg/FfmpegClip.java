package film.infrastructure.ffmpeg;

import film.domain.model.Second;
import film.domain.model.SegmentSpec;
import film.domain.port.Clip;

import java.nio.file.Path;

/**
 * Cuts a segment to 30 fps h264/aac with zero-based pts.
 */
public final class FfmpegClip implements Clip {
    private final FfmpegProcess ffmpeg;
    public FfmpegClip(final Path logDir) {
        this.ffmpeg = new FfmpegProcess(logDir);
    }
    @Override
    public void cut(final SegmentSpec spec, final Second end, final Path workspace, final Path dest) {
        final Path input = workspace.resolve(spec.source().filename()).normalize();
        final String id = spec.id().label();
        final ProcessBuilder builder = new ProcessBuilder(
            "ffmpeg",
            "-y",
            "-nostats",
            "-loglevel", "info",
            "-i", input.toString(),
            "-ss", spec.from().ffmpeg(),
            "-to", end.ffmpeg(),
            "-vf", "fps=30,format=yuv420p,setpts=PTS-STARTPTS",
            "-af", "aresample=48000,asetpts=PTS-STARTPTS",
            "-c:v", "libx264",
            "-preset", "fast",
            "-crf", "22",
            "-c:a", "aac",
            "-b:a", "192k",
            "-movflags", "+faststart",
            dest.toString()
        );
        builder.directory(workspace.toFile());
        ffmpeg.run(builder, "cut-" + id, "cut " + id);
    }
}
