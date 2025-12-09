import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class SandboxManager {

    private final Path sandboxRoot;

    public SandboxManager(Path baseDir) throws IOException {
        this.sandboxRoot = baseDir.resolve("sandbox");
        if (!Files.exists(sandboxRoot)) {
            Files.createDirectories(sandboxRoot);
        }
    }

    /**
     * Создаёт уникальную папку для теста.
     */
    public Path createSandbox(String runId) throws IOException {
        Path sandboxDir = sandboxRoot.resolve(runId);
        if (Files.exists(sandboxDir)) {
            deleteDirectory(sandboxDir);
        }
        Files.createDirectories(sandboxDir);
        return sandboxDir;
    }

    /**
     * Удаляет папку sandbox рекурсивно.
     */
    public void deleteDirectory(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteDirectory(entry);
                }
            }
        }
        Files.deleteIfExists(path);
    }

    /**
     * Компилирует Java код в указанной папке.
     */
    public String compileJava(Path sandboxDir, String fileName) throws IOException, InterruptedException {
        ProcessBuilder compileBuilder = new ProcessBuilder("javac", fileName)
                .directory(sandboxDir.toFile())
                .redirectErrorStream(true);

        Process compile = compileBuilder.start();

        StringBuilder compileOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(compile.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                compileOutput.append(line).append("\n");
            }
        }

        int result = compile.waitFor();
        return result == 0 ? null : compileOutput.toString();
    }

    /**
     * Запускает Java программу в sandbox с таймаутом.
     */
    public String runJava(Path sandboxDir, String className, long timeoutSeconds) throws IOException, InterruptedException {
        ProcessBuilder runBuilder = new ProcessBuilder("java", className)
                .directory(sandboxDir.toFile())
                .redirectErrorStream(true);

        Process run = runBuilder.start();
        boolean finished = run.waitFor(timeoutSeconds, TimeUnit.SECONDS);

        if (!finished) {
            run.destroyForcibly();
            return null;
        }

        StringBuilder outputBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(run.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputBuilder.append(line).append("\n");
            }
        }

        Path outputFile = sandboxDir.resolve("OUTPUT.TXT");
        if (Files.exists(outputFile)) {
            String fileOutput = Files.readString(outputFile).trim();
            if (!fileOutput.isEmpty()) {
                return fileOutput;
            }
        }

        return outputBuilder.toString().trim();
    }
}
