package film.infrastructure.ffmpeg;

import film.domain.model.MediaContract;

/**
 * Builds constant-speed video and audio filter suffixes for ffmpeg graphs.
 *
 * <p>Usage: {@code PaceChain.videoSuffix(6.0)} {@code PaceChain.audio(contract, 6.0)}
 */
public final class PaceChain {
    private PaceChain() {
    }
    public static String videoSuffix(final double factor) {
        if (factor == 1.0) {
            return "";
        }
        return ",setpts=PTS/" + factor;
    }
    public static String audio(final MediaContract contract, final double factor) {
        if (factor == 1.0) {
            return ClipCommand.audioResample(contract);
        }
        final StringBuilder chain = new StringBuilder(ClipCommand.audioResample(contract));
        double left = factor;
        while (left > 2.0) {
            chain.append(",atempo=2.0");
            left /= 2.0;
        }
        while (left < 0.5) {
            chain.append(",atempo=0.5");
            left /= 0.5;
        }
        if (Math.abs(left - 1.0) > 0.001) {
            chain.append(",atempo=").append(left);
        }
        return chain.toString();
    }
}
