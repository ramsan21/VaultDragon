import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CSVToJson {
    public static void main(String[] args) {
        String csvFilePath = "path/to/your/file.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode jsonArray = objectMapper.createArrayNode();

            String line;
            int index = 0;
            while ((line = br.readLine()) != null) {
                if (index % 10 == 0) {
                    String[] fields = line.split(",");

                    ObjectNode jsonNode = objectMapper.createObjectNode();
                    jsonNode.put("appId", "IDC");
                    jsonNode.put("groupId", "PH01");
                    jsonNode.put("userId", fields[0]); // Assuming the userId is in the first column

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
