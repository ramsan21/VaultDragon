import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Pattern;

public class RemoveBlockCommentsAndExtraLines {

    public static void main(String[] args) throws IOException {
        // Define the starting directory. Change this to the directory you want to start with.
        Path startDir = Paths.get("path/to/your/directory");

        // Use Files.walkFileTree to traverse the directory and its subdirectories
        Files.walkFileTree(startDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".java")) {
                    removeBlockCommentsAndExtraLines(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void removeBlockCommentsAndExtraLines(Path file) throws IOException {
        // Read the content of the file
        String content = new String(Files.readAllBytes(file));
        
        // Define a regex pattern to find block comments
        Pattern blockCommentPattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

        // Replace block comments with an empty string
        String modifiedContent = blockCommentPattern.matcher(content).replaceAll("");

        // Define a regex pattern to replace multiple line breaks with a single line break
        Pattern multipleNewLinesPattern = Pattern.compile("\\n{2,}");
        
        // Replace multiple line breaks with a single line break
        modifiedContent = multipleNewLinesPattern.matcher(modifiedContent).replaceAll("\n");

        // Write the modified content back to the file
        Files.write(file, modifiedContent.getBytes());
    }
}
