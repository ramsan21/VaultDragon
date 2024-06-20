import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class CreateCSVFile {
    public static void main(String[] args) {
        // Define the headers
        String[] headers = {"Name", "Age", "City"};

        // Define the data
        List<String> data = Arrays.asList("John,25,New York", "Jane,30,London", "Bob,35,Paris");

        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));

        // Create the file name with the timestamp
        String fileName = "data_" + timestamp + ".csv";

        try (FileWriter writer = new FileWriter(fileName)) {
            // Write the headers
            writer.write(String.join(",", headers) + "\n");

            // Write the data
            for (String line : data) {
                writer.write(line + "\n");
            }

            System.out.println("CSV file created: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
