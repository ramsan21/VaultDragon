import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessJavaFiles {

    public static void main(String[] args) throws IOException {
        // Define the starting directory. Change this to the directory you want to start with.
        Path startDir = Paths.get("path/to/your/directory");

        // Use Files.walkFileTree to traverse the directory and its subdirectories
        Files.walkFileTree(startDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".java")) {
                    processFile(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void processFile(Path file) throws IOException {
        // Read all lines from the file
        List<String> lines = Files.readAllLines(file);

        // Flag to indicate if we should stop processing lines
        boolean skipRemainingLines = false;

        // Replace the first 9 characters of each line and remove lines after "/* Location:"
        List<String> modifiedLines = lines.stream()
            .map(line -> {
                if (skipRemainingLines) {
                    return null;
                }
                if (line.startsWith("/* Location:")) {
                    skipRemainingLines = true;
                    return null;
                }
                return line.length() > 9 ? "REPLACED" + line.substring(9) : "REPLACED";
            })
            .filter(line -> line != null)
            .collect(Collectors.toList());

        // Write the modified lines back to the file
        Files.write(file, modifiedLines);
    }
}
