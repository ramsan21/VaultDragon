import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileProcessor {

    public static void main(String[] args) {
        // Provide the root directory path
        String rootDir = "path/to/root/directory";

        try (Stream<Path> filePaths = Files.walk(Paths.get(rootDir))) {
            List<Path> files = filePaths
                    .filter(Files::isRegularFile) // Only regular files
                    .collect(Collectors.toList());

            for (Path file : files) {
                processFile(file);
            }

            System.out.println("File processing completed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processFile(Path filePath) {
        try {
            // Read all lines from the file
            List<String> lines = Files.readAllLines(filePath);
            
            // Process each line
            List<String> processedLines = lines.stream()
                    .filter(line -> !line.trim().isEmpty()) // Remove empty lines
                    .map(line -> {
                        if (line.startsWith("/*")) {
                            return line.length() > 9 ? line.substring(9) : ""; // Trim first 9 characters
                        }
                        return line;
                    })
                    .collect(Collectors.toList());

            // Write back the modified lines to the same file
            Files.write(filePath, processedLines);

        } catch (IOException e) {
            System.err.println("Error processing file: " + filePath);
            e.printStackTrace();
        }
    }
}
