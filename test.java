import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

public class CSVToJson {
    public static void main(String[] args) {
        String csvFilePath = "path/to/your/file.csv";

        try (Reader reader = new FileReader(csvFilePath)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode jsonArray = objectMapper.createArrayNode();

            int index = 0;
            for (CSVRecord record : records) {
                if (index % 10 == 0) {
                    ObjectNode jsonNode = objectMapper.createObjectNode();
                    jsonNode.put("appId", "IDC");
                    jsonNode.put("groupId", "PH01");
                    jsonNode.put("userId", record.get("userId")); // Change this to the appropriate column name from your CSV

                    jsonArray.add(jsonNode);
                }
                index++;
            }

            // Convert ArrayNode to JSON string
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonArray);
            System.out.println(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
