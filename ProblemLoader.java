import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.json.*;

public class ProblemLoader {
    
    protected Path baseProblemsPath;
    
    public ProblemLoader() {
        this.baseProblemsPath = Paths.get("problems");
    }
    
    public ProblemLoader(String basePath) {
        this.baseProblemsPath = Paths.get(basePath);
    }
    
    public List<JSONObject> loadProblems() throws IOException {
        List<JSONObject> problems = new ArrayList<>();
        File dir = baseProblemsPath.toFile();
        File[] subdirs = dir.listFiles(File::isDirectory);
        if (subdirs == null) return problems;

        for (File d : subdirs) {
            Path metaPath = d.toPath().resolve("meta.json");
            if (!Files.exists(metaPath)) continue;
            String content = Files.readString(metaPath);
            JSONObject j = new JSONObject(content);
            problems.add(j);
        }
        return problems;
    }
    
    public String getProblemInfo(String id) throws IOException {
        Path folder = baseProblemsPath.resolve(id);
        
        StringBuilder info = new StringBuilder();
        info.append("УСЛОВИЕ:\n");
        info.append(Files.readString(folder.resolve("condition.txt")));
        
        info.append("\n\nПРИМЕР ВВОДА:\n");
        info.append(Files.readString(folder.resolve("input.txt")));
        
        info.append("\n\nПРИМЕР ВЫВОДА:\n");
        info.append(Files.readString(folder.resolve("output.txt")));
        
        return info.toString();
    }
    
    public void showProblem(String id) throws IOException {
        System.out.println(getProblemInfo(id));
    }
}