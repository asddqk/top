import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.json.*;

public class WebProblemLoader extends ProblemLoader {
    
    // Приватный конструктор
    private WebProblemLoader(Path problemsPath) {
        super(problemsPath.toString());
    }
    
    // Фабричный метод для создания экземпляра
    public static WebProblemLoader create() {
        Path problemsPath = findProblemsPath();
        System.out.println("[WebProblemLoader] Using path: " + problemsPath);
        return new WebProblemLoader(problemsPath);
    }
    
    // Статические методы-обертки
    public static String getTopicsStatic() throws IOException {
        return create().getTopics();
    }
    
    public static String getProblemsByTopicStatic(String topic) throws IOException {
        return create().getProblemsByTopic(topic);
    }
    
    public static String getProblemInfoStatic(String id) throws IOException {
        return create().getProblemInfoExtended(id);
    }
    
    // Методы экземпляра
    public String getTopics() throws IOException {
        List<JSONObject> problems = loadProblems();
        Set<String> topics = new HashSet<>();
        for (JSONObject p : problems) {
            topics.add(p.getString("topic"));
        }
        return String.join(",", topics);
    }
    
    public String getProblemsByTopic(String topic) throws IOException {
        List<JSONObject> problems = loadProblems();
        StringBuilder result = new StringBuilder();
        
        for (JSONObject p : problems) {
            if (p.getString("topic").equalsIgnoreCase(topic)) {
                result.append(p.getString("id"))
                      .append(" - ")
                      .append(p.getString("name"))
                      .append(" (Сложность: ")
                      .append(p.getInt("difficulty"))
                      .append("%)\n");
            }
        }
        
        return result.toString();
    }
    
    // Дополненная версия getProblemInfo
    public String getProblemInfoExtended(String id) throws IOException {
        // Используем базовый метод родителя
        String baseInfo = super.getProblemInfo(id);
        
        // Добавляем веб-специфичную информацию
        Path folder = baseProblemsPath.resolve(id);
        StringBuilder webInfo = new StringBuilder(baseInfo);
        
       /* // Добавляем эталонное решение
        Path solutionPath = folder.resolve("attempt.py");
        if (Files.exists(solutionPath)) {
            webInfo.append("\n\n=== ЭТАЛОННОЕ РЕШЕНИЕ ===\n");
            webInfo.append(Files.readString(solutionPath));
        }
        */
        return webInfo.toString();
    }
    
    // Новый метод, специфичный для WebProblemLoader
    public String getSolution(String id) throws IOException {
        Path folder = baseProblemsPath.resolve(id);
        Path solutionPath = folder.resolve("attempt.py");
        
        if (Files.exists(solutionPath)) {
            return Files.readString(solutionPath);
        } else {
            return "Решение не найдено";
        }
    }
    
    private static Path findProblemsPath() {
        Path current = Paths.get("").toAbsolutePath();
        
        for (int i = 0; i < 3; i++) {
            if (Files.exists(current.resolve("problems"))) {
                return current.resolve("problems");
            }
            if (current.getParent() == null) break;
            current = current.getParent();
        }
        
        return Paths.get(System.getProperty("user.dir")).resolve("problems");
    }
    
    // Метод для получения пути к проблемам
    public Path getWebProblemsPath() {
        return baseProblemsPath;
    }
}