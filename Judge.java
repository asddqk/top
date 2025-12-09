import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class Judge {
    
    private static Path getBaseDir() {
        Path current = Paths.get("").toAbsolutePath();
        
        System.out.println("[Judge.getBaseDir] Starting from: " + current);
        
        for (int i = 0; i < 5; i++) {
            Path testProblems = current.resolve("problems");
            Path testSandbox = current.resolve("sandbox");
            
            System.out.println("[Judge.getBaseDir] Checking problems: " + testProblems);
            System.out.println("[Judge.getBaseDir] Problems exists: " + Files.exists(testProblems));
            System.out.println("[Judge.getBaseDir] Sandbox exists: " + Files.exists(testSandbox));
            
            if (Files.exists(testProblems) && Files.exists(testSandbox)) {
                System.out.println("[Judge.getBaseDir] Found base dir: " + current);
                return current;
            }
            
            if (current.getParent() == null) break;
            current = current.getParent();
        }
        
        Path fallback = Paths.get(System.getProperty("user.dir"));
        System.out.println("[Judge.getBaseDir] Using fallback: " + fallback);
        return fallback;
    }
    
    public static String runAndTest(String id, String userCode) throws IOException, InterruptedException {
    System.out.println("\n=== JUDGE START ===");
    System.out.println("[Judge] Problem ID: " + id);
    System.out.println("[Judge] Code length: " + userCode.length());

    Path baseDir = getBaseDir();
    SandboxManager sandboxManager = new SandboxManager(baseDir);

    // Проверяем тесты
    Path testFolder = baseDir.resolve("problems").resolve(id).resolve("test");
    System.out.println("[Judge] Test folder: " + testFolder);
    if (!Files.exists(testFolder)) {
        return "❌ Ошибка: папка с тестами не найдена.\n" +
               "Путь: " + testFolder + "\n" +
               "Проверьте, что задача " + id + " существует.";
    }

    List<String[]> tests = new ArrayList<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(testFolder, "tin_*.txt")) {
        for (Path tin : stream) {
            String num = tin.getFileName().toString().replaceAll("\\D+", "");
            Path tout = testFolder.resolve("tout_" + num + ".txt");
            if (Files.exists(tout)) {
                tests.add(new String[]{Files.readString(tin).trim(), Files.readString(tout).trim()});
            }
        }
    }

    if (tests.isEmpty()) {
        return "Ошибка: тесты не найдены для задачи " + id;
    }

    System.out.println("[Judge] Found " + tests.size() + " test(s)");

    StringBuilder result = new StringBuilder();
    boolean allPassed = true;

    for (int tIndex = 0; tIndex < tests.size(); tIndex++) {
        String[] test = tests.get(tIndex);
        int testNum = tIndex + 1;

        String runId = "web_" + System.currentTimeMillis() + "_" + testNum + "_" + Thread.currentThread().getId();
        Path sandboxDir = sandboxManager.createSandbox(runId);

        try {
            // Сохраняем код
            Path javaFile = sandboxDir.resolve("Main.java");
            Files.writeString(javaFile, userCode);

            // Входной файл
            Path inputFile = sandboxDir.resolve("INPUT.TXT");
            Files.writeString(inputFile, test[0]);

            // Компиляция
            String compileError = sandboxManager.compileJava(sandboxDir, "Main.java");
            if (compileError != null) {
                result.append("❌ Ошибка компиляции для теста #").append(testNum).append("\n");
                result.append(compileError);
                allPassed = false;
                continue;
            }

            // Запуск
            String output = sandboxManager.runJava(sandboxDir, "Main", 2);
            if (output == null) {
                result.append("❌ Превышено время выполнения теста #").append(testNum).append(" (2 секунды)\n");
                allPassed = false;
                continue;
            }

            String expected = test[1];
            if (output.equals(expected)) {
                result.append("✅ Тест #").append(testNum).append(": Пройден\n");
            } else {
                result.append("❌ Тест #").append(testNum).append(": Ошибка\n");
                result.append("Ожидалось:\n").append(expected).append("\n");
                result.append("Получено:\n").append(output).append("\n");
                allPassed = false;
            }

        } finally {
            sandboxManager.deleteDirectory(sandboxDir);
        }
    }

    if (allPassed) {
        result.append("\n🎉 Все тесты пройдены! Задача решена верно!");
    } else {
        result.append("\n⚠️ Есть ошибки! Проверьте решение.");
    }

    System.out.println("[Judge] Result: " + (allPassed ? "PASSED" : "FAILED"));
    System.out.println("=== JUDGE END ===\n");

    return result.toString();
}

    private static void deleteDirectory(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteDirectory(entry);
                }
            }
        }
        Files.deleteIfExists(path);
    }
}