package film.infrastructure.ffmpeg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Runs ffmpeg/ffprobe with stderr streamed to build/logs as it arrives.
 *
 * <p>Usage: {@code new FfmpegProcess(workspace.resolve("build/logs")).run(builder, "cut-beavers", "cut beavers")}
 */
public final class FfmpegProcess {
    private final Path logDir;
    public FfmpegProcess(final Path logDir) {
        this.logDir = logDir;
    }
    public void run(final ProcessBuilder builder, final String logName, final String label) {
        waitFor(builder, logName, label, TimeUnit.HOURS.toNanos(2));
    }
    public String output(final ProcessBuilder builder, final String logName, final String label) {
        return waitFor(builder, logName, label, TimeUnit.SECONDS.toNanos(60));
    }
    private String waitFor(
        final ProcessBuilder builder,
        final String logName,
        final String label,
        final long timeoutNanos
    ) {
        try {
            Files.createDirectories(logDir);
            final Path logFile = logDir.resolve(logName + ".log");
            appendFilmLog("start " + label + " → " + logFile);
            System.out.println("log " + logFile + " (" + label + ")");
            builder.redirectErrorStream(true);
            final Process process = builder.start();
            final StringBuilder captured = new StringBuilder();
            final Thread pump = new Thread(() -> stream(process.getInputStream(), logFile, label, captured));
            pump.setDaemon(true);
            pump.start();
            final long deadline = System.nanoTime() + timeoutNanos;
            while (true) {
                if (process.waitFor(30, TimeUnit.SECONDS)) {
                    break;
                }
                if (System.nanoTime() > deadline) {
                    process.destroyForcibly();
                    throw new IllegalStateException(label + " timed out see " + logFile);
                }
                appendFilmLog("still running " + label);
                System.out.println("still running " + label + " (see " + logFile + ")");
            }
            pump.join(TimeUnit.SECONDS.toMillis(10));
            if (process.exitValue() != 0) {
                throw new IllegalStateException(label + " failed exit " + process.exitValue() + " see " + logFile);
            }
            appendFilmLog("done " + label);
            return captured.toString().trim();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(label + " interrupted", ex);
        } catch (final IOException ex) {
            throw new IllegalStateException(label + " cannot run", ex);
        }
    }
    private void stream(
        final InputStream input,
        final Path logFile,
        final String label,
        final StringBuilder captured
    ) {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            BufferedWriter writer = Files.newBufferedWriter(
                logFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        ) {
            writer.write("started " + Instant.now());
            writer.newLine();
            writer.flush();
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
                writer.flush();
                if (!line.isBlank()) {
                    captured.setLength(0);
                    captured.append(line);
                }
                System.out.println("[" + label + "] " + line);
            }
            writer.write("finished " + Instant.now());
            writer.newLine();
            writer.flush();
        } catch (final IOException ex) {
            throw new IllegalStateException("cannot write log " + logFile, ex);
        }
    }
    private void appendFilmLog(final String line) {
        try {
            final Path filmLog = logDir.resolve("film.log");
            Files.createDirectories(logDir);
            try (BufferedWriter writer = Files.newBufferedWriter(
                filmLog,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            )) {
                writer.write(Instant.now() + " " + line);
                writer.newLine();
                writer.flush();
            }
        } catch (final IOException ex) {
            throw new IllegalStateException("cannot write film log", ex);
        }
    }
}
